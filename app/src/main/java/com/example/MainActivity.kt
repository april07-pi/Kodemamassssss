package com.example

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.PrivacyPolicyDialog
import com.example.ui.TermsOfServiceDialog
import com.example.ui.RateAppDialog
import com.example.ui.UserFeedbackDialog
import com.example.ui.PlayStoreShowcaseDialog
import com.example.ui.SettingsHubDialog
import com.example.ui.SubscriptionPlansDialog
import com.example.ui.MentorshipCareerHub
import com.example.ui.FirebasePeopleHub
import com.example.ui.LearnTab
import com.example.ui.ActiveLessonSimulator
import com.example.ui.AiChatTab
import com.example.ui.BuildsTab
import com.example.ui.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import com.example.ui.theme.Localization
import com.example.ui.theme.LocalKodeMamasColors
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeIndigo
import com.example.ui.theme.ThemeGold
import com.example.ui.theme.ThemeGoldLight
import com.example.ui.theme.ThemeSoftBg
import com.example.ui.theme.ThemeCardBorder
import com.example.ui.theme.ThemeDarkBg

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemDark
            }
            MyApplicationTheme(darkTheme = isDark) {
                MainAppScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val colors = LocalKodeMamasColors.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val langCode by viewModel.currentLanguageCode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val userRating by viewModel.userRating.collectAsState()

    val currentLesson by viewModel.currentActiveLesson.collectAsState()

    val showOnboarding by viewModel.showOnboarding.collectAsState()
    val showCourseDetail by viewModel.showCourseDetail.collectAsState()
    val allLessonsList by viewModel.allLessons.collectAsState()

    // Dialogs
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showShowcaseDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf("") }

    LaunchedEffect(userProfile) {
        userProfile?.let { editNameInput = it.name }
    }

    // Determine current active screen mode
    if (showOnboarding) {
        // Screen 1: Welcome / Onboarding Screen
        KodeMamasOnboardingView(
            langCode = langCode,
            onBeginClick = { viewModel.dismissOnboarding() }
        )
    } else if (currentLesson != null) {
        // Fullscreen Active Lesson Simulator - Accessible from anywhere
        ActiveLessonSimulator(viewModel = viewModel, langCode = langCode)
    } else if (showCourseDetail) {
        // Screen 3: Course Overview / Lesson Detail Screen
        KodeMamasCourseDetailView(
            viewModel = viewModel,
            onBackClick = { viewModel.closeCourseDetail() },
            onStartLesson = {
                val lesson = allLessonsList.firstOrNull()
                if (lesson != null) {
                    viewModel.startLesson(lesson)
                }
            }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            containerColor = colors.background,
            topBar = {
                if (currentLesson == null) {
                    AppHeader(
                        userProfile = userProfile,
                        langCode = langCode,
                        isOnline = isOnline,
                        onLangClick = { showLanguageDialog = true },
                        onToggleNetwork = { viewModel.toggleNetworkMode() },
                        onEditProfile = { showEditProfileDialog = true },
                        onOpenSettings = { showSettingsDialog = true },
                        activeScreen = selectedTab,
                        onSelectScreen = { screenId ->
                            when (screenId) {
                                "onboarding" -> viewModel.openOnboarding()
                                "home" -> {
                                    viewModel.closeCourseDetail()
                                    viewModel.selectTab("home")
                                }
                                "course" -> viewModel.openCourseDetail()
                                "ai_chat" -> {
                                    viewModel.closeCourseDetail()
                                    viewModel.selectTab("ai_chat")
                                }
                                "mentorship" -> {
                                    viewModel.closeCourseDetail()
                                    viewModel.selectTab("mentorship")
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (currentLesson == null) {
                    OfficialKodeMamasBottomBar(
                        currentTab = selectedTab,
                        langCode = langCode,
                        onSelectTab = { tab ->
                            viewModel.closeCourseDetail()
                            viewModel.selectTab(tab)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background)
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
                            "home" -> KodeMamasHomeView(
                                viewModel = viewModel,
                                onNavigateToCourse = { viewModel.openCourseDetail() },
                                onNavigateToMentorship = { viewModel.selectTab("mentorship") },
                                onStartProject = { viewModel.selectTab("projects") },
                                onOpenShowcase = { showShowcaseDialog = true },
                                onOpenSettings = { showSettingsDialog = true },
                                onNavigateToAiChat = { viewModel.selectTab("ai_chat") }
                            )
                            "learn" -> LearnTab(
                                viewModel = viewModel,
                                langCode = langCode
                            )
                            "mentorship", "profile" -> KodeMamasMentorshipProgramView(
                                viewModel = viewModel,
                                onBackClick = { viewModel.selectTab("home") },
                                onJoinProgram = { showSubscriptionDialog = true }
                            )
                            "projects", "builds" -> BuildsTab(viewModel = viewModel, langCode = langCode)
                            "community" -> CommunityTab(viewModel = viewModel, langCode = langCode)
                            "ai_chat" -> AiChatTab(viewModel = viewModel, langCode = langCode)
                            else -> KodeMamasHomeView(
                                viewModel = viewModel,
                                onNavigateToCourse = { viewModel.openCourseDetail() },
                                onNavigateToMentorship = { viewModel.selectTab("mentorship") },
                                onStartProject = { viewModel.selectTab("projects") },
                                onOpenShowcase = { showShowcaseDialog = true },
                                onOpenSettings = { showSettingsDialog = true },
                                onNavigateToAiChat = { viewModel.selectTab("ai_chat") }
                            )
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
        SettingsHubDialog(
            userProfile = userProfile,
            currentThemeMode = themeMode,
            onSetThemeMode = { viewModel.setThemeMode(it) },
            onDismiss = { showSettingsDialog = false },
            onOpenRateDialog = {
                showSettingsDialog = false
                showRateDialog = true
            },
            onOpenFeedbackDialog = {
                showSettingsDialog = false
                showFeedbackDialog = true
            },
            onOpenPrivacyPolicy = {
                showSettingsDialog = false
                showPrivacyDialog = true
            },
            onOpenTermsOfService = {
                showSettingsDialog = false
                showTermsDialog = true
            },
            onOpenShowcase = {
                showSettingsDialog = false
                showShowcaseDialog = true
            },
            onOpenLanguagePicker = {
                showSettingsDialog = false
                showLanguageDialog = true
            },
            onOpenEditProfile = {
                showSettingsDialog = false
                showEditProfileDialog = true
            },
            onToggleDataSaving = {
                viewModel.toggleDataSaving(it)
            },
            onResetProgress = {
                viewModel.resetProgressToZero()
            }
        )
    }

    if (showRateDialog) {
        RateAppDialog(
            initialRating = userRating,
            onDismiss = { showRateDialog = false },
            onSubmit = { rating, comment ->
                viewModel.submitRating(rating, comment)
            }
        )
    }

    if (showFeedbackDialog) {
        UserFeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { category, text ->
                viewModel.submitFeedback(category, text)
            }
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showTermsDialog) {
        TermsOfServiceDialog(
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showShowcaseDialog) {
        PlayStoreShowcaseDialog(
            onDismiss = { showShowcaseDialog = false },
            onNavigateToTab = { tab ->
                viewModel.selectTab(tab)
            }
        )
    }

    if (showSubscriptionDialog) {
        SubscriptionPlansDialog(
            viewModel = viewModel,
            onDismiss = { showSubscriptionDialog = false }
        )
    }

    if (showEditProfileDialog) {
        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2E094E),
                border = BorderStroke(1.dp, Color(0xFF4D177E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Edit Profile & Hub",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mama • Bloemfontein Hub",
                        color = ThemeGold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Your Name", color = Color.White.copy(alpha = 0.7f)) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E0635),
                            unfocusedContainerColor = Color(0xFF1E0635),
                            cursorColor = ThemeGold,
                            focusedIndicatorColor = ThemeGold,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditProfileDialog = false }) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editNameInput.isNotBlank()) {
                                    val safeName = SecurityUtils.sanitizePlainText(editNameInput.trim(), maxLength = 80)
                                    viewModel.updateProfileName(safeName)
                                }
                                showEditProfileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeGold)
                        ) {
                            Text("Save", color = Color(0xFF26053D), fontWeight = FontWeight.Bold)
                        }
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
    onOpenSettings: () -> Unit = {},
    activeScreen: String = "home",
    onSelectScreen: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ThemeDarkBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Logo & Controls row
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrowScreen = maxWidth < 380.dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Kode",
                            color = Color.White,
                            fontSize = if (isNarrowScreen) 20.sp else 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "Mamas",
                            color = ThemeGold,
                            fontSize = if (isNarrowScreen) 20.sp else 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    Text(
                        text = "SOUTH AFRICA",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "by Nokwazi Nobuhle Xaba",
                        color = ThemeGold.copy(alpha = 0.95f),
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Network Status Toggle Widget
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isOnline) Color(0xFF064E3B) else Color.White.copy(alpha = 0.12f))
                            .clickable { onToggleNetwork() }
                            .padding(horizontal = if (isNarrowScreen) 6.dp else 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            if (!isNarrowScreen) {
                                Text(
                                    text = if (isOnline) "ONLINE" else "OFFLINE",
                                    color = if (isOnline) Color(0xFF34D399) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Language selector pill
                    val currentLangName = Localization.languages.find { it.code == langCode }?.localName ?: "English"
                    val displayLang = if (isNarrowScreen) {
                        "🇿🇦 ${currentLangName.take(3)}"
                    } else {
                        "🇿🇦 $currentLangName"
                    }

                    Button(
                        onClick = onLangClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F2937)
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = displayLang,
                                color = ThemeGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change Language",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Settings & Hub Icon button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1F2937))
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Hub",
                            tint = ThemeGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Screen Switcher Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val screens = listOf(
                Pair("onboarding", "✨ 1. Welcome"),
                Pair("home", "🏠 2. Home"),
                Pair("course", "📚 3. Course"),
                Pair("ai_chat", "🤖 4. AI Mama (Gemini)"),
                Pair("mentorship", "🤝 5. Mentorship")
            )
            items(screens) { (id, title) ->
                val isSelected = activeScreen == id
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Color(0xFFFA4D89) else Color(0xFF1B0630),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFFA4D89) else Color(0xFF38105B)),
                    modifier = Modifier.clickable { onSelectScreen(id) }
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile summary card (Sub-banner) - Responsive across small, medium and large screens
        userProfile?.let { profile ->
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isSmallPhone = maxWidth < 380.dp
                if (isSmallPhone) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1F2937))
                            .border(1.dp, Color(0xFF374151), RoundedCornerShape(20.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ThemeGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    color = Color(0xFF111827)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Sawubona, ${profile.name} ",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = ThemeGold,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { onEditProfile() }
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${profile.role} • Bloemfontein Hub",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Full width responsive stats bar for readable streak and XP on small screens
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF111827).copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "XP",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = Localization.translate("total_xp", langCode).uppercase(),
                                        color = Color(0xFFFFB300),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${profile.xp}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(Color(0xFF374151))
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Whatshot,
                                    contentDescription = "Streak",
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = Localization.translate("streak", langCode).uppercase(),
                                        color = Color(0xFFFF5722),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${profile.streak}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF1F2937))
                            .border(1.dp, Color(0xFF374151), RoundedCornerShape(22.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Native avatar icon
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ThemeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.name.take(1).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = Color(0xFF111827)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sawubona, ${profile.name} ",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = ThemeGold,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onEditProfile() }
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${profile.role} • Bloemfontein Hub",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Stats values
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Localization.translate("total_xp", langCode).uppercase(),
                                    color = Color(0xFFFFB300),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "XP Logo",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(14.dp)
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
                                        modifier = Modifier.size(15.dp)
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
    }
}

// ---------------------- COMPONENT: NAVIGATION ----------------------
@Composable
fun AppBottomNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    langCode: String
) {
    val colors = LocalKodeMamasColors.current

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = colors.bottomNavBackground,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("home", Icons.Default.Home, Localization.translate("dashboard", langCode)),
            Triple("builds", Icons.Default.Build, Localization.translate("builds", langCode)),
            Triple("learn", Icons.Default.School, Localization.translate("lessons", langCode)),
            Triple("ai_chat", Icons.Default.AutoAwesome, Localization.translate("ai_assistant", langCode)),
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
                        tint = if (isSelected) (if (colors.isDark) colors.brandGold else ThemeIndigo) else colors.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) (if (colors.isDark) colors.brandGold else ThemeIndigo) else colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = if (colors.isDark) colors.surfaceVariant else Color(0xFFF3E8FF)
                )
            )
        }
    }
}

// ---------------------- TAB 1: DASHBOARD / HOME ----------------------
@Composable
fun DashboardTab(
    viewModel: MainViewModel,
    langCode: String,
    onOpenLanguageDialog: () -> Unit = {},
    onOpenShowcase: () -> Unit = {},
    onOpenRateDialog: () -> Unit = {},
    onOpenFeedbackDialog: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenTermsOfService: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenSubscriptionPlans: () -> Unit = {}
) {
    val colors = LocalKodeMamasColors.current
    val lessons by viewModel.allLessons.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val challenges by viewModel.allChallenges.collectAsState()
    val activeChallenge by viewModel.activeChallenge.collectAsState()
    val currentPlan by viewModel.currentPlanTier.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showCertificateDialog by remember { mutableStateOf(false) }

    // State for Interactive Code Preview Demo
    var selectedDemoCategory by remember { mutableStateOf("HTML") }
    var isDemoRunning by remember { mutableStateOf(false) }
    var demoRunCompleted by remember { mutableStateOf(false) }

    val allLanguagesShowcase = listOf(
        Triple("zu", "isiZulu", "Sawubona! Funda amakhodi"),
        Triple("xh", "isiXhosa", "Molo! Khowuda lula"),
        Triple("af", "Afrikaans", "Hallo! Leer kodering"),
        Triple("nso", "Sepedi", "Dumela! Ithute khoute"),
        Triple("tn", "Setswana", "Dumela! Ithute go khouta"),
        Triple("st", "Sesotho", "Khotso! Ithute khoutu"),
        Triple("ts", "Xitsonga", "Avuxeni! Dyondza khoudu"),
        Triple("ss", "siSwati", "Sawubona! Funda kukhoda"),
        Triple("ve", "Tshivenda", "Ndaa! Guda u khouda"),
        Triple("nr", "isiNdebele", "Lotjhani! Funda ikhowudi"),
        Triple("en", "English", "Welcome! Learn to code"),
        Triple("sasl", "Sign Language", "Visual Gestures & Guides")
    )

    val progressList by viewModel.allProgress.collectAsState()
    val completedLessonIds = remember(progressList) {
        progressList.filter { it.isCompleted }.map { it.lessonId }.toSet()
    }
    val completedCount = completedLessonIds.size
    val totalCount = lessons.size.coerceAtLeast(1)
    val progressRatio = (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val activeLesson = lessons.firstOrNull { it.isUnlocked && it.id !in completedLessonIds } ?: lessons.firstOrNull { it.isUnlocked } ?: lessons.firstOrNull()
    val recentlyCompleted = lessons.lastOrNull { it.id in completedLessonIds }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. LEARNER GREETING & STATUS CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("learner_welcome_card"),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ThemeGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (userProfile?.name?.take(1)?.uppercase() ?: "K"),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Color(0xFF111827)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Sawubona, ${userProfile?.name ?: "Learner"}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${userProfile?.role ?: "Student"} • Bloemfontein Hub 🇿🇦",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        // Data Saver Toggle
                        userProfile?.let { profile ->
                            IconButton(
                                onClick = { viewModel.toggleDataSavingMode(!profile.dataSavingMode) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (profile.dataSavingMode) Color(0xFFE8F0FE) else colors.surfaceVariant)
                                    .testTag("data_saver_toggle")
                            ) {
                                Icon(
                                    imageVector = if (profile.dataSavingMode) Icons.Default.SignalCellularAlt2Bar else Icons.Default.SignalCellularAlt,
                                    contentDescription = "Data Saver Mode",
                                    tint = if (profile.dataSavingMode) Color(0xFF1A73E8) else colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Code in Your Language. Learn Anywhere. Build Your Future.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeIndigo
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Learner Stat Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Streak
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFF5722).copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Whatshot,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Column {
                                    Text(
                                        text = "${userProfile?.streak ?: 0}d",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.textPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Streak",
                                        fontSize = 9.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // XP
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ThemeGold.copy(alpha = 0.12f))
                                .border(1.dp, ThemeGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Column {
                                    Text(
                                        text = "${userProfile?.xp ?: 0} XP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.textPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Learned",
                                        fontSize = 9.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Completed Lessons
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.08f))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Column {
                                    Text(
                                        text = "$completedCount/$totalCount",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.textPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Done",
                                        fontSize = 9.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. PRIMARY STANDOUT CTA: CURRENT LESSON & PROGRESS (THE HERO FOCUS)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .testTag("continue_learning_hero_card"),
                colors = CardDefaults.cardColors(containerColor = ThemeDarkBg),
                border = BorderStroke(1.5.dp, ThemeGold.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeGold)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CURRENT MODULE",
                                color = Color(0xFF111827),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${activeLesson?.category ?: "HTML"} • ${activeLesson?.durationMinutes ?: 15} Mins",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = activeLesson?.title ?: "Web Fundamentals: Spaza Shop Storefront",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 24.sp
                    )

                    if (!activeLesson?.titleLocalized.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeLesson!!.titleLocalized,
                            color = ThemeGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Course Track Progress",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(progressRatio * 100).toInt()}% Completed",
                            color = ThemeGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ThemeGold,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )

                    if (recentlyCompleted != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Recently completed: ${recentlyCompleted.title}",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // PRIMARY ACTION BUTTON (Visual ceiling & standout)
                    Button(
                        onClick = {
                            activeLesson?.let { viewModel.selectLesson(it) }
                            viewModel.selectTab("learn")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("continue_learning_primary_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = ThemeGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue Learning",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. QUICK ACCESS TO CORE COURSES
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Core Curriculum",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "7 comprehensive learning paths for mobile, web, AI and design",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }

                    TextButton(
                        onClick = {
                            viewModel.setSelectedCategoryFilter("All")
                            viewModel.selectTab("learn")
                        },
                        modifier = Modifier.testTag("view_all_courses_button")
                    ) {
                        Text(
                            text = "All Lessons",
                            color = ThemeIndigo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val coreCategories = listOf(
                        Triple("HTML", "HTML5 & Spaza Storefronts", Color(0xFFFF5722)),
                        Triple("CSS", "CSS3 African Tech Palette & Design", Color(0xFF2563EB)),
                        Triple("JavaScript", "JavaScript Logic & Calculations", Color(0xFFD97706)),
                        Triple("Python", "Python Data & Harvest Forecasting", Color(0xFF059669)),
                        Triple("Mobile Dev", "Android & Jetpack Compose Apps", Color(0xFF0284C7)),
                        Triple("Data & AI", "Gemini AI & Spaza Analytics", Color(0xFF10B981)),
                        Triple("Design", "UI/UX & Inclusive African Styling", Color(0xFFF59E0B))
                    )

                    coreCategories.forEach { (cat, title, tagColor) ->
                        val matchingLesson = lessons.find { it.category == cat }
                        val isLessonCompleted = matchingLesson != null && matchingLesson.id in completedLessonIds
                        val isUnlocked = true // Accessible to all learners

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (cat == "Data & AI") {
                                        viewModel.closeCourseDetail()
                                        viewModel.selectTab("ai_chat")
                                    } else {
                                        matchingLesson?.let {
                                            viewModel.selectLesson(it)
                                            viewModel.setSelectedCategoryFilter(cat)
                                            viewModel.closeCourseDetail()
                                            viewModel.selectTab("learn")
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            border = BorderStroke(1.dp, if (matchingLesson == activeLesson) ThemeIndigo else colors.cardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(tagColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (cat == "JavaScript") "JS" else if (cat == "Python") "PY" else if (cat == "Data & AI") "🤖" else cat,
                                        color = tagColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (cat == "Data & AI") "${Localization.translate("cat_data_ai", langCode)} • ${Localization.translate("ai_assistant", langCode)}" else cat,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tagColor
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (cat == "Data & AI") "• ${Localization.translate("mama_ruth_ai", langCode)}" else "• ${matchingLesson?.durationMinutes ?: 15} mins",
                                            fontSize = 10.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (cat == "Data & AI") Localization.translate("ai_banner_desc", langCode) else if (matchingLesson != null) Localization.getLessonTitle(matchingLesson.id, langCode) else title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (isLessonCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE6F4EA))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "COMPLETED ✓",
                                            color = Color(0xFF137333),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ThemeIndigo.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (matchingLesson == activeLesson) "IN PROGRESS" else "START",
                                            color = ThemeIndigo,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = colors.textSecondary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. RECOMMENDED NEXT STEP: DAILY CODING PRACTICE
        item {
            val challenge = challenges.firstOrNull()
            if (challenge != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DAILY CODING PRACTICE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF5722)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (challenge.isCompleted) Color(0xFFE6F4EA) else ThemeGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (challenge.isCompleted) "SOLVED (+20 XP)" else "+20 XP REWARD",
                                    color = if (challenge.isCompleted) Color(0xFF137333) else Color(0xFFB06000),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = challenge.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = challenge.description,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        if (!challenge.isCompleted) {
                            Button(
                                onClick = {
                                    viewModel.setActiveChallenge(challenge)
                                    viewModel.solveChallenge()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("solve_challenge_button")
                            ) {
                                Text(
                                    text = "Accept Challenge & Run Calculation",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Challenge completed! 20 XP added to your learner profile.",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. OFFLINE LEARNING & PRACTICAL TOOLS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ThemeIndigo.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = ThemeIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Offline Mode & Data Saver",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Built for township and rural South African networks",
                                    fontSize = 10.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE6F4EA))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "0 DATA MODE",
                                color = Color(0xFF137333),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Download lessons once over Wi-Fi or mobile data to practice HTML, CSS, JavaScript, and Python 100% offline without using airtime.",
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.downloadAllLessons() },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DownloadForOffline,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (userProfile?.hasDownloadedOffline == true) "All Downloaded ✓" else "Download Lessons",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showCertificateDialog = true },
                            border = BorderStroke(1.dp, ThemeIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = ThemeIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "My Certificate",
                                color = ThemeIndigo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 6. MULTILINGUAL ACCESS (ALL 12 OFFICIAL SOUTH AFRICAN LANGUAGES)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Learn in Your Language 🇿🇦",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "12 official South African languages supported",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }

                    TextButton(onClick = onOpenLanguageDialog) {
                        Text(text = "Change", color = ThemeIndigo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allLanguagesShowcase) { (code, name, greeting) ->
                        val isSelected = code == langCode
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) ThemeIndigo else colors.surface)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) ThemeGold else colors.cardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.changeLanguage(code) }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) ThemeGold else colors.textPrimary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = ThemeGold,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = greeting,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else colors.textSecondary,
                                    lineHeight = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. INTERACTIVE CODE DEMO SANDBOX
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Interactive Code Sandbox",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Try real code in 30 seconds",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ThemeGold.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SANDBOX",
                                color = Color(0xFF92400E),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Selector Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categories = listOf("HTML", "CSS", "JS", "Python")
                        categories.forEach { cat ->
                            val isCatSelected = selectedDemoCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCatSelected) ThemeIndigo else colors.surfaceVariant)
                                    .clickable {
                                        selectedDemoCategory = cat
                                        demoRunCompleted = false
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isCatSelected) Color.White else colors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Code Editor Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ThemeDarkBg)
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = when (selectedDemoCategory) {
                                        "HTML" -> "index.html (Mam's Spaza Storefront)"
                                        "CSS" -> "style.css (African Tech Palette)"
                                        "JS" -> "calculator.js (Bread & Milk Cart)"
                                        else -> "crops.py (Harvest Tracker)"
                                    },
                                    color = ThemeGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = "UTF-8", color = Color.Gray, fontSize = 9.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = when (selectedDemoCategory) {
                                    "HTML" -> "<h1>Mam's Soweto Spaza</h1>\n<p>Fresh Bread: R16 | Milk: R22</p>\n<button>Order on WhatsApp</button>"
                                    "CSS" -> "body {\n  background: #6D28D9; /* Primary Purple */\n  color: #FBBF24; /* Accent Gold */\n  border-radius: 16px;\n}"
                                    "JS" -> "const bread = 16, milk = 22;\nconst total = (bread * 2) + milk;\nconsole.log('Spaza Total: R' + total);"
                                    else -> "harvest = {'maize_bags': 45, 'price': 180}\nrevenue = harvest['maize_bags'] * harvest['price']\nprint(f'Township Yield: R{revenue}')"
                                },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.White,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isDemoRunning = true
                            demoRunCompleted = true
                            isDemoRunning = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_demo_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = ThemeGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Run Code Simulator", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (demoRunCompleted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF9F7FD))
                                .border(1.dp, ThemeIndigo.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⚡ OUTPUT PREVIEW:", fontSize = 9.sp, fontWeight = FontWeight.Black, color = ThemeIndigo)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "0.04s (100% Offline)", fontSize = 9.sp, color = Color(0xFF137333), fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                when (selectedDemoCategory) {
                                    "HTML" -> {
                                        Column {
                                            Text(text = "Mam's Soweto Spaza", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Black)
                                            Text(text = "• Fresh Bread: R16\n• Fresh Milk: R22", fontSize = 11.sp, color = Color.DarkGray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF25D366))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(text = "Order on WhatsApp", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    "CSS" -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ThemeIndigo)
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "African EdTech Palette Active: #6D28D9 & #FBBF24 applied!",
                                                color = ThemeGold,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    "JS" -> {
                                        Text(
                                            text = "Spaza Total Calculated: 2 Loaves (R32) + 1 Milk (R22) = R54.00",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF137333)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "Predictive Harvest Revenue: 45 Bags Maize @ R180 = R8,100 Projected",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF137333)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. MENTORSHIP & FOUNDER STORY
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ThemeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👩🏽‍💻", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Nokwazi Nobuhle Xaba",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Founder • IT & Computer Science • Bloemfontein 🇿🇦",
                                fontSize = 10.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = Localization.translate("founder_desc", langCode),
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.selectTab("mentorship") },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Connect with Mentors & Career Support",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 9. MEMBERSHIP PLANS & CAPITEC EFT
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🇿🇦", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KodeMamas Membership",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.textPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ThemeGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentPlan,
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Capitec Acc: 2121743886",
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("2121743886"))
                                Toast.makeText(context, "Capitec Account 2121743886 copied! 🇿🇦", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Copy Acc", color = ThemeIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onOpenSubscriptionPlans,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("View Plans & EFT Instructions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // 10. SECONDARY ACTIONS: SHOWCASE TOUR & RATE APP
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenShowcase,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ThemeIndigo)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ThemeIndigo, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tour Guide", color = ThemeIndigo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenFeedbackDialog,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Icon(imageVector = Icons.Default.Feedback, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Feedback", color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 11. COMPLIANCE & LEGAL LINKS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpenPrivacyPolicy) {
                    Text("Privacy Policy", fontSize = 11.sp, color = colors.textSecondary)
                }
                Text("•", color = colors.textSecondary, fontSize = 11.sp)
                TextButton(onClick = onOpenTermsOfService) {
                    Text("Terms", fontSize = 11.sp, color = colors.textSecondary)
                }
                Text("•", color = colors.textSecondary, fontSize = 11.sp)
                TextButton(onClick = onOpenSettings) {
                    Text("Settings", fontSize = 11.sp, color = colors.textSecondary)
                }
            }
        }
    }

    if (showCertificateDialog) {
        Dialog(onDismissRequest = { showCertificateDialog = false }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(2.dp, ThemeGold, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🏆", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "KodeMamas Certificate",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
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

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "This certificate confirms that ${userProfile?.name ?: "Learner"} has successfully completed foundation coursework in mobile & web development with KodeMamas.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { showCertificateDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Ngiyabonga! (Close)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------------- TAB 2: LEARN / LESSONS ----------------------
// Note: LearnTab implementation has been moved and upgraded in com.example.ui.LearnScreens.kt


// ---------------------- SCREEN: INTERACTIVE ACTIVE LESSON SIMULATOR ----------------------
// Note: ActiveLessonSimulator implementation has been moved and upgraded in com.example.ui.LearnScreens.kt


// ---------------------- HELPER: FORMATTED CHAT MESSAGE ----------------------
// Note: FormattedChatMessage implementation has been moved and upgraded in com.example.ui.AiChatScreens.kt

// ---------------------- TAB 3: AI ASSISTANT / CHATBOT ----------------------
// Note: AiChatTab implementation has been moved and upgraded in com.example.ui.AiChatScreens.kt

// ---------------------- TAB: BUILDS / SANDBOX ----------------------
// Note: BuildsTab implementation has been moved and upgraded in com.example.ui.BuildsScreens.kt

// ---------------------- TAB 4: COMMUNITY SECTION ----------------------
// Note: CommunityTab implementation has been moved and upgraded in com.example.ui.CommunityScreens.kt

// ---------------------- TAB 6: MENTORSHIP & CAREERS (PREMIUM) ----------------------
@Composable
fun MentorshipTab(viewModel: MainViewModel, langCode: String) {
    MentorshipCareerHub(viewModel = viewModel, langCode = langCode)
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
