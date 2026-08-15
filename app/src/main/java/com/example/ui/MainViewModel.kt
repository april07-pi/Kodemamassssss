package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.StreakNotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = Repository(db)

    // 100-Day Onboarding Journey SharedPreferences & state
    private val sharedPrefs = application.getSharedPreferences("kodemamas_100day_prefs", android.content.Context.MODE_PRIVATE)
    
    private val _completedPhases = MutableStateFlow<Set<String>>(emptySet())
    val completedPhases: StateFlow<Set<String>> = _completedPhases.asStateFlow()
    
    private val _activeOnboardingWin = MutableStateFlow<OnboardingPhase?>(null)
    val activeOnboardingWin: StateFlow<OnboardingPhase?> = _activeOnboardingWin.asStateFlow()

    init {
        val saved = sharedPrefs.getStringSet("completed_onboarding_phases", emptySet()) ?: emptySet()
        _completedPhases.value = saved
    }

    // Lang State (Sync'd with the DB Profile & SharedPreferences for quick cold starts)
    private val _currentLanguageCode = MutableStateFlow(
        application.getSharedPreferences("kodemamas_100day_prefs", android.content.Context.MODE_PRIVATE)
            .getString("selected_language_code", "en") ?: "en"
    )
    val currentLanguageCode: StateFlow<String> = _currentLanguageCode.asStateFlow()

    // Screen navigation state: "home", "learn", "community", "mentorship", "profile"
    private val _selectedTab = MutableStateFlow("home")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    // Core Database Flows
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLessons: StateFlow<List<Lesson>> = repository.allLessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChallenges: StateFlow<List<CodingChallenge>> = repository.allChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPosts: StateFlow<List<DiscussionPost>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiChats: StateFlow<List<MentorChat>> = repository.aiChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mentorChats: StateFlow<List<MentorChat>> = repository.mentorChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBuddies: StateFlow<List<Buddy>> = repository.allBuddies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBuilds: StateFlow<List<ProblemBuild>> = repository.allBuilds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeBuild = MutableStateFlow<ProblemBuild?>(null)
    val activeBuild: StateFlow<ProblemBuild?> = _activeBuild.asStateFlow()

    fun selectBuild(build: ProblemBuild?) {
        _activeBuild.value = build
    }

    fun createProblemBuild(
        category: String,
        categoryIcon: String,
        title: String,
        problemStatement: String,
        targetUsers: String,
        currentSolution: String,
        currentSolutionFlaw: String,
        proposedTechSolution: String
    ) {
        viewModelScope.launch {
            val id = "build_" + System.currentTimeMillis()
            val newBuild = ProblemBuild(
                id = id,
                category = category,
                categoryIcon = categoryIcon,
                title = title,
                problemStatement = problemStatement,
                targetUsers = targetUsers,
                currentSolution = currentSolution,
                currentSolutionFlaw = currentSolutionFlaw,
                proposedTechSolution = proposedTechSolution,
                requiredSkills = "HTML,CSS,JavaScript,Forms,Basic Data,UI Design",
                discoverCompleted = true,
                defineCompleted = true,
                designCompleted = true,
                buildProgressPercent = 20,
                testCompleted = false,
                improveCompleted = false,
                showcaseCompleted = false
            )
            repository.insertBuild(newBuild)
            _activeBuild.value = newBuild
        }
    }

    fun updateBuildProgress(buildId: String, newPercent: Int) {
        viewModelScope.launch {
            repository.updateBuildProgress(buildId, newPercent)
        }
    }

    // Lesson Active States
    private val _currentActiveLesson = MutableStateFlow<Lesson?>(null)
    val currentActiveLesson: StateFlow<Lesson?> = _currentActiveLesson.asStateFlow()

    private val _currentActiveSteps = MutableStateFlow<List<LessonStep>>(emptyList())
    val currentActiveSteps: StateFlow<List<LessonStep>> = _currentActiveSteps.asStateFlow()

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

    // Live Code Simulator States
    private val _editorText = MutableStateFlow("")
    val editorText: StateFlow<String> = _editorText.asStateFlow()

    private val _simulatorOutput = MutableStateFlow("")
    val simulatorOutput: StateFlow<String> = _simulatorOutput.asStateFlow()

    private val _simulatorSuccess = MutableStateFlow(false)
    val simulatorSuccess: StateFlow<Boolean> = _simulatorSuccess.asStateFlow()

    // Quiz Navigation States
    private val _activeQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val activeQuizQuestions: StateFlow<List<QuizQuestion>> = _activeQuizQuestions.asStateFlow()

    // We can support 0..N questions in a lesson. We'll track the active question index.
    private val _quizQuestionIndex = MutableStateFlow(0)
    val quizQuestionIndex: StateFlow<Int> = _quizQuestionIndex.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow(-1)
    val selectedAnswerIndex: StateFlow<Int> = _selectedAnswerIndex.asStateFlow()

    private val _quizChecked = MutableStateFlow(false)
    val quizChecked: StateFlow<Boolean> = _quizChecked.asStateFlow()

    private val _quizCorrect = MutableStateFlow(false)
    val quizCorrect: StateFlow<Boolean> = _quizCorrect.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizFinished = MutableStateFlow(false)
    val quizFinished: StateFlow<Boolean> = _quizFinished.asStateFlow()

    // Network Status (Simulated Offline/Online Mode)
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Chat states
    private val _aiGenerating = MutableStateFlow(false)
    val aiGenerating: StateFlow<Boolean> = _aiGenerating.asStateFlow()

    private val _mentorTyping = MutableStateFlow(false)
    val mentorTyping: StateFlow<Boolean> = _mentorTyping.asStateFlow()

    // Simulated compilation & download progress flows
    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    // Selected challenge in Daily Challenge View
    private val _activeChallenge = MutableStateFlow<CodingChallenge?>(null)
    val activeChallenge: StateFlow<CodingChallenge?> = _activeChallenge.asStateFlow()

    // Buddy System State Flows
    private val _selectedBuddy = MutableStateFlow<Buddy?>(null)
    val selectedBuddy: StateFlow<Buddy?> = _selectedBuddy.asStateFlow()

    private val _activeBuddyMessages = MutableStateFlow<List<BuddyMessage>>(emptyList())
    val activeBuddyMessages: StateFlow<List<BuddyMessage>> = _activeBuddyMessages.asStateFlow()

    private var messagesJob: kotlinx.coroutines.Job? = null

    fun selectBuddyForChat(buddy: Buddy?) {
        _selectedBuddy.value = buddy
        messagesJob?.cancel()
        if (buddy != null) {
            messagesJob = viewModelScope.launch {
                repository.getMessagesForBuddy(buddy.id).collect {
                    _activeBuddyMessages.value = it
                }
            }
        } else {
            _activeBuddyMessages.value = emptyList()
        }
    }

    fun connectWithBuddy(buddyId: String, connected: Boolean) {
        viewModelScope.launch {
            repository.updateBuddyConnection(buddyId, connected)
            if (_selectedBuddy.value?.id == buddyId) {
                _selectedBuddy.value = _selectedBuddy.value?.copy(isConnected = connected)
            }
        }
    }

    private var isSendingBuddyMessage = false

    fun sendBuddyMessage(buddyId: String, text: String, sharedResourceTitle: String = "", sharedResourceCode: String = "") {
        val trimmed = text.trim()
        if (trimmed.isEmpty() && sharedResourceTitle.isEmpty()) return
        if (isSendingBuddyMessage) return
        isSendingBuddyMessage = true
        viewModelScope.launch {
            try {
                val msg = BuddyMessage(
                    buddyId = buddyId,
                    senderId = "me",
                    messageText = trimmed,
                    sharedResourceTitle = sharedResourceTitle,
                    sharedResourceCode = sharedResourceCode
                )
                repository.insertBuddyMessage(msg)
                
                // Highlight interactive responses in native/South African flavors to make them super immersive
                delay(1500)
                val replyText = when {
                    sharedResourceTitle.isNotEmpty() -> "Wow, thank you so much for sharing '${sharedResourceTitle}'! This will help me immensely in my storefront too. Siyabonga kakhulu!"
                    trimmed.lowercase().contains("molo") || trimmed.lowercase().contains("hello") || trimmed.lowercase().contains("yebo") || trimmed.lowercase().contains("dumelang") -> 
                        "Dumela! Thank you for connecting with me. How are your lessons going? Let's check our code and build something great! 🇿🇦"
                    trimmed.lowercase().contains("help") || trimmed.lowercase().contains("stuck") || trimmed.lowercase().contains("code") || trimmed.lowercase().contains("compiler") ->
                        "Don't worry sister! Double check your tags. Make sure your closing tags have the slash like </h1> or </ul>. I am always online to help review! 👩‍💻"
                    else -> "This is awesome! Let's make sure we keep up our daily streak and finish our next coding challenges together. Step by step!"
                }
                val replyMsg = BuddyMessage(
                    buddyId = buddyId,
                    senderId = buddyId,
                    messageText = replyText
                )
                repository.insertBuddyMessage(replyMsg)
            } finally {
                isSendingBuddyMessage = false
            }
        }
    }

    init {
        viewModelScope.launch {
            // Populate database if empty
            repository.prepopulateDatabaseIfEmpty()
            
            // Check & track daily streak on app launch
            val streak = repository.recordActivityAndIncrementStreak()

            // Pull the preferred language code from database and sync with preferences
            val savedPrefsLang = sharedPrefs.getString("selected_language_code", "en") ?: "en"
            userProfile.collect { profile ->
                profile?.let {
                    val dbLang = it.languageCode
                    if (dbLang != savedPrefsLang && savedPrefsLang != "en" && dbLang == "en") {
                        // DB has default "en" but preferences has a custom language: update DB to match preference
                        repository.updateUserLanguage(savedPrefsLang)
                        _currentLanguageCode.value = savedPrefsLang
                    } else {
                        // DB has custom language (or both are en): sync preference to match DB
                        sharedPrefs.edit().putString("selected_language_code", dbLang).apply()
                        _currentLanguageCode.value = dbLang
                    }
                    if (it.streakNotificationEnabled) {
                        StreakNotificationHelper.showStreakNotification(
                            getApplication(),
                            it.streak,
                            it.xp,
                            it.languageCode
                        )
                    }
                }
            }
        }
    }

    fun triggerStreakNotification(context: Context) {
        val profile = userProfile.value
        val streakDays = profile?.streak ?: 1
        val xp = profile?.xp ?: 0
        val lang = _currentLanguageCode.value
        StreakNotificationHelper.showStreakNotification(context, streakDays, xp, lang)
        android.widget.Toast.makeText(
            context,
            "🔥 Daily Streak Notification Sent! Keep your streak active.",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    fun toggleStreakNotification(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateStreakNotification(enabled)
            val msg = if (enabled) "Daily streak notifications enabled! 🔔" else "Streak notifications paused."
            android.widget.Toast.makeText(getApplication(), msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun claimDailyStreakBonus() {
        viewModelScope.launch {
            val newStreak = repository.recordActivityAndIncrementStreak()
            val profile = userProfile.value
            if (profile != null) {
                repository.updateProfile(profile.copy(xp = profile.xp + 15))
            }
            android.widget.Toast.makeText(
                getApplication(),
                "🔥 Daily Streak Claimed! Streak: $newStreak Days (+15 XP)",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    fun toggleNetworkMode() {
        _isOnline.value = !_isOnline.value
        val message = if (_isOnline.value) {
            "Switching to ONLINE MODE (Standard data/Wi-Fi charges may apply)."
        } else {
            "KodeMamas is operating in zero-data local-only offline mode."
        }
        android.widget.Toast.makeText(
            getApplication(),
            message,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    fun changeLanguage(langCode: String) {
        viewModelScope.launch {
            sharedPrefs.edit().putString("selected_language_code", langCode).apply()
            repository.updateUserLanguage(langCode)
            _currentLanguageCode.value = langCode
        }
    }

    fun updateOfflineUserProfile(name: String, role: String, langCode: String, newXp: Int) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            android.widget.Toast.makeText(getApplication(), "Name cannot be empty!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (trimmedName.length > 30) {
            android.widget.Toast.makeText(getApplication(), "Name is too long (max 30 characters)!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (!trimmedName.all { it.isLetterOrDigit() || it.isWhitespace() || it == '-' || it == '\'' }) {
            android.widget.Toast.makeText(getApplication(), "Name contains invalid characters!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            sharedPrefs.edit().putString("selected_language_code", langCode).apply()
            val existing = userProfile.value
            val updated = existing?.copy(
                name = trimmedName,
                role = role,
                languageCode = langCode,
                xp = newXp
            ) ?: UserProfile(
                id = 1,
                name = trimmedName,
                email = "${trimmedName.lowercase().replace(" ", "")}@kodemamas.org",
                role = role,
                languageCode = langCode,
                xp = newXp,
                streak = 1,
                dataSavingMode = false,
                isPremium = false,
                isPlus = false,
                hasDownloadedOffline = false
            )
            repository.updateProfile(updated)
            _currentLanguageCode.value = langCode
        }
    }

    fun toggleDataSavingMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDataSaving(enabled)
        }
    }

    fun selectLesson(lesson: Lesson) {
        viewModelScope.launch {
            _currentActiveLesson.value = lesson
            _currentStepIndex.value = 0
            _editorText.value = ""
            _simulatorOutput.value = ""
            _simulatorSuccess.value = false
            _quizFinished.value = false
            _quizScore.value = 0
            
            // Collect steps for active lesson
            repository.getStepsForLesson(lesson.id).collect { steps ->
                _currentActiveSteps.value = steps
                // Initialize default editor code snippet for step index 0
                if (steps.isNotEmpty()) {
                    _editorText.value = steps[0].codeSnippet
                }
            }
        }
    }

    fun closeActiveLesson() {
        _currentActiveLesson.value = null
        _currentActiveSteps.value = emptyList()
        _currentStepIndex.value = 0
    }

    fun setStepIndex(index: Int) {
        val steps = _currentActiveSteps.value
        if (index in steps.indices) {
            _currentStepIndex.value = index
            _editorText.value = steps[index].codeSnippet
            _simulatorOutput.value = ""
            _simulatorSuccess.value = false
        }
    }

    fun updateEditorText(text: String) {
        _editorText.value = text
    }

    fun downloadAllLessons() {
        if (_downloadProgress.value != null) return
        viewModelScope.launch {
            _downloadProgress.value = 0f
            for (i in 1..20) {
                delay(80)
                _downloadProgress.value = i / 20f
            }
            val lessons = allLessons.value
            for (l in lessons) {
                repository.updateLessonDownloaded(l.id, true)
            }
            repository.updateDownloadedOfflineStatus(true)
            // Add downloading XP reward
            val currentProfile = userProfile.value
            if (currentProfile != null) {
                repository.updateProfile(currentProfile.copy(xp = currentProfile.xp + 40, hasDownloadedOffline = true))
            }
            completeOnboardingPhase("admit")
            _downloadProgress.value = null
        }
    }

    fun toggleSingleLessonDownload(lessonId: String) {
        viewModelScope.launch {
            val lesson = allLessons.value.find { it.id == lessonId } ?: return@launch
            val nextState = !lesson.isDownloaded
            repository.updateLessonDownloaded(lessonId, nextState)
        }
    }

    fun runSimulatorCode() {
        val currentLesson = _currentActiveLesson.value ?: return
        val step = _currentActiveSteps.value.getOrNull(_currentStepIndex.value) ?: return
        val currentCode = _editorText.value.trim()

        if (currentCode.isEmpty()) {
            _simulatorOutput.value = "Error: Input text is empty. Enter your coding statement first!"
            _simulatorSuccess.value = false
            return
        }

        if (_isCompiling.value) return // Prevent multiple compile runs
        _isCompiling.value = true

        viewModelScope.launch {
            delay(900) // Fast realistic compiler run simulation

            val lowerCode = currentCode.lowercase()
            var isSuccess = false
            var output = ""

            when (currentLesson.id) {
                "html_1" -> {
                    if (step.stepNumber == 2) {
                        if (lowerCode.contains("<h1>") && lowerCode.contains("</h1>")) {
                            output = "🚀 Web Emulator Preview:\n✨ Successfully Rendered Header!\n<h1>Mam's Spaza Shop</h1> with warm township gold styling."
                            isSuccess = true
                        } else {
                            output = "Web Preview Output:\n--------------------\n$currentCode\n--------------------\nTip: Wrap your heading with <h1> and </h1> tags!"
                        }
                    } else if (step.stepNumber == 3) {
                        if ((lowerCode.contains("<ul>") && lowerCode.contains("</ul>")) || lowerCode.contains("<li>") || lowerCode.contains("<p>")) {
                            output = "🚀 Web Emulator Preview:\n🛒 Spaza Product Inventory list generated!\nFound stock items: Bread, Milk, Rooibos Tea."
                            isSuccess = true
                        } else {
                            output = "Web Preview Output:\n--------------------\n$currentCode\n--------------------\nTip: Use <ul> and <li> tags to list your stock items!"
                        }
                    } else {
                        isSuccess = true
                        output = "🚀 Web Preview:\nStep completed! Click Next to continue."
                    }
                }
                "css_2" -> {
                    if (lowerCode.contains("background-color") || lowerCode.contains("color") || lowerCode.contains("#121212") || lowerCode.contains("gold") || lowerCode.contains("#ffd700") || lowerCode.contains("{")) {
                        output = "🎨 CSS styling compiled:\n✅ Background set to deep midnight #121212!\n✅ Accent color painted Gold (#FFD700)!"
                        isSuccess = true
                    } else {
                        output = "CSS compiler output:\nModified style sheet rules. Use background-color and color to configure colors!"
                    }
                }
                "js_3" -> {
                    if (lowerCode.contains("calculatetotal") || lowerCode.contains("price") || lowerCode.contains("console.log") || lowerCode.contains("18.5") || lowerCode.contains("function")) {
                        output = "⚙️ JavaScript Output:\nR85.00\n\n✅ Code execution compiled!\nCalculates 2 Blue Ribbon bread & 3 Clover milks perfectly (2*18.5 + 3*16 = 37 + 48 = 85)."
                        isSuccess = true
                    } else {
                        output = "⚙️ JavaScript Console:\nRunning script...\nResult: Undefined or incomplete. Define variables and return the total sum!"
                    }
                }
                "python_4" -> {
                    if (lowerCode.contains("temp") || lowerCode.contains("if") || lowerCode.contains("print")) {
                        output = "🐍 Python Terminal Output:\nWarning: High Heat (32°C)! Increase crop irrigation x2.\n\n✅ Agricultural forecast script executed successfully!"
                        isSuccess = true
                    } else {
                        output = "🐍 Python IDLE Console:\nIndentation or syntax check: Use 'if temp > 30:' and print your irrigation alert."
                    }
                }
                "html_5" -> {
                    if (step.stepNumber == 1) {
                        if (lowerCode.contains("<h1>") && lowerCode.contains("</h1>")) {
                            output = "🚀 Web Emulator Preview:\n✨ High-Converting StoryBrand Header rendered!\n<h1>Grow Your Business with KodeMamas</h1>"
                            isSuccess = true
                        } else {
                            output = "Web Preview:\nUse <h1>...</h1> to build your hero section title."
                        }
                    } else {
                        if (lowerCode.contains("<button>") && lowerCode.contains("</button>")) {
                            output = "🚀 Web Emulator Preview:\n🔘 Call to Action Button Rendered: [Join Training Now]!\nGreat customer conversion trigger."
                            isSuccess = true
                        } else {
                            output = "Web Preview:\nWrap your call to action text in <button> and </button> tags."
                        }
                    }
                }
                "css_6" -> {
                    if (lowerCode.contains(".mama") || lowerCode.contains(".student") || lowerCode.contains("color") || lowerCode.contains("{")) {
                        output = "🎨 CSS Classes Compiled:\n✅ Class .mama styled with Gold (#FFD700)!\n✅ Class .student styled with Indigo (#4B0082)!\nRoles successfully documented in SOP dashboard."
                        isSuccess = true
                    } else {
                        output = "CSS Compiler:\nUse class selectors .mama and .student to color-code roles."
                    }
                }
                "js_7" -> {
                    if (lowerCode.contains("revenue") || lowerCode.contains("profit") || lowerCode.contains("expenses") || lowerCode.contains("console.log") || lowerCode.contains("-")) {
                        output = "⚙️ Cash Flow Engine Output:\nNet Profit: R5,500.00\n\n✅ Healthy cash reserve calculation compiled! Revenue (R15,000) - Expenses (R9,500) = R5,500 profit."
                        isSuccess = true
                    } else {
                        output = "⚙️ JS Console:\nDefine revenue and expenses, then calculate 'const profit = revenue - expenses;'."
                    }
                }
                "html_8" -> {
                    if (lowerCode.contains("<input") || lowerCode.contains("email") || lowerCode.contains("placeholder")) {
                        output = "🚀 Web Emulator Preview:\n📝 Lead capture input rendered: [Enter your email...]\nReady for customer feedback integration."
                        isSuccess = true
                    } else {
                        output = "Web Preview:\nUse <input type=\"email\" placeholder=\"Enter your email\" /> to create the input field."
                    }
                }
                "python_9" -> {
                    if (lowerCode.contains("for") || lowerCode.contains("sales") || lowerCode.contains("print")) {
                        output = "🐍 Python Terminal Output:\nMonth 1: R5,750.00\nMonth 2: R6,612.50\nMonth 3: R7,604.38\n\n✅ 90-Day Compounding Forecast generated with 15% monthly growth!"
                        isSuccess = true
                    } else {
                        output = "🐍 Python Console:\nUse 'for month in [1, 2, 3]:' loop to simulate 90-day compounding revenue."
                    }
                }
                "js_10" -> {
                    if (lowerCode.contains("steps") || lowerCode.contains("foreach") || lowerCode.contains("console.log") || lowerCode.contains("for")) {
                        output = "⚙️ Automation Console:\n1. Lead\n2. Team\n3. Plan\n4. Experience\n5. Marketing\n6. Scale\n\n✅ 6-Step SOP automated checklist looped cleanly!"
                        isSuccess = true
                    } else {
                        output = "⚙️ JS Console:\nIterate through SOP steps array using steps.forEach(...) or for loop."
                    }
                }
                else -> {
                    when (currentLesson.category) {
                        "HTML" -> {
                            if (lowerCode.contains("<") && lowerCode.contains(">")) {
                                output = "🚀 Web Emulator Preview:\nHTML syntax parsed successfully!\n$currentCode"
                                isSuccess = true
                            } else {
                                output = "HTML compiler: Enter valid HTML tags (e.g. <h1>, <p>, <button>)."
                            }
                        }
                        "CSS" -> {
                            if (lowerCode.contains("{") || lowerCode.contains(":") || lowerCode.contains(";")) {
                                output = "🎨 CSS styling compiled successfully!\nRules applied to viewport."
                                isSuccess = true
                            } else {
                                output = "CSS compiler: Define CSS properties using 'selector { property: value; }'."
                            }
                        }
                        "JavaScript" -> {
                            if (lowerCode.contains("const") || lowerCode.contains("let") || lowerCode.contains("var") || lowerCode.contains("function") || lowerCode.contains("console.log")) {
                                output = "⚙️ JavaScript Execution Output:\nExecution successful (0 errors).\nLog: Output rendered."
                                isSuccess = true
                            } else {
                                output = "JS Console: Write valid JavaScript statements or console.log(...)."
                            }
                        }
                        "Python" -> {
                            if (lowerCode.contains("print") || lowerCode.contains("=") || lowerCode.contains("def") || lowerCode.contains("for") || lowerCode.contains("if")) {
                                output = "🐍 Python Terminal Output:\nProgram executed with exit code 0.\nOutputs rendered."
                                isSuccess = true
                            } else {
                                output = "Python IDLE: Enter valid Python syntax (e.g. print(...))."
                            }
                        }
                        else -> {
                            output = "Compiled successfully!\n$currentCode"
                            isSuccess = true
                        }
                    }
                }
            }

            _simulatorOutput.value = output
            _simulatorSuccess.value = isSuccess

            if (isSuccess) {
                completeOnboardingPhase("activate")
            }
            _isCompiling.value = false
        }
    }

    fun completeStep() {
        viewModelScope.launch {
            val currentLesson = _currentActiveLesson.value ?: return@launch
            val steps = _currentActiveSteps.value
            val currentIdx = _currentStepIndex.value
            
            if (currentIdx >= steps.size - 1 || steps.isEmpty()) {
                // Lesson steps finished -> Load interactive quiz questions using firstOrNull to avoid re-triggering
                val questions = repository.getQuizForLesson(currentLesson.id).firstOrNull() ?: emptyList()
                if (questions.isNotEmpty()) {
                    _activeQuizQuestions.value = questions
                    _quizQuestionIndex.value = 0
                    _selectedAnswerIndex.value = -1
                    _quizChecked.value = false
                    _quizCorrect.value = false
                    _quizScore.value = 0
                    _quizFinished.value = false
                    _currentActiveSteps.value = emptyList() // clear steps to open quiz
                } else {
                    // If no quiz questions found in DB, auto-complete lesson
                    _quizFinished.value = true
                    repository.saveUserProgress(
                        lessonId = currentLesson.id,
                        stepIndex = 1,
                        completed = true,
                        quizCompleted = true,
                        score = 100
                    )
                    StreakNotificationHelper.showLessonCompletedNotification(
                        getApplication(),
                        currentLesson.title,
                        userProfile.value?.streak ?: 1,
                        _currentLanguageCode.value
                    )
                    completeOnboardingPhase("accomplish")
                }
            } else {
                setStepIndex(currentIdx + 1)
            }
        }
    }

    fun selectQuizAnswer(index: Int) {
        if (!_quizChecked.value) {
            _selectedAnswerIndex.value = index
        }
    }

    fun checkQuizAnswer() {
        val questions = _activeQuizQuestions.value
        val currentQIdx = _quizQuestionIndex.value
        val selectedIdx = _selectedAnswerIndex.value

        if (selectedIdx == -1 || currentQIdx !in questions.indices) return

        val correctIndex = questions[currentQIdx].correctAnswerIndex
        val isCorrect = selectedIdx == correctIndex
        
        _quizChecked.value = true
        _quizCorrect.value = isCorrect
        
        if (isCorrect) {
            _quizScore.value += 1
        }
    }

    fun nextQuizStep() {
        val questions = _activeQuizQuestions.value
        val currentQIdx = _quizQuestionIndex.value

        if (currentQIdx >= questions.size - 1 || questions.isEmpty()) {
            // End of Quiz
            _quizFinished.value = true
            // Save user progress in Database! This grants XP, unlocks next lesson and triggers streak notification.
            viewModelScope.launch {
                val lesson = _currentActiveLesson.value ?: return@launch
                val passed = if (questions.isEmpty()) true else _quizScore.value >= (questions.size / 2.0)
                repository.saveUserProgress(
                    lessonId = lesson.id,
                    stepIndex = 1,
                    completed = true,
                    quizCompleted = true,
                    score = _quizScore.value
                )
                StreakNotificationHelper.showLessonCompletedNotification(
                    getApplication(),
                    lesson.title,
                    userProfile.value?.streak ?: 1,
                    _currentLanguageCode.value
                )
                if (passed) {
                    completeOnboardingPhase("accomplish")
                }
            }
        } else {
            _quizQuestionIndex.value = currentQIdx + 1
            _selectedAnswerIndex.value = -1
            _quizChecked.value = false
            _quizCorrect.value = false
        }
    }

    // Community section - Add post
    fun addForumPost(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        if (trimmed.length > 500) {
            android.widget.Toast.makeText(getApplication(), "Post is too long (max 500 characters)!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val lowerContent = trimmed.lowercase()
        val blockedWords = listOf("bypass", "unauthorized", "hack", "vulgar", "profanity") // custom placeholder blocked words
        if (blockedWords.any { lowerContent.contains(it) }) {
            android.widget.Toast.makeText(getApplication(), "Your post contains blocked or unsafe words. Let's keep the community safe and encouraging!", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val newPost = DiscussionPost(
                id = "post_${System.currentTimeMillis()}",
                author = profile.name + " (" + (if (profile.role == "Mama") "Mama" else "Student") + ")",
                role = profile.role,
                content = trimmed,
                timestamp = System.currentTimeMillis(),
                likes = 0,
                commentCount = 0,
                languageCode = _currentLanguageCode.value
            )
            repository.addDiscussionPost(newPost)
        }
    }

    fun likeForumPost(postId: String) {
        viewModelScope.launch {
            repository.incrementPostLikes(postId)
        }
    }

    // AI chat - send message
    fun sendAiChat(messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isEmpty() || _aiGenerating.value) return
        viewModelScope.launch {
            val userMsg = MentorChat(isAi = true, isUser = true, messageText = trimmed)
            repository.insertChatMessage(userMsg)

            _aiGenerating.value = true

            // Generate response
            if (!_isOnline.value) {
                delay(1200) // fast realistic delay offline simulation
                val fallbackReply = generateOfflineRecommendation(trimmed)
                repository.insertChatMessage(MentorChat(isAi = true, isUser = false, messageText = fallbackReply))
                _aiGenerating.value = false
            } else {
                // Create context prompt
                val systemPrompt = "You are an empathetic, patient, and highly encouraging Socratic AI Builder Coach for KodeMamas. Inspired by SuaCode's Kwame 2.0 (tested with thousands of learners across African countries), your mission is to transform learners into independent Builders who turn real problems into working technology. NEVER just give out raw code immediately when requested; instead ask guiding questions like 'Before I give you code, tell me what this button/function should do in plain words.' Guide them through: Discover -> Define -> Design -> Learn -> Build -> Test -> Improve -> Showcase. Keep explanations friendly, simple, and under 150 words in all 11 official South African languages plus SASL."
                val aiResponse = GeminiService.generateResponse(trimmed, systemPrompt)
                repository.insertChatMessage(MentorChat(isAi = true, isUser = false, messageText = aiResponse))
                _aiGenerating.value = false
            }
        }
    }

    private fun generateOfflineRecommendation(query: String): String {
        val text = query.lowercase()
        return when {
            text.contains("login") || text.contains("register") || text.contains("account") -> {
                "🔑 **Offline Profile & Login Help / Izikhokelo zokuNgena:**\n\n" +
                "• **English:** Click the profile edit card in the top right to set up your offline account on this device. Enter your name, select your role (Mama, Student, Mentor), and pick your language out of the 12 South African options!\n" +
                "• **Zulu:** Chofoza ikhadi lomlando phezulu kwesokudla ukuze usethe i-akhawunti yakho. Faka igama, khetha indima yakho (u-Mama, uMfundi, noma uMentora) futhi ukhethe ulimi!\n" +
                "• **Xhosa:** Cofa ikhadi leprophayili phezulu ngasekunene ukuseta iakhawunti yakho. Faka igama lakho, khetha indima yakho (uMama, uMfundi, okanye uMcebisi) uze ukhethe ulwimi lwasekhaya!"
            }
            text.contains("after login") || text.contains("tab") || text.contains("content") -> {
                "📱 **What's inside after login / Izinto ezikhoyo emva kokungena:**\n\n" +
                "Once you log in, you can access the following 5 main tabs:\n" +
                "1. **Home / Dashboard:** Tracks your 100-Day Journey progress, streak, XP, and shows visual 'win' popups as you complete coding steps!\n" +
                "2. **Learn:** 10 full interactive lessons in 12 languages covering HTML, CSS, JavaScript, and Python with custom Spaza and smart farming business scenarios!\n" +
                "3. **Code:** A real-time sandboxed code editor simulator where you can test your HTML headers, CSS styling, and JavaScript calculations.\n" +
                "4. **Forum:** Interact with other Mamas and girls locally in South Africa, share advice on growing your business, and post your coding milestones!\n" +
                "5. **Buddies:** Find and match with study buddies near you based on language, role, and current course progress!"
            }
            text.contains("grow") || text.contains("business") || text.contains("donald miller") || text.contains("6-step") || text.contains("plan") -> {
                "📈 **Donald Miller's 6-Step Small Business Plan:**\n\n" +
                "Our advanced coding lessons (5 to 10) are designed specifically around this powerful framework:\n" +
                "1. **Lead Yourself (Pilot):** Build habits and SOPs first so you aren't the bottleneck (Lesson 6/10).\n" +
                "2. **StoryBrand Marketing (Engines):** Clarify your message so customers listen. Create headers and call-to-action buttons (Lesson 5).\n" +
                "3. **Sales & Offers (Wings):** Focus on cash flow, high-converting checkout lists, and pricing calculators (Lesson 7).\n" +
                "4. **Customer Experience (Body):** Build automated customer feedback systems (Lesson 8).\n" +
                "5. **Predictive Analytics (Fuel):** Use Python loops to forecast 90-day compound revenue goals (Lesson 9).\n" +
                "6. **Operations & Scale (Controls):** Create interactive SOP checklists to automate daily steps (Lesson 10)."
            }
            text.contains("html") -> {
                "🌐 **HTML (Web Layout) tip:**\n\n" +
                "• Use `<h1>Title</h1>` for the largest headers.\n" +
                "• Use `<button>Buy Now</button>` for clear Call-to-Actions (StoryBrand Principle!).\n" +
                "• Use `<input type=\"email\" />` to collect customer leads dynamically."
            }
            text.contains("css") -> {
                "🎨 **CSS (Styling) tip:**\n\n" +
                "• Use `.mama { color: gold; }` to target specific classes for visual branding.\n" +
                "• Use `background-color: #121212;` for elegant dark themes."
            }
            text.contains("javascript") || text.contains("js") -> {
                "⚡ **JavaScript (Calculations) tip:**\n\n" +
                "• Use loops like `steps.forEach()` to print out automated checklists.\n" +
                "• Use simple mathematical variables `const profit = revenue - expenses;` to track spaza cash flow."
            }
            text.contains("python") -> {
                "🐍 **Python (Data & Loops) tip:**\n\n" +
                "• Use `for month in range(1, 4):` loops to simulate 90-day compounding revenue trackers.\n" +
                "• Always align your logic blocks using correct **Indentation** (4 spaces) to prevent syntax errors."
            }
            else -> {
                "👋 **Molo! Dumelang! Sanibonani! Hello!**\n\n" +
                "I am your offline tech buddy! Type a question about **login** or **after login** features, **grow business** lessons, or specific languages (like **Zulu** or **Xhosa**) and I will help you step-by-step!"
            }
        }
    }

    fun clearAiMessages() {
        viewModelScope.launch {
            repository.clearAiChats()
            repository.insertChatMessage(
                MentorChat(
                    isAi = true,
                    isUser = false,
                    messageText = "Hello again! 👋 Let's start a fresh coding study lesson. Ask me any simple questions about HTML, CSS, JavaScript, or Python!"
                )
            )
        }
    }

    // 1-on-1 mentorship chat with Nokwazi
    fun sendMentorChat(messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isEmpty() || _mentorTyping.value) return
        viewModelScope.launch {
            val userMsg = MentorChat(isAi = false, isUser = true, messageText = trimmed)
            repository.insertChatMessage(userMsg)

            _mentorTyping.value = true
            delay(2000) // Realistic typing status

            val replies = listOf(
                "Wow, that is a fantastic question! I highly recommend checking out Lesson 1 for the Spaza Shop setup first, it will clear that syntax.",
                "Excellent progress! Remember to save your lesson downloaded to read when you travel out of signal coverage. Let me know if you want me to review your CV draft!",
                "Ngiyabonga for your message! You have built a strong logical base. Let's arrange a 1-on-1 Zoom setup call on our premium tier once you finish Lesson 3.",
                "Molo student! I am currently checking coding logs from Bloemfontein. Your profile looks amazing. Continue building and creating!"
            )
            val randomReply = replies.random()
            val mentorReply = MentorChat(isAi = false, isUser = false, messageText = randomReply)
            repository.insertChatMessage(mentorReply)
            _mentorTyping.value = false
        }
    }

    fun setActiveChallenge(challenge: CodingChallenge) {
        _activeChallenge.value = challenge
    }

    fun solveChallenge() {
        val currChall = _activeChallenge.value ?: return
        viewModelScope.launch {
            repository.updateChallengeStatus(currChall.id, true)
            // update local list status
            _activeChallenge.value = currChall.copy(isCompleted = true)
        }
    }

    fun claimPlusUpgrade() {
        viewModelScope.launch {
            repository.updatePlusStatus(true)
            repository.updatePremiumStatus(false) // Upgrade to Plus
        }
    }

    fun claimPremiumUpgrade() {
        viewModelScope.launch {
            repository.updatePremiumStatus(true)
            repository.updatePlusStatus(false) // Upgrade to Premium (Premium takes higher priority)
        }
    }

    fun cancelPremium() {
        viewModelScope.launch {
            repository.updatePremiumStatus(false)
        }
    }

    fun cancelPlus() {
        viewModelScope.launch {
            repository.updatePlusStatus(false)
        }
    }

    fun cancelAllSubscriptions() {
        viewModelScope.launch {
            repository.updatePremiumStatus(false)
            repository.updatePlusStatus(false)
        }
    }

    // 100-Day Onboarding Journey Methods
    val onboardingPhases = listOf(
        OnboardingPhase(
            id = "assess",
            name = "1. Assess",
            feeling = "Did I make the right choice?",
            taskName = "Lock in Your Motivation Goals",
            taskDescription = "Confirm your learning path and set coding goals (e.g., build a spaza shop, design responsive layouts) to lock in your study motivation.",
            xpReward = 20,
            actionText = "Set Study Goals",
            phaseNumber = 1
        ),
        OnboardingPhase(
            id = "admit",
            name = "2. Admit",
            feeling = "This is harder than I thought.",
            taskName = "Download Offline Lessons",
            taskDescription = "Simplify your study. Save all 4 core lessons offline to learn with zero mobile data charges or signal drops.",
            xpReward = 30,
            actionText = "Save Lessons Offline",
            phaseNumber = 2
        ),
        OnboardingPhase(
            id = "affirm",
            name = "3. Affirm",
            feeling = "Am I doing this right?",
            taskName = "Read Community Success Stories",
            taskDescription = "Witness real-world proof. Read how other South African mothers and sisters are successfully building tech solutions.",
            xpReward = 20,
            actionText = "Read Success Stories",
            phaseNumber = 3
        ),
        OnboardingPhase(
            id = "activate",
            name = "4. Activate",
            feeling = "I'm stuck.",
            taskName = "Run 'Code Your Name' in Simulator",
            taskDescription = "Get value in 5 minutes! Launch the code editor, customize a script with your name, and see it render live.",
            xpReward = 50,
            actionText = "Launch Editor",
            phaseNumber = 4
        ),
        OnboardingPhase(
            id = "acclimate",
            name = "5. Acclimate",
            feeling = "Making this part of my life.",
            taskName = "Check Study Stats & Streak Log",
            taskDescription = "Build study habit loops. View your XP progress, streak charts, and digital literacy analytics regularly.",
            xpReward = 30,
            actionText = "Analyze Habits",
            phaseNumber = 5
        ),
        OnboardingPhase(
            id = "accomplish",
            name = "6. Accomplish",
            feeling = "I'm winning!",
            taskName = "Pass a Lesson Quiz",
            taskDescription = "Celebrate your success! Complete the quiz questions in Lesson 1 to earn your official HTML & digital literacy badge.",
            xpReward = 40,
            actionText = "Start Quiz",
            phaseNumber = 6
        ),
        OnboardingPhase(
            id = "adopt",
            name = "7. Adopt",
            feeling = "This is who I am now.",
            taskName = "Connect with a Study Buddy",
            taskDescription = "Join the tech sisterhood. Message a nearby learning partner to share coding templates and build together.",
            xpReward = 35,
            actionText = "Find Partners",
            phaseNumber = 7
        ),
        OnboardingPhase(
            id = "advocate",
            name = "8. Advocate",
            feeling = "Everyone needs this!",
            taskName = "Get Your Local Referral Poster",
            taskDescription = "Become a promoter. Generate your custom referral invite and help other township mothers learn how to code.",
            xpReward = 50,
            actionText = "Get Invite Poster",
            phaseNumber = 8
        )
    )

    fun completeOnboardingPhase(phaseId: String) {
        val currentSet = _completedPhases.value
        if (phaseId in currentSet) return
        
        val phase = onboardingPhases.find { it.id == phaseId } ?: return
        val updatedSet = currentSet + phaseId
        _completedPhases.value = updatedSet
        sharedPrefs.edit().putStringSet("completed_onboarding_phases", updatedSet).apply()
        
        // Award XP
        viewModelScope.launch {
            val existing = userProfile.value
            if (existing != null) {
                repository.updateProfile(existing.copy(xp = existing.xp + phase.xpReward))
            }
        }
        
        // Set active win to show the celebratory overlay/dialog
        _activeOnboardingWin.value = phase
    }
    
    fun dismissOnboardingWin() {
        _activeOnboardingWin.value = null
    }
}

data class OnboardingPhase(
    val id: String,
    val name: String,
    val feeling: String,
    val taskName: String,
    val taskDescription: String,
    val xpReward: Int,
    val actionText: String,
    val phaseNumber: Int
)
