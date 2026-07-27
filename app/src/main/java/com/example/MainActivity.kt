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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.*
import androidx.compose.foundation.shape.CircleShape
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.theme.Localization
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(true) }
    var showOfflineAccountDialog by remember { mutableStateOf(false) }
    var showTourStep by remember { mutableStateOf<Int?>(null) }

    val downloadProgress by viewModel.downloadProgress.collectAsState()

    if (showOnboarding && currentLesson == null) {
        OnboardingScreen(
            onGetStarted = { 
                showOnboarding = false 
                // Automatically prompt guided tour for first-time inquisitive/nervous users
                showTourStep = 0
            },
            onExploreCourses = {
                viewModel.selectTab("learn")
                showOnboarding = false
                showTourStep = 0
            },
            langCode = langCode,
            isOnline = isOnline,
            onToggleNetwork = { viewModel.toggleNetworkMode() },
            viewModel = viewModel
        )
    } else {
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
                        onToggleNetwork = { viewModel.toggleNetworkMode() },
                        onEditProfile = { showOfflineAccountDialog = true },
                        onLanguageSelected = { code -> viewModel.changeLanguage(code) },
                        onStartTour = { showTourStep = 0 },
                        onSettingsClick = { showSettingsDialog = true }
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
                    // Standard tabs based content structure with optional Offline Banner
                    Column(modifier = Modifier.fillMaxSize()) {
                        OfflineStatusBanner(
                            isOnline = isOnline,
                            onToggleNetwork = { viewModel.toggleNetworkMode() },
                            langCode = langCode
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                                },
                                label = "tabChange"
                            ) { tab ->
                                when (tab) {
                                    "home" -> DashboardTab(viewModel = viewModel, langCode = langCode, onShowOnboarding = { showOnboarding = true })
                                    "learn" -> LearnTab(viewModel = viewModel, langCode = langCode)
                                    "ai_chat" -> AiChatTab(viewModel = viewModel, langCode = langCode)
                                    "community" -> CommunityTab(viewModel = viewModel, langCode = langCode)
                                    "mentorship" -> MentorshipTab(viewModel = viewModel, langCode = langCode)
                                }
                            }
                        }
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

    if (showSettingsDialog) {
        SettingsDialog(
            currentLangCode = langCode,
            userProfile = userProfile,
            isOnline = isOnline,
            onDismiss = { showSettingsDialog = false },
            onLangSelected = { code ->
                viewModel.changeLanguage(code)
            },
            onToggleDataSaver = { enabled ->
                viewModel.toggleDataSavingMode(enabled)
            },
            onToggleNetwork = {
                viewModel.toggleNetworkMode()
            }
        )
    }

    if (showOfflineAccountDialog) {
        OfflineAccountDialog(
            userProfile = userProfile,
            onDismiss = { showOfflineAccountDialog = false },
            onSubmit = { name, role, language, xp ->
                viewModel.updateOfflineUserProfile(name, role, language, xp)
                showOfflineAccountDialog = false
                android.widget.Toast.makeText(
                    viewModel.getApplication(),
                    "🎉 Offline Account is active on this device!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // 100-Day Onboarding Journey Win Celebration Dialogue
    val activeOnboardingWin by viewModel.activeOnboardingWin.collectAsState()
    activeOnboardingWin?.let { winPhase ->
        OnboardingWinDialog(
            phase = winPhase,
            onDismiss = { viewModel.dismissOnboardingWin() }
        )
    }

    // Download progress overlay dialog
    downloadProgress?.let { progress ->
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            androidx.compose.material3.Card(
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, ThemeCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = ThemeIndigo, modifier = Modifier.size(40.dp))
                    Text(
                        text = "Downloading Course Materials... ${(progress * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = ThemeIndigo,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "Saving to offline cache. Please wait. Sula kancane, silungisa izifundo...",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    // Interactive Guided Product Tour Dialog
    showTourStep?.let { step ->
        GuidedTourDialog(
            step = step,
            onNext = {
                if (step < 3) {
                    showTourStep = step + 1
                } else {
                    showTourStep = null
                    android.widget.Toast.makeText(viewModel.getApplication(), "🎉 Tour completed! You're ready to build, sister!", android.widget.Toast.LENGTH_LONG).show()
                }
            },
            onSkip = {
                showTourStep = null
                android.widget.Toast.makeText(viewModel.getApplication(), "Tour skipped. Enjoy learning!", android.widget.Toast.LENGTH_SHORT).show()
            },
            langCode = langCode
        )
    }
}

// Interactive Guided Product Tour Dialog Composable
@Composable
fun GuidedTourDialog(
    step: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    langCode: String
) {
    val title = when (step) {
        0 -> "Welcome to KodeMamas! ✨"
        1 -> "Interactive Lessons 📚"
        2 -> "Empathetic AI Tutor 🤖"
        3 -> "Community & Mentors 👥"
        else -> "Product Tour"
    }
    
    val text = when (step) {
        0 -> "Sawubona! Let's build your future. Your Dashboard features a 100-Day Onboarding Tracker to keep you motivated and celebrate your daily streaks."
        1 -> "Select the Learn tab to access 10 interactive lessons customized with South African Spaza examples. Download them to study completely offline!"
        2 -> "Need step-by-step assistance? Tap the AI Chat tab to communicate with our friendly tutor, fluent in all 12 official South African languages!"
        3 -> "Use the Community tab to ask questions on the forums, chat with professional mentors, and share code templates with study buddies!"
        else -> ""
    }
    
    val nextLabel = if (step == 3) "Finish Tour" else "Next Step ➜"

    androidx.compose.ui.window.Dialog(onDismissRequest = onSkip) {
        androidx.compose.material3.Card(
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF1B0B30)),
            border = BorderStroke(1.5.dp, ThemeGold),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = ThemeGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${step + 1} / 4",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = text,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onSkip,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip Tour", fontSize = 11.sp)
                    }
                    
                    androidx.compose.material3.Button(
                        onClick = onNext,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ThemeGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(nextLabel, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// ---------------------- COMPONENT: HEADER ----------------------
@Composable
fun AppHeader(
    userProfile: UserProfile?,
    langCode: String,
    isOnline: Boolean,
    onLangClick: () -> Unit,
    onToggleNetwork: () -> Unit,
    onEditProfile: () -> Unit = {},
    onLanguageSelected: (String) -> Unit = {},
    onStartTour: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                // Language selector pill restored for premium spacing
                Button(
                    onClick = onLangClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("lang_selector_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val flag = when (langCode) {
                            "en" -> "🇿🇦"
                            "zu" -> "🇿🇦"
                            "xh" -> "🇿🇦"
                            "af" -> "🇿🇦"
                            "nso" -> "🇿🇦"
                            "tn" -> "🇿🇦"
                            "st" -> "🇿🇦"
                            "ts" -> "🇿🇦"
                            "ss" -> "🇿🇦"
                            "ve" -> "🇿🇦"
                            "nr" -> "🇿🇦"
                            "sasl" -> "🤟"
                            else -> "🇿🇦"
                        }
                        Text(
                            text = "$flag " + (Localization.languages.find { it.code == langCode }?.localName ?: "English"),
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

                // Help/Tour icon button for Inquisitive/Nervous User
                androidx.compose.material3.IconButton(
                    onClick = onStartTour,
                    modifier = Modifier.size(34.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Help,
                        contentDescription = "Take Guided Tour",
                        tint = ThemeGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings icon button for language and other configs
                androidx.compose.material3.IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(34.dp).testTag("settings_button")
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                        contentDescription = "Settings Menu",
                        tint = ThemeGold,
                        modifier = Modifier.size(20.dp)
                    )
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
                    .clickable { onEditProfile() }
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sawubona, ${profile.name}! 👋",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = ThemeGold,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "${profile.role} • Bloemfontein Hub",
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

        // Persistent selector removed for premium spacing, keeping layout neat
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
                        modifier = Modifier
                            .size(22.dp)
                            .padding(bottom = 4.dp) // Pushes icon away from label slightly to prevent overlaps
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ThemeIndigo else Color.Gray,
                        modifier = Modifier.padding(top = 4.dp) // Extra vertical separation
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ThemeIndigo.copy(alpha = 0.08f)
                )
            )
        }
    }
}

// ---------------------- DATA MODEL: CAREER PATHWAY ITEM ----------------------
data class PathwayItem(
    val title: String,
    val salary: String,
    val cities: String,
    val roadmap: List<String>,
    val capstone: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

// ---------------------- TAB 1: DASHBOARD / HOME ----------------------
@Composable
fun DashboardTab(viewModel: MainViewModel, langCode: String, onShowOnboarding: () -> Unit = {}) {
    val lessons by viewModel.allLessons.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val challenges by viewModel.allChallenges.collectAsState()
    val activeChallenge by viewModel.activeChallenge.collectAsState()

    var showCertificateDialog by remember { mutableStateOf(false) }
    var showLanguagePickerFromHub by remember { mutableStateOf(false) }

    // Navigation sub-state for the dashboard category switcher
    var activeSubSection by remember { mutableStateOf("dashboard") } // dashboard, pathways, parent, school, analytics, viral

    // --- Interactive state for Pathways ---
    var selectedPathIndex by remember { mutableStateOf(0) }
    var pathwaysSearchQuery by remember { mutableStateOf("") }
    val pathwayQuizAnswers = remember { androidx.compose.runtime.mutableStateMapOf<Int, Int?>() }
    val pathwayChecklistState = remember { androidx.compose.runtime.mutableStateMapOf<String, Boolean>() }

    // --- Interactive state for Parent Portal ---
    var parentName by remember { mutableStateOf("Mama Nobuhle") }
    var isSafeSearchEnabled by remember { mutableStateOf(true) }
    var isCommunityCirclesOnly by remember { mutableStateOf(true) }
    var studyGoalTime by remember { mutableStateOf("1.5 Hours / Day") }
    var showGoalSettings by remember { mutableStateOf(false) }

    // --- Interactive state for School Hub ---
    var inviteStudentName by remember { mutableStateOf("") }
    var inviteStudentPhone by remember { mutableStateOf("") }
    val simulatedClassStudents = remember {
        mutableStateListOf(
            Triple("Lindiwe Cele", "240 XP", "Lesson 2"),
            Triple("Sipho Ndlovu", "180 XP", "Lesson 1"),
            Triple("Nompumelelo Dlamini", "120 XP", "Lesson 1"),
            Triple("Thabo Mokoena", "60 XP", "Lesson 1")
        )
    }

    // --- Interactive state for Viral Hub ---
    var simulatedInvitesCount by remember { mutableStateOf(2) }
    var showSharingSimulationResult by remember { mutableStateOf(false) }
    var selectedSharingPlatform by remember { mutableStateOf("") }
    val generatedReferralCode = "MAMA-7842-NALEDI"

    val context = androidx.compose.ui.platform.LocalContext.current

    val allPathways = remember {
        listOf(
            PathwayItem(
                title = "Web Developer",
                salary = "R12,000 - R18,000 / month",
                cities = "Johannesburg, Cape Town, Bloemfontein",
                roadmap = listOf("Write semantic HTML structure", "Style with responsive CSS sheets", "Incorporate local offline-first elements", "Deploy to static web hosting channels"),
                capstone = "Bloemfontein Bakery digital storefront catalog",
                quizQuestion = "Which HTML element denotes the primary navigational section?",
                quizOptions = listOf("<div id=\"nav\">", "<header>", "<nav>", "<aside>"),
                correctAnswerIndex = 2,
                explanation = "Phenomenal! Under WCAG accessibility standards, the <nav> block explicitly communicates navigation capabilities to screen readers and compilers."
            ),
            PathwayItem(
                title = "Mobile App Developer",
                salary = "R15,000 - R22,000 / month",
                cities = "Durban, Johannesburg, Midrand",
                roadmap = listOf("Master Kotlin variables and functions", "Build responsive Jetpack Compose rows", "Define Room SQLite tables and queries", "Execute remote Retrofit network pipelines"),
                capstone = "Township Taxi Hub Tracker & routing catalog",
                quizQuestion = "Which state holder keeps state safe from Compose screen recomposition?",
                quizOptions = listOf("remember { mutableStateOf() }", "val a = mutableStateOf()", "var state: String = \"\"", "LiveData"),
                correctAnswerIndex = 0,
                explanation = "Perfect! Wrapping state in remember guarantees it stays cached in memory over recompositions."
            ),
            PathwayItem(
                title = "Software Engineer",
                salary = "R18,000 - R26,000 / month",
                cities = "Pretoria, Centurion, Johannesburg",
                roadmap = listOf("Understand algorithms on space/time scales", "Track offline version checkpoints via Git", "Write rigorous unit tests using JUnit", "Configure server CI/CD build actions"),
                capstone = "Spaza Shop order billing compiler logic",
                quizQuestion = "Which program translates high-level code into executable machine binaries?",
                quizOptions = listOf("Interpreter", "Compiler", "Debugger", "Linker"),
                correctAnswerIndex = 1,
                explanation = "Yes! Compilers perform semantic checks and generate optimized executable instructions."
            ),
            PathwayItem(
                title = "Data Analyst",
                salary = "R14,000 - R20,000 / month",
                cities = "Gqeberha, Rosebank, Johannesburg",
                roadmap = listOf("Query databases via SQL selection joins", "Clean statistics using MS Excel spreadsheets", "Wrangle metrics in Python Pandas frames", "Render diagnostic visual dashboards"),
                capstone = "Soweto Spaza Crop crop forecast visualizer",
                quizQuestion = "Which SQL clause joins related tables together based on a common key?",
                quizOptions = listOf("MERGE", "WHERE", "JOIN", "UNION"),
                correctAnswerIndex = 2,
                explanation = "Correct! JOIN binds columns from separate database tables matching matching row values."
            ),
            PathwayItem(
                title = "AI Engineer",
                salary = "R22,000 - R32,000 / month",
                cities = "Sandton, Stellenbosch, Pretoria",
                roadmap = listOf("Familiarize with math weights & layers", "Train visual classifiers in PyTorch libraries", "Leverage Gemini API securely via REST", "Engineer aligned structured prompts"),
                capstone = "Multilingual South African AI Study Tutor Chatbot",
                quizQuestion = "Which Gemini model on Google AI Studio minimizes text processing costs?",
                quizOptions = listOf("Gemini 1.5 Pro", "Gemini 1.5 Flash", "Gemini 1.0 Ultra", "Gemma 7B"),
                correctAnswerIndex = 1,
                explanation = "Correct! Gemini 1.5 Flash is highly structured for massive high-speed text queries."
            ),
            PathwayItem(
                title = "Cybersecurity Analyst",
                salary = "R16,000 - R24,000 / month",
                cities = "Cape Town, Rosebank, Stellenbosch",
                roadmap = listOf("Familiarize with Linux directory parameters", "Hash login profiles secure with crypt keys", "Scan security breaches in socket loops", "Enforce JSON Web Token auth blocks"),
                capstone = "Spaza billing gateway credential encryption hub",
                quizQuestion = "Which cryptographic standard represents the current secure standard for passwords?",
                quizOptions = listOf("MD5", "BCrypt", "Plain text", "ROT13"),
                correctAnswerIndex = 1,
                explanation = "Correct! BCrypt uses secure salted hashing to prevent reverse tables matching breaches."
            ),
            PathwayItem(
                title = "UX/UI Designer",
                salary = "R11,000 - R17,000 / month",
                cities = "Randburg, Johannesburg, Cape Town",
                roadmap = listOf("Sketch basic UI layout grid scopes", "Establish clear typography spacing parameters", "Incorporate Google Material Design 3 guidelines", "Link responsive flow triggers inside Figma prototypes"),
                capstone = "KodeMamas mobile system UI wireframe overhaul",
                quizQuestion = "What is the recommended accessible touch target padding dimension?",
                quizOptions = listOf("24dp x 24dp", "32dp x 32dp", "40dp x 40dp", "48dp x 48dp"),
                correctAnswerIndex = 3,
                explanation = "Absolutely! Material 3 targets a minimum tap target of 48dp to ensure usability."
            ),
            PathwayItem(
                title = "Cloud Engineer",
                salary = "R19,000 - R28,000 / month",
                cities = "Midrand, Centurion, Cape Town",
                roadmap = listOf("Allocate secure cloud bucket clusters", "Launch server configurations in Cloud Run container ports", "Manage portable images via Docker", "Setup secure connection routes"),
                capstone = "Offline-first sync database backup utility",
                quizQuestion = "Which virtualization tool isolates applications in lightweight, separate runtimes?",
                quizOptions = listOf("VirtualBox", "Docker", "Xen", "Kubernetes"),
                correctAnswerIndex = 1,
                explanation = "Correct! Docker packs application software and configurations into lightweight containers."
            ),
            PathwayItem(
                title = "DevOps Engineer",
                salary = "R22,000 - R31,000 / month",
                cities = "Rosebank, Durban, Centurion",
                roadmap = listOf("Configure test workflows triggered by Git commits", "Verify build compilers in automated cycles", "Draft robust Bash shell automation scripts", "Observe system stability dashboards"),
                capstone = "KodeMamas automated compile stability checker",
                quizQuestion = "Which file type defines workflows in GitHub Actions?",
                quizOptions = listOf("JSON", "YAML (.yml)", "XML", "Properties"),
                correctAnswerIndex = 1,
                explanation = "Perfect! GitHub system parsers read and compile action rules specified in YAML configs."
            ),
            PathwayItem(
                title = "Digital Entrepreneur",
                salary = "R10,000 - R50,000 / month",
                cities = "Nationwide South Africa (Township focus)",
                roadmap = listOf("Verify business model canvas details", "Register new SME with local CIPC offices", "Connect instant EFT or Capitec Pay channels", "Publish local social media promotions"),
                capstone = "Bloemfontein neighborhood digital spaza index storefront",
                quizQuestion = "Which rapid local checkout standard bypasses plastic credit cards in townships?",
                quizOptions = listOf("Cheque lines", "Wire code", "Capitec Pay", "Manual bank dispatch"),
                correctAnswerIndex = 2,
                explanation = "Spot on! Capitec Pay has accelerated township checkouts by routing payments directly to the user's mobile app."
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prominent Language Toggle Hub
        item {
            SouthAfricanLanguageHub(
                currentLangCode = langCode,
                onLangSelected = { viewModel.changeLanguage(it) },
                onShowAllLanguages = { showLanguagePickerFromHub = true }
            )
        }

        // Horizontal Category Tabs Hub
        item {
            val categories = listOf(
                Triple("dashboard", "👩‍🎓 " + Localization.translate("sub_dashboard", langCode), "Main studies"),
                Triple("pathways", "🗺️ " + Localization.translate("sub_pathways", langCode), "Roadmaps & roles"),
                Triple("parent", "👥 " + Localization.translate("sub_parent", langCode), "Progress & safety"),
                Triple("school", "🏫 " + Localization.translate("sub_schools", langCode), "Classrooms & NGOs"),
                Triple("analytics", "📊 " + Localization.translate("sub_analytics", langCode), "Platform records"),
                Triple("viral", "📣 " + Localization.translate("sub_viral", langCode), "Invites & sharing")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (id, label, desc) ->
                    val isSelected = activeSubSection == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) ThemeIndigo else Color.White)
                            .border(1.dp, if (isSelected) ThemeIndigo else ThemeCardBorder, RoundedCornerShape(16.dp))
                            .clickable { activeSubSection = id }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else ThemeIndigo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (activeSubSection == "dashboard") {
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
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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

                            Button(
                                onClick = onShowOnboarding,
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Welcome Screen",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Welcome Screen",
                                    color = Color.Black,
                                    fontSize = 11.sp,
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

            // Expert Quick Continue Study shortcut item
            item {
                val nextLessonToStudy = lessons.find { it.isUnlocked } ?: lessons.firstOrNull()
                nextLessonToStudy?.let { lesson ->
                    androidx.compose.material3.Card(
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                        border = BorderStroke(1.dp, Color(0xFFD8B4FE)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectLesson(lesson) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Quick Study",
                                tint = ThemeIndigo,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "EXPERT SHORTCUT: NEXT LESSON",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ThemeIndigo
                                )
                                Text(
                                    text = "Continue Studying: ${lesson.title}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // 100-Day Journey Progress Tracking Section
            item {
                OnboardingJourneyWidget(viewModel = viewModel, langCode = langCode)
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
                                text = if (userProfile?.hasDownloadedOffline == true || downloadedCount == lessons.size) "All ${lessons.size} Saved" else "$downloadedCount/${lessons.size} Saved",
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
                            Text(
                                text = "Claim Certificate",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Bloemfontein Hub Certified",
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
        } else if (activeSubSection == "pathways") {
            // Interactive 10 Career Pathways Grid and assessments
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "🗺️ Complete Professional Tech Pathways (10 Routes)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = ThemeIndigo
                    )
                    Text(
                        text = "Master production-grade skills aligned to South African tech hubs with visual checklists and diagnostic roadmap quizzes.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )

                    // Search input
                    OutlinedTextField(
                        value = pathwaysSearchQuery,
                        onValueChange = { 
                            pathwaysSearchQuery = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search professional pathways...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = ThemeIndigo, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    // ⚡ Do All Pathways (Auto-Complete Action Button)
                    val coroutineScope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            // 1. Check all checklist items for all 10 pathways
                            repeat(10) { pIdx ->
                                repeat(5) { cIdx ->
                                    pathwayChecklistState["${pIdx}_${cIdx}"] = true
                                }
                                // 2. Select correct quiz answer for all 10 pathways
                                pathwayQuizAnswers[pIdx] = allPathways[pIdx].correctAnswerIndex
                            }
                            
                            // 3. Update database XP (+1500 XP) and Streak (+10)
                            coroutineScope.launch {
                                try {
                                    val db = com.example.data.AppDatabase.getDatabase(context)
                                    db.userDao().updateXpAndStreak(xpGained = 1500, newStreak = 10)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            // 4. Toast and Trigger Certificate Dialogue
                            android.widget.Toast.makeText(
                                context,
                                "🎉 Halala! You successfully completed all 10 Professional Career Pathways! +1500 XP Gained!",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            showCertificateDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemeGold,
                            contentColor = ThemeDarkBg
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = ThemeDarkBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ Do All Pathways (Auto-Complete)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    val filteredPathways = allPathways.filter {
                        it.title.lowercase().contains(pathwaysSearchQuery.lowercase())
                    }

                    if (filteredPathways.isEmpty()) {
                        Text("No matching pathways found. Check spelling!", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }

                    filteredPathways.forEach { path ->
                        val originalPathIndex = allPathways.indexOf(path)
                        val isExpanded = selectedPathIndex == originalPathIndex

                        // Progress Calculation
                        val checkedItemsCount = (0..4).count { cIdx -> pathwayChecklistState["${originalPathIndex}_${cIdx}"] ?: false }
                        val isQuizCorrect = pathwayQuizAnswers[originalPathIndex] == path.correctAnswerIndex
                        val progressPct = ((checkedItemsCount + (if (isQuizCorrect) 1 else 0)) * 100) / 6

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(
                                    width = if (isExpanded) 2.dp else 1.dp,
                                    color = if (isExpanded) ThemeIndigo else ThemeCardBorder,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    selectedPathIndex = originalPathIndex
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(if (progressPct == 100) Color(0xFFE6F4EA) else ThemeIndigo.copy(alpha = 0.08f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = if (progressPct == 100) "🏆" else "🛣️", fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = path.title,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = ThemeIndigo
                                            )
                                            Text(
                                                text = "Completed: $progressPct%",
                                                fontSize = 11.sp,
                                                color = if (progressPct == 100) Color(0xFF137333) else Color.Gray,
                                                fontWeight = if (progressPct == 100) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (progressPct == 100) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE6F4EA))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "MASTERED",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF137333)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expand info",
                                            tint = ThemeIndigo,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "💼 SOUTH AFRICAN REALITY & SALARY",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = ThemeGold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Est. Starting: ${path.salary}\nPriority Townships/Hubs: ${path.cities}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "🗺️ INTERACTIVE LEARNING ROADMAP",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = ThemeIndigo
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    path.roadmap.forEachIndexed { rIdx, step ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE2E8F0)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "✓", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = step, fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "🏆 CAPSTONE PORTFOLIO PROJECT",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = ThemeIndigo
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = path.capstone,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "⚡ PATHWAY DIAGNOSTIC ASSESSMENT",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = ThemeIndigo
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = path.quizQuestion, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val selectedQuizAnswer = pathwayQuizAnswers[originalPathIndex]
                                    val quizAnswerFeedback = if (selectedQuizAnswer != null) {
                                        if (selectedQuizAnswer == path.correctAnswerIndex) {
                                            path.explanation
                                        } else {
                                            "Incorrect choice! Reflect on standard local coding processes and select again."
                                        }
                                    } else ""

                                    // Interactive Quiz Choices Custom Buttons
                                    path.quizOptions.forEachIndexed { oIdx, option ->
                                        val isSelectedChoice = selectedQuizAnswer == oIdx
                                        val quizButtonBgColor = if (isSelectedChoice) {
                                            if (oIdx == path.correctAnswerIndex) Color(0xFFE6F4EA) else Color(0xFFFCE8E6)
                                        } else Color.White
                                        val quizBorderColor = if (isSelectedChoice) {
                                            if (oIdx == path.correctAnswerIndex) Color(0xFF137333) else Color(0xFFC5221F)
                                        } else Color.LightGray

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(quizButtonBgColor)
                                                .border(1.dp, quizBorderColor, RoundedCornerShape(10.dp))
                                                .clickable {
                                                    pathwayQuizAnswers[originalPathIndex] = oIdx
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(text = option, fontSize = 11.sp, color = Color.Black)
                                        }
                                    }

                                    if (selectedQuizAnswer != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ThemeIndigo.copy(alpha = 0.05f))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = quizAnswerFeedback,
                                                fontSize = 11.sp,
                                                color = ThemeIndigo,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "📋 ROADMAP PREPARATION CHECKLIST",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = ThemeIndigo
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val localChecklistItems = listOf(
                                        "Simulated South African CV prepared on mentorship module",
                                        "3 local Github repositories hosting standard markup files",
                                        "Simulated counselor 1-on-1 scheduled (Premium Career)",
                                        "Tested bakery order and crop forecast compilers offline",
                                        "Assigned technical mock reviews on Cape Town technical forum"
                                    )
                                    localChecklistItems.forEachIndexed { cIdx, elementLabel ->
                                        val isChecked = pathwayChecklistState["${originalPathIndex}_${cIdx}"] ?: false
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { 
                                                    pathwayChecklistState["${originalPathIndex}_${cIdx}"] = !isChecked 
                                                }
                                                .padding(vertical = 4.dp)
                                        ) {
                                            // Custom designed Checkbox
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isChecked) ThemeIndigo else Color.Transparent)
                                                    .border(1.5.dp, if (isChecked) ThemeIndigo else Color.LightGray, RoundedCornerShape(4.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = elementLabel,
                                                fontSize = 11.sp,
                                                color = if (isChecked) ThemeIndigo else Color.DarkGray,
                                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeSubSection == "parent") {
            // Parent & Guardian dashboard portal
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(ThemeIndigo)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "Molo, ${parentName}! 👥",
                                color = ThemeGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reviewing Naledi's progress, digital safety filters, and township learning resources.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Student Progress Quick Report Card
                    Text(
                        text = "📈 Child Learning Report & Speed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Nokwazi Naledi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Grade 10 • Bloemfontein Tech Hub", fontSize = 11.sp, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ThemeIndigo.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("LEVEL 3", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ThemeIndigo)
                                }
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("STREAK", fontSize = 9.sp, color = Color.Gray)
                                    Text("${userProfile?.streak ?: 5} Days Active", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                }
                                Column {
                                    Text("XP POINTS", fontSize = 9.sp, color = Color.Gray)
                                    Text("${userProfile?.xp ?: 180} XP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                }
                                Column {
                                    Text("COMPLETED", fontSize = 9.sp, color = Color.Gray)
                                    val completedCount = maxOf(0, lessons.count { it.isUnlocked } - 1)
                                    Text("$completedCount / ${lessons.size} Lessons", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                }
                            }
                        }
                    }

                    // Interactive Goals Setting
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Set Student Study Goal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Current allocation: $studyGoalTime", fontSize = 11.sp, color = Color.Gray)
                                }
                                Button(
                                    onClick = { showGoalSettings = !showGoalSettings },
                                    colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Change", fontSize = 10.sp)
                                }
                            }

                            if (showGoalSettings) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val goalOptions = listOf("30m / Day", "1 Hour / Day", "1.5 Hours / Day", "2 Hours / Day")
                                    goalOptions.forEach { opt ->
                                        val isSel = studyGoalTime == opt
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) ThemeIndigo else Color.LightGray.copy(alpha = 0.2f))
                                                .clickable {
                                                    studyGoalTime = opt
                                                    showGoalSettings = false
                                                    android.widget.Toast.makeText(context, "Naledi's study goal updated to $opt!", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = opt, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Digital Literacy & Offline support
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF9F5FF))
                            .border(1.dp, ThemeIndigo.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📖 Guardian Digital Literacy & Offline Guide", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ThemeIndigo)
                            Text(
                                text = "• What is Coding? Coding is writing instructions for computers. It fosters high-paying jobs in South Africa without requiring traditional high cost hardware.",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                lineHeight = 15.sp
                            )
                            Text(
                                text = "• Supporting Offline Study: Download lessons locally inside Bloemfontein Hub, and study at home without cellular billing or signal interruptions.",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                lineHeight = 15.sp
                            )
                            Text(
                                text = "• Safety & Security: Our online forums use filtered moderation models to prevent cyberbullying or unsafe tech contact.",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Safety Toggles
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔒 Guard & Safety Configurations", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Safe Search Moderation Filter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Blocks unapproved community topics", fontSize = 10.sp, color = Color.Gray)
                                }
                                Switch(
                                    checked = isSafeSearchEnabled,
                                    onCheckedChange = { isSafeSearchEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ThemeIndigo)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Verified Community Circles Only", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Verifies peers using regional student IDs", fontSize = 10.sp, color = Color.Gray)
                                }
                                Switch(
                                    checked = isCommunityCirclesOnly,
                                    onCheckedChange = { isCommunityCirclesOnly = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ThemeIndigo)
                                )
                            }
                        }
                    }

                    // Help line contacts
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF7E0))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("🚨 National Emergency Support Services", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB06000))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• South African Child Safety Line: 0800 055 555\n• Township Digital Trust Helpline: Telephonic routing 112", fontSize = 10.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        } else if (activeSubSection == "school") {
            // Schools & Community / Teacher dashboard
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF3E8FF))
                            .border(1.dp, ThemeIndigo.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "🏫 School & Classroom Hub",
                                color = ThemeIndigo,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Teacher Account: logged in as Mrs. Dlamini (Bloemfontein High School). Tracking Grade 10-A ICT curriculum performance metrics.",
                                color = Color.DarkGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Class Status KPI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .border(1.dp, ThemeCardBorder, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("STUDENTS", fontSize = 8.sp, color = Color.Gray)
                                Text("${simulatedClassStudents.size} Registered", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .border(1.dp, ThemeCardBorder, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("COMPLETIONS", fontSize = 8.sp, color = Color.Gray)
                                Text("89% Average", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .border(1.dp, ThemeCardBorder, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("AVERAGE STREAK", fontSize = 8.sp, color = Color.Gray)
                                Text("4.6 Days", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Teacher Actions: Manual Invite
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📝 Add / Invite Student to Classroom Group", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            val isPhoneFormatValid = inviteStudentPhone.trim().length == 10 && inviteStudentPhone.all { it.isDigit() }
                            val isNameFormatValid = inviteStudentName.trim().isNotEmpty() && inviteStudentName.all { it.isLetter() || it.isWhitespace() }
                            val isInviteFormValid = isPhoneFormatValid && isNameFormatValid

                            OutlinedTextField(
                                value = inviteStudentName,
                                onValueChange = { inviteStudentName = it.take(50) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Student Full Name (e.g. Sipho)") },
                                singleLine = true,
                                isError = inviteStudentName.isNotEmpty() && !isNameFormatValid,
                                supportingText = {
                                    if (inviteStudentName.isNotEmpty() && !isNameFormatValid) {
                                        Text("Please enter letters only.", color = Color.Red, fontSize = 10.sp)
                                    }
                                }
                            )

                            OutlinedTextField(
                                value = inviteStudentPhone,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) {
                                        inviteStudentPhone = input.take(12)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Cell Number (e.g. 0723456789)") },
                                singleLine = true,
                                isError = inviteStudentPhone.isNotEmpty() && !isPhoneFormatValid,
                                supportingText = {
                                    if (inviteStudentPhone.isNotEmpty() && !isPhoneFormatValid) {
                                        Text("Enter a valid 10-digit South African number.", color = Color.Red, fontSize = 10.sp)
                                    }
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (isInviteFormValid) {
                                            simulatedClassStudents.add(Triple(inviteStudentName, "0 XP", "Rookie"))
                                            android.widget.Toast.makeText(context, "Invite link sent to $inviteStudentPhone! Student added to classroom roster.", android.widget.Toast.LENGTH_LONG).show()
                                            inviteStudentName = ""
                                            inviteStudentPhone = ""
                                        }
                                    },
                                    enabled = isInviteFormValid,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ThemeIndigo,
                                        disabledContainerColor = Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Invite Student", fontSize = 11.sp, color = if (isInviteFormValid) Color.White else Color.DarkGray)
                                }

                                OutlinedButton(
                                    onClick = {
                                        // Bulk Import simulation
                                        simulatedClassStudents.add(Triple("Gontse Morake", "310 XP", "Lesson 3"))
                                        simulatedClassStudents.add(Triple("Lefa Modise", "150 XP", "Lesson 1"))
                                        simulatedClassStudents.add(Triple("Amogelang Seola", "80 XP", "Lesson 1"))
                                        android.widget.Toast.makeText(context, "Parsed bulk-students-list.csv! Imported 3 students instantly.", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.dp, Color.LightGray),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Import CSV List", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Classroom Student List Table
                    Text(
                        text = "📋 Classroom Student Directory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Student Name", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                Row {
                                    Text("Score XP", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                                    Text("Step", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(70.dp))
                                }
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.4f))

                            simulatedClassStudents.forEach { (name, score, step) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                    Row {
                                        Text(score, fontSize = 12.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                                        Text(step, fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.width(70.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Active Coding Clubs & NGO Partners Group
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🤝 Community Fellowships & NGO Partners", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("• Bloemfontein High School Tech Club • Supported by SA Mobile Alliance", fontSize = 11.sp, color = Color.DarkGray)
                            Text("• Gugulethu Coding Queens Hub • Supported by Soweto Dev NGO", fontSize = 11.sp, color = Color.DarkGray)
                            Text("• Mitchells Plain Coding Sisters • Supported by Youth Uplifting Network", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        } else if (activeSubSection == "analytics") {
            // Live platform performance metrics
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "📊 Live Hub Performance Metrics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = ThemeIndigo
                    )

                    // Large Score Panels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, ThemeCardBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("DAILY USERS (DAU)", fontSize = 8.sp, color = Color.Gray)
                                Text("1,520", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ThemeIndigo)
                                Text("+12% from yesterday", fontSize = 9.sp, color = Color(0xFF137333))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, ThemeCardBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("MONTHLY ACTIVE (MAU)", fontSize = 8.sp, color = Color.Gray)
                                Text("35,200", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ThemeIndigo)
                                Text("3.4k first-generation", fontSize = 9.sp, color = Color(0xFF137333))
                            }
                        }
                    }

                    // Gorgeous custom graphics monitor drawn with canvas
                    Text(
                        text = "📈 Monthly Platform Active Student Growth Curve",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Active Students Syncing Offline (Thousands)", fontSize = 9.sp, color = Color.Gray)
                                Text("Dec '25 - June '26", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Custom Graphic Canvas
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height

                                // Draw grids
                                for (i in 0..3) {
                                    val yOffset = canvasHeight * i / 3f
                                    drawLine(
                                        color = Color.LightGray.copy(alpha = 0.35f),
                                        start = androidx.compose.ui.geometry.Offset(0f, yOffset),
                                        end = androidx.compose.ui.geometry.Offset(canvasWidth, yOffset),
                                        strokeWidth = 1f
                                    )
                                }

                                // Monthly values data representations
                                val monthlyValues = listOf(0.12f, 0.28f, 0.35f, 0.54f, 0.72f, 0.95f)
                                val coordinatesList = monthlyValues.mapIndexed { index, value ->
                                    val x = canvasWidth * index / (monthlyValues.size - 1)
                                    val y = canvasHeight * (1f - value)
                                    androidx.compose.ui.geometry.Offset(x, y)
                                }

                                // Connect line points
                                for (i in 0 until coordinatesList.size - 1) {
                                    drawLine(
                                        color = ThemeIndigo,
                                        start = coordinatesList[i],
                                        end = coordinatesList[i + 1],
                                        strokeWidth = 4f
                                    )
                                }

                                // Draw anchor dots Represent gold accents
                                coordinatesList.forEach { point ->
                                    drawCircle(
                                        color = ThemeGold,
                                        radius = 6f,
                                        center = point
                                    )
                                    drawCircle(
                                        color = ThemeIndigo,
                                        radius = 3.5f,
                                        center = point
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "June")
                                monthLabels.forEach { m ->
                                    Text(text = m, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Subscriptions Breakdown chart view
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📊 Conversion & Career Interest Share", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            // Pathway bar 1: Web Dev interest share group
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Web & Frontend Development", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("44% share", fontSize = 10.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray.copy(alpha = 0.3f))) {
                                    Box(modifier = Modifier.fillMaxWidth(0.44f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(ThemeIndigo))
                                }
                            }

                            // Pathway bar 2: Mobile App Dev share group
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Mobile Dev & Kotlin compilers", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("32% share", fontSize = 10.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray.copy(alpha = 0.3f))) {
                                    Box(modifier = Modifier.fillMaxWidth(0.32f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(ThemeIndigo))
                                }
                            }

                            // Pathway bar 3: Digital Entrepreneurs
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Digital Entrepreneurship & SMEs", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("24% share", fontSize = 10.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray.copy(alpha = 0.3f))) {
                                    Box(modifier = Modifier.fillMaxWidth(0.24f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(ThemeIndigo))
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeSubSection == "viral") {
            // Ambassador & Referral space
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.horizontalGradient(listOf(ThemeIndigo, ThemeIndigo.copy(alpha = 0.85f))))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(ThemeGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📣", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ambassador Referral Hub & Codes",
                                    color = ThemeGold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Earn specialized badges, physical rewards, and free Premium access by inviting township peers to code offline alongside your group.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Share Refer Link Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔗 Your Unique Referral Account parameters", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Share this referral code or direct SMS link below to track metrics instantly", fontSize = 11.sp, color = Color.Gray)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF9F5FF))
                                    .border(1.dp, ThemeIndigo.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = generatedReferralCode,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = ThemeIndigo
                                    )
                                    Button(
                                        onClick = {
                                            android.widget.Toast.makeText(context, "Referral Code Copied to clipboard! Share on WhatsApp Lite.", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Copy Key", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Ambassador progression tracker simulator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏅 Ambassador Rank Progress", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$simulatedInvitesCount / 8 Referrals", fontSize = 11.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            // Custom progress metric bar
                            val progressionPct = simulatedInvitesCount / 8f
                            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color.LightGray.copy(alpha = 0.3f))) {
                                Box(modifier = Modifier.fillMaxWidth(progressionPct).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(ThemeIndigo))
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("• Rank: " + (if (simulatedInvitesCount < 3) "Township Rookie Coder" else if (simulatedInvitesCount < 6) "Community Tech Beacon" else "Gauteng/Bloem Tech Ambassador Champion 👑"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("• Next Reward: " + (if (simulatedInvitesCount < 3) "Invite 1 more friend to unlock Free static hosting web guide!" else "Get 3 more references to claim a physical KodeMamas Cape Town Hub cap!"), fontSize = 10.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(10.dp))

                            // SUCCESS INVITE SIMULATOR
                            Button(
                                onClick = {
                                    if (simulatedInvitesCount < 8) {
                                        simulatedInvitesCount++
                                        android.widget.Toast.makeText(context, "Ref code MAMA-7842 used! Sipho Khumalo successfully registered. You earned +50 XP!", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Maximum level reached! You are already a Township Hub President!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text("Simulate WhatsApp Invite Success 📣 (+50 XP)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    // Visual Social proof card layout preview for whatsapp
                    Text(
                        text = "📱 WhatsApp / Facebook Lite Achievement Card Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(ThemeDarkBg)
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("KODEMAMAS CHAMPION CARD", fontSize = 8.sp, fontWeight = FontWeight.Black, color = ThemeGold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(ThemeIndigo),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👩‍🎓", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Naledi Nobuhle Xaba", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
                            Text("Tech Apprentice • Stream Level 3", fontSize = 11.sp, color = ThemeGold)
                            Spacer(modifier = Modifier.height(10.dp))
                            val completedCount = maxOf(0, lessons.count { it.isUnlocked } - 1)
                            Text(
                                "Finished $completedCount/${lessons.size} Local Coding compilers modules. Studying offline-first configurations at Bloemfontein Hub!",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showSharingSimulationResult = true
                                        selectedSharingPlatform = "WhatsApp Lite"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("WhatsApp", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        showSharingSimulationResult = true
                                        selectedSharingPlatform = "Facebook Lite"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Facebook", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (showSharingSimulationResult) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE6F4EA))
                                .border(1.dp, Color(0xFF137333), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Achievement Card successfully posted to $selectedSharingPlatform!", fontSize = 11.sp, color = Color(0xFF137333), fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { showSharingSimulationResult = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF137333)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Done", fontSize = 9.sp)
                                }
                            }
                        }
                    }
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
                    val completedPathwaysCount = (0..9).count { pIdx ->
                        val hasAllChecked = (0..4).all { cIdx -> pathwayChecklistState["${pIdx}_${cIdx}"] == true }
                        val isQuizDone = pathwayQuizAnswers[pIdx] == allPathways[pIdx].correctAnswerIndex
                        hasAllChecked && isQuizDone
                    }
                    val certificateText = if (completedPathwaysCount == 10) {
                        "This marks that Student Naledi has successfully mastered all 10 Professional Tech Career Pathways on KodeMamas with complete assessment and project portfolio checks!"
                    } else {
                        "This marks that Student NALEDI has completed digital catalog initialization layout using offline-first HTML and CSS compilers."
                    }

                    Text(
                        text = certificateText,
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

    if (showLanguagePickerFromHub) {
        LanguagePickerDialog(
            currentLangCode = langCode,
            onDismiss = { showLanguagePickerFromHub = false },
            onLangSelected = { code ->
                viewModel.changeLanguage(code)
                showLanguagePickerFromHub = false
            }
        )
    }
}

// ---------------------- COMPONENT: SOUTH AFRICAN LANGUAGE HUB ----------------------
@Composable
fun SouthAfricanLanguageHub(
    currentLangCode: String,
    onLangSelected: (String) -> Unit,
    onShowAllLanguages: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sa_language_hub_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ThemeCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ThemeIndigo.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🇿🇦", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = Localization.translate("in_your_language", currentLangCode),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ThemeIndigo
                        )
                        Text(
                            text = "Choose your preferred language",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Localization.languages.forEach { lang ->
                    val isSelected = currentLangCode == lang.code
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ThemeIndigo else Color.White)
                            .border(1.dp, if (isSelected) ThemeIndigo else ThemeCardBorder, RoundedCornerShape(12.dp))
                            .clickable { onLangSelected(lang.code) }
                            .testTag("lang_toggle_${lang.code}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(
                                text = lang.localName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = lang.displayName,
                                fontSize = 8.sp,
                                color = if (isSelected) ThemeGold else Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // More button
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, ThemeCardBorder, RoundedCornerShape(12.dp))
                        .clickable { onShowAllLanguages() }
                        .testTag("lang_toggle_more"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "List Picker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ThemeIndigo,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Show All 12 🇿🇦",
                            fontSize = 8.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                                text = Localization.translate(lesson.id + "_title", langCode),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) Color.Black else Color.Gray
                            )
                            Text(
                                text = Localization.translate(lesson.id + "_desc", langCode),
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
                        val localizedStepTitle = Localization.translate(step.id + "_title", langCode)
                        Text(
                            text = if (localizedStepTitle == step.id + "_title") step.title else localizedStepTitle,
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
                                        text = Localization.translate("in_your_language", langCode),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = ThemeIndigo
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val localizedStepDesc = Localization.translate(step.id + "_desc", langCode)
                                    Text(
                                        text = if (localizedStepDesc == step.id + "_desc") step.descriptionLocalized else localizedStepDesc,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 17.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val localizedStepHint = Localization.translate(step.id + "_hint", langCode)
                                    if (localizedStepHint != step.id + "_hint") {
                                        Text(
                                            text = "💡 $localizedStepHint",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ThemeGold,
                                            lineHeight = 15.sp
                                        )
                                    }
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
                        text = if (isOnline) "Equipped with Gemini Smart Assistant" else "Offline-Ready Response Assistant Mode",
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
    val buddies by viewModel.allBuddies.collectAsState()
    val selectedBuddy by viewModel.selectedBuddy.collectAsState()
    val activeMessages by viewModel.activeBuddyMessages.collectAsState()

    var postInput by remember { mutableStateOf("") }
    var activeSubTab by remember { mutableStateOf("forum") } // forum, buddies

    // Buddy filter states
    var filterInterest by remember { mutableStateOf("All") }
    var filterLanguage by remember { mutableStateOf("All") }
    var filterProgress by remember { mutableStateOf("All") }

    var showShareResourceDropdown by remember { mutableStateOf(false) }

    val filteredBuddies = buddies.filter { buddy ->
        val matchInterest = filterInterest == "All" || buddy.interests.contains(filterInterest)
        val matchLang = filterLanguage == "All" || buddy.languageCode.equals(filterLanguage, ignoreCase = true)
        val matchProgress = filterProgress == "All" || buddy.currentLesson.contains(filterProgress, ignoreCase = true)
        matchInterest && matchLang && matchProgress
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tab header
        Text(
            text = "Community Hub 🇿🇦",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black
        )
        Text(
            text = "Connect, share coding templates, and get peer support from other South African mothers & students.",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Modern segment tab controller
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, ThemeCardBorder, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeSubTab == "forum") ThemeIndigo else Color.Transparent)
                    .clickable { activeSubTab = "forum" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📢 " + Localization.translate("sub_analytics", langCode).replace("Live Analytics", "Township Forum").replace("Analytics", "Forum"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (activeSubTab == "forum") Color.White else Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeSubTab == "buddies") ThemeIndigo else Color.Transparent)
                    .clickable { activeSubTab = "buddies" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤝 " + "Mama buddies",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (activeSubTab == "buddies") Color.White else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeSubTab == "forum") {
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
                    val isPostLengthValid = postInput.length <= 500
                    val isPostEmpty = postInput.trim().isEmpty()
                    val hasBlockedWords = listOf("http", "www", "free money", "bitcoin", "casino").any { postInput.lowercase().contains(it) }
                    val isPostValid = isPostLengthValid && !isPostEmpty && !hasBlockedWords

                    TextField(
                        value = postInput,
                        onValueChange = { postInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = { Text(text = "Share your daily coding win with Bloemfontein Hub...", fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFAF9FF),
                            unfocusedContainerColor = Color(0xFFFAF9FF),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Inline error warnings
                        if (hasBlockedWords) {
                            Text("Please avoid links or spam content.", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        } else if (postInput.length > 500) {
                            Text("Content exceeds 500 character limit!", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        // Character counter
                        Text(
                            text = "${postInput.length} / 500",
                            color = if (isPostLengthValid) Color.Gray else Color.Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (isPostValid) {
                                viewModel.addForumPost(postInput)
                                postInput = ""
                            }
                        },
                        enabled = isPostValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemeIndigo,
                            disabledContainerColor = Color.LightGray
                        ),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Post to Forum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isPostValid) Color.White else Color.DarkGray)
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
        } else {
            // Buddy System Section
            Column(modifier = Modifier.fillMaxWidth()) {
                // Filter Panel Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = ThemeIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Filter Peer Network",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        // Interests Filter Row
                        Column {
                            Text(text = "Interests:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All", "Spaza Shops", "Agriculture", "Tech Jobs", "Motherhood", "NGOs").forEach { opt ->
                                    val isSel = filterInterest == opt
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) ThemeIndigo else Color(0xFFF3F2FF))
                                            .clickable { filterInterest = opt }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = opt, fontSize = 10.sp, color = if (isSel) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Languages Filter Row
                        Column {
                            Text(text = "Native Language:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (listOf(Triple("All", "All", "🇿🇦")) + Localization.languages.map { lang ->
                                    Triple(lang.code, lang.displayName, if (lang.code == "sasl") "🤟" else "🇿🇦")
                                }).forEach { (code, label, flag) ->
                                    val isSel = filterLanguage == code
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) ThemeIndigo else Color(0xFFF3F2FF))
                                            .clickable { filterLanguage = code }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "$flag $label", fontSize = 10.sp, color = if (isSel) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Progress Filter Row
                        Column {
                            Text(text = "Learning Topic:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All", "HTML", "CSS", "JS", "Python").forEach { opt ->
                                    val isSel = filterProgress == opt
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) ThemeIndigo else Color(0xFFF3F2FF))
                                            .clickable { filterProgress = opt }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = opt, fontSize = 10.sp, color = if (isSel) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Buddies list
                if (filteredBuddies.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "🔍", fontSize = 32.sp)
                            Text(text = "No matching companions in your area.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(text = "Try adjusting your filters to broaden search.", fontSize = 10.sp, color = Color.LightGray)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredBuddies) { buddy ->
                            val isConnected = buddy.isConnected
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .border(1.dp, if (isConnected) ThemeIndigo else ThemeCardBorder, RoundedCornerShape(20.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(ThemeIndigo.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (buddy.avatarRes == "mama_avatar") "👩🏾" else "👧🏾",
                                                fontSize = 20.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = buddy.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.Black
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(ThemeGold.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(text = "${buddy.xp} XP", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFFE5A900))
                                                }
                                            }
                                            Text(
                                                text = when (buddy.role) {
                                                    "Mama" -> "Mama Student"
                                                    "Student" -> "Township Tech Student"
                                                    else -> "Tech Advisor"
                                                } + " • " + (Localization.languages.find { it.code.equals(buddy.languageCode, ignoreCase = true) }?.displayName ?: "English") + " 🇿🇦",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        if (isConnected) {
                                            // Connected Badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE8F5E9))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(text = "Buddy Active 🤝", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                            }
                                        }
                                    }

                                    Divider(color = Color(0xFFECEBFF), thickness = 1.dp)

                                    // Display interests
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Interests: ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.horizontalScroll(rememberScrollState())
                                        ) {
                                            buddy.interests.split(",").forEach { interest ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFFAF9FF))
                                                        .border(1.dp, Color(0xFFECEBFF), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(text = interest.trim(), fontSize = 8.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    // Display Progress
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = "Lesson progress",
                                            tint = ThemeIndigo,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Working on: ",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = buddy.currentLesson,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Action buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (!isConnected) {
                                            Button(
                                                onClick = {
                                                    viewModel.connectWithBuddy(buddy.id, true)
                                                    android.widget.Toast.makeText(
                                                        viewModel.getApplication(),
                                                        "🤝 Connected with ${buddy.name}! You can now share learning templates.",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Text(text = "Connect 🤝", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    viewModel.selectBuddyForChat(buddy)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                                modifier = Modifier.weight(1.5f),
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Text(text = "Chat & Share Templates 💬", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.connectWithBuddy(buddy.id, false)
                                                    android.widget.Toast.makeText(
                                                        viewModel.getApplication(),
                                                        "Removed buddy relationship.",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                modifier = Modifier.weight(0.7f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                                                contentPadding = PaddingValues(vertical = 8.dp)
                                            ) {
                                                Text(text = "Disconnect", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Peer Chat System Dialog Overlay
    selectedBuddy?.let { buddy ->
        var chatInputText by remember { mutableStateOf("") }
        var showShareDialog by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { viewModel.selectBuddyForChat(null) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, ThemeCardBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(ThemeIndigo.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (buddy.avatarRes == "mama_avatar") "👩🏾" else "👧🏾", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = buddy.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Offline Study Buddy • ${buddy.currentLesson}", fontSize = 9.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.selectBuddyForChat(null) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close chat", tint = Color.Gray)
                        }
                    }

                    Divider(color = Color(0xFFECEBFF), modifier = Modifier.padding(vertical = 8.dp))

                    // Tips banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ThemeSoftBg)
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Peer tip: Code template sharing doesn't consume mobile data!", fontSize = 9.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Messages List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeMessages) { msg ->
                            val isMe = msg.senderId == "me"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isMe) 16.dp else 4.dp,
                                                bottomEnd = if (isMe) 4.dp else 16.dp
                                            )
                                        )
                                        .background(if (isMe) ThemeIndigo else Color(0xFFF3F2FF))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = msg.messageText,
                                            color = if (isMe) Color.White else Color.Black,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )

                                        // Render shared resource panel if present!
                                        if (msg.sharedResourceTitle.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isMe) Color(0xFF0C0714) else Color.White)
                                                    .border(1.dp, if (isMe) Color(0xFF2C1945) else Color(0xFFECEBFF), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Code,
                                                            contentDescription = "Code snippet icon",
                                                            tint = ThemeGold,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = msg.sharedResourceTitle,
                                                            fontSize = 9.sp,
                                                            color = if (isMe) Color.White else Color.Black,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Text(
                                                        text = msg.sharedResourceCode,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 8.sp,
                                                        color = if (isMe) Color(0xFF86E3CE) else Color(0xFF1B5E20),
                                                        lineHeight = 10.sp,
                                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = if (isMe) "Sent offline" else "Connected buddy",
                                            fontSize = 7.sp,
                                            color = (if (isMe) Color.White else Color.Gray).copy(alpha = 0.6f),
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input bar and actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { showShareDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFE5E0FA))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Attachment,
                                contentDescription = "Share Template",
                                tint = ThemeIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        TextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text(text = "Type message...", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(19.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFAF9FF),
                                unfocusedContainerColor = Color(0xFFFAF9FF),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontSize = 11.sp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (chatInputText.trim().isNotEmpty()) {
                                    viewModel.sendBuddyMessage(buddy.id, chatInputText)
                                    chatInputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(ThemeIndigo)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Share resource popover chooser
        if (showShareDialog) {
            val templates = listOf(
                Triple("Mam's Spaza storefront (HTML)", "<h1>Mam's Spaza Shop</h1>\n<p>Fresh Daily Bread: R18.50</p>", "html_1"),
                Triple("African Warmth palette (CSS)", "body {\n  background-color: #121212;\n  color: #FFD700;\n}", "css_2"),
                Triple("Cart price calculator (JS)", "function calculateTotal(bread, milk) {\n  return (bread * 18.5) + (milk * 16);\n}", "js_3"),
                Triple("Smart irrigation alert (Python)", "temp = 32\nif temp > 30:\n  print('Warning: Double watering')\n", "python_4")
            )

            Dialog(onDismissRequest = { showShareDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, ThemeCardBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Share Coding Template 📂",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Select a local compiler workspace template to share with your study buddy.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        templates.forEach { (title, code, id) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFAF9FF))
                                    .border(1.dp, Color(0xFFECEBFF), RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.sendBuddyMessage(
                                            buddyId = buddy.id,
                                            text = "Check out my compilation workspace code for '${title}'!",
                                            sharedResourceTitle = title,
                                            sharedResourceCode = code
                                        )
                                        showShareDialog = false
                                    }
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Code, contentDescription = "Template selection icon", tint = ThemeIndigo, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = code.replace("\n", " "), maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = { showShareDialog = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(text = "Close", color = ThemeIndigo, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
    var nameCV by remember { mutableStateOf("Nokwazi Nobuhle Xaba") }
    var locationCV by remember { mutableStateOf("Bloemfontein, Free State") }
    var selectedRoleCV by remember { mutableStateOf("Spaza Manager & Digitization Specialist") }
    var skillsCV by remember { mutableStateOf("HTML storefront alignment, CSS palettes tuning, Local Android compilers handling") }

    // local state for Interview Prep
    var activeFeedbackText by remember { mutableStateOf("") }
    
    // South African simulated payment dialog state
    var showPaymentDialog by remember { mutableStateOf(false) }
    var initialChoiceIsPremium by remember { mutableStateOf(true) }
    var showUpgradeToPremiumAlert by remember { mutableStateOf(false) }

    if (showPaymentDialog) {
        SouthAfricanPaymentDialog(
            initialIsPremium = initialChoiceIsPremium,
            langCode = langCode,
            onDismiss = { showPaymentDialog = false },
            onPaymentSuccess = { isPremiumOption ->
                if (isPremiumOption) {
                    viewModel.claimPremiumUpgrade()
                } else {
                    viewModel.claimPlusUpgrade()
                }
                showPaymentDialog = false
            }
        )
    }

    if (showUpgradeToPremiumAlert) {
        Dialog(onDismissRequest = { showUpgradeToPremiumAlert = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(2.dp, ThemeIndigo, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Premium Career Feature 👑",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = ThemeIndigo
                    )
                    
                    Text(
                        text = "This premium high-touch module (1-on-1 advisor chats, professional South African CV builders, and counselor mock reviews) is reserved for the R299/month Premium Career Pack.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "Your current status: " + (if (profile?.isPlus == true) "Plus Track (R99) ⚡" else "Free trial 🆓"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeGold
                    )

                    Button(
                        onClick = {
                            showUpgradeToPremiumAlert = false
                            initialChoiceIsPremium = true
                            showPaymentDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Upgrade to Premium • R299", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { showUpgradeToPremiumAlert = false },
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Cancel", color = Color.Gray)
                    }
                }
            }
        }
    }

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
                                text = if (profile?.isPremium == true) "KodeMamas Premium Active 🌟" else if (profile?.isPlus == true) "KodeMamas Plus Active ⚡" else "Join Premium or Plus Track",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (profile?.isPremium == true) "UNLIMITED AI + 1-ON-1 COUNSELING" else if (profile?.isPlus == true) "UNLIMITED AI + ADVANCED COURSES" else "Free trial available to all township graduates!",
                                color = ThemeGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                              )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (profile?.isPremium != true && profile?.isPlus != true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    initialChoiceIsPremium = false
                                    showPaymentDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Plus • R99/m", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    initialChoiceIsPremium = true
                                    showPaymentDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Premium • R299/m", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    } else if (profile?.isPlus == true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    initialChoiceIsPremium = true
                                    showPaymentDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text(text = "Upgrade to Premium • R299", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.cancelPlus() },
                                modifier = Modifier.weight(0.7f),
                                border = BorderStroke(1.dp, Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "Cancel Plus", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.claimPlusUpgrade() },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text(text = "Downgrade to Plus • R99", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.cancelAllSubscriptions() },
                                modifier = Modifier.weight(0.8f),
                                border = BorderStroke(1.dp, Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "Cancel Premium", color = Color.White, fontSize = 11.sp)
                            }
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
                            .clickable {
                                if (profile?.isPremium == true) {
                                    mentorNavState = "advisor"
                                } else {
                                    initialChoiceIsPremium = true
                                    showUpgradeToPremiumAlert = true
                                }
                            }
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
                                Text(text = "1-on-1 advisor chat", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                Text(text = "Matches with Founder Nokwazi inside Bloemfontein campus", fontSize = 11.sp, color = Color.Gray)
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
                            .clickable {
                                if (profile?.isPremium == true) {
                                    mentorNavState = "cv"
                                } else {
                                    initialChoiceIsPremium = true
                                    showUpgradeToPremiumAlert = true
                                }
                            }
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
                            .clickable {
                                if (profile?.isPremium == true) {
                                    mentorNavState = "interview"
                                } else {
                                    initialChoiceIsPremium = true
                                    showUpgradeToPremiumAlert = true
                                }
                            }
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
                Text(text = "Founder Nokwazi 👩🏾‍💼", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
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
                        Text(text = "Nokwazi is replying to your message...", color = Color.Gray, fontSize = 11.sp)
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
                    placeholder = { Text(text = "Ask Nokwazi about internships in Bloemfontein...", fontSize = 12.sp) }
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

// ---------------------- COMPONENT: SETTINGS DIALOG (SETTINGS MENU) ----------------------
@Composable
fun SettingsDialog(
    currentLangCode: String,
    userProfile: UserProfile?,
    isOnline: Boolean,
    onDismiss: () -> Unit,
    onLangSelected: (String) -> Unit,
    onToggleDataSaver: (Boolean) -> Unit,
    onToggleNetwork: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(2.dp, ThemeIndigo, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = ThemeIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Settings Menu",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = ThemeIndigo
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3E8FF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "v1.2",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeIndigo
                        )
                    }
                }

                Text(
                    text = "Configure app language toggle (all 12 South African languages) and device data optimization.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // Language Toggle Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "APP LANGUAGE TOGGLE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeIndigo.copy(alpha = 0.8f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().testTag("settings_language_list"),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(Localization.languages) { lang ->
                                val isSelected = lang.code == currentLangCode
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ThemeIndigo.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable { onLangSelected(lang.code) }
                                        .testTag("settings_lang_option_${lang.code}")
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val flag = when (lang.code) {
                                            "sasl" -> "🤟"
                                            else -> "🇿🇦"
                                        }
                                        Text(text = flag, fontSize = 16.sp)
                                        Text(
                                            text = lang.localName,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ThemeIndigo else Color.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Text(
                                        text = lang.displayName,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Preferences & Network optimization section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "PREFERENCES & NETWORK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeIndigo.copy(alpha = 0.8f)
                    )

                    // Data Saver switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.05f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Data-Saving Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "Limit background data & compress visual assets",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = userProfile?.dataSavingMode == true,
                            onCheckedChange = { onToggleDataSaver(it) },
                            modifier = Modifier.testTag("settings_data_saver_switch")
                        )
                    }

                    // Network toggle switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.05f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Online Connectivity",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                            Text(
                                text = if (isOnline) "Connected to standard data" else "Zero-data offline lessons active",
                                fontSize = 10.sp,
                                color = if (isOnline) Color(0xFF10B981) else Color.Gray
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = isOnline,
                            onCheckedChange = { onToggleNetwork() },
                            modifier = Modifier.testTag("settings_network_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("settings_dismiss_button")
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
                    }
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
                    LazyColumn(
                        modifier = Modifier.testTag("language_list"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(Localization.languages) { lang ->
                            val isSelected = lang.code == currentLangCode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ThemeIndigo.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { onLangSelected(lang.code) }
                                    .testTag("lang_option_${lang.code}")
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
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("lang_dialog_cancel_button")
                    ) {
                        Text(text = "Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------------- COMPONENT: OFFLINE ACCOUNT DIALOG ----------------------
@Composable
fun OfflineAccountDialog(
    userProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSubmit: (name: String, role: String, languageCode: String, xp: Int) -> Unit
) {
    var nameInput by remember { mutableStateOf(userProfile?.name ?: "") }
    var selectedRole by remember { mutableStateOf(userProfile?.role ?: "Mama") }
    var selectedLangCode by remember { mutableStateOf(userProfile?.languageCode ?: "en") }
    var xpLevel by remember { mutableStateOf("Novice (0 XP)") }

    val isNameValid = nameInput.trim().length in 1..30 && nameInput.all { it.isLetterOrDigit() || it.isWhitespace() || it == '-' || it == '\'' }
    val nameErrorMessage = when {
        nameInput.trim().isEmpty() -> "Name cannot be empty"
        nameInput.length > 30 -> "Name must be 30 characters or less"
        !nameInput.all { it.isLetterOrDigit() || it.isWhitespace() || it == '-' || it == '\'' } -> "Special characters not allowed"
        else -> null
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(2.dp, ThemeIndigo, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = Localization.translate("easy_offline_title", selectedLangCode),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = ThemeIndigo
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F0FE))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OFFLINE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = ThemeIndigo
                        )
                    }
                }

                Text(
                    text = Localization.translate("easy_offline_desc", selectedLangCode),
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Name Input
                Column {
                    Text(
                        text = "YOUR NAME / HUB NICKNAME",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeIndigo.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text("e.g. Sister Naledi", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = nameErrorMessage != null && nameInput.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThemeIndigo,
                            unfocusedBorderColor = Color.LightGray,
                            focusedLabelColor = ThemeIndigo,
                            errorBorderColor = Color.Red
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (nameErrorMessage != null && nameInput.isNotEmpty()) {
                        Text(
                            text = nameErrorMessage,
                            color = Color.Red,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Role Selection
                Column {
                    Text(
                        text = "YOUR COMMUNITY ROLE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeIndigo.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Mama", "Student", "Girl").forEach { role ->
                            val isChosen = selectedRole == role
                            val icon = when (role) {
                                "Mama" -> "👩‍👧"
                                "Student" -> "👩‍🎓"
                                else -> "👧"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isChosen) ThemeIndigo else Color.White)
                                    .border(
                                        1.dp,
                                        if (isChosen) ThemeIndigo else Color.LightGray,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedRole = role }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = icon, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = role,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChosen) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                // Native Language Selection
                Column {
                    Text(
                        text = "LOCAL HOME LANGUAGE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeIndigo.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Localization.languages.forEach { lang ->
                            val isChosen = selectedLangCode == lang.code
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isChosen) ThemeIndigo.copy(alpha = 0.12f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isChosen) ThemeIndigo else Color.LightGray,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedLangCode = lang.code }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = lang.localName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) ThemeIndigo else Color.Black
                                )
                            }
                        }
                    }
                }

                // Starting XP level
                Column {
                    Text(
                        text = "YOUR STARTING XP EXPERIENCE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeIndigo.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Novice (0 XP)", "Rookie (100 XP)", "Expert (180 XP)").forEach { tier ->
                            val isChosen = xpLevel == tier
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isChosen) ThemeIndigo else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isChosen) ThemeIndigo else Color.LightGray,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { xpLevel = tier }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tier.substringBefore(" ("),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = Localization.translate("cancel_btn", selectedLangCode),
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank() && isNameValid) {
                                val targetXp = when (xpLevel) {
                                    "Rookie (100 XP)" -> 100
                                    "Expert (180 XP)" -> 180
                                    else -> 0
                                }
                                onSubmit(nameInput, selectedRole, selectedLangCode, targetXp)
                            }
                        },
                        enabled = nameInput.isNotBlank() && isNameValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ThemeIndigo,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(2.0f)
                    ) {
                        Text(
                            text = Localization.translate("save_btn", selectedLangCode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ---------------------- COMPONENT: LANDING / ONBOARDING SCREEN ----------------------
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onExploreCourses: () -> Unit = {},
    langCode: String,
    isOnline: Boolean,
    onToggleNetwork: () -> Unit,
    viewModel: MainViewModel
) {
    var showLanguagePickerInSplash by remember { mutableStateOf(false) }
    var selectedPremiumBenefit by remember { mutableStateOf<String?>(null) }
    var premiumBenefitDescription by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F041C),
                        Color(0xFF07030F),
                        Color(0xFF000000)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Quick Lang & Connection top header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF10B981) else Color(0xFFFFB800))
                    )
                    Text(
                        text = if (isOnline) "ONLINE" else "OFFLINE READY",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { showLanguagePickerInSplash = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🇿🇦", fontSize = 12.sp)
                        Text(
                            text = langCode.uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change Language",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Hero Brand Branding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "</> ",
                        color = Color(0xFFFFD700),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "Kode",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "Mamas",
                        color = Color(0xFF8B5CF6),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                    )
                }

                Text(
                    text = "Code in Your Language. Learn Anywhere. Build Your Future.",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF6D28D9).copy(alpha = 0.2f))
                        .border(BorderStroke(1.dp, Color(0xFF6D28D9).copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "EMPOWERING GIRLS. MOTHERS. STUDENTS. COMMUNITIES. FUTURES. ♡",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Adaptive Multi-Pane Columns Block
            val configuration = LocalConfiguration.current
            val isWideScreen = configuration.screenWidthDp > 600

            if (isWideScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        LeftPanelSection(
                            onGetStarted = onGetStarted,
                            onExploreCourses = onExploreCourses,
                            langCode = langCode
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        RightPanelSection(
                            onShowBenefit = { name, desc ->
                                selectedPremiumBenefit = name
                                premiumBenefitDescription = desc
                            }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    LeftPanelSection(
                        onGetStarted = onGetStarted,
                        onExploreCourses = onExploreCourses,
                        langCode = langCode
                    )
                    
                    // Core illustration banner in center
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AfricanQueenIllustration()
                    }

                    RightPanelSection(
                        onShowBenefit = { name, desc ->
                            selectedPremiumBenefit = name
                            premiumBenefitDescription = desc
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Founder Story Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF120822)),
                border = BorderStroke(1.dp, Color(0xFF6D28D9).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "FOUNDER'S MISSION ✨",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nokwazi Nobuhle Xaba, a tech student from Bloemfontein, founded KodeMamas to reduce technology barriers for South African women. Our mission is to democratize coding education in local languages, empowering mothers, girls, and township communities to build their own limitless futures.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom Brand Branding Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F041C))
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "KODEMAMAS.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CODE TODAY.",
                        color = Color(0xFF8B5CF6),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CHANGE TOMORROW.",
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "♡",
                        color = Color(0xFF8B5CF6),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    if (showLanguagePickerInSplash) {
        LanguagePickerDialog(
            currentLangCode = langCode,
            onDismiss = { showLanguagePickerInSplash = false },
            onLangSelected = { code ->
                viewModel.changeLanguage(code)
                showLanguagePickerInSplash = false
            }
        )
    }

    if (selectedPremiumBenefit != null) {
        Dialog(onDismissRequest = { selectedPremiumBenefit = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0B30)),
                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "👑 Premium Advantage", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(text = selectedPremiumBenefit!!, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    Text(
                        text = premiumBenefitDescription,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { selectedPremiumBenefit = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(text = "Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LeftPanelSection(
    onGetStarted: () -> Unit,
    onExploreCourses: () -> Unit,
    langCode: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Points card wrapper with thin purple border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1B0B30).copy(alpha = 0.3f))
                .border(BorderStroke(1.5.dp, Color(0xFF6D28D9)), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Point 1
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "LEARN CODING IN YOUR LANGUAGE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "12 South African languages supported",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }

                // Point 2
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "OFFLINE + ONLINE LEARNING",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Download lessons. Learn anywhere.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }

                // Point 3
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "BUILT FOR SA COMMUNITIES",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Designed for township & rural learners.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Action block
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "START YOUR JOURNEY",
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowDown,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(12.dp)
                )
            }

            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "START LEARNING",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = onExploreCourses,
                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "EXPLORE COURSES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // YOU GET Checklist Block
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF120822)),
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "YOU GET",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                val benefitsList = listOf(
                    "Interactive Lessons",
                    "Quizzes & Challenges",
                    "Progress Tracking",
                    "Certificates",
                    "Community Support",
                    "Daily Coding Challenges",
                    "Mentorship & Career Support"
                )
                benefitsList.forEach { benefit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = benefit,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RightPanelSection(
    onShowBenefit: (String, String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Code Block Window (Interactive Code Console)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
        ) {
            Column {
                // Title bar with dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF252526))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFBBF24)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    }
                    Text(
                        text = "KodeMamas.js",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Code contents
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row {
                        Text(text = "const ", color = Color(0xFFF472B6), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "KodeMamas = {", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row {
                        Text(text = "  mission: ", color = Color(0xFFFFD700), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "\"Digital inclusion\"", color = Color(0xFF34D399), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = ",", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row {
                        Text(text = "  focus: ", color = Color(0xFFFFD700), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "\"Women in tech\"", color = Color(0xFF34D399), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = ",", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row {
                        Text(text = "  impact: ", color = Color(0xFFFFD700), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "\"Stronger communities\"", color = Color(0xFF34D399), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = ",", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row {
                        Text(text = "  future: ", color = Color(0xFFFFD700), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "\"Limitless\"", color = Color(0xFF34D399), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(text = "};", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // LEARN TOP SKILLS Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0B30).copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, Color(0xFF6D28D9).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "LEARN TOP SKILLS ⭐️",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val skillBadges = listOf(
                        "HTML" to Color(0xFFFF5722),
                        "CSS" to Color(0xFF2196F3),
                        "JS" to Color(0xFFFFEB3B),
                        "Python" to Color(0xFF4CAF50)
                    )
                    skillBadges.forEach { (name, color) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, color), RoundedCornerShape(8.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                color = if (color == Color(0xFFFFEB3B)) Color.White else color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // AI ASSISTANT Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF120822)),
            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(text = "⚡ AI ASSISTANT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Your 24/7 coding learning buddy. Get conceptual explanations, code reviews, and mock practice.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Quote bubble card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, topStart = 20.dp, topEnd = 20.dp))
                .background(Color(0xFF130925))
                .border(BorderStroke(1.dp, Color(0xFF8B5CF6)), RoundedCornerShape(bottomStart = 20.dp, topStart = 20.dp, topEnd = 20.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "“I don't just learn code. I build my future.”",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "— Mamas Student, Soweto ♡",
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        // Horizontally Scrollable Premium Features cards at the bottom of the section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "PREMIUM OPPORTUNITIES 👑",
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            val premiums = listOf(
                "1-on-1 Mentorship" to "Connect directly with leading South African female developers. Get weekly code reviews, private Q&A sessions, and career coaching tailored for township and rural learners.",
                "CV & Resume Help" to "Learn how to format your coding milestones into a high-impact tech biography. We help you highlight your local Capstone projects (like Spaza storefronts) to impress recruiters.",
                "Interview Prep" to "Receive mock interviews and technical voice-prep to tackle junior engineer loops with confidence. Master algorithms and behavioral questions easily.",
                "Career Guidance" to "Explore 10 South African career roadmaps, salaries, and hotspot cities. Align your course milestones with local market demands.",
                "Internships & Jobs" to "Access exclusive community job boards and township internships. Enter placement pathways designed with local South African technology corporate partners.",
                "Networking Opportunities" to "Join virtual circles and community hackathons. Exchange tips, collaborate on group projects, and build lifelong technical sisterhood."
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                premiums.forEach { (name, description) ->
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF22113D))
                            .border(BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                            .clickable { onShowBenefit(name, description) }
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.height(60.dp)
                        ) {
                            Text(text = name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(text = "Learn more →", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AfricanQueenIllustration() {
    Box(
        modifier = Modifier
            .size(220.dp)
            .aspectRatio(1f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF007F).copy(alpha = 0.35f), Color(0xFF4B0082).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = w * 0.48f
                )
            )
            
            drawCircle(
                color = Color(0xFF1E0B36),
                radius = w * 0.38f,
                center = Offset(centerX, centerY)
            )
            
            val neckPath = Path().apply {
                moveTo(centerX - w*0.08f, centerY + h*0.12f)
                lineTo(centerX - w*0.06f, centerY + h*0.35f)
                lineTo(centerX + w*0.08f, centerY + h*0.35f)
                lineTo(centerX + w*0.06f, centerY + h*0.12f)
                close()
            }
            drawPath(neckPath, color = Color(0xFF5D4037))
            
            drawRoundRect(
                color = Color(0xFFFFD700),
                topLeft = Offset(centerX - w*0.09f, centerY + h*0.22f),
                size = Size(w * 0.18f, h * 0.03f),
                cornerRadius = CornerRadius(w*0.015f, h*0.015f)
            )
            drawRoundRect(
                color = Color(0xFFFFA000),
                topLeft = Offset(centerX - w*0.08f, centerY + h*0.27f),
                size = Size(w * 0.16f, h * 0.03f),
                cornerRadius = CornerRadius(w*0.015f, h*0.015f)
            )
            
            val facePath = Path().apply {
                moveTo(centerX, centerY - h*0.15f)
                quadraticTo(centerX + w*0.18f, centerY - h*0.10f, centerX + w*0.15f, centerY)
                lineTo(centerX + w*0.22f, centerY + h*0.03f)
                lineTo(centerX + w*0.14f, centerY + h*0.07f)
                quadraticTo(centerX + w*0.18f, centerY + h*0.10f, centerX + w*0.11f, centerY + h*0.12f)
                lineTo(centerX + w*0.08f, centerY + h*0.17f)
                lineTo(centerX - w*0.1f, centerY + h*0.15f)
                lineTo(centerX - w*0.09f, centerY + h*0.05f)
                lineTo(centerX - w*0.1f, centerY - h*0.05f)
                close()
            }
            drawPath(facePath, color = Color(0xFF4E342E))
            
            val wrap1 = Path().apply {
                moveTo(centerX - w*0.22f, centerY - h*0.04f)
                quadraticTo(centerX - w*0.28f, centerY - h*0.28f, centerX, centerY - h*0.36f)
                quadraticTo(centerX + w*0.22f, centerY - h*0.25f, centerX + w*0.11f, centerY - h*0.08f)
                lineTo(centerX - w*0.05f, centerY - h*0.12f)
                close()
            }
            drawPath(wrap1, color = Color(0xFF9C27B0))
            
            val wrap2 = Path().apply {
                moveTo(centerX - w*0.18f, centerY - h*0.14f)
                quadraticTo(centerX - w*0.15f, centerY - h*0.42f, centerX + w*0.05f, centerY - h*0.40f)
                quadraticTo(centerX + w*0.18f, centerY - h*0.30f, centerX + w*0.08f, centerY - h*0.12f)
                close()
            }
            drawPath(wrap2, color = Color(0xFFE91E63))
            
            val stripe = Path().apply {
                moveTo(centerX - w*0.12f, centerY - h*0.25f)
                quadraticTo(centerX - w*0.06f, centerY - h*0.40f, centerX + w*0.1f, centerY - h*0.32f)
                lineTo(centerX + w*0.07f, centerY - h*0.27f)
                quadraticTo(centerX - w*0.05f, centerY - h*0.36f, centerX - w*0.1f, centerY - h*0.21f)
                close()
            }
            drawPath(stripe, color = Color(0xFFFFD700))
            
            drawArc(
                color = Color(0xFFFF9800),
                startAngle = -120f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = Offset(centerX - w*0.18f, centerY - h*0.46f),
                size = Size(w * 0.35f, h * 0.25f)
            )
            
            drawArc(
                color = Color(0xFFFFD700),
                startAngle = -100f,
                sweepAngle = 40f,
                useCenter = false,
                topLeft = Offset(centerX - w*0.14f, centerY - h*0.48f),
                size = Size(w * 0.28f, h * 0.20f)
            )
            
            drawCircle(
                color = Color(0xFFFFD700),
                radius = w * 0.12f,
                center = Offset(centerX - w*0.05f, centerY + h*0.08f),
                style = Stroke(width = w * 0.024f)
            )
        }
    }
}

// ---------------------- COMPONENT: SOUTH AFRICAN PAYMENT SIMULATOR ----------------------
@Composable
fun SouthAfricanPaymentDialog(
    initialIsPremium: Boolean = true,
    langCode: String = "en",
    onDismiss: () -> Unit,
    onPaymentSuccess: (isPremium: Boolean) -> Unit
) {
    var isPremiumChosen by remember { mutableStateOf(initialIsPremium) }
    var selectedMethod by remember { mutableStateOf("capitec_pay") } // capitec_pay, ozow, credit_card, voucher
    var cellNumber by remember { mutableStateOf("0723456789") }
    var voucherCode by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("Capitec") } // Capitec, FNB, Standard Bank, Nedbank, ABSA
    var cardNumber by remember { mutableStateOf("4000 1234 5678 9010") }
    
    var isProcessing by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf("") }

    // Start simulated payment steps
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(2.dp, ThemeIndigo, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            if (isProcessing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = ThemeIndigo, strokeWidth = 4.dp)
                    
                    Text(
                        text = processingStep,
                        fontWeight = FontWeight.Bold,
                        color = ThemeIndigo,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Secured with South African Bank-Grade Encryption 🛡️",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KodeMamas Checkout 🇿🇦",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = ThemeIndigo
                            )
                            Text(
                                text = if (isPremiumChosen) "Unlimited AI + 1-on-1 Careers Mentorship" else "Unlimited AI Tutor + Complete Advanced Courses",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeGold.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPremiumChosen) "R299" else "R99",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFFD4AF37)
                            )
                        }
                    }

                    // Package Switcher Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3E8FF)) // Soft light purple
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isPremiumChosen) ThemeIndigo else Color.Transparent)
                                .clickable { isPremiumChosen = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Plus • R99/m",
                                color = if (!isPremiumChosen) Color.White else ThemeIndigo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPremiumChosen) ThemeIndigo else Color.Transparent)
                                .clickable { isPremiumChosen = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Premium • R299/m",
                                color = if (isPremiumChosen) Color.White else ThemeIndigo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    // Capitec Bank Details Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F0FE))
                            .border(1.5.dp, ThemeIndigo, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🏦 " + Localization.translate("capitec_title", langCode),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = ThemeIndigo
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ThemeGold)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 7.sp,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = Localization.translate("account_info", langCode) + "\n" +
                                       "Price: " + (if (isPremiumChosen) "R299" else "R99"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                lineHeight = 15.sp
                            )
                            Text(
                                text = "💡 " + Localization.translate("payment_inst", langCode),
                                fontSize = 9.sp,
                                color = ThemeIndigo,
                                lineHeight = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Select your preferred payment method:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // Payment Method Quick Selection Grid (2x2 or Scrollable Row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("capitec_pay", "Capitec Pay", "📱"),
                            Triple("ozow", "Ozow EFT", "⚡")
                        ).forEach { (id, label, icon) ->
                            val isSelected = selectedMethod == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ThemeIndigo.copy(alpha = 0.12f) else Color(0xFFF5F5F5))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ThemeIndigo else Color.LightGray,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMethod = id }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) ThemeIndigo else Color.Black
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("credit_card", "Debit / Card", "💳"),
                            Triple("voucher", "Store Voucher", "🎟️")
                        ).forEach { (id, label, icon) ->
                            val isSelected = selectedMethod == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ThemeIndigo.copy(alpha = 0.12f) else Color(0xFFF5F5F5))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ThemeIndigo else Color.LightGray,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedMethod = id }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) ThemeIndigo else Color.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // DYNAMIC FORM CONTENTS
                    when (selectedMethod) {
                        "capitec_pay" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Enter your mobile phone number linked to your Capitec Account:",
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                                OutlinedTextField(
                                    value = cellNumber,
                                    onValueChange = { cellNumber = it },
                                    label = { Text("Cellphone Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                Text(
                                    text = "💡 Instantly prompts your Capitec banking app. No card required! Extremely safe for township communities.",
                                    fontSize = 10.sp,
                                    color = ThemeIndigo,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                        "ozow" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Select your bank for secure Instant EFT checkout via Ozow Secure Link:",
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                                
                                val banks = listOf("Capitec", "FNB", "Standard Bank", "Nedbank", "ABSA")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    banks.forEach { bank ->
                                        val isCurrent = selectedBank == bank
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isCurrent) ThemeIndigo else Color(0xFFEEEEEE))
                                                .clickable { selectedBank = bank }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = bank,
                                                color = if (isCurrent) Color.White else Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "💡 Securely login to your banking portal to confirm immediately. Fully verified by SARB regulations.",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                        "credit_card" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Pay via PayFast Multi-card Secure portal:",
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { cardNumber = it },
                                    label = { Text("Card Number (Visa / Mastercard)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                Text(
                                    text = "💡 Fully 3D Secure verified and encrypted. Supports any South African debit/credit cards.",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                        "voucher" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Enter sponsored training code or Pep/Boxer offline-purchased KodeMamas Voucher:",
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                                OutlinedTextField(
                                    value = voucherCode,
                                    onValueChange = { voucherCode = it },
                                    placeholder = { Text("e.g. TOWNSHIP-MAMA-GRAD") },
                                    label = { Text("Promo/Voucher Code") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                Text(
                                    text = "💡 Best for students who purchased a cash ticket training code at localized township retail centers.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFD4AF37),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ACTIONS
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val priceString = if (isPremiumChosen) "R299" else "R99"
                                when (selectedMethod) {
                                    "capitec_pay" -> {
                                        processingStep = "Initiating Capitec Pay request for $priceString..."
                                        delay(1000)
                                        processingStep = "Awaiting authorization in your banking app..."
                                        delay(1400)
                                        processingStep = "Approved! Finalizing secure token..."
                                        delay(800)
                                    }
                                    "ozow" -> {
                                        processingStep = "Redirecting securely to $selectedBank portal..."
                                        delay(1000)
                                        processingStep = "Authorizing instant EFT transfer of $priceString..."
                                        delay(1400)
                                        processingStep = "Payment received! Syncing with KodeMamas..."
                                        delay(800)
                                    }
                                    "credit_card" -> {
                                        processingStep = "Verifying 3D Secure / OTP validation..."
                                        delay(1200)
                                        processingStep = "Processing card authentication..."
                                        delay(1200)
                                        processingStep = "Authorized successfully!"
                                        delay(600)
                                    }
                                    "voucher" -> {
                                        processingStep = "Validating township voucher parameters..."
                                        delay(1000)
                                        processingStep = "Code verified & approved by Bloemfontein hub!"
                                        delay(1000)
                                    }
                                }
                                onPaymentSuccess(isPremiumChosen)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedMethod == "voucher") {
                                "Verify & Activate " + (if (isPremiumChosen) "Premium" else "Plus")
                            } else {
                                "Simulate Secure Payment • " + (if (isPremiumChosen) "R299" else "R99")
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Cancel checkout", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineStatusBanner(
    isOnline: Boolean,
    onToggleNetwork: () -> Unit,
    langCode: String
) {
    if (!isOnline) {
        var isDismissed by remember { mutableStateOf(false) }
        if (!isDismissed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("offline_status_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFFBEB)
                ),
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Offline Mode Information",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zero-Data Offline Study Mode Active",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B), // High contrast dark indigo/charcoal title
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Learn coding with zero mobile data charges! Lessons, quizzes, and the local playground are fully functional offline. Standard AI chat, live peer-to-peer matching, and forum posts are paused.",
                                color = Color(0xFF1C1917), // High contrast very dark stone/charcoal body text for outdoor readability
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { isDismissed = true },
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("dismiss_offline_banner_button")
                        ) {
                            Text("Keep Learning Offline", color = Color(0xFF451A03), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onToggleNetwork,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD97706)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("enable_online_mode_button")
                        ) {
                            Text("Go Online", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 100-DAY ONBOARDING JOURNEY UI COMPONENTS ----------------------

@Composable
fun OnboardingJourneyWidget(viewModel: MainViewModel, langCode: String) {
    val completedPhases by viewModel.completedPhases.collectAsState()
    val onboardingPhases = viewModel.onboardingPhases
    
    var selectedPhaseId by remember { mutableStateOf<String?>("assess") }
    
    LaunchedEffect(completedPhases) {
        val nextIncomplete = onboardingPhases.firstOrNull { it.id !in completedPhases }?.id
        if (nextIncomplete != null) {
            selectedPhaseId = nextIncomplete
        }
    }
    
    val selectedPhase = onboardingPhases.find { it.id == selectedPhaseId } ?: onboardingPhases.first()
    
    // Dialog states for individual phase flows
    var showGoalDialog by remember { mutableStateOf(false) }
    var showStoryDialog by remember { mutableStateOf(false) }
    var showPosterDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_journey_widget"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ThemeCardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "100-Day Study Onboarding 🇿🇦",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Joey Coleman's 8-Phase Learning Onboarding",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                
                // Progress count
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ThemeIndigo.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${completedPhases.size}/8 Phases Done",
                        color = ThemeIndigo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            val progress = completedPhases.size.toFloat() / 8f
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progress == 1f) Color(0xFF10B981) else ThemeIndigo,
                trackColor = Color(0xFFF3F2FF)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Scrollable Timeline Circles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                onboardingPhases.forEach { phase ->
                    val isCompleted = phase.id in completedPhases
                    val isSelected = phase.id == selectedPhaseId
                    val isCurrentFocus = onboardingPhases.firstOrNull { it.id !in completedPhases }?.id == phase.id
                    
                    val circleBg = when {
                        isCompleted -> Color(0xFFD1FAE5) // light green
                        isSelected -> ThemeIndigo
                        isCurrentFocus -> Color(0xFFFEF3C7) // light gold
                        else -> Color(0xFFF3F4F6) // light grey
                    }
                    
                    val circleBorderColor = when {
                        isSelected && isCompleted -> Color(0xFF10B981)
                        isSelected -> ThemeIndigo
                        isCurrentFocus -> Color(0xFFF59E0B)
                        else -> Color.Transparent
                    }
                    
                    val textColor = when {
                        isSelected -> Color.White
                        isCompleted -> Color(0xFF065F46)
                        isCurrentFocus -> Color(0xFF92400E)
                        else -> Color.Gray
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(circleBg)
                            .border(
                                width = if (circleBorderColor != Color.Transparent) 2.dp else 0.dp,
                                color = circleBorderColor,
                                shape = RoundedCornerShape(19.dp)
                            )
                            .clickable { selectedPhaseId = phase.id }
                            .testTag("phase_circle_${phase.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${phase.phaseNumber}",
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selected Phase Card Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPhase.id in completedPhases) Color(0xFFF9FBF9) else Color(0xFFFAF9FF)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selectedPhase.id in completedPhases) Color(0xFFE6F4EA) else ThemeCardBorder.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedPhase.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        
                        // XP badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (selectedPhase.id in completedPhases) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+${selectedPhase.xpReward} XP",
                                color = if (selectedPhase.id in completedPhases) Color(0xFF065F46) else Color(0xFFB45309),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Emotional context
                    Text(
                        text = "Learner Feels: \"${selectedPhase.feeling}\"",
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = selectedPhase.taskName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = selectedPhase.taskDescription,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Action button
                    if (selectedPhase.id in completedPhases) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFFD1FAE5),
                                disabledContentColor = Color(0xFF065F46)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Phase Completed! +${selectedPhase.xpReward} XP Earned", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                when (selectedPhase.id) {
                                    "assess" -> showGoalDialog = true
                                    "admit" -> {
                                        viewModel.downloadAllLessons()
                                    }
                                    "affirm" -> showStoryDialog = true
                                    "activate" -> {
                                        viewModel.selectTab("learn")
                                    }
                                    "acclimate" -> {
                                        viewModel.completeOnboardingPhase("acclimate")
                                    }
                                    "accomplish" -> {
                                        viewModel.selectTab("learn")
                                    }
                                    "adopt" -> {
                                        viewModel.selectTab("mentorship")
                                        viewModel.completeOnboardingPhase("adopt")
                                    }
                                    "advocate" -> showPosterDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ThemeIndigo
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_action_button_${selectedPhase.id}")
                        ) {
                            Text(
                                text = selectedPhase.actionText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Interactive Phase Dialogs
    if (showGoalDialog) {
        GoalSettingDialog(
            onDismiss = { showGoalDialog = false },
            onSave = {
                viewModel.completeOnboardingPhase("assess")
                showGoalDialog = false
            }
        )
    }
    
    if (showStoryDialog) {
        SuccessStoryDialog(
            onDismiss = { showStoryDialog = false },
            onRead = {
                viewModel.completeOnboardingPhase("affirm")
                showStoryDialog = false
            }
        )
    }
    
    if (showPosterDialog) {
        InvitePosterDialog(
            onDismiss = { showPosterDialog = false },
            onShare = {
                viewModel.completeOnboardingPhase("advocate")
                showPosterDialog = false
            }
        )
    }
}

@Composable
fun GoalSettingDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var selectedGoalIndex by remember { mutableStateOf(0) }
    val goals = listOf(
        "Build a web storefront for my local Spaza shop / family business 🛒",
        "Gain mobile development skills to work as a freelance programmer 💻",
        "Introduce digital literacy and coding classes to my school/NGO 🏫",
        "Master logic and technical problem solving to tutor kids 👩‍🎓"
    )
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("goal_setting_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ThemeCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Confirm Your Onboarding Goal 🎯",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "Phase 1: Assess • Review Your Motivation",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.forEachIndexed { index, goal ->
                        val isSelected = index == selectedGoalIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ThemeIndigo.copy(alpha = 0.08f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) ThemeIndigo else Color(0xFFEEEEEE),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedGoalIndex = index }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedGoalIndex = index },
                                colors = RadioButtonDefaults.colors(selectedColor = ThemeIndigo)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = goal,
                                fontSize = 11.sp,
                                color = Color.Black,
                                lineHeight = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lock in Goal (+20 XP)", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessStoryDialog(
    onDismiss: () -> Unit,
    onRead: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("success_story_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ThemeCardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Bloemfontein Sister Success Story 📖",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "Phase 3: Affirm • See Real Proof",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(ThemeGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TJ", fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    }
                    Column {
                        Text("Mashiane TJ", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                        Text("Bloemfontein Hub Alumni • Spaza shop owner", fontSize = 10.sp, color = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "\"I had zero tech experience. I sell fruits and vegetables in Bloemfontein. When Nokwazi introduced KodeMamas, I was overwhelmed. But downloading the lessons offline changed everything.\n\nToday, my spaza storefront is completely styled and mapped on a local offline web compiler, and my customers are amazed. If other South African mothers can do it, you can too, sister! Choose well, and keep building!\"",
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color.DarkGray,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Go Back", color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onRead,
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("I Can Do This! (+20 XP)", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun InvitePosterDialog(
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("invite_poster_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ThemeCardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Your Community Invitation Poster 📣",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "Phase 8: Advocate • Spread the Sisterhood",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeDarkBg),
                    border = BorderStroke(2.dp, ThemeGold)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🇿🇦 KODEMAMAS SISTERHOOD 👩‍🎓",
                            color = ThemeGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Learn to Code Offline – Zero Data Costs!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Zulu • Xhosa • Afrikaans • isiNdebele & more",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Join using my referral code below:",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeGold.copy(alpha = 0.15f))
                                .border(1.dp, ThemeGold, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "MAMA-7842-NALEDI",
                                color = ThemeGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            android.widget.Toast.makeText(context, "Referral Code copied to clipboard! Share with your neighborhood sisterhood.", android.widget.Toast.LENGTH_LONG).show()
                            onShare()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Share Code (+50 XP)", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingWinDialog(
    phase: com.example.ui.OnboardingPhase,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("onboarding_win_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, ThemeGold)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(ThemeGold.copy(alpha = 0.15f))
                        .border(1.dp, ThemeGold, RoundedCornerShape(36.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 38.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "HALALA, STUDY SISTER! 🎉",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Phase Completed Successfully!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF10B981)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE6F4EA))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+${phase.xpReward} XP Awarded!",
                        color = Color(0xFF137333),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = phase.taskName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                val motivationalText = when (phase.id) {
                    "assess" -> "Fantastic choice! By committing to your goals, you've taken the first brave step on your 100-day journey. You chose well, sister!"
                    "admit" -> "Amazing! Downloading lessons means you are completely prepared for offline township study with zero cellular billing."
                    "affirm" -> "Inspirational! Real-world proof shows that other mothers are doing it. You are on the right path!"
                    "activate" -> "Phenomenal! Running your first dynamic simulator script is a huge win. The machine translates your thoughts into real results!"
                    "acclimate" -> "Brilliant! Reviewing your progress regularly is how habits are born. Consistency always beats intensity."
                    "accomplish" -> "Superb! Passing the quiz means you are locking in solid, verified tech knowledge. You are becoming a master!"
                    "adopt" -> "Magnificent! Connecting with study buddies is how we build township technical sisterhood. We grow further together."
                    "advocate" -> "Heroic! By spreading the tech sisterhood, you are empowering other mothers and girls in your community to build their future."
                    else -> "Keep learning and building! Each step brings you closer to your technical goals."
                }
                
                Text(
                    text = motivationalText,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_onboarding_win_button")
                ) {
                    Text("Continue My Journey 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}



