package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MentorChat
import com.example.ui.theme.ThemeCardBorder
import com.example.ui.theme.ThemeDarkBg
import com.example.ui.theme.ThemeGold
import com.example.ui.theme.ThemeIndigo

@Composable
fun MentorshipCareerHub(viewModel: MainViewModel, langCode: String) {
    val profile by viewModel.userProfile.collectAsState()
    val isTyping by viewModel.mentorTyping.collectAsState()
    val mentorChats by viewModel.mentorChats.collectAsState()
    val currentPlan by viewModel.currentPlanTier.collectAsState()
    val paymentRef by viewModel.paymentReference.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Navigation state inside Mentorship / Career hub
    var currentScreen by remember { mutableStateOf("menu") }
    // Possible values:
    // "menu", "plans", "advisor", "matching", "coaching", "cv",
    // "portfolio", "linkedin", "interview", "mock_interview",
    // "readiness", "reviews", "opportunities", "resources"

    var showPlansDialog by remember { mutableStateOf(false) }

    if (showPlansDialog) {
        SubscriptionPlansDialog(
            viewModel = viewModel,
            onDismiss = { showPlansDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        when (currentScreen) {
            "menu" -> {
                MentorshipMainMenu(
                    profile = profile,
                    currentPlan = currentPlan,
                    onNavigate = { currentScreen = it },
                    onOpenPlansDialog = { showPlansDialog = true },
                    onCopyCapitecAccount = {
                        clipboardManager.setText(AnnotatedString("2121743886"))
                        Toast.makeText(context, "Capitec Account 2121743886 copied! 🇿🇦", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            "plans" -> {
                CareerSubScreenHeader(title = "Membership Plans & Banking", onBack = { currentScreen = "menu" })
                SubscriptionPlansContent(viewModel = viewModel)
            }
            "advisor" -> {
                OneOnOneMentorChatScreen(
                    mentorChats = mentorChats,
                    isTyping = isTyping,
                    onSendMessage = { viewModel.sendMentorChat(it) },
                    onBack = { currentScreen = "menu" }
                )
            }
            "matching" -> {
                MentorMatchingScreen(viewModel = viewModel, onBack = { currentScreen = "menu" })
            }
            "coaching" -> {
                CareerCoachingScreen(onBack = { currentScreen = "menu" })
            }
            "cv" -> {
                CvBuilderScreen(
                    initialName = profile?.name ?: "Nokwazi Nobuhle Xaba",
                    onBack = { currentScreen = "menu" }
                )
            }
            "portfolio" -> {
                PortfolioBuilderScreen(
                    initialName = profile?.name ?: "Nokwazi Nobuhle Xaba",
                    onBack = { currentScreen = "menu" }
                )
            }
            "linkedin" -> {
                LinkedInAssistanceScreen(onBack = { currentScreen = "menu" })
            }
            "interview" -> {
                InterviewPrepScreen(onBack = { currentScreen = "menu" })
            }
            "mock_interview" -> {
                MockInterviewSimulatorScreen(onBack = { currentScreen = "menu" })
            }
            "readiness" -> {
                InternshipJobReadinessScreen(onBack = { currentScreen = "menu" })
            }
            "reviews" -> {
                ProjectReviewsScreen(onBack = { currentScreen = "menu" })
            }
            "opportunities" -> {
                OpportunitiesPortalScreen(onBack = { currentScreen = "menu" })
            }
            "resources" -> {
                AdditionalCareerResourcesScreen(onBack = { currentScreen = "menu" })
            }
            else -> {
                MentorshipMainMenu(
                    profile = profile,
                    currentPlan = currentPlan,
                    onNavigate = { currentScreen = it },
                    onOpenPlansDialog = { showPlansDialog = true },
                    onCopyCapitecAccount = {
                        clipboardManager.setText(AnnotatedString("2121743886"))
                        Toast.makeText(context, "Capitec Account 2121743886 copied! 🇿🇦", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// ---------------------- SUB SCREEN HEADER ----------------------
@Composable
fun CareerSubScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ThemeIndigo)
        }
        Text(
            text = title,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            color = Color.Black
        )
    }
}

// ---------------------- MAIN MENU HUB ----------------------
@Composable
fun MentorshipMainMenu(
    profile: com.example.data.UserProfile?,
    currentPlan: String,
    onNavigate: (String) -> Unit,
    onOpenPlansDialog: () -> Unit,
    onCopyCapitecAccount: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Mentorship & Career Suite 🌟",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            Text(
                text = "Premium African tech careers, 1-on-1 mentorship, CV & portfolio tools, and Capitec-powered access.",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }

        // Current Plan & Capitec Bank Summary Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(ThemeDarkBg)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ThemeGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (currentPlan == "PREMIUM") "👑" else "⭐", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (currentPlan) {
                                        "PREMIUM" -> "Premium Member (R299/yr)"
                                        "STANDARD" -> "Standard Member (R99/yr)"
                                        else -> "Free Learner Status"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (currentPlan == "PREMIUM") "ALL 12 CAREER SERVICES UNLOCKED" else "Upgrade to unlock full career tools",
                                    color = ThemeGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Status Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentPlan != "FREE") Color(0xFF2E7D32) else Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (currentPlan != "FREE") "ACTIVE" else "FREE TRIAL",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Capitec Account Quick Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💳", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Capitec Bank Acc: 2121743886", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Standard: R99/yr • Premium: R299/yr", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                                }
                            }

                            Button(
                                onClick = onCopyCapitecAccount,
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenPlansDialog,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View All Plans (R99/R299)", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { onNavigate("plans") },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Banking Info", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "12 Premium Career Modules 🚀",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Tailored for township matriculants, mothers, and career switchers entering tech:",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // 12 Core Premium Features explicitly listed in the user prompt:
        val careerFeatures = listOf(
            CareerFeatureItem("advisor", "One-on-one tech mentorship", "Direct 1-on-1 chat with solo founder & tech mentor Nokwazi Nobuhle Xaba (Bloemfontein)", Icons.Default.ChatBubble, Color(0xFFE8EAF6), Color(0xFF3F51B5)),
            CareerFeatureItem("matching", "Mentor matching system", "Connect directly with solo founder Nokwazi Nobuhle Xaba in Bloemfontein", Icons.Default.People, Color(0xFFF3E5F5), Color(0xFF9C27B0)),
            CareerFeatureItem("coaching", "Career coaching & guidance", "Strategic roadmaps, digital skills milestones, and transition advice", Icons.Default.Explore, Color(0xFFE0F7FA), Color(0xFF0097A7)),
            CareerFeatureItem("cv", "CV & Resume Builder", "Localized South African developer resume generator with instant copy", Icons.Default.Badge, Color(0xFFE8F5E9), Color(0xFF2E7D32)),
            CareerFeatureItem("portfolio", "Portfolio Builder", "Build an impressive developer portfolio showing real projects & code", Icons.Default.Work, Color(0xFFFFF3E0), Color(0xFFEF6C00)),
            CareerFeatureItem("linkedin", "LinkedIn profile assistance", "Optimized headlines, summaries, and keywords for SA tech recruiters", Icons.Default.Share, Color(0xFFE1F5FE), Color(0xFF0288D1)),
            CareerFeatureItem("interview", "Interview preparation", "Township interview flashcards & technical reasoning guides", Icons.Default.QuestionAnswer, Color(0xFFFCE4EC), Color(0xFFC2185B)),
            CareerFeatureItem("mock_interview", "Mock interviews simulator", "Practice live technical & behavioral questions with real-time feedback", Icons.Default.RecordVoiceOver, Color(0xFFEDE7F6), Color(0xFF673AB7)),
            CareerFeatureItem("readiness", "Internship/job readiness", "Readiness audit checklist, etiquette, and SA tech salary expectations", Icons.Default.FactCheck, Color(0xFFEFEBE9), Color(0xFF5D4037)),
            CareerFeatureItem("reviews", "Project reviews", "Submit code repositories for review by solo founder Nokwazi Nobuhle Xaba", Icons.Default.RateReview, Color(0xFFF9FBE7), Color(0xFF827717)),
            CareerFeatureItem("opportunities", "Opportunities & jobs portal", "Curated SA tech internships, learnerships, and graduate programmes", Icons.Default.BusinessCenter, Color(0xFFE0F2F1), Color(0xFF00796B)),
            CareerFeatureItem("resources", "Additional career resources", "Downloadable syntax cheat sheets, cold email templates, and glossary", Icons.Default.LibraryBooks, Color(0xFFFFF8E1), Color(0xFFF57F17))
        )

        items(careerFeatures) { feature ->
            CareerFeatureCard(
                item = feature,
                onClick = { onNavigate(feature.id) }
            )
        }
    }
}

data class CareerFeatureItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color
)

@Composable
fun CareerFeatureCard(item: CareerFeatureItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, ThemeCardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = item.iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ThemeIndigo, modifier = Modifier.size(20.dp))
        }
    }
}

// ---------------------- 1. 1-ON-1 TECH MENTOR CHAT ----------------------
@Composable
fun OneOnOneMentorChatScreen(
    mentorChats: List<MentorChat>,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    var chatText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        CareerSubScreenHeader(title = "1-on-1 Mentor Chat 👩🏾‍💼", onBack = onBack)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEDE7F6))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Connected with Solo Founder & Tech Mentor Nokwazi Nobuhle Xaba (Bloemfontein). Ask in your home language!",
                    fontSize = 11.sp,
                    color = ThemeIndigo,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp),
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
                            .fillMaxWidth(0.82f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isUs) ThemeIndigo else Color.White)
                            .border(1.dp, ThemeCardBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = chat.messageText,
                            color = if (isUs) Color.White else Color.Black,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (isTyping) {
                item {
                    Text(
                        text = "Mentor is typing reply...",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = chatText,
                onValueChange = { chatText = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = { Text("Ask mentor about coding, careers, or CV...", fontSize = 12.sp) }
            )

            IconButton(
                onClick = {
                    if (chatText.isNotBlank()) {
                        onSendMessage(chatText)
                        chatText = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ThemeIndigo)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ---------------------- 2. MENTOR MATCHING SCREEN ----------------------
@Composable
fun MentorMatchingScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cloudMentors by viewModel.allMentors.collectAsState()
    var matchedMentor by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "Tech Mentor Matching 🤝", onBack = onBack)

        Text(
            text = "Connect directly with solo founder and tech mentor Nokwazi Nobuhle Xaba in Bloemfontein, or registered members from the Firebase Database. Receive 1-on-1 guidance in your home language.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )

        matchedMentor?.let { name ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F5E9))
                    .border(1.dp, Color(0xFF81C784), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎉", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Matched with $name!", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFF2E7D32))
                        Text("Introduction message dispatched. Your mentor will connect in the 1-on-1 chat.", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Available Tech Mentors (${cloudMentors.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEDE7F6))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("Firebase Synced ☁️", fontSize = 9.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
            }
        }

        cloudMentors.forEach { m ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ThemeCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(m.name, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.Black)
                                if (m.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = ThemeIndigo, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text("${m.role} • ${m.townshipOrCity}", fontSize = 11.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3E5F5))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(m.primaryLanguage, fontSize = 9.sp, color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(m.bio, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Skills: ${m.skills}", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            matchedMentor = m.name
                            Toast.makeText(context, "Matched with ${m.name}! Check 1-on-1 Mentor Chat.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Request Mentor Match", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ---------------------- 3. CAREER COACHING SCREEN ----------------------
@Composable
fun CareerCoachingScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "Career Coaching & Guidance 🧭", onBack = onBack)

        Text(
            text = "Personalized milestones guiding township matriculants & mothers to their first junior developer role.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )

        val stages = listOf(
            Triple("Stage 1: Foundation (Weeks 1-4)", "Master HTML5, semantic markup, and CSS styling. Build your first responsive spaza shop storefront website.", "COMPLETED ✅"),
            Triple("Stage 2: Logic & Automation (Weeks 5-8)", "Learn JavaScript fundamentals, variables, loops, and Python scripts. Complete 20 daily coding challenges.", "IN PROGRESS ⏳"),
            Triple("Stage 3: Offline Mobile & Git (Weeks 9-12)", "Learn version control with GitHub, offline mobile caching, and building localized apps.", "UPCOMING 🔒"),
            Triple("Stage 4: Job Application & Portfolio (Weeks 13-16)", "Construct professional developer CV, LinkedIn profile, and participate in mock technical interviews.", "UPCOMING 🔒")
        )

        stages.forEach { (stage, desc, status) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ThemeCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stage, fontWeight = FontWeight.Black, fontSize = 13.sp, color = ThemeIndigo)
                        Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (status.contains("COMPLETED")) Color(0xFF2E7D32) else ThemeGold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(desc, fontSize = 11.sp, color = Color.DarkGray, lineHeight = 15.sp)
                }
            }
        }
    }
}

// ---------------------- 4. CV BUILDER SCREEN ----------------------
@Composable
fun CvBuilderScreen(initialName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var name by remember { mutableStateOf(initialName) }
    var location by remember { mutableStateOf("Bloemfontein, Free State") }
    var targetRole by remember { mutableStateOf("Junior Frontend & Mobile Developer") }
    var skills by remember { mutableStateOf("HTML5, CSS3, JavaScript, Python, Offline Mobile Architecture, Git") }
    var education by remember { mutableStateOf("KodeMamas Coding Academy (Multilingual Certificate in Tech)") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CareerSubScreenHeader(title = "CV & Resume Builder 📄", onBack = onBack)

        Text(
            text = "Generate a clean, high-impact developer CV tailored for South African recruiters and tech companies.",
            fontSize = 11.sp,
            color = Color.Gray
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location (Township/City, Province)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = targetRole,
            onValueChange = { targetRole = it },
            label = { Text("Target Role") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = skills,
            onValueChange = { skills = it },
            label = { Text("Technical Skills (comma separated)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = education,
            onValueChange = { education = it },
            label = { Text("Education & Credentials") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // CV Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.5.dp, ThemeIndigo, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(name.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
                Text("$targetRole • $location", fontSize = 11.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ThemeCardBorder)

                Text("PROFESSIONAL SUMMARY:", fontWeight = FontWeight.Black, fontSize = 10.sp, color = ThemeIndigo)
                Text(
                    "Dedicated South African tech learner trained through KodeMamas offline-first academy. Skilled in $skills with strong problem-solving and community collaboration ethos.",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("EDUCATION & CERTIFICATIONS:", fontWeight = FontWeight.Black, fontSize = 10.sp, color = ThemeIndigo)
                Text(education, fontSize = 11.sp, color = Color.DarkGray)

                Spacer(modifier = Modifier.height(8.dp))
                Text("CORE SKILLS:", fontWeight = FontWeight.Black, fontSize = 10.sp, color = ThemeIndigo)
                Text(skills, fontSize = 11.sp, color = Color.DarkGray)
            }
        }

        Button(
            onClick = {
                val cvText = """
                    ${name.uppercase()}
                    $targetRole - $location
                    
                    PROFESSIONAL SUMMARY:
                    Dedicated South African tech learner trained through KodeMamas offline-first academy. Skilled in $skills.
                    
                    EDUCATION:
                    $education
                    
                    CORE SKILLS:
                    $skills
                """.trimIndent()
                clipboardManager.setText(AnnotatedString(cvText))
                Toast.makeText(context, "CV copied to clipboard! Ready to send to recruiters.", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy Complete CV Text", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------- 5. PORTFOLIO BUILDER SCREEN ----------------------
@Composable
fun PortfolioBuilderScreen(initialName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "Developer Portfolio Builder 💻", onBack = onBack)

        Text(
            text = "Showcase your real-world coding projects, GitHub repositories, and live demo links to prospective employers.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )

        val projects = listOf(
            Triple("Township Spaza Shop Order System", "HTML, CSS, JavaScript web app allowing community members to pre-order bread and milk offline.", "github.com/kodemamas/spaza-shop-app"),
            Triple("Taxi Fare Calculator & Transit Route", "Python script computing South African minibus taxi change and seat passenger totals.", "github.com/kodemamas/taxi-fare-python"),
            Triple("Stokvel Savings Tracker Dashboard", "Interactive CSS grid dashboard visualizing monthly grocery collective payouts.", "github.com/kodemamas/stokvel-dashboard")
        )

        projects.forEach { (title, tech, repo) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ThemeCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(title, fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Black)
                    Text(tech, fontSize = 11.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(repo, fontSize = 10.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Live demo link opened: $repo", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Project Code & Live Preview", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

// ---------------------- 6. LINKEDIN PROFILE ASSISTANCE ----------------------
@Composable
fun LinkedInAssistanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "LinkedIn Profile Assistance 💼", onBack = onBack)

        Text(
            text = "Pre-written, optimized headlines and summaries crafted for South African tech recruiters searching on LinkedIn.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )

        val headlineText = "Junior Software Developer | HTML, CSS, JavaScript, Python | KodeMamas Academy Graduate | Passionate about Digital Inclusion in SA"
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ThemeCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("RECOMMENDED HEADLINE:", fontWeight = FontWeight.Black, fontSize = 11.sp, color = ThemeIndigo)
                Spacer(modifier = Modifier.height(4.dp))
                Text(headlineText, fontSize = 12.sp, color = Color.Black, lineHeight = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(headlineText))
                        Toast.makeText(context, "Headline copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Copy Headline", fontSize = 11.sp)
                }
            }
        }

        val aboutText = "I am an enthusiastic entry-level software developer who learned to code through KodeMamas, mastering HTML, CSS, JavaScript, and Python. As someone from South Africa, I believe technology must empower every community. I bring strong problem solving, attention to detail, and a hunger to contribute to high-performing engineering teams."
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ThemeCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("RECOMMENDED ABOUT / BIO SECTION:", fontWeight = FontWeight.Black, fontSize = 11.sp, color = ThemeIndigo)
                Spacer(modifier = Modifier.height(4.dp))
                Text(aboutText, fontSize = 12.sp, color = Color.Black, lineHeight = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(aboutText))
                        Toast.makeText(context, "Bio copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Copy Bio", fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------------------- 7. INTERVIEW PREP SCREEN ----------------------
@Composable
fun InterviewPrepScreen(onBack: () -> Unit) {
    var selectedQuestionIndex by remember { mutableStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    val questions = listOf(
        Pair("What is the difference between an inline element and a block element in HTML?", "Block elements (like <div>, <p>, <h1>) take up the full available width and start on a new line. Inline elements (like <span>, <a>, <strong>) only take up as much width as necessary and do not start on a new line."),
        Pair("How do you store and retrieve data in Python using dictionaries?", "A Python dictionary uses key-value pairs e.g. person = {'name': 'Mama', 'city': 'Bloemfontein'}. You access values using person['name'] or person.get('city')."),
        Pair("What is the CSS Box Model?", "The CSS Box Model consists of content, padding (space inside the border), border (edge line), and margin (space outside the border)."),
        Pair("How would you explain what an API is to someone who has never coded?", "An API (Application Programming Interface) is like a restaurant waiter. You (the client) give your order to the waiter (the API), who goes to the kitchen (the server/database) and brings back your food (the data).")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "Interview Preparation 🎯", onBack = onBack)

        Text("Question Flashcard (${selectedQuestionIndex + 1}/${questions.size}):", fontSize = 12.sp, color = Color.Gray)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, ThemeIndigo)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = questions[selectedQuestionIndex].first,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    lineHeight = 20.sp
                )

                if (showAnswer) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = ThemeCardBorder)
                    Text("EXPLANATION & TALKING POINTS:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = ThemeIndigo)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = questions[selectedQuestionIndex].second,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showAnswer = !showAnswer }) {
                        Text(if (showAnswer) "Hide Answer" else "Reveal Answer 💡", color = ThemeIndigo, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            selectedQuestionIndex = (selectedQuestionIndex + 1) % questions.size
                            showAnswer = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Next Question", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ---------------------- 8. MOCK INTERVIEWS SIMULATOR ----------------------
@Composable
fun MockInterviewSimulatorScreen(onBack: () -> Unit) {
    var currentStep by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var feedback by remember { mutableStateOf("") }

    val steps = listOf(
        MockQuestion(
            "Behavioral: 'Tell me about a time you ran into a bug in your code and how you resolved it.'",
            listOf(
                "I gave up and asked someone else to write the entire code for me.",
                "I isolated the bug using print/console.log, reviewed syntax, and tested small sections iteratively until it worked.",
                "I deleted the entire app and started over from zero without checking logs."
            ),
            1,
            "Spot on! Employers look for methodical debugging, systematic isolation of errors, and perseverance."
        ),
        MockQuestion(
            "Technical: 'In JavaScript, what will `typeof [1, 2, 3]` return?'",
            listOf(
                "'array'",
                "'object'",
                "'number'"
            ),
            1,
            "Correct! In JavaScript, arrays are technically specialized objects. Recognizing this is a classic junior interview question."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "Mock Interview Simulator 🎙️", onBack = onBack)

        val q = steps[currentStep]

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, ThemeCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("QUESTION ${currentStep + 1}:", fontWeight = FontWeight.Black, fontSize = 10.sp, color = ThemeIndigo)
                Spacer(modifier = Modifier.height(4.dp))
                Text(q.prompt, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                Spacer(modifier = Modifier.height(12.dp))

                q.options.forEachIndexed { index, opt ->
                    val isSelected = selectedOption == index
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ThemeIndigo.copy(alpha = 0.08f) else Color(0xFFF9F9FB))
                            .border(1.dp, if (isSelected) ThemeIndigo else ThemeCardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                selectedOption = index
                                feedback = if (index == q.correctIndex) q.feedback else "Try another option that emphasizes professional engineering practices."
                            }
                            .padding(12.dp)
                    ) {
                        Text(opt, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }

                if (feedback.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(10.dp)
                    ) {
                        Text(feedback, fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        if (currentStep < steps.size - 1) {
            Button(
                onClick = {
                    currentStep++
                    selectedOption = null
                    feedback = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Proceed to Next Mock Question", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class MockQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val feedback: String
)

// ---------------------- 9. INTERNSHIP & JOB READINESS ----------------------
@Composable
fun InternshipJobReadinessScreen(onBack: () -> Unit) {
    val items = listOf(
        Pair("Git & GitHub Proficiency", "Can clone, commit, push, create branches, and write clear commit messages."),
        Pair("Semantic HTML & CSS Responsive Layouts", "Can turn a mobile Figma/sketch design into clean code without breaking on low-end screens."),
        Pair("Foundational JavaScript or Python Logic", "Understands functions, conditionals, arrays, and basic API consumption."),
        Pair("Clean South African Resume & LinkedIn", "Profile updated with projects, certificates, and clear contact information."),
        Pair("Understanding of South African Tech Market", "Familiarity with junior salaries (R15k - R30k/month average in JHB/CPT) and B-BBEE digital skills initiatives.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "Internship & Job Readiness 📋", onBack = onBack)

        Text(
            text = "Audit your preparation before interviewing for junior software developer roles or graduate programmes in South Africa.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )

        items.forEach { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ThemeCardBorder)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Black)
                        Text(desc, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

// ---------------------- 10. PROJECT REVIEWS SCREEN ----------------------
@Composable
fun ProjectReviewsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var repoUrl by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "Technical Project Reviews 🔍", onBack = onBack)

        Text(
            text = "Submit your GitHub repository or offline project archive for detailed code review by senior engineers.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )

        OutlinedTextField(
            value = repoUrl,
            onValueChange = { repoUrl = it },
            label = { Text("GitHub Repository URL or Project Title") },
            placeholder = { Text("https://github.com/myname/my-project") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("What specific feedback do you need?") },
            placeholder = { Text("e.g. Code cleanliness, CSS responsiveness on small devices, etc.") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3
        )

        Button(
            onClick = {
                if (repoUrl.isNotBlank()) {
                    submitted = true
                    Toast.makeText(context, "Project submitted for review! Mentor assigned.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Submit for Code Review", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (submitted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(14.dp)
            ) {
                Text(
                    text = "✅ Project queued for review. Review comments will appear in your 1-on-1 Mentor Chat with Nokwazi Nobuhle Xaba within 24 hours.",
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ---------------------- 11. OPPORTUNITIES PORTAL ----------------------
@Composable
fun OpportunitiesPortalScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val opportunities = listOf(
        OpportunityItem("Capitec Tech Graduate Programme", "Capitec Bank • Stellenbosch / Remote", "Junior Software Engineers & Data Analysts (R25k/mo)", "Open for applications. Focus on digital inclusion and youth employment.", "APPLY"),
        OpportunityItem("Vodacom Digital Skills Learnership", "Vodacom SA • Midrand / Regional Hubs", "Web Development & IT Support Stipend Learnership", "Includes national certificate and mentorship for female matriculants.", "APPLY"),
        OpportunityItem("Takealot Junior Developer Academy", "Takealot Group • Cape Town", "Junior Frontend (HTML/CSS/JS) & Warehouse Tech", "Seeking passionate self-taught developers and coding academy alumni.", "APPLY"),
        OpportunityItem("GirlCode South Africa Hackathon & Fellowship", "GirlCode SA • Hybrid / Nationwide", "Tech Fellowships & Cloud Computing Mentorship", "Annual hackathons with cash prizes and corporate internship placement.", "APPLY")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "Opportunities & Jobs Portal 💼", onBack = onBack)

        Text(
            text = "Curated South African internships, graduate schemes, and bursaries welcoming non-traditional tech learners.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )

        opportunities.forEach { opp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ThemeCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(opp.title, fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Black)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ThemeGold.copy(alpha = 0.3f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE", color = Color(0xFF8C6D00), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Text(opp.company, fontSize = 11.sp, color = ThemeIndigo, fontWeight = FontWeight.Bold)
                    Text(opp.role, fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(opp.description, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Application packet for ${opp.title} sent to your email!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply / Request Referral", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class OpportunityItem(
    val title: String,
    val company: String,
    val role: String,
    val description: String,
    val action: String
)

// ---------------------- 12. ADDITIONAL CAREER RESOURCES ----------------------
@Composable
fun AdditionalCareerResourcesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val resources = listOf(
        Pair("HTML & CSS Township Cheat Sheet", "Instant reference for <div>, <section>, flexbox, grid, and mobile styling rules."),
        Pair("Python Syntax & Algorithms Guide", "Variables, dictionaries, loops, and basic functions with African analogies."),
        Pair("Cold Email Template to SA Tech Recruiters", "Respectful, professional outreach message to hiring managers on LinkedIn."),
        Pair("Multilingual Tech Glossary (12 Languages)", "Key computing terms translated into isiZulu, isiXhosa, Afrikaans, Sepedi, and SASL.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CareerSubScreenHeader(title = "Additional Career Resources 📚", onBack = onBack)

        Text(
            text = "Downloadable cheat sheets, email templates, and technical toolkits to accelerate your transition.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )

        resources.forEach { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ThemeCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ThemeIndigo.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = ThemeIndigo, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Black)
                        Text(desc, fontSize = 10.sp, color = Color.Gray, lineHeight = 14.sp)
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("$title\n$desc\nAvailable offline in KodeMamas."))
                            Toast.makeText(context, "$title saved offline!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Download", tint = ThemeIndigo)
                    }
                }
            }
        }
    }
}
