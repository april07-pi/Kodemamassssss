package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ThemeCardBorder
import com.example.ui.theme.ThemeDarkBg
import com.example.ui.theme.ThemeGold
import com.example.ui.theme.ThemeIndigo
import com.example.data.SecurityUtils

@Composable
fun SubscriptionPlansDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF9F9FB))
                .border(2.dp, ThemeIndigo.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ThemeDarkBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ThemeGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🇿🇦", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "KodeMamas Membership",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Standard R99/yr • Premium R299/yr",
                                    color = ThemeGold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SubscriptionPlansContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SubscriptionPlansContent(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentPlan by viewModel.currentPlanTier.collectAsState()
    val paymentRef by viewModel.paymentReference.collectAsState()
    val paymentStatus by viewModel.paymentStatus.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTierForPayment by remember { mutableStateOf(if (currentPlan == "STANDARD") "STANDARD" else "PREMIUM") }
    var inputPaymentRef by remember { mutableStateOf(paymentRef) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Introduction Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF26053D), Color(0xFF4A1878))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🇿🇦", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Township & Rural Tech Empowerment",
                        color = ThemeGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Affordable, community-first coding education in South Africa. Pay easily using Capitec EFT or Instant Payment.",
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Current Status: ",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (currentPlan) {
                                    "PREMIUM" -> ThemeGold
                                    "STANDARD" -> Color(0xFF4CAF50)
                                    else -> Color.White.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (currentPlan) {
                                "PREMIUM" -> "👑 PREMIUM MEMBER"
                                "STANDARD" -> "⭐ STANDARD MEMBER"
                                else -> "FREE TRIAL"
                            },
                            color = if (currentPlan == "PREMIUM") Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // ================= PLAN 1: STANDARD (R99/year) =================
        val isStandardActive = currentPlan == "STANDARD"
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isStandardActive) 2.dp else 1.dp,
                    color = if (isStandardActive) Color(0xFF2E7D32) else ThemeCardBorder,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Standard",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FOUNDATIONAL",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Core coding curriculum & multilingual tools",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "R99",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "/ year",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                Text(
                    text = "Includes all essentials:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))

                val standardFeatures = listOf(
                    "Coding courses (HTML, CSS, JavaScript, Python)",
                    "Multilingual learning (All 12 SA languages)",
                    "Offline lesson access & downloads",
                    "Quizzes and exercises",
                    "Progress tracking dashboard",
                    "Gamification & XP rewards",
                    "Certificates of completion",
                    "Community access & discussions",
                    "Basic digital skills content"
                )

                standardFeatures.forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isStandardActive) {
                    OutlinedButton(
                        onClick = { /* already active */ },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.5.dp, Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Standard Plan", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            selectedTierForPayment = "STANDARD"
                            Toast.makeText(context, "Standard Plan selected. Transfer R99 to Capitec and enter EFT ref below.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedTierForPayment == "STANDARD") "Selected: Standard — R99/yr 👇" else "Select Standard — R99/year",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ================= PLAN 2: PREMIUM (R299/year) =================
        val isPremiumActive = currentPlan == "PREMIUM"
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isPremiumActive) 2.5.dp else 1.5.dp,
                    color = if (isPremiumActive) ThemeGold else Color(0xFFDAA520),
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF8))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ThemeDarkBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑 ", fontSize = 10.sp)
                            Text(
                                text = "RECOMMENDED FOR CAREERS",
                                color = ThemeGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "R299",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFB8860B)
                        )
                        Text(
                            text = "/ year",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Premium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    text = "Everything in Standard, plus full mentorship & job readiness:",
                    fontSize = 11.sp,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.SemiBold
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFFFE082))

                val premiumFeatures = listOf(
                    "One-on-one tech mentorship",
                    "Mentor matching",
                    "Career coaching",
                    "CV Builder",
                    "Portfolio Builder",
                    "LinkedIn profile assistance",
                    "Interview preparation",
                    "Mock interviews",
                    "Internship/job readiness",
                    "Project reviews",
                    "Opportunities portal",
                    "Additional career resources"
                )

                premiumFeatures.forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(ThemeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isPremiumActive) {
                    Button(
                        onClick = { /* already active */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Premium Member 🌟", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                } else {
                    Button(
                        onClick = {
                            selectedTierForPayment = "PREMIUM"
                            Toast.makeText(context, "Premium Plan selected. Transfer R299 to Capitec and enter EFT ref below.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedTierForPayment == "PREMIUM") "Selected: Premium — R299/yr 👇" else "Select Premium — R299/year 👑",
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // ================= CAPITEC PAYMENT DETAILS =================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFF003087), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Bank Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF003087)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💳", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Capitec Bank Payment Details",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color(0xFF003087)
                            )
                            Text(
                                text = "Official KodeMamas Payment Account",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("VERIFIED SA", color = Color(0xFF0277BD), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Number Highlight Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF4F7FB))
                        .border(1.dp, Color(0xFFD0E1F9), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CAPITEC ACCOUNT NUMBER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF555555)
                                )
                                Text(
                                    text = "2121743886",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF003087),
                                    letterSpacing = 1.sp
                                )
                            }

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("2121743886"))
                                    Toast.makeText(context, "Capitec Account 2121743886 Copied!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003087)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2ECF9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Bank", fontSize = 10.sp, color = Color.Gray)
                                Text("Capitec Bank", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Column {
                                Text("Universal Branch Code", fontSize = 10.sp, color = Color.Gray)
                                Text("470010", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Column {
                                Text("Account Type", fontSize = 10.sp, color = Color.Gray)
                                Text("Savings / Transact", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Account Holder", fontSize = 10.sp, color = Color.Gray)
                                Text("KodeMamas Education", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Reference Suggestion", fontSize = 10.sp, color = Color.Gray)
                                Text("KM-[Your Name / Phone]", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003087))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Steps Guide
                Text("How to pay with Capitec or any SA Banking App:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Open your Capitec app or any banking app (FNB, Standard Bank, Nedbank, Absa, TymeBank)\n" +
                           "2. Transfer R99 (Standard) or R299 (Premium) to Capitec Account: 2121743886 (Branch: 470010)\n" +
                           "3. Use your name or mobile number as the payment reference\n" +
                           "4. Copy your bank transaction reference from your receipt or SMS notification\n" +
                           "5. Enter the reference below and tap Verify & Submit to activate.",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Current verification status card
                if (paymentStatus != "NONE" && currentPlan != "FREE") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (paymentStatus == "VERIFIED") Color(0xFFE8F5E9) else Color(0xFFFFF8E1))
                            .border(1.dp, if (paymentStatus == "VERIFIED") Color(0xFF81C784) else Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (paymentStatus == "VERIFIED") Icons.Default.CheckCircle else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (paymentStatus == "VERIFIED") Color(0xFF2E7D32) else Color(0xFFF57F17),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (paymentStatus == "VERIFIED") "✅ $currentPlan Plan Verified & Active" else "⏳ Payment Pending Verification ($currentPlan)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (paymentStatus == "VERIFIED") Color(0xFF2E7D32) else Color(0xFFF57F17)
                                )
                            }
                            if (paymentRef.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Recorded EFT Ref: $paymentRef", fontSize = 11.sp, color = Color.DarkGray)
                            }
                            if (isAdmin && paymentStatus == "PENDING_VERIFICATION") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        viewModel.verifyPaymentByAdmin()
                                        Toast.makeText(context, "Admin verified payment!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve Payment (Founder & Admin)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Payment Reference Input & Strict Verification
                Text("Submit Bank EFT Reference to Unlock Plan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selected Tier: $selectedTierForPayment (${if (selectedTierForPayment == "STANDARD") "R99/year" else "R299/year"})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedTierForPayment == "STANDARD") Color(0xFF2E7D32) else Color(0xFFDAA520)
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = inputPaymentRef,
                    onValueChange = {
                        inputPaymentRef = it
                        if (inputError != null) inputError = null
                    },
                    isError = inputError != null,
                    label = { Text("Capitec / Bank Payment Reference", fontSize = 11.sp) },
                    placeholder = { Text("e.g. CAP-88492048 or KM-NOKWAZI-99", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF003087),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                if (inputError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = inputError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val trimmed = inputPaymentRef.trim()
                        if (!SecurityUtils.isValidPaymentReference(trimmed)) {
                            inputError = "⚠️ Please enter a valid bank transaction reference (min 6 characters) from your Capitec transfer receipt. Blank or dummy references cannot activate plans."
                            Toast.makeText(context, "Invalid payment reference", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        inputError = null
                        val success = viewModel.submitPaymentReference(selectedTierForPayment, trimmed)
                        if (success) {
                            showSuccessBanner = true
                            Toast.makeText(context, "$selectedTierForPayment reference recorded for verification! 🇿🇦", Toast.LENGTH_LONG).show()
                        } else {
                            inputError = "Payment reference rejected. Please check your bank transaction code."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTierForPayment == "STANDARD") Color(0xFF2E7D32) else ThemeGold
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (selectedTierForPayment == "STANDARD") Color.White else Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify & Submit $selectedTierForPayment Payment Reference",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTierForPayment == "STANDARD") Color.White else Color.Black
                    )
                }

                if (currentPlan != "FREE") {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            viewModel.downgradeToFree()
                            inputPaymentRef = ""
                            inputError = null
                            Toast.makeText(context, "Plan reset to free trial mode", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset back to Free Trial", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
