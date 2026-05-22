package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.Localization
import com.example.ui.theme.MyApplicationTheme

// Theme color duplicates to ensure compile-safety
val ThemeIndigo = Color(0xFF4B0082) // Main Artistic Flair deep indigo/purple
val ThemeGold = Color(0xFFFFD700)   // Bright South African Gold accent
val ThemeGoldLight = Color(0xFFFFF2B2)
val ThemeSoftBg = Color(0xFFFAF9FF) // Light artistic lavender white
val ThemeCardBorder = Color(0xFFE5E0FA)
val ThemeDarkBg = Color(0xFF0C0714) // Rich night background

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val langCode by viewModel.currentLanguageCode.collectAsState()

    val currentLesson by viewModel.currentActiveLesson.collectAsState()

    // Dialogs
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeSoftBg),
        topBar = {
            if (currentLesson == null) {
                AppHeader(
                    userProfile = userProfile,
                    langCode = langCode,
                    isOnline = isOnline,
                    onLangClick = { showLanguageDialog = true },
                    onToggleNetwork = { viewModel.toggleNetworkMode() }
                )
            }
        },
        bottomBar = {
            if (currentLesson == null) {
                AppBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    langCode = langCode
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ThemeSoftBg)
        ) {
            if (currentLesson != null) {
                // If a lesson is being taken, show full-bleed coding simulator view
                ActiveLessonSimulator(viewModel = viewModel, langCode = langCode)
            } else {
                // Standard tabs based content structure
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "tabChange"
                ) { tab ->
                    when (tab) {
                        "home" -> DashboardTab(viewModel = viewModel, langCode = langCode)
                        "learn" -> LearnTab(viewModel = viewModel, langCode = langCode)
                        "ai_chat" -> AiChatTab(viewModel = viewModel, langCode = langCode)
                        "community" -> CommunityTab(viewModel = viewModel, langCode = langCode)
                        "mentorship" -> MentorshipTab(viewModel = viewModel, langCode = langCode)
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLangCode = langCode,
            onDismiss = { showLanguageDialog = false },
            onLangSelected = { code ->
                viewModel.changeLanguage(code)
                showLanguageDialog = false
            }
        )
    }
}

// ---------------------- COMPONENT: HEADER ----------------------
@Composable
fun AppHeader(
    userProfile: UserProfile?,
    langCode: String,
    isOnline: Boolean,
    onLangClick: () -> Unit,
    onToggleNetwork: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(ThemeIndigo, Color(0xFF330066))
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Logo & Controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kode",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Mamas",
                        color = ThemeGold,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }
                Text(
                    text = "SOUTH AFRICA",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "by Nokwazi Nobuhle Xaba",
                    color = ThemeGold.copy(alpha = 0.95f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Network Status Toggle Widget
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isOnline) Color(0xFF10B981).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.15f))
                        .clickable { onToggleNetwork() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isOnline) Color(0xFF10B981) else Color(0xFFFFB800))
                        )
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            color = if (isOnline) Color(0xFF10B981) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Language selector pill
                Button(
                    onClick = onLangClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = Localization.languages.find { it.code == langCode }?.localName ?: "English",
                            color = ThemeGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change Language",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile summary card (Sub-banner)
        userProfile?.let { profile ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Native avatar design representing a mama with traditional headwrap (Naledi)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ThemeGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.name.take(1).uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = ThemeIndigo
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sawubona, ${profile.name}! 👋",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${profile.role} • Soweto Hub",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }

                // Stats values
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Localization.translate("total_xp", langCode).uppercase(),
                            color = ThemeGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "XP Logo",
                                tint = ThemeGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${profile.xp}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Localization.translate("streak", langCode).uppercase(),
                            color = Color(0xFFFF5722),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Whatshot,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${profile.streak}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- COMPONENT: NAVIGATION ----------------------
@Composable
fun AppBottomNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    langCode: String
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, ThemeCardBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("home", Icons.Default.Home, Localization.translate("dashboard", langCode)),
            Triple("learn", Icons.Default.School, Localization.translate("lessons", langCode)),
            Triple("ai_chat", Icons.Default.Assistant, Localization.translate("ai_assistant", langCode)),
            Triple("community", Icons.Default.Forum, Localization.translate("community", langCode)),
            Triple("mentorship", Icons.Default.CardMembership, Localization.translate("premium", langCode))
        )

        items.forEach { (route, icon, label) ->
            val isSelected = selectedTab == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) ThemeIndigo else Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ThemeIndigo else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ThemeIndigo.copy(alpha = 0.08f)
                )
            )
        }
    }
}

// ---------------------- TAB 1: DASHBOARD / HOME ----------------------
@Composable
fun DashboardTab(viewModel: MainViewModel, langCode: String) {
    val lessons by viewModel.allLessons.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val challenges by viewModel.allChallenges.collectAsState()
    val activeChallenge by viewModel.activeChallenge.collectAsState()

    var showCertificateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Hero banner following Artistic Flair mockup
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .border(1.dp, ThemeCardBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                // Emerald local downloaded pills representing township accessibility
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE6F4EA))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "OFFLINE COMPILER",
                            color = Color(0xFF137333),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Column {
                    Text(
                        text = Localization.translate("learn_coding", langCode) + "!",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build digital storefronts, smart predictive crops modules, and master technology at your own pace.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                // Find first playable lesson or navigate to Learn tab
                                val firstPlayable = lessons.find { it.isUnlocked } ?: lessons.firstOrNull()
                                firstPlayable?.let { viewModel.selectLesson(it) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = Localization.translate("get_started", langCode),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Data Saver toggle switch
                        userProfile?.let { profile ->
                            Button(
                                onClick = { viewModel.toggleDataSavingMode(!profile.dataSavingMode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (profile.dataSavingMode) Color(0xFFE8F0FE) else Color.Gray.copy(alpha = 0.08f)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = if (profile.dataSavingMode) Icons.Default.SignalCellularAlt2Bar else Icons.Default.SignalCellularAlt,
                                    contentDescription = "Data Saver",
                                    tint = if (profile.dataSavingMode) Color(0xFF1A73E8) else Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.translate("data_saver", langCode),
                                    color = if (profile.dataSavingMode) Color(0xFF1A73E8) else Color.DarkGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stats rows (Streak, Completed Lessons, Offline downloads)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Offline Lesson Downloads
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.downloadAllLessons() }
                        .padding(14.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Offline Cache",
                            tint = ThemeIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "OFFLINE LESSONS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = ThemeIndigo
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val downloadedCount = lessons.count { it.isDownloaded }
                        Text(
                            text = if (userProfile?.hasDownloadedOffline == true || downloadedCount == 4) "All 4 Saved" else "$downloadedCount/4 Saved",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Tap to save mobile data",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Card 2: Career & Certificate Status
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ThemeDarkBg)
                        .border(1.dp, ThemeCardBorder.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable { showCertificateDialog = true }
                        .padding(14.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdded,
                            contentDescription = "Certificates",
                            tint = ThemeGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "MY CERTIFICATE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = ThemeGold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val completedCount = lessons.count { l ->
                            l.id == "html_1" // simple placeholder indicator
                        }
                        Text(
                            text = "Claim Certificate",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Soweto Hub Certified",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Daily Coding Challenge Interactive segment
        item {
            val challenge = challenges.firstOrNull()
            if (challenge != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, ThemeCardBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "Daily challenge",
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Localization.translate("daily_challenges", langCode).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFF5722)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (challenge.isCompleted) Color(0xFFE6F4EA) else Color(0xFFFEF7E0))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (challenge.isCompleted) "SOLVED (+20 XP)" else "ACTIVE",
                                    color = if (challenge.isCompleted) Color(0xFF137333) else Color(0xFFB06000),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = challenge.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Text(
                            text = challenge.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        if (!challenge.isCompleted) {
                            Button(
                                onClick = {
                                    viewModel.setActiveChallenge(challenge)
                                    // solve challenge directly
                                    viewModel.solveChallenge()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Accept Challenge & Run Calculation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "Well done! Your baking order calculations are compile-accurate. You've earned 20 XP!",
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section: "Meet the Founders" Story (Emotionally connecting with South Africans)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ThemeIndigo)
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ThemeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✊", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Localization.translate("founder_story", langCode),
                            color = ThemeGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = Localization.translate("founder_desc", langCode),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Testimonials Slide Show
        item {
            Column {
                Text(
                    text = Localization.translate("testimonials", langCode),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Render dynamic testimonies
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE8F0FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "👩🏾‍🍳", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Mama Thandi", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                Text(text = "Soweto Spaza Owner", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"I finished Lesson 2 (CSS Styling) and designed a storefront for my fresh bread. Two localized customers called after viewing my catalog. God bless KodeMamas!\"",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(ThemeIndigo.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Created by Nokwazi Nobuhle Xaba",
                        color = ThemeIndigo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showCertificateDialog) {
        Dialog(onDismissRequest = { showCertificateDialog = false }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .border(2.dp, ThemeGold, RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🏆", fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "KodeMamas Certificate",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = ThemeIndigo,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "South African Mobile Tech Alliance",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "This marks that Student NALEDI has completed digital catalog initialization layout using offline-first HTML and CSS compilers.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showCertificateDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Ngiyabonga! (Close)", color = Color.White)
                    }
                }
            }
        }
    }
}

// ---------------------- TAB 2: LEARN / LESSONS ----------------------
@Composable
fun LearnTab(viewModel: MainViewModel, langCode: String) {
    val lessons by viewModel.allLessons.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Mobile Coding Path",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black
        )
        Text(
            text = "Select an interactive course below to build South African spaza applications and smart prediction crops forecasts.",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(lessons) { lesson ->
                val isUnlocked = lesson.isUnlocked || lesson.id == "html_1" // Force unlock html_1 just in case

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isUnlocked) Color.White else Color.Gray.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            if (isUnlocked) ThemeCardBorder else Color.LightGray.copy(alpha = 0.3f),
                            RoundedCornerShape(24.dp)
                        )
                        .clickable(enabled = isUnlocked) {
                            viewModel.selectLesson(lesson)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Level tag & Category indicator
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isUnlocked) {
                                        when (lesson.category) {
                                            "HTML" -> Color(0xFFFFECE6)
                                            "CSS" -> Color(0xFFE8F0FE)
                                            "JavaScript" -> Color(0xFFFEF7E0)
                                            else -> Color(0xFFE6F4EA)
                                        }
                                    } else Color.LightGray.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (lesson.category) {
                                    "HTML" -> "HTML"
                                    "CSS" -> "CSS"
                                    "JavaScript" -> "JS"
                                    else -> "PY"
                                },
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) {
                                    when (lesson.category) {
                                        "HTML" -> Color(0xFFFF5722)
                                        "CSS" -> Color(0xFF1973E8)
                                        "JavaScript" -> Color(0xFFB06000)
                                        else -> Color(0xFF137333)
                                    }
                                } else Color.Gray,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = lesson.category,
                                    fontSize = 10.sp,
                                    color = ThemeIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(Color.Gray, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${lesson.durationMinutes} Mins",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lesson.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color.Black else Color.Gray
                            )
                            Text(
                                text = lesson.titleLocalized,
                                fontSize = 12.sp,
                                color = if (isUnlocked) ThemeIndigo else Color.LightGray,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Right action items (Save / Lock status)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.toggleSingleLessonDownload(lesson.id) }) {
                                Icon(
                                    imageVector = if (lesson.isDownloaded) Icons.Default.OfflinePin else Icons.Default.Downloading,
                                    contentDescription = "Save Offline",
                                    tint = if (lesson.isDownloaded) Color(0xFF2D7D32) else Color.Gray.copy(alpha = 0.5f)
                                )
                            }

                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.ChevronRight else Icons.Default.Lock,
                                contentDescription = if (isUnlocked) "Open Course" else "Locked",
                                tint = if (isUnlocked) ThemeIndigo else Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- SCREEN: INTERACTIVE ACTIVE LESSON SIMULATOR ----------------------
@Composable
fun ActiveLessonSimulator(viewModel: MainViewModel, langCode: String) {
    val lesson by viewModel.currentActiveLesson.collectAsState()
    val steps by viewModel.currentActiveSteps.collectAsState()
    val stepIndex by viewModel.currentStepIndex.collectAsState()

    val editorText by viewModel.editorText.collectAsState()
    val simulatorOutput by viewModel.simulatorOutput.collectAsState()
    val simulatorSuccess by viewModel.simulatorSuccess.collectAsState()

    // Quiz states
    val quizQuestions by viewModel.activeQuizQuestions.collectAsState()
    val quizIndex by viewModel.quizQuestionIndex.collectAsState()
    val selectedAns by viewModel.selectedAnswerIndex.collectAsState()
    val quizChecked by viewModel.quizChecked.collectAsState()
    val quizCorrect by viewModel.quizCorrect.collectAsState()
    val quizScore by viewModel.quizScore.collectAsState()
    val quizFinished by viewModel.quizFinished.collectAsState()

    val step = steps.getOrNull(stepIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeSoftBg)
    ) {
        // Upper simulator bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ThemeIndigo)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.closeActiveLesson() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Exit lesson", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = lesson?.title ?: "Learning Studio",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Course: ${lesson?.category}",
                        color = ThemeGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Slide step counts
            if (steps.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Step ${stepIndex + 1}/${steps.size}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (quizQuestions.isNotEmpty() && !quizFinished) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeGold.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Quiz ${quizIndex + 1}/${quizQuestions.size}",
                        color = ThemeGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (step != null) {
                // RENDER STEPS CONTEXT
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, ThemeCardBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeIndigo.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CONCEPT",
                                color = ThemeIndigo,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = step.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = step.description,
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            lineHeight = 18.sp
                        )

                        // If South African local language translation is active
                        if (langCode != "en") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFAF9FF))
                                    .border(1.dp, ThemeCardBorder, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Ngolimi Lwakho 🇿🇦:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = ThemeIndigo
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = step.descriptionLocalized,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (step.completionRequirement == "RUN_CODE") {
                    // LIVE MOBILE EDITOR / COMPILER WIDGET
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(ThemeDarkBg)
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "KODEMAMAS MOBILE EDITOR",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = ThemeGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Live editing input text field
                        TextField(
                            value = editorText,
                            onValueChange = { viewModel.updateEditorText(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            textStyle = TextStyle(
                                color = ThemeGold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF130D1E),
                                unfocusedContainerColor = Color(0xFF130D1E),
                                cursorColor = ThemeGold,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Help/Hint button
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ThemeGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Requirement: " + step.answerHint,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.runSimulatorCode() },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = Localization.translate("submit_code", langCode), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // SIMULATOR RESULTS PANEL
                    if (simulatorOutput.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (simulatorSuccess) Color(0xFFE6F4EA) else Color(0xFFFFEBE8))
                                .border(
                                    1.dp,
                                    if (simulatorSuccess) Color(0xFF34A853) else Color(0xFFEA4335),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (simulatorSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (simulatorSuccess) Color(0xFF2B8A3E) else Color(0xFFC92A2A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (simulatorSuccess) "COMPILER SUCCESS!" else "COMPLIANCE ALERT",
                                        fontWeight = FontWeight.Black,
                                        color = if (simulatorSuccess) Color(0xFF2B8A3E) else Color(0xFFC92A2A),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = simulatorOutput,
                                    color = Color.DarkGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // NAVIGATION FLOWS IN SLIDES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (stepIndex > 0) {
                        OutlinedButton(
                            onClick = { viewModel.setStepIndex(stepIndex - 1) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ThemeIndigo)
                        ) {
                            Text(text = "Previous", color = ThemeIndigo, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.completeStep() },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (step.completionRequirement == "RUN_CODE" && !simulatorSuccess) Color.Gray else ThemeIndigo
                        ),
                        enabled = step.completionRequirement == "READ" || simulatorSuccess,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (stepIndex == steps.size - 1) "Launch Assessment ⭐" else "Next Step",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            } else if (quizQuestions.isNotEmpty()) {
                // RENDER PLAYABLE TRANSLATED QUIZ
                if (!quizFinished) {
                    val activeQ = quizQuestions[quizIndex]

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ThemeGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "MULTIPLE CHOICE QUIZ",
                                    color = ThemeIndigo,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = activeQ.question,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            if (langCode != "en") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = activeQ.questionLocalized,
                                    color = ThemeIndigo,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Render dynamic A, B, C, D choices
                    val options = listOf(activeQ.optionA, activeQ.optionB, activeQ.optionC, activeQ.optionD)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        options.forEachIndexed { optIndex, rawText ->
                            val isSelected = selectedAns == optIndex
                            val optionCode = when (optIndex) {
                                0 -> "A"
                                1 -> "B"
                                2 -> "C"
                                else -> "D"
                            }

                            // Calculate border colors if checked
                            val borderCol = if (quizChecked) {
                                if (optIndex == activeQ.correctAnswerIndex) Color(0xFF34A853)
                                else if (isSelected) Color(0xFFEA4335)
                                else ThemeCardBorder
                            } else {
                                if (isSelected) ThemeIndigo else ThemeCardBorder
                            }

                            val bgContainerCol = if (quizChecked) {
                                if (optIndex == activeQ.correctAnswerIndex) Color(0xFFE6F4EA)
                                else if (isSelected) Color(0xFFFFEBE8)
                                else Color.White
                            } else {
                                if (isSelected) ThemeIndigo.copy(alpha = 0.05f) else Color.White
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bgContainerCol)
                                    .border(1.dp, borderCol, RoundedCornerShape(16.dp))
                                    .clickable { viewModel.selectQuizAnswer(optIndex) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) ThemeIndigo else Color.Gray.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionCode,
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = rawText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Bottom validation state
                    if (quizChecked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (quizCorrect) Color(0xFFE6F4EA) else Color(0xFFFFEBE8))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (quizCorrect) "Halala! Correct Answer! 🎉" else "Hawu! Not quite right.",
                                    color = if (quizCorrect) Color(0xFF137333) else Color(0xFFC5221F),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeQ.explanation,
                                    color = Color.DarkGray,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Action buttons
                    if (!quizChecked) {
                        Button(
                            onClick = { viewModel.checkQuizAnswer() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                            shape = RoundedCornerShape(14.dp),
                            enabled = selectedAns != -1
                        ) {
                            Text(text = "Verify Answer", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextQuizStep() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (quizIndex == quizQuestions.size - 1) "Complete Quiz! 🏁" else "Next Question",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                } else {
                    // QUIZ FINISHED CELEBRATION
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(28.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👑", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = Localization.translate("congrats", langCode),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = ThemeIndigo
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You got $quizScore out of ${quizQuestions.size} correct, earning beautiful XP rewards!",
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { viewModel.closeActiveLesson() },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(text = "Back to Path Map", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- TAB 3: AI ASSISTANT / CHATBOT ----------------------
@Composable
fun AiChatTab(viewModel: MainViewModel, langCode: String) {
    val chats by viewModel.aiChats.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isGenerating by viewModel.aiGenerating.collectAsState()

    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chatbot prompt header box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ThemeIndigo)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(ThemeGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤖", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "KodeMamas AI Helper",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isOnline) "Equipped with Gemini 3.5-Flash" else "Offline-Ready Response Assistant Mode",
                        fontSize = 10.sp,
                        color = ThemeGold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.clearAiMessages() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Clear Chats", tint = Color.White)
                }
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            reverseLayout = false
        ) {
            items(chats) { msg ->
                val fromUser = msg.isUser
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = if (fromUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (fromUser) 16.dp else 2.dp,
                                    bottomEnd = if (fromUser) 2.dp else 16.dp
                                )
                            )
                            .background(if (fromUser) ThemeIndigo else Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.messageText,
                            color = if (fromUser) Color.White else Color.Black,
                            fontSize = 13.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            if (isGenerating) {
                item {
                    Text(
                        text = "Mama Assistant is typing in local code blocks...",
                        color = ThemeIndigo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // Typing inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = Localization.translate("ask_something", langCode),
                        fontSize = 12.sp
                    )
                }
            )

            IconButton(
                onClick = {
                    if (textInput.isNotEmpty()) {
                        viewModel.sendAiChat(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ThemeIndigo)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ---------------------- TAB 4: COMMUNITY SECTION ----------------------
@Composable
fun CommunityTab(viewModel: MainViewModel, langCode: String) {
    val posts by viewModel.allPosts.collectAsState()
    var postInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Community Circle 🇿🇦",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black
        )
        Text(
            text = "Connect with mamas, girls, and tech mentors in your area to ask questions or share achievements.",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Write a post dialogue box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, ThemeCardBorder, RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            Column {
                TextField(
                    value = postInput,
                    onValueChange = { postInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    placeholder = { Text(text = "Share your daily coding win with Soweto Hub...", fontSize = 12.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFAF9FF),
                        unfocusedContainerColor = Color(0xFFFAF9FF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (postInput.trim().isNotEmpty()) {
                            viewModel.addForumPost(postInput)
                            postInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = "Post to Forum", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Posts list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(posts) { p ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (p.role == "Mentor") ThemeGold else ThemeIndigo.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (p.role == "Mentor") "⭐" else "👩🏾",
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = p.author,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = when (p.role) {
                                        "Mentor" -> "Matched Tech Instructor"
                                        "Mama" -> "Mama Student"
                                        else -> "Township Tech Student"
                                    },
                                    fontSize = 9.sp,
                                    color = ThemeIndigo,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = p.content,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.clickable { viewModel.likeForumPost(p.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Like",
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${p.likes} Likes", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.InsertComment,
                                    contentDescription = "Comments",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Review replies", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- TAB 5: MENTORSHIP & CAREERS (PREMIUM) ----------------------
@Composable
fun MentorshipTab(viewModel: MainViewModel, langCode: String) {
    val profile by viewModel.userProfile.collectAsState()
    val isTyping by viewModel.mentorTyping.collectAsState()
    val mentorChats by viewModel.mentorChats.collectAsState()

    var chatText by remember { mutableStateOf("") }
    var mentorNavState by remember { mutableStateOf("menu") } // menu, advisor, cv, interview

    // Local states for CV Builder input
    var nameCV by remember { mutableStateOf("Naledi Thoko") }
    var locationCV by remember { mutableStateOf("Soweto, Gauteng") }
    var selectedRoleCV by remember { mutableStateOf("Spaza Manager & Digitization Specialist") }
    var skillsCV by remember { mutableStateOf("HTML storefront alignment, CSS palettes tuning, Local Android compilers handling") }

    // local state for Interview Prep
    var activeFeedbackText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (mentorNavState == "menu") {
            // MAIN CAREER SERVICES MENU
            Text(
                text = "Premium Mentorship & Careers 🌟",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            Text(
                text = "Unlock professional internship matching, CV compilers tools, and premium counselor connections.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Current Premium Status header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ThemeDarkBg)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(ThemeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👑", fontSize = 17.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Mamas Premium Career Pack",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (profile?.isPremium == true) "PRO ACTIVE • MATCHED" else "Free trial available to all township graduates!",
                                color = ThemeGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (profile?.isPremium != true) {
                        Button(
                            onClick = { viewModel.claimPremiumUpgrade() },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Unlock Free Career Guidance", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.cancelPremium() },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Leave Premium Group", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-services rows
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Feature 1: 1-on-1 advisor
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .clickable { mentorNavState = "advisor" }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ThemeIndigo.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.ChatBubble, contentDescription = null, tint = ThemeIndigo)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "1-on-1 counselor chat", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                Text(text = "Mathes with Mentor Akhona inside Soweto campus", fontSize = 11.sp, color = Color.Gray)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = ThemeIndigo)
                        }
                    }
                }

                // Feature 2: CV Builder
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .clickable { mentorNavState = "cv" }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ThemeIndigo.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = ThemeIndigo)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Localized CV / Resume Creator", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                Text(text = "Configure professional developer resume file layouts", fontSize = 11.sp, color = Color.Gray)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = ThemeIndigo)
                        }
                    }
                }

                // Feature 3: Interview prep
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .clickable { mentorNavState = "interview" }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ThemeIndigo.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null, tint = ThemeIndigo)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Mock Interview Simulators", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                Text(text = "Test core developer interview templates with counselor feedback", fontSize = 11.sp, color = Color.Gray)
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = ThemeIndigo)
                        }
                    }
                }
            }

        } else if (mentorNavState == "advisor") {
            // ADVISOR CHAT
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { mentorNavState = "menu" }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = ThemeIndigo)
                }
                Text(text = "Counselor Akhona 👩🏾‍💼", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mentorChats) { chat ->
                    val isUs = chat.isUser
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUs) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isUs) ThemeIndigo else Color.White)
                                .border(1.dp, ThemeCardBorder, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = chat.messageText,
                                color = if (isUs) Color.White else Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (isTyping) {
                    item {
                        Text(text = "Akhona is replying to your message...", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = chatText,
                    onValueChange = { chatText = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = { Text(text = "Ask Akhona about internships in Johannesburg...", fontSize = 12.sp) }
                )

                IconButton(
                    onClick = {
                        if (chatText.isNotEmpty()) {
                            viewModel.sendMentorChat(chatText)
                            chatText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(ThemeIndigo)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                }
            }

        } else if (mentorNavState == "cv") {
            // CV CREATOR
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { mentorNavState = "menu" }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = ThemeIndigo)
                }
                Text(text = "Tuned Resumes Builder", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
            }

            // Simple responsive input fields scroll
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = nameCV,
                    onValueChange = { nameCV = it },
                    label = { Text("Your Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = locationCV,
                    onValueChange = { locationCV = it },
                    label = { Text("Location (Township/City)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = selectedRoleCV,
                    onValueChange = { selectedRoleCV = it },
                    label = { Text("Target Role") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = skillsCV,
                    onValueChange = { skillsCV = it },
                    label = { Text("Mamas Tech Skills (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CV PREVIEW BOARD IN ARTISTIC FLAIR STYLE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(2.dp, ThemeIndigo, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "RESUME OUTPUT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = ThemeIndigo)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE6F4EA))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "READY FOR LOCAL ATTACHMENT", color = Color(0xFF137333), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = nameCV.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = locationCV, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(ThemeCardBorder)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "CAREER STATEMENT:", fontWeight = FontWeight.Black, fontSize = 11.sp, color = ThemeIndigo)
                        Text(
                            text = "Matriculated South African student seeking entry into junior roles, offering high levels of diligence and structured offline technical training as a certified $selectedRoleCV from KodeMamas academy.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "VERIFIED IN-APP SKILLS:", fontWeight = FontWeight.Black, fontSize = 11.sp, color = ThemeIndigo)
                        Text(
                            text = skillsCV,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

        } else if (mentorNavState == "interview") {
            // INTERVIEW ASSESSMENT PREP
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { mentorNavState = "menu" }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = ThemeIndigo)
                }
                Text(text = "Township Interview Trainer", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, ThemeCardBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "QUESTION FLASHCARD:", fontSize = 9.sp, fontWeight = FontWeight.Black, color = ThemeIndigo)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A client asks us to make their online baking catalogue background match the color of localized African pumpkins using external stylesheet. What styling parameter handles this?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val answers = listOf(
                    "background: red;",
                    "background-color: darkorange;",
                    "color: gold;",
                    "opacity: 1;"
                )
                answers.forEach { ans ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                activeFeedbackText = if (ans.contains("darkorange")) {
                                    "Excellent response! 🎉 Pumpkin color aligns perfectly with #FF8C00 (DarkOrange). This demonstrates you have fully mastered CSS color styling rules."
                                } else {
                                    "Hawu! Not quite right. Try option 'darkorange' which renders warm African pumpkin colors."
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Text(text = ans, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                    }
                }
            }

            if (activeFeedbackText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(ThemeIndigo.copy(alpha = 0.08f))
                        .padding(14.dp)
                ) {
                    Text(text = activeFeedbackText, color = ThemeIndigo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------- COMPONENT: DIALOG DYNAMIC SELECTOR ----------------------
@Composable
fun LanguagePickerDialog(
    currentLangCode: String,
    onDismiss: () -> Unit,
    onLangSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(2.dp, ThemeIndigo, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Khetha ulimi lwakho 🇿🇦",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = ThemeIndigo
                )
                Text(
                    text = "Select your home language for lesson guides & customized localized subtitles.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(modifier = Modifier.height(300.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Localization.languages) { lang ->
                            val isSelected = lang.code == currentLangCode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ThemeIndigo.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { onLangSelected(lang.code) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lang.localName,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ThemeIndigo else Color.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = lang.displayName,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
