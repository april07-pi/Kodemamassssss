package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.UserProfile
import com.example.ui.theme.LocalKodeMamasColors
import com.example.ui.theme.Localization

@Composable
fun SettingsHubDialog(
    userProfile: UserProfile?,
    currentThemeMode: String,
    onSetThemeMode: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenRateDialog: () -> Unit,
    onOpenFeedbackDialog: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTermsOfService: () -> Unit,
    onOpenShowcase: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onToggleDataSaving: (Boolean) -> Unit,
    onResetProgress: () -> Unit = {}
) {
    val colors = LocalKodeMamasColors.current

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
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = colors.brandGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Settings & Hub",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Preferences, Theme & Compliance",
                                fontSize = 11.sp,
                                color = colors.brandGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SECTION 1: THEME & DARK MODE
                    item {
                        SettingsSectionCard(title = "Appearance & Dark Mode", icon = Icons.Default.DarkMode) {
                            Text(
                                text = "Choose your preferred appearance or follow your system settings for optimal comfort.",
                                fontSize = 11.5.sp,
                                color = colors.textSecondary,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val themeOptions = listOf(
                                    Triple("DARK", "🌙 Dark", Icons.Default.NightlightRound),
                                    Triple("LIGHT", "☀️ Light", Icons.Default.LightMode),
                                    Triple("SYSTEM", "📱 System", Icons.Default.SettingsBrightness)
                                )

                                themeOptions.forEach { (mode, label, _) ->
                                    val isSelected = currentThemeMode.equals(mode, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) colors.brandPurple else colors.surfaceVariant)
                                            .border(1.dp, if (isSelected) colors.brandGold else colors.cardBorder, RoundedCornerShape(12.dp))
                                            .clickable { onSetThemeMode(mode) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 2: RATE & REPUTATION (Directly addressing item 3 in report)
                    item {
                        SettingsSectionCard(title = "Community & App Feedback", icon = Icons.Default.Star) {
                            Text(
                                text = "Your ratings and feedback help KodeMamas bring coding education to more township schools and mothers.",
                                fontSize = 11.5.sp,
                                color = colors.textSecondary,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Rate Your App Button
                            Button(
                                onClick = onOpenRateDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.brandGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Rate KodeMamas on Google Play",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Feedback loop button
                            OutlinedButton(
                                onClick = onOpenFeedbackDialog,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, colors.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Feedback, contentDescription = null, tint = colors.brandPurple, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Suggest a Feature or Send Feedback",
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // SECTION 3: PLAY STORE SHOWCASE & TOUR (Addressing item 1 in report)
                    item {
                        SettingsSectionCard(title = "Play Store & Feature Tour", icon = Icons.Default.AutoAwesome) {
                            Text(
                                text = "Take a guided visual tour of our 6 core capabilities including offline learning, playgrounds, and 12 official South African languages.",
                                fontSize = 11.5.sp,
                                color = colors.textSecondary,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = onOpenShowcase,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, colors.brandGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = colors.brandGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "View Annotated Feature Showcase",
                                    color = colors.brandGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // SECTION 4: DATA SAVING & OFFLINE ARCHITECTURE
                    item {
                        SettingsSectionCard(title = "Data & Offline Settings", icon = Icons.Default.OfflinePin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Data-Saving Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Compresses network requests & prioritizes offline local cache.",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Switch(
                                    checked = userProfile?.dataSavingMode ?: false,
                                    onCheckedChange = { onToggleDataSaving(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = colors.brandGold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceVariant)
                                    .clickable { onOpenLanguagePicker() }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Translate, contentDescription = null, tint = colors.brandGold, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text(
                                            text = "App Language",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "12 Official South African Languages Supported",
                                            fontSize = 10.5.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.08f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                    .clickable { onResetProgress() }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Reset Course Progress to 0%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                    Text(
                                        text = "Clear completed modules and restart curriculum from zero.",
                                        fontSize = 10.5.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Progress", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // SECTION 5: LEGAL & POLICIES (Addressing item 4 in report)
                    item {
                        SettingsSectionCard(title = "Legal, Privacy & Compliance", icon = Icons.Default.Security) {
                            Text(
                                text = "KodeMamas is fully compliant with South African POPIA regulations and international data privacy laws.",
                                fontSize = 11.5.sp,
                                color = colors.textSecondary,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceVariant)
                                    .clickable { onOpenPrivacyPolicy() }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Policy, contentDescription = null, tint = colors.brandGold, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Privacy Policy (POPIA Compliant)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary
                                    )
                                }
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceVariant)
                                    .clickable { onOpenTermsOfService() }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = colors.brandPurple, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Terms of Service",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = colors.textPrimary
                                    )
                                }
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary)
                            }
                        }
                    }

                    // SECTION 6: ABOUT & ATTRIBUTION
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.brandPurple.copy(alpha = 0.08f))
                                .border(1.dp, colors.brandPurple.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "KodeMamas South Africa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Solo Founded & Created by Nokwazi Nobuhle Xaba (Bloemfontein)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.brandGold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Empowering South African township and rural communities with offline coding education and digital skills.",
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "v${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE}) • ASO Optimized • Offline-First",
                                fontSize = 10.sp,
                                color = colors.textSecondary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalKodeMamasColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.brandGold,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}
