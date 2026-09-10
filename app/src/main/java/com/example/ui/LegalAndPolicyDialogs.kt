package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalKodeMamasColors

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val colors = LocalKodeMamasColors.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.brandPurple.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = colors.brandGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Privacy Policy",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "POPIA & Global Data Protection",
                                fontSize = 11.sp,
                                color = colors.brandGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.brandPurple.copy(alpha = 0.1f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🇿🇦 KodeMamas respects your constitutional right to privacy under South Africa's Protection of Personal Information Act (POPIA No. 4 of 2013).",
                        fontSize = 11.sp,
                        color = colors.textPrimary,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        PolicySection(
                            title = "1. Our Mission & Digital Inclusion",
                            content = "KodeMamas is an offline-first multilingual coding education platform built specifically for South African township and rural communities. Founded by Nokwazi Nobuhle Xaba, our mission is to provide safe, accessible, and culturally empowering coding education to mothers, girls, and youth.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "2. Data We Collect & Store",
                            content = "We practice strict data minimization. The only data stored includes:\n• Your chosen learner profile name and community hub (e.g. Bloemfontein Hub, Soweto Hub)\n• Preferred South African language (selected from the 12 official languages)\n• Lesson progress, quiz scores, badges, and learning streak\n• Offline downloaded lessons for local playback\n• User-initiated discussion messages or notes",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "3. Offline-First Architecture & Privacy",
                            content = "Unlike web-heavy apps that continuously stream user metrics to external servers, KodeMamas is built with an offline-first local database (Room SQLite). Your lesson completion data, code playground files, and progress remain on your device even without an internet connection.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "4. AI Learning Assistant & Chat Data",
                            content = "When you ask questions in the AI Assistant tab, text is transmitted via secure encrypted channels (HTTPS/TLS) to generate helpful coding and conceptual answers. We do not use your private interactions to build advertising profiles, nor do we sell chat transcripts.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "5. Zero Data Selling & Third-Party Sharing",
                            content = "KodeMamas will NEVER sell, rent, or trade your personal information, email address, or learning metrics to advertisers, data brokers, or third-party marketers. Any external integrations are strictly for non-commercial educational delivery.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "6. Your Rights Under POPIA",
                            content = "As a user in South Africa and globally, you have the right to:\n• Access and review all data stored in your local profile\n• Update or rectify your profile name, language, and settings\n• Clear or reset your data at any time\n• Request complete deletion of your account and records by reaching out to our community team.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "7. Contact & Privacy / Information Officer",
                            content = "If you have questions, feedback, or wish to exercise your rights under POPIA, you can use the in-app 'Feedback & Suggestion' feature or email:\n• Privacy & Information Officer: Nokwazi Nobuhle Xaba (Bloemfontein)\n• Email: kodemamas@gmail.com\n• Republic of South Africa",
                            colors = colors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.brandGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "I Understand & Accept",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TermsOfServiceDialog(
    onDismiss: () -> Unit
) {
    val colors = LocalKodeMamasColors.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.brandPurple.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Policy,
                                contentDescription = null,
                                tint = colors.brandGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Terms of Service",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Educational Platform Guidelines",
                                fontSize = 11.sp,
                                color = colors.brandGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        PolicySection(
                            title = "1. Acceptance of Terms",
                            content = "By downloading, installing, or accessing the KodeMamas application, you agree to comply with and be bound by these Terms of Service. If you do not agree to these terms, please do not use the application.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "2. Educational Purpose & Intended Use",
                            content = "KodeMamas is created to provide beginner-friendly, accessible coding education (HTML, CSS, JavaScript, Python), digital literacy, mentorship, and career readiness for South African learners. The platform is designed for positive personal growth and workforce empowerment.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "3. Community Code of Conduct",
                            content = "KodeMamas is a safe, inclusive, and uplifting community for mothers, young girls, students, and township community members entering technology for the first time.\n• Treat fellow learners and mentors with respect and ubuntu.\n• No harassment, hate speech, discrimination, or abusive behavior will be tolerated.\n• Posts violating these standards are subject to immediate removal.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "4. Interactive Code Playground & Sandboxing",
                            content = "Code executed within the interactive code playground and lesson simulators runs within safe client-side sandbox environments. Users agree not to attempt to execute malicious code, exploit vulnerabilities, or misuse the learning simulator.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "5. Certificates of Completion & Skills",
                            content = "Certificates awarded upon completing modules reflect your verified progress and quiz scores within the app. These credentials celebrate your achievement and can be referenced in your CV or portfolio.",
                            colors = colors
                        )
                    }
                    item {
                        PolicySection(
                            title = "6. Intellectual Property & Brand Rights",
                            content = "All original educational curriculum, branding, illustrations, translations, and software code are the intellectual property of KodeMamas and founder Nokwazi Nobuhle Xaba. You may freely use the code you write during lessons for personal or commercial projects.",
                            colors = colors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.brandGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "I Agree & Continue",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: String,
    colors: com.example.ui.theme.KodeMamasColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, colors.cardBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colors.brandGold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            fontSize = 11.5.sp,
            color = colors.textPrimary.copy(alpha = 0.9f),
            lineHeight = 16.5.sp
        )
    }
}
