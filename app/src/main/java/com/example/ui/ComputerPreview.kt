package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ThemeGold
import com.example.ui.theme.ThemeIndigo

/**
 * ComputerDesktopPreview
 *
 * Renders an authentic computer monitor / desktop browser window preview
 * for HTML, CSS, JavaScript, and Python code in the IDE and Lesson Simulator.
 * Features realistic window controls (red/yellow/green traffic lights), URL bar,
 * desktop viewport zoom, devtools console, and 100% offline local WebView rendering.
 */
@Composable
fun ComputerDesktopPreview(
    code: String,
    language: String,
    consoleOutput: String = "",
    title: String = "Mama's Spaza Web App",
    modifier: Modifier = Modifier
) {
    var isDesktopMode by remember { mutableStateOf(true) } // true: Desktop 1024px, false: Mobile 375px
    var activeTab by remember { mutableIntStateOf(0) } // 0: Browser Preview, 1: Computer Terminal
    var refreshKey by remember { mutableIntStateOf(0) }
    var isFullscreen by remember { mutableStateOf(false) }

    val isWebCode = remember(code, language) {
        language.equals("HTML", ignoreCase = true) ||
                language.equals("CSS", ignoreCase = true) ||
                code.contains("<html", ignoreCase = true) ||
                code.contains("<h1>", ignoreCase = true) ||
                code.contains("<div", ignoreCase = true) ||
                code.contains("<p>", ignoreCase = true) ||
                code.contains("<button", ignoreCase = true) ||
                code.contains("<!DOCTYPE", ignoreCase = true)
    }

    // Wrap the preview card
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("computer_desktop_preview_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.5.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. COMPUTER WINDOW HEADER (Traffic light dots & window title)
            ComputerWindowTopBar(
                title = title,
                isDesktopMode = isDesktopMode,
                activeTab = activeTab,
                isWebCode = isWebCode,
                onToggleDeviceMode = { isDesktopMode = !isDesktopMode },
                onSelectTab = { activeTab = it },
                onRefresh = { refreshKey++ },
                onExpandFullscreen = { isFullscreen = true }
            )

            // 2. BROWSER ADDRESS BAR (When on Browser Preview tab)
            if (activeTab == 0 && isWebCode) {
                ComputerBrowserAddressBar(
                    isDesktopMode = isDesktopMode,
                    onRefresh = { refreshKey++ }
                )
            }

            // 3. MAIN PREVIEW VIEWPORT AREA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(if (activeTab == 0 && isWebCode) Color(0xFF0F172A) else Color(0xFF0D1117))
            ) {
                if (activeTab == 0 && isWebCode) {
                    // LIVE BROWSER WEBVIEW
                    OfflineHtmlRenderer(
                        rawCode = code,
                        language = language,
                        isDesktopMode = isDesktopMode,
                        refreshKey = refreshKey
                    )
                } else {
                    // DESKTOP COMPUTER TERMINAL
                    ComputerTerminalScreen(
                        code = code,
                        language = language,
                        consoleOutput = consoleOutput
                    )
                }
            }

            // 4. COMPUTER STATUS FOOTER
            ComputerWindowStatusBar(
                language = language,
                isDesktopMode = isDesktopMode,
                isWebCode = isWebCode
            )
        }
    }

    // Fullscreen Dialog View
    if (isFullscreen) {
        Dialog(
            onDismissRequest = { isFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WindowTrafficDots()
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "🖥️ Computer Monitor Preview (Full Screen)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { isFullscreen = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        ComputerBrowserAddressBar(
                            isDesktopMode = isDesktopMode,
                            onRefresh = { refreshKey++ }
                        )

                        Box(modifier = Modifier.fillMaxSize()) {
                            if (isWebCode) {
                                OfflineHtmlRenderer(
                                    rawCode = code,
                                    language = language,
                                    isDesktopMode = isDesktopMode,
                                    refreshKey = refreshKey
                                )
                            } else {
                                ComputerTerminalScreen(
                                    code = code,
                                    language = language,
                                    consoleOutput = consoleOutput
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComputerWindowTopBar(
    title: String,
    isDesktopMode: Boolean,
    activeTab: Int,
    isWebCode: Boolean,
    onToggleDeviceMode: () -> Unit,
    onSelectTab: (Int) -> Unit,
    onRefresh: () -> Unit,
    onExpandFullscreen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Traffic lights + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            WindowTrafficDots()
            Spacer(modifier = Modifier.width(10.dp))

            // Active Tab Pill (Desktop Browser Tab)
            Surface(
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                color = if (activeTab == 0) Color(0xFF1E293B) else Color(0xFF0F172A),
                modifier = Modifier.clickable { onSelectTab(0) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isWebCode) "🌐 $title" else "🖥️ Screen Output",
                        color = if (activeTab == 0) Color.White else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Terminal Tab Pill
            Surface(
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                color = if (activeTab == 1) Color(0xFF1E293B) else Color(0xFF0F172A),
                modifier = Modifier.clickable { onSelectTab(1) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💻 Terminal",
                        color = if (activeTab == 1) Color.White else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Action controls (Device mode toggle & Fullscreen)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Desktop / Mobile Mode Toggle
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF334155),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onToggleDeviceMode() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDesktopMode) Icons.Default.Computer else Icons.Default.Smartphone,
                        contentDescription = if (isDesktopMode) "Desktop View" else "Mobile View",
                        tint = ThemeGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isDesktopMode) "Desktop" else "Mobile",
                        color = ThemeGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Fullscreen Button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155))
                    .clickable { onExpandFullscreen() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun WindowTrafficDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444)) // Red: Close
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B)) // Yellow: Minimize
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981)) // Green: Maximize
        )
    }
}

@Composable
private fun ComputerBrowserAddressBar(
    isDesktopMode: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Navigation Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(14.dp)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Forward",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(14.dp)
            )
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reload",
                tint = Color(0xFF94A3B8),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRefresh() }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // URL Input Bar
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secure Connection",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "http://localhost:3000/spaza-preview.html",
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Viewport dimensions pill
        Text(
            text = if (isDesktopMode) "1024×768" else "375×667",
            color = Color(0xFF64748B),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * OfflineHtmlRenderer hosts an Android WebView directly without network calls.
 * Renders complete HTML/CSS/JS with pleasant local defaults.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun OfflineHtmlRenderer(
    rawCode: String,
    language: String,
    isDesktopMode: Boolean,
    refreshKey: Int
) {
    val preparedHtml = remember(rawCode, language, isDesktopMode, refreshKey) {
        buildPreparedHtml(rawCode, language, isDesktopMode)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = isDesktopMode
                    settings.loadWithOverviewMode = isDesktopMode
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false
                    settings.cacheMode = WebSettings.LOAD_NO_CACHE

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            return true
                        }
                    }

                    loadDataWithBaseURL(
                        "http://localhost:3000/",
                        preparedHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            update = { webView ->
                webView.settings.useWideViewPort = isDesktopMode
                webView.settings.loadWithOverviewMode = isDesktopMode
                webView.loadDataWithBaseURL(
                    "http://localhost:3000/",
                    preparedHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * ComputerTerminalScreen displays command output styled like a Unix/Mac desktop terminal.
 */
@Composable
private fun ComputerTerminalScreen(
    code: String,
    language: String,
    consoleOutput: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Terminal shell prompt
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "kodemamas@desktop:~/projects/spaza$ ",
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (language) {
                    "Python" -> "python3 main.py"
                    "JavaScript" -> "node app.js"
                    "CSS" -> "npx stylelint style.css"
                    else -> "open index.html"
                },
                color = ThemeGold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Output text
        val displayText = if (consoleOutput.isNotBlank()) consoleOutput else "Ready to execute. Tap \"Execute in Simulator\" to run your code."
        Text(
            text = displayText,
            color = if (displayText.contains("Error", ignoreCase = true)) Color(0xFFF87171) else Color(0xFF34D399),
            fontSize = 11.5.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Process exit indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "[Process completed with exit code 0]",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun ComputerWindowStatusBar(
    language: String,
    isDesktopMode: Boolean,
    isWebCode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isWebCode) "🖥️ Desktop HTML5/CSS3 Engine Active" else "💻 Local Terminal Engine",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "100% Offline • Localhost",
            color = ThemeGold,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Wraps user code into a standard HTML5 template with clean default styles.
 */
private fun buildPreparedHtml(code: String, language: String, isDesktopMode: Boolean): String {
    val viewportWidth = if (isDesktopMode) "1024" else "width=device-width, initial-scale=1.0"

    // If it's already a full HTML document
    if (code.contains("<!DOCTYPE html", ignoreCase = true) || code.contains("<html", ignoreCase = true)) {
        return if (!code.contains("viewport", ignoreCase = true)) {
            code.replace("<head>", "<head><meta name=\"viewport\" content=\"$viewportWidth\">")
        } else {
            code
        }
    }

    // If CSS snippet, wrap inside a mock preview storefront
    if (language.equals("CSS", ignoreCase = true) || (!code.contains("<") && code.contains("{"))) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="$viewportWidth">
              <style>
                $code
              </style>
            </head>
            <body>
              <div class="card" style="margin: 20px; padding: 20px; border-radius: 12px; font-family: sans-serif;">
                <h1 style="margin-top:0;">Mama's Soweto Spaza Shop</h1>
                <p>Welcome to our online catalog! Fresh milk, brown bread, and prepaid electricity.</p>
                <button style="padding: 10px 16px; border-radius: 8px; cursor: pointer; font-weight: bold;">Order on WhatsApp</button>
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    // Default HTML wrapper for snippets
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="$viewportWidth">
          <style>
            * { box-sizing: border-box; }
            body {
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
              margin: 0;
              padding: 16px;
              background-color: #0F172A;
              color: #F8FAFC;
              line-height: 1.5;
            }
            h1, h2, h3 { color: #FBBF24; margin-top: 0; }
            button {
              background: #6D28D9;
              color: white;
              border: none;
              padding: 10px 16px;
              border-radius: 8px;
              font-weight: bold;
              cursor: pointer;
            }
            input, select {
              padding: 8px 12px;
              border-radius: 6px;
              border: 1px solid #475569;
              background: #1E293B;
              color: white;
              margin: 4px 0;
            }
            .card {
              background: #1E293B;
              border-radius: 12px;
              padding: 16px;
              border: 1px solid #334155;
            }
          </style>
        </head>
        <body>
          $code
        </body>
        </html>
    """.trimIndent()
}
