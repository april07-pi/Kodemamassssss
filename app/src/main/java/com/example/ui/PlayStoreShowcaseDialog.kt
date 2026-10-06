package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalKodeMamasColors

data class FeatureShowcaseItem(
    val title: String,
    val subtitle: String,
    val annotation: String,
    val icon: ImageVector,
    val badge: String,
    val useCase: String,
    val mockSnippet: String,
    val targetTab: String
)

@Composable
fun PlayStoreShowcaseDialog(
    onDismiss: () -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    val colors = LocalKodeMamasColors.current

    val showcaseItems = listOf(
        FeatureShowcaseItem(
            title = "12 Official SA Languages",
            subtitle = "Learn to code in your mother tongue",
            annotation = "Accessible coding instructions translated into isiZulu, isiXhosa, Afrikaans, Sepedi, Setswana, Sesotho, and more.",
            icon = Icons.Default.Translate,
            badge = "MULTILINGUAL EDTECH",
            useCase = "A mother in Bloemfontein or Khayelitsha learns fundamental algorithms explained with relatable cultural analogies.",
            mockSnippet = "🇿🇦 isiZulu: \"Funda amakhodi\"\n• Umsebenzi 1: Yakha i-Spaza Shop Webpage\n• I-HTML Tag: <h1>Sawubona Mzansi</h1>",
            targetTab = "learn"
        ),
        FeatureShowcaseItem(
            title = "Interactive Code Playground",
            subtitle = "Live mobile compiler & sandbox",
            annotation = "Run HTML, CSS, JavaScript, and Python directly on your phone with live rendering and syntax highlighting.",
            icon = Icons.Default.Terminal,
            badge = "HANDS-ON SIMULATOR",
            useCase = "Students practice real coding challenges without needing an expensive laptop or broadband WiFi.",
            mockSnippet = "<div class=\"spaza-card\">\n  <h2>Lindiwe Spaza</h2>\n  <button onclick=\"buy()\">Thenga</button>\n</div>",
            targetTab = "builds"
        ),
        FeatureShowcaseItem(
            title = "Unrestricted AI Assistant",
            subtitle = "Gemini-powered 24/7 coding tutor",
            annotation = "Ask any question from basic HTML tags to data science and career advice with culturally grounded South African guidance.",
            icon = Icons.Default.AutoAwesome,
            badge = "GEMINI INTELLIGENCE",
            useCase = "Learner asks: 'How do I build a database for inventory?' and gets step-by-step code and clear explanations.",
            mockSnippet = "🤖 KodeMamas AI:\n\"Halala mama! To calculate your daily stock profit, here is a clean JavaScript function...\"",
            targetTab = "ai_chat"
        ),
        FeatureShowcaseItem(
            title = "100% Offline-First Architecture",
            subtitle = "Download once, code anywhere with 0 data",
            annotation = "Low RAM and weak battery optimization designed for entry-level smartphones in township and rural communities.",
            icon = Icons.Default.CloudDownload,
            badge = "DATA-SAVING & OFFLINE",
            useCase = "Study on taxi commutes or load-shedding blackouts without spending airtime or mobile data bundles.",
            mockSnippet = "🔌 Offline Mode Active\n✓ 4 Courses Cached Locally\n✓ Room SQLite Database Ready\n✓ 0 MB Data Consumed",
            targetTab = "home"
        ),
        FeatureShowcaseItem(
            title = "Township Tech Mentorship",
            subtitle = "Direct 1-on-1 founder & mentor guidance",
            annotation = "Connect directly with solo founder & tech mentor Nokwazi Nobuhle Xaba in Bloemfontein for authentic personalized code reviews and tech career guidance.",
            icon = Icons.Default.Groups,
            badge = "FOUNDER MENTORSHIP",
            useCase = "Get practical mentorship on full-stack development, portfolio building, and landing remote freelancing opportunities.",
            mockSnippet = "👩🏽‍💻 Solo Tech Mentor Nokwazi Nobuhle Xaba:\n\"Sawubona! Let's review your project architecture and get your Android/Python portfolio industry-ready.\"",
            targetTab = "mentorship"
        ),
        FeatureShowcaseItem(
            title = "Certificates & CV Builder",
            subtitle = "Verified skills for the modern economy",
            annotation = "Earn authenticated certificates upon quiz completion and generate tech-ready CV summaries with verified badges.",
            icon = Icons.Default.WorkspacePremium,
            badge = "CAREER READINESS",
            useCase = "Mothers and graduates present tangible proof of programming skills to employers and local businesses.",
            mockSnippet = "🏆 Certificate of Completion\n• Awarded to: Nokwazi Xaba\n• Skill: Web & Python Programming\n• KodeMamas South Africa",
            targetTab = "home"
        )
    )

    var selectedIndex by remember { mutableIntStateOf(0) }
    val currentItem = showcaseItems[selectedIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Play Store Showcase",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.brandGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FEATURE TOUR",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.brandGold
                                )
                            }
                        }
                        Text(
                            text = "Core features & contextual use cases in KodeMamas",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Feature Selector Tabs (Horizontal indicator pills)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(showcaseItems) { index, item ->
                        val isSelected = selectedIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) colors.brandPurple else colors.surfaceVariant)
                                .border(1.dp, if (isSelected) colors.brandGold else colors.cardBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedIndex = index }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) colors.brandGold else colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "0${index + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Feature Card
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Phone frame simulation card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF2E094E),
                                            Color(0xFF19062B)
                                        )
                                    )
                                )
                                .border(2.dp, colors.brandGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                // Top badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.brandGold)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = currentItem.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }

                                    Text(
                                        text = "${selectedIndex + 1} of ${showcaseItems.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = currentItem.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = currentItem.subtitle,
                                    fontSize = 12.sp,
                                    color = colors.brandGold,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Visual mock interface snippet
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0C0714))
                                        .border(1.dp, Color(0xFF42166E), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        ) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                                        }
                                        Text(
                                            text = currentItem.mockSnippet,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFE2D6F5),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Annotation Box
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = colors.brandGold, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "FEATURE HIGHLIGHT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.brandGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentItem.annotation,
                                fontSize = 12.sp,
                                color = colors.textPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Contextual Use Case
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.brandPurple.copy(alpha = 0.1f))
                                .border(1.dp, colors.brandPurple.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = colors.brandPurple, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "REAL-WORLD USE CASE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.brandPurple
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentItem.useCase,
                                fontSize = 11.5.sp,
                                color = colors.textPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation and Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (selectedIndex > 0) {
                                selectedIndex--
                            } else {
                                selectedIndex = showcaseItems.lastIndex
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Previous", color = colors.textPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onNavigateToTab(currentItem.targetTab)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.brandGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(
                            text = "Explore This Feature",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (selectedIndex < showcaseItems.lastIndex) {
                                selectedIndex++
                            } else {
                                selectedIndex = 0
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Next", color = colors.textPrimary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
