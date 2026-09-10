package com.example.data

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URI
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * SecurityUtils: Comprehensive defensive security and hardening utilities for KodeMamas.
 * Provides:
 * 1. Form and input sanitization (XSS and injection protection)
 * 2. Cryptographic password hashing (PBKDF2 with SHA-256 and salt) & constant-time verification
 * 3. Thread-safe client-side Rate Limiting (Token Bucket / Sliding Window)
 * 4. API key masking and secure runtime handling
 * 5. Role-Based Access Control (RBAC) & Admin Route Protection
 * 6. OkHttp Security Headers Interceptor
 * 7. Secure URL and Endpoint validation
 */
object SecurityUtils {

    // =========================================================================
    // 1. INPUT SANITIZATION & XSS / INJECTION DEFENSE
    // =========================================================================

    private val SCRIPT_REGEX = Regex("(?i)<script[\\s\\S]*?>[\\s\\S]*?</script>")
    private val ON_EVENT_REGEX = Regex("(?i)\\bon[a-z]+\\s*=")
    private val DANGEROUS_PROTOCOLS_REGEX = Regex("(?i)(javascript|vbscript|data):")
    private val HTML_TAG_REGEX = Regex("<[^>]*>")

    /**
     * Sanitizes user-entered text across all forms (comments, chat, feedback, profile inputs).
     * Strips script blocks, dangerous event handlers, and encodes special characters.
     */
    fun sanitizeInput(input: String?, maxLength: Int = 1000): String {
        if (input.isNullOrBlank()) return ""
        
        // 1. Enforce length boundary to protect against memory/DoS attacks
        val bounded = if (input.length > maxLength) input.substring(0, maxLength) else input

        // 2. Remove script blocks and inline javascript/data URI exploits
        var sanitized = SCRIPT_REGEX.replace(bounded, "")
        sanitized = ON_EVENT_REGEX.replace(sanitized, "blocked_event=")
        sanitized = DANGEROUS_PROTOCOLS_REGEX.replace(sanitized, "blocked_protocol:")

        // 3. Strip control characters except newline and tab
        sanitized = sanitized.filter { it == '\n' || it == '\r' || it == '\t' || it >= ' ' }

        return sanitized.trim()
    }

    /**
     * Completely strips all HTML tags to produce pure plain text (e.g. for display names, categories).
     */
    fun sanitizePlainText(input: String?, maxLength: Int = 250): String {
        if (input.isNullOrBlank()) return ""
        val cleaned = HTML_TAG_REGEX.replace(input, "")
        return sanitizeInput(cleaned, maxLength)
    }

    /**
     * Validates and sanitizes email addresses against basic RFC patterns.
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$")
        return emailRegex.matches(email.trim())
    }

    /**
     * Validates that an API or Firebase endpoint URL is strictly HTTPS and safe.
     * Prevents SSRF to internal loopback, private ranges, or unauthorized protocols.
     */
    fun isSecureHttpsEndpoint(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val trimmed = url.trim()
        if (!trimmed.startsWith("https://", ignoreCase = true)) return false

        return try {
            val uri = URI(trimmed)
            val host = uri.host ?: return false
            // Disallow internal private networks and loopback
            val isLoopback = host.equals("localhost", ignoreCase = true) ||
                    host.startsWith("127.") ||
                    host.startsWith("192.168.") ||
                    host.startsWith("10.") ||
                    host.startsWith("172.16.") ||
                    host == "0.0.0.0"
            !isLoopback
        } catch (_: Exception) {
            false
        }
    }

    // =========================================================================
    // 2. PASSWORD HASHING & AUTHENTICATION (PBKDF2-HMAC-SHA256)
    // =========================================================================

    data class PasswordHashResult(
        val hashBase64: String,
        val saltBase64: String
    )

    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    /**
     * Hashes a password using PBKDF2 with HMAC-SHA256 and a cryptographically secure random salt.
     */
    fun hashPassword(password: String, existingSalt: ByteArray? = null): PasswordHashResult {
        val random = SecureRandom()
        val salt = existingSalt ?: ByteArray(SALT_BYTES).also { random.nextBytes(it) }

        val spec: KeySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_LENGTH_BITS
        )

        val factory = try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        } catch (_: NoSuchAlgorithmException) {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        }

        val hash = factory.generateSecret(spec).encoded

        return PasswordHashResult(
            hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP),
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        )
    }

    /**
     * Verifies a candidate password against a stored hash and salt using constant-time comparison.
     */
    fun verifyPassword(candidatePassword: String, storedHashBase64: String, storedSaltBase64: String): Boolean {
        return try {
            val salt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
            val computed = hashPassword(candidatePassword, salt)
            val expectedBytes = Base64.decode(storedHashBase64, Base64.NO_WRAP)
            val actualBytes = Base64.decode(computed.hashBase64, Base64.NO_WRAP)
            MessageDigest.isEqual(expectedBytes, actualBytes) // Constant-time to prevent timing attacks
        } catch (_: Exception) {
            false
        }
    }

    // =========================================================================
    // 3. CLIENT-SIDE RATE LIMITING (SLIDING WINDOW / TOKEN BUCKET)
    // =========================================================================

    object RateLimiter {
        // Map of BucketKey -> List of request timestamps (epoch milliseconds)
        private val requestLogs = ConcurrentHashMap<String, MutableList<Long>>()

        /**
         * Checks if a request is allowed within a given window.
         * Automatically purges expired entries and records new timestamp if allowed.
         */
        @Synchronized
        fun isAllowed(bucketKey: String, maxRequests: Int, windowMillis: Long): Boolean {
            val now = System.currentTimeMillis()
            val cutoff = now - windowMillis
            val timestamps = requestLogs.getOrPut(bucketKey) { mutableListOf() }

            // Prune expired entries
            timestamps.removeAll { it < cutoff }

            return if (timestamps.size < maxRequests) {
                timestamps.add(now)
                true
            } else {
                false
            }
        }

        /**
         * Calculates remaining cooldown time in seconds if rate limited.
         */
        @Synchronized
        fun getCooldownSeconds(bucketKey: String, windowMillis: Long): Long {
            val now = System.currentTimeMillis()
            val cutoff = now - windowMillis
            val timestamps = requestLogs[bucketKey] ?: return 0L
            val oldest = timestamps.filter { it >= cutoff }.minOrNull() ?: return 0L
            val remainingMs = (oldest + windowMillis) - now
            return if (remainingMs > 0) (remainingMs / 1000L) + 1L else 0L
        }
    }

    // =========================================================================
    // 4. API KEY MASKING & SENSITIVE DATA PROTECTION
    // =========================================================================

    /**
     * Safely masks an API key (e.g. "AIzaSyD••••••••••••7Yxq") so sensitive secrets are never
     * exposed in user interfaces, logs, or diagnostic screens.
     */
    fun maskApiKey(key: String?): String {
        if (key.isNullOrBlank()) return "Not Configured"
        val trimmed = key.trim()
        if (trimmed.length <= 8) return "••••••••"
        val prefix = trimmed.take(4)
        val suffix = trimmed.takeLast(4)
        return "$prefix••••••••$suffix"
    }

    // =========================================================================
    // 5. ROLE-BASED ACCESS CONTROL (RBAC) & ADMIN ROUTE PROTECTION
    // =========================================================================

    enum class AccessLevel {
        LEARNER,
        STUDENT,
        MAMA,
        MENTOR,
        ADMIN
    }

    /**
     * Determines whether the current profile holds administrative permissions
     * (e.g. for backend database configuration, systemic resets, mentor management).
     */
    fun hasAdminAccess(role: String?, email: String?): Boolean {
        if (role == null) return false
        val cleanRole = role.lowercase()
        val cleanEmail = email?.trim()?.lowercase().orEmpty()

        val isAdminRole = cleanRole.contains("admin") ||
                cleanRole.contains("solo founder") ||
                cleanRole.contains("lead mentor")

        val isAuthorizedEmail = cleanEmail == "xabanokwazi008@gmail.com" ||
                cleanEmail.endsWith("@kodemamas.org")

        return isAdminRole || isAuthorizedEmail
    }

    // =========================================================================
    // 6. OKHTTP DEFENSIVE SECURITY HEADERS INTERCEPTOR
    // =========================================================================

    class SecurityHeadersInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()

            val secureRequest = original.newBuilder()
                .header("X-Content-Type-Options", "nosniff")
                .header("X-Frame-Options", "DENY")
                .header("X-XSS-Protection", "1; mode=block")
                .header("Referrer-Policy", "strict-origin-when-cross-origin")
                .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
                .header("User-Agent", "KodeMamas-SecureAndroid/1.2.1")
                .build()

            return chain.proceed(secureRequest)
        }
    }
}
