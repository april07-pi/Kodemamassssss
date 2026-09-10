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
                    onOpenSettings = { showSettingsDialog = true }
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
                        "home" -> DashboardTab(
                            viewModel = viewModel,
                            langCode = langCode,
                            onOpenLanguageDialog = { showLanguageDialog = true },
                            onOpenShowcase = { showShowcaseDialog = true },
                            onOpenRateDialog = { showRateDialog = true },
                            onOpenFeedbackDialog = { showFeedbackDialog = true },
                            onOpenPrivacyPolicy = { showPrivacyDialog = true },
                            onOpenTermsOfService = { showTermsDialog = true },
                            onOpenSettings = { showSettingsDialog = true },
                            onOpenSubscriptionPlans = { showSubscriptionDialog = true }
                        )
                        "builds" -> BuildsTab(viewModel = viewModel, langCode = langCode)
                        "learn" -> LearnTab(viewModel = viewModel, langCode = langCode)
                        "ai_chat" -> AiChatTab(viewModel = viewModel, langCode = langCode)
                        "community" -> CommunityTab(viewModel = viewModel, langCode = langCode)
                        "mentorship" -> MentorshipTab(viewModel = viewModel, langCode = langCode)
                        else -> DashboardTab(
                            viewModel = viewModel,
                            langCode = langCode,
                            onOpenLanguageDialog = { showLanguageDialog = true },
                            onOpenShowcase = { showShowcaseDialog = true },
                            onOpenRateDialog = { showRateDialog = true },
                            onOpenFeedbackDialog = { showFeedbackDialog = true },
                            onOpenPrivacyPolicy = { showPrivacyDialog = true },
                            onOpenTermsOfService = { showTermsDialog = true },
                            onOpenSettings = { showSettingsDialog = true },
                            onOpenSubscriptionPlans = { showSubscriptionDialog = true }
                        )
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
                                    viewModel.updateProfileName(editNameInput.trim())
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
    onOpenSettings: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF26053D))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
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
                        .background(if (isOnline) Color(0xFF0F3E2E) else Color.White.copy(alpha = 0.15f))
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
                                .background(if (isOnline) Color(0xFF00E676) else Color(0xFFFFB800))
                        )
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            color = if (isOnline) Color(0xFF00E676) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Language selector pill
                Button(
                    onClick = onLangClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38105B)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "🇿🇦 ${Localization.languages.find { it.code == langCode }?.localName ?: "English"}",
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

                Spacer(modifier = Modifier.width(6.dp))

                // Settings & Hub Icon button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF38105B))
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

        Spacer(modifier = Modifier.height(14.dp))

        // Profile summary card (Sub-banner)
        userProfile?.let { profile ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF350B56))
                    .border(1.dp, Color(0xFF4F1A7E), RoundedCornerShape(22.dp))
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
                        color = Color(0xFF26053D)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sawubona, ${profile.name.take(6)}... ",
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
                    Text(
                        text = "${profile.role} •",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Bloemfontein Hub",
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
                        tint = if (isSelected) (if (colors.isDark) colors.brandGold else Color(0xFF26053D)) else colors.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) (if (colors.isDark) colors.brandGold else Color(0xFF26053D)) else colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = if (colors.isDark) colors.surfaceVariant else Color(0xFFE8DEF8)
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO SECTION INTRODUCING KODEMAMAS
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .border(1.dp, ThemeCardBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column {
                    // Badge: Built for South African Communities
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeIndigo.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🇿🇦 BUILT FOR SOUTH AFRICAN COMMUNITIES",
                                color = ThemeIndigo,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = Localization.translate("learn_coding", langCode) + "!",
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Empowering mothers, girls, students, and underserved communities to code in their home language, build digital storefronts, and enter the tech ecosystem.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Call-To-Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val firstPlayable = lessons.find { it.isUnlocked } ?: lessons.firstOrNull()
                                firstPlayable?.let { viewModel.selectLesson(it) }
                                viewModel.selectTab("learn")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("hero_start_coding_button")
                        ) {
                            Text(
                                text = Localization.translate("get_started", langCode),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenLanguageDialog,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ThemeIndigo),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier.testTag("hero_language_button")
                        ) {
                            Text(
                                text = "12 Languages 🇿🇦",
                                color = ThemeIndigo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Data Saver toggle switch
                        userProfile?.let { profile ->
                            IconButton(
                                onClick = { viewModel.toggleDataSavingMode(!profile.dataSavingMode) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (profile.dataSavingMode) Color(0xFFE8F0FE) else Color.Gray.copy(alpha = 0.1f))
                                    .testTag("data_saver_toggle")
                            ) {
                                Icon(
                                    imageVector = if (profile.dataSavingMode) Icons.Default.SignalCellularAlt2Bar else Icons.Default.SignalCellularAlt,
                                    contentDescription = "Data Saver",
                                    tint = if (profile.dataSavingMode) Color(0xFF1A73E8) else Color.DarkGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1.5. KODEMAMAS MEMBERSHIP PLANS & CAPITEC BANKING HERO BANNER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, ThemeGold.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = ThemeDarkBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🇿🇦", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KodeMamas Membership Plans",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeGold)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (currentPlan) {
                                    "PREMIUM" -> "PREMIUM (R299/yr)"
                                    "STANDARD" -> "STANDARD (R99/yr)"
                                    else -> "CAPITEC EFT"
                                },
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Standard Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Standard", color = ThemeGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                Text("R99 / year", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Offline lessons, 12 languages, quizzes & certificates", color = Color.White.copy(alpha = 0.75f), fontSize = 9.sp, lineHeight = 12.sp)
                            }
                        }

                        // Premium Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, ThemeGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Premium", color = ThemeGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                Text("R299 / year", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("1-on-1 mentorship, CV/portfolio builder, mock interviews", color = Color.White.copy(alpha = 0.75f), fontSize = 9.sp, lineHeight = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Capitec Account Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1332))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Capitec Acc: 2121743886",
                            color = Color.White,
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
                            Text("Copy Acc", color = ThemeGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenSubscriptionPlans,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("View Plans & EFT Instructions", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        // 2. OFFLINE + ONLINE LEARNING ARCHITECTURE SHOWCASE
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(listOf(ThemeDarkBg, Color(0xFF1B0C2E))))
                    .border(1.dp, ThemeGold.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
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
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ThemeGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.WifiOff, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Offline + Online Learning",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Engineered for Township & Rural Networks",
                                    color = ThemeGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE6F4EA))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = "0 DATA MODE", color = Color(0xFF137333), fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Download lessons once over Wi-Fi or data, then practice HTML, CSS, JavaScript, and Python 100% offline without spending mobile airtime. Fast loading, small app footprint, and low battery consumption on entry-level Android devices.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Download all button
                        Button(
                            onClick = { viewModel.downloadAllLessons() },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DownloadForOffline, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (userProfile?.hasDownloadedOffline == true) "All 4 Downloaded" else "Download Lessons",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Certificate shortcut
                        OutlinedButton(
                            onClick = { showCertificateDialog = true },
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "My Certificate",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. LANGUAGE ACCESSIBILITY SHOWCASE (ALL 12 SOUTH AFRICAN LANGUAGES)
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
                            color = Color.Black
                        )
                        Text(
                            text = "Supporting all 12 official South African languages",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    TextButton(onClick = onOpenLanguageDialog) {
                        Text(text = "View All", color = ThemeIndigo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allLanguagesShowcase) { (code, name, greeting) ->
                        val isSelected = code == langCode
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) ThemeIndigo else Color.White)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ThemeGold else ThemeCardBorder,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable { viewModel.changeLanguage(code) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = if (isSelected) ThemeGold else Color.Black
                                    )
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ThemeGold, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = greeting,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. APP PREVIEW / LIVE INTERACTIVE CODING DEMO
        item {
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
                        Column {
                            Text(
                                text = "Interactive Code Demo",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "Try coding right here in 30 seconds!",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeGold.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "LIVE SANDBOX", color = Color(0xFFB06000), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                                    .background(if (isCatSelected) ThemeIndigo else Color(0xFFF3F1FA))
                                    .clickable {
                                        selectedDemoCategory = cat
                                        demoRunCompleted = false
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isCatSelected) ThemeGold else Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Code Editor Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ThemeDarkBg)
                            .padding(14.dp)
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

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = when (selectedDemoCategory) {
                                    "HTML" -> "<h1>Mam's Soweto Spaza</h1>\n<p>Fresh Bread: R16 | Milk: R22</p>\n<button>Order on WhatsApp</button>"
                                    "CSS" -> "body {\n  background: #4B0082; /* Deep Indigo */\n  color: #FFD700; /* Radiant Gold */\n  border-radius: 16px;\n}"
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

                    // Run Demo Action Button
                    Button(
                        onClick = {
                            isDemoRunning = true
                            demoRunCompleted = true
                            isDemoRunning = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_demo_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = ThemeGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Run Live Code Simulator", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Render Simulated Output Result
                    if (demoRunCompleted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF9F7FD))
                                .border(1.dp, ThemeIndigo.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⚡ OUTPUT PREVIEW:", fontSize = 9.sp, fontWeight = FontWeight.Black, color = ThemeIndigo)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Compiled in 0.04s (100% Offline)", fontSize = 9.sp, color = Color(0xFF137333), fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                when (selectedDemoCategory) {
                                    "HTML" -> {
                                        Column {
                                            Text(text = "Mam's Soweto Spaza", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.Black)
                                            Text(text = "• Fresh Bread: R16\n• Fresh Milk: R22", fontSize = 11.sp, color = Color.DarkGray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF25D366))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(text = "Order on WhatsApp", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    "CSS" -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(ThemeIndigo)
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = "African Tech Color Palette Active: Deep Indigo & Gold applied!",
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
                                            fontSize = 12.sp,
                                            color = Color(0xFF137333)
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "Predictive Harvest Revenue: 45 Bags Maize @ R180 = R8,100 Projected",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
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

        // 5. TOWNSHIP TECH MENTORSHIP & CAREER SHOWCASE
        item {
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
                        Column {
                            Text(
                                text = "Mentorship & Career Hub",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "1-on-1 guidance for girls, mothers & students",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeGold)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "CAREERS", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Feature 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF9F7FD))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "👩🏾‍💼 1-on-1 Mentor", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ThemeIndigo)
                                Text(text = "SA Tech Mentors", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                        // Feature 2
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF9F7FD))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "📄 CV Builder", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ThemeIndigo)
                                Text(text = "Tech Resume Creator", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                        // Feature 3
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF9F7FD))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "🎯 Mock Interview", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ThemeIndigo)
                                Text(text = "Interview Prep", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.selectTab("mentorship") },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explore_mentorship_button")
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

        // 6. DAILY CODING CHALLENGE INTERACTIVE SEGMENT
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

        // 7. FOUNDER MISSION & STORY (Nokwazi Nobuhle Xaba - Bloemfontein, Free State)
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .background(ThemeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👩🏽‍💻", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Nokwazi Nobuhle Xaba",
                                color = ThemeGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Founder • IT & Computer Science Student • Bloemfontein 🇿🇦",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Origin Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "🎓 IT & Comp Sci", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "📍 Bloemfontein", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ThemeGold.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "🇿🇦 12 Official Languages", color = ThemeGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = Localization.translate("founder_desc", langCode),
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }


        // 8.5 INTERACTIVE FEATURE TOUR & PLAY STORE SHOWCASE
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenShowcase() },
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.5.dp, ThemeGold.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ThemeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📸", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Play Store Feature Tour",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ThemeGold.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "6 SLIDES",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeGold
                                    )
                                }
                            }
                            Text(
                                text = "Take a visual walk-through of all platform pillars & offline tools",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenShowcase,
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF26053D),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explore Platform Showcase",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF26053D),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 8.6 TESTERS FEEDBACK & RATE APP CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⭐", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rate KodeMamas on Google Play",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Your reviews help bring free coding to township schools",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onOpenRateDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Rate App", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedButton(
                            onClick = onOpenFeedbackDialog,
                            border = BorderStroke(1.dp, ThemeGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Feedback,
                                contentDescription = null,
                                tint = ThemeGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Feedback", fontWeight = FontWeight.Bold, color = ThemeGold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 8.7 LEGAL & COMPLIANCE (POPIA / TERMS / SETTINGS)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Compliance & Transparency",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onOpenPrivacyPolicy) {
                            Text("🔒 Privacy Policy", fontSize = 11.sp, color = ThemeGold, fontWeight = FontWeight.SemiBold)
                        }
                        Text("•", color = colors.textSecondary, fontSize = 12.sp)
                        TextButton(onClick = onOpenTermsOfService) {
                            Text("📜 Terms of Service", fontSize = 11.sp, color = ThemeGold, fontWeight = FontWeight.SemiBold)
                        }
                        Text("•", color = colors.textSecondary, fontSize = 12.sp)
                        TextButton(onClick = onOpenSettings) {
                            Text("⚙️ Settings", fontSize = 11.sp, color = ThemeGold, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 9. FOOTER CREATOR CREDIT
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(ThemeIndigo.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "KodeMamas • Created by Nokwazi Nobuhle Xaba\nEmpowering South African Communities",
                        color = if (colors.isDark) Color.White.copy(alpha = 0.85f) else ThemeIndigo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
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
                                        text = Localization.translate("in_your_language", langCode),
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

// ---------------------- HELPER: FORMATTED CHAT MESSAGE ----------------------
@Composable
fun FormattedChatMessage(text: String) {
    val context = LocalContext.current
    val isGrounded = text.contains("Grounded with Google Search Database") || text.contains("Google Search Database & Real-Time Web Grounding")
    val lines = text.split("\n")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (isGrounded) {
            Surface(
                color = Color(0xFFF1F5FD),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFD0E1FD)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A73E8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Grounded with Google Search Database & Gemini AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF174EA6)
                    )
                }
            }
        }

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
            } else if (trimmedLine.startsWith("•") && (trimmedLine.contains("http://") || trimmedLine.contains("https://"))) {
                val urlMatcher = Regex("""https?://[^\s)\]]+""")
                val match = urlMatcher.find(trimmedLine)
                val url = match?.value

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔗", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trimmedLine.replace(Regex("""https?://[^\s)\]]+"""), "").replace("[", "").replace("]", "").replace("•", "").trim(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (url != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        FilledTonalButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFE8F0FE),
                                contentColor = Color(0xFF1A73E8)
                            )
                        ) {
                            Text("Open", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = buildAnnotatedString {
                        var i = 0
                        while (i < line.length) {
                            if (i + 1 < line.length && line[i] == '*' && line[i + 1] == '*') {
                                val end = line.indexOf("**", i + 2)
                                if (end != -1) {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))) {
                                        append(line.substring(i + 2, end))
                                    }
                                    i = end + 2
                                    continue
                                }
                            } else if (line[i] == '*' && (i + 1 == line.length || line[i + 1] != '*')) {
                                val end = line.indexOf('*', i + 1)
                                if (end != -1) {
                                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Color(0xFF2C2C2C))) {
                                        append(line.substring(i + 1, end))
                                    }
                                    i = end + 1
                                    continue
                                }
                            }
                            append(line[i])
                            i++
                        }
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF222222),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ---------------------- TAB 3: AI ASSISTANT / CHATBOT ----------------------
@Composable
fun AiChatTab(viewModel: MainViewModel, langCode: String) {
    val chats by viewModel.aiChats.collectAsState()
    val isGenerating by viewModel.aiGenerating.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isGoogleSearchEnabled by viewModel.isGoogleSearchEnabled.collectAsState()
    val selectedModel by viewModel.selectedGeminiModel.collectAsState()
    val aiLanguageCode by viewModel.aiLanguageCode.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showKeyDialog by remember { mutableStateOf(false) }
    var keyInput by remember(customApiKey) { mutableStateOf(customApiKey) }

    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            title = {
                Text(
                    text = "AI Engine & Google Search Settings",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "KodeMamas AI connects online Google Search Database grounding with Google Gemini AI models to provide verified real-time answers.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )

                    // Connectivity Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF380A60))
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF00E676) else Color(0xFFFF9800))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOnline) "Network: Online (Web Grounding Active)" else "Network: Offline (Local Engine Mode)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Google Search Grounding Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Search Grounding",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFC107)
                            )
                            Text(
                                text = "Query Google's live database for real-time sources",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = isGoogleSearchEnabled,
                            onCheckedChange = { viewModel.toggleGoogleSearch(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFC107),
                                checkedTrackColor = Color(0xFF6B27A8)
                            )
                        )
                    }

                    // Gemini Model Selector
                    Text(
                        text = "Active Gemini Model",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            GeminiService.MODEL_GEMINI_3_5_FLASH to "3.5 Flash",
                            GeminiService.MODEL_GEMINI_3_1_PRO to "3.1 Pro",
                            GeminiService.MODEL_GEMINI_FLASH_LATEST to "Flash Latest"
                        ).forEach { (modelId, label) ->
                            FilterChip(
                                selected = selectedModel == modelId,
                                onClick = { viewModel.setSelectedGeminiModel(modelId) },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFC107),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF380A60),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Text(
                        text = if (customApiKey.isNotBlank()) "✅ Custom Gemini API Key configured" else "⚡ Using AI Studio / Cloud Gemini Key",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (customApiKey.isNotBlank()) Color(0xFF00E676) else Color(0xFFFFC107)
                    )
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Custom Gemini API Key (Optional)", color = Color(0xFFFFC107)) },
                        placeholder = { Text("Paste AI Studio API key here...", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFC107),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color(0xFFFFC107)
                        )
                    )
                    Text(
                        text = "Leave empty to use the built-in Gemini Intelligence Engine, or enter your personal Gemini API key.",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCustomApiKey(keyInput)
                        showKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        keyInput = ""
                        viewModel.setCustomApiKey("")
                        showKeyDialog = false
                    }
                ) {
                    Text("Clear / Default", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = Color(0xFF26053D)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF2E094E))
            .border(1.dp, Color(0xFF4D177E), RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Helper Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🤖", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "KodeMamas AI Assistant",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Google Search Database • Gemini AI Online",
                        color = Color(0xFFFFC107),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = { showKeyDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "AI Settings",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { viewModel.clearAiMessages() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Chat",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status & Search Grounding Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF380A60))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF00E676) else Color(0xFFFF9800))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOnline) "Online" else "Offline",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedModel == GeminiService.MODEL_GEMINI_3_1_PRO) "Gemini 3.1 Pro" else "Gemini 3.5 Flash",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Google Search Grounding Toggle Pill
                FilterChip(
                    selected = isGoogleSearchEnabled,
                    onClick = { viewModel.toggleGoogleSearch() },
                    label = {
                        Text(
                            text = if (isGoogleSearchEnabled) "Google Search: ON" else "Google Search: OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFC107),
                        selectedLabelColor = Color.Black,
                        selectedLeadingIconColor = Color.Black,
                        containerColor = Color.White.copy(alpha = 0.1f),
                        labelColor = Color.White.copy(alpha = 0.8f),
                        iconColor = Color.White.copy(alpha = 0.8f)
                    ),
                    border = null
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 12 South African Official Languages Selector Bar (Removing the English Barrier)
            val saLanguages = listOf(
                "auto" to "🌍 Auto-Detect (All 12 SA)",
                "zu" to "🇿🇦 isiZulu",
                "xh" to "🇿🇦 isiXhosa",
                "af" to "🇿🇦 Afrikaans",
                "nso" to "🇿🇦 Sepedi",
                "tn" to "🇿🇦 Setswana",
                "st" to "🇿🇦 Sesotho",
                "ts" to "🇿🇦 Xitsonga",
                "ss" to "🇿🇦 siSwati",
                "ve" to "🇿🇦 Tshivenda",
                "nr" to "🇿🇦 isiNdebele",
                "sasl" to "🤟 SASL (Sign)",
                "en" to "🇬🇧 English"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                items(saLanguages) { (code, label) ->
                    val isSelected = aiLanguageCode == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setAiLanguageCode(code) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFC107),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF380A60),
                            labelColor = Color.White.copy(alpha = 0.9f)
                        ),
                        border = if (isSelected) BorderStroke(1.dp, Color(0xFFFFC107)) else null
                    )
                }
            }

            // Quick Search / Prompt Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                item {
                    SuggestionChip(
                        onClick = {
                            viewModel.sendAiChat("Chaza ukuthi i-AI isebenza kanjani ngazo zonke izilimi eziyi-12 zokuhlela amakhompyutha nobuchwepheshe eNingizimu Afrika ngaphandle kwesiNgisi")
                        },
                        label = { Text("🇿🇦 Zonke Izilimi Eziyi-12", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4A1878))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            viewModel.sendAiChat("Sawubona! Ungichazele ukuthi yini ama-variables nama-loops nge-Python ngesiZulu")
                        },
                        label = { Text("🇿🇦 isiZulu: Python & Loops", fontSize = 11.sp, color = Color.White) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4A1878))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            viewModel.sendAiChat("Molo! Ndicacisele ukuba lisebenza njani ifom ye-HTML ne-CSS ngesiXhosa")
                        },
                        label = { Text("🇿🇦 isiXhosa: HTML & CSS", fontSize = 11.sp, color = Color.White) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4A1878))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            viewModel.sendAiChat("Explain visual coding gestures and SASL sign gloss for deaf South African tech learners")
                        },
                        label = { Text("🤟 SASL: Sign Language Code", fontSize = 11.sp, color = Color.White) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4A1878))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            viewModel.sendAiChat("Search Google database for South African tech internships and coding bursaries 2026")
                        },
                        label = { Text("🇿🇦 SA Tech Internships 2026", fontSize = 11.sp, color = Color.White) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4A1878))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            viewModel.sendAiChat("Search Google database: What are the best practices for building a responsive web app for township spaza shops?")
                        },
                        label = { Text("🛒 Spaza Shop Web App", fontSize = 11.sp, color = Color.White) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4A1878))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            viewModel.sendAiChat("Search Google database: Latest features in Python 3.12 and practical coding examples")
                        },
                        label = { Text("🐍 Python 3.12 Features", fontSize = 11.sp, color = Color.White) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4A1878))
                    )
                }
                item {
                    SuggestionChip(
                        onClick = {
                            viewModel.sendAiChat("Explain systems requests feasibility analysis in simple terms with an IT steering committee example")
                        },
                        label = { Text("💡 Systems Feasibility", fontSize = 11.sp, color = Color.White) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4A1878))
                    )
                }
            }

            // Chat Messages Container
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chats) { msg ->
                    if (msg.isUser) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF4A1878))
                                    .border(1.dp, Color(0xFF6B27A8), RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.messageText,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        // Assistant Message: Clean white card with formatted Markdown and Google Search Grounding badge
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(14.dp)
                            ) {
                                FormattedChatMessage(text = msg.messageText)
                            }
                        }
                    }
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color(0xFFFFC107),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOnline && isGoogleSearchEnabled) "Searching Google database & consulting Gemini AI..." else "Mama AI is generating answer...",
                                color = Color(0xFFFFC107),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Capsule Input Row with Search and Send buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Ask anything or search Google database...",
                            color = Color(0xFF757575),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF26053D)
                    ),
                    shape = RoundedCornerShape(25.dp),
                    singleLine = true
                )

                // Quick Google Database Search action button
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.searchGoogleDatabase(textInput.trim())
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC107))
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Google Database",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Standard Chat Send button
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendAiChat(textInput.trim())
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF380A60))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------------- TAB: BUILDS / SANDBOX ----------------------
@Composable
fun BuildsTab(viewModel: MainViewModel, langCode: String) {
    val editorText by viewModel.editorText.collectAsState()
    val simulatorOutput by viewModel.simulatorOutput.collectAsState()
    val simulatorSuccess by viewModel.simulatorSuccess.collectAsState()

    var selectedLang by remember { mutableStateOf("HTML") }

    LaunchedEffect(selectedLang) {
        if (editorText.isEmpty()) {
            when (selectedLang) {
                "HTML" -> viewModel.updateEditorText("<h1>Mam's Spaza Shop</h1>\n<p>Fresh daily baked bread & milk</p>\n<ul>\n  <li>Blue Ribbon Bread - R18.50</li>\n  <li>Clover Milk - R16.00</li>\n</ul>")
                "CSS" -> viewModel.updateEditorText("body {\n  background-color: #0C0714;\n  color: #FFD700;\n  font-family: sans-serif;\n}")
                "JavaScript" -> viewModel.updateEditorText("function calculateTotal(breadQty, milkQty) {\n  const breadPrice = 18.50;\n  const milkPrice = 16.00;\n  return (breadQty * breadPrice) + (milkQty * milkPrice);\n}\nconsole.log('R' + calculateTotal(2, 3));")
                "Python" -> viewModel.updateEditorText("temp = 32\nif temp > 30:\n    print('Warning: High Heat! Increase irrigation x2.')\nelse:\n    print('Normal climate. Maintain standard water flow.')")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Builds Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF2E094E))
                .border(1.dp, Color(0xFF4D177E), RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ThemeGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF26053D), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "KodeMamas Code Builds",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Interactive Mobile Compiler & Sandbox",
                            color = ThemeGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Build real-world projects for township businesses, agriculture, and schools right from your phone — offline or online.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Language Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("HTML", "CSS", "JavaScript", "Python").forEach { lang ->
                val isSelected = selectedLang == lang
                Button(
                    onClick = {
                        selectedLang = lang
                        when (lang) {
                            "HTML" -> viewModel.updateEditorText("<h1>Mam's Spaza Shop</h1>\n<p>Fresh daily baked bread & milk</p>\n<ul>\n  <li>Blue Ribbon Bread - R18.50</li>\n  <li>Clover Milk - R16.00</li>\n</ul>")
                            "CSS" -> viewModel.updateEditorText("body {\n  background-color: #0C0714;\n  color: #FFD700;\n  font-family: sans-serif;\n}")
                            "JavaScript" -> viewModel.updateEditorText("function calculateTotal(breadQty, milkQty) {\n  const breadPrice = 18.50;\n  const milkPrice = 16.00;\n  return (breadQty * breadPrice) + (milkQty * milkPrice);\n}\nconsole.log('R' + calculateTotal(2, 3));")
                            "Python" -> viewModel.updateEditorText("temp = 32\nif temp > 30:\n    print('Warning: High Heat! Increase irrigation x2.')\nelse:\n    print('Normal climate. Maintain standard water flow.')")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) ThemeGold else Color(0xFF350B56)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(
                        text = if (lang == "JavaScript") "JS" else lang,
                        color = if (isSelected) Color(0xFF26053D) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Editor widget
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ThemeDarkBg)
                .border(1.dp, Color(0xFF4D177E), RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$selectedLang LIVE SCRIPT",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
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

            TextField(
                value = editorText,
                onValueChange = { viewModel.updateEditorText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                textStyle = TextStyle(
                    color = ThemeGold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = ThemeGold,
                    unfocusedTextColor = ThemeGold,
                    focusedContainerColor = Color(0xFF130D1E),
                    unfocusedContainerColor = Color(0xFF130D1E),
                    cursorColor = ThemeGold,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.runSimulatorCode() },
                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Run Code in Mobile Simulator", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Compiler output
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
                            text = if (simulatorSuccess) "OUTPUT GENERATED" else "COMPILER NOTICE",
                            fontWeight = FontWeight.Black,
                            color = if (simulatorSuccess) Color(0xFF2B8A3E) else Color(0xFFC92A2A),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = simulatorOutput,
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ---------------------- TAB 4: COMMUNITY SECTION ----------------------
@Composable
fun CommunityTab(viewModel: MainViewModel, langCode: String) {
    var selectedCommunitySubTab by remember { mutableStateOf(0) } // 0: Firebase People Database, 1: Forum Circle
    val posts by viewModel.allPosts.collectAsState()
    var postInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Subtab Switcher: Firebase People DB vs Township Forum Circle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEDE7F6))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedCommunitySubTab == 0) ThemeIndigo else Color.Transparent)
                    .clickable { selectedCommunitySubTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "People in Tech (Firebase 🇿🇦)",
                    color = if (selectedCommunitySubTab == 0) Color.White else Color(0xFF4A148C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedCommunitySubTab == 1) ThemeIndigo else Color.Transparent)
                    .clickable { selectedCommunitySubTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Township Forum",
                    color = if (selectedCommunitySubTab == 1) Color.White else Color(0xFF4A148C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        if (selectedCommunitySubTab == 0) {
            // Live Firebase People Database Hub
            FirebasePeopleHub(
                viewModel = viewModel,
                langCode = langCode,
                onConnectWithPerson = { person ->
                    viewModel.selectTab("mentorship")
                }
            )
        } else {
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
}
}

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
