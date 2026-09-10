package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = Repository(db)

    // Lang State (Sync'd with the DB Profile)
    private val _currentLanguageCode = MutableStateFlow("en")
    val currentLanguageCode: StateFlow<String> = _currentLanguageCode.asStateFlow()

    // Screen navigation state: "home", "builds", "learn", "ai_chat", "community", "mentorship"
    private val _selectedTab = MutableStateFlow("ai_chat")
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

    // Firebase People Database Connection & Flows
    val firebaseService = FirebasePeopleDatabaseService(application, db.personDao())

    val allPeople: StateFlow<List<PersonEntity>> = repository.allPeople
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMentors: StateFlow<List<PersonEntity>> = repository.allMentors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firebaseStatus: StateFlow<FirebaseConnectionStatus> = firebaseService.connectionStatus
    val firebaseLastSync: StateFlow<Long> = firebaseService.lastSyncTimestamp
    val firebaseSyncedCount: StateFlow<Int> = firebaseService.syncedCount
    val firebaseStatusMessage: StateFlow<String> = firebaseService.statusMessage
    val firebaseDbUrl: StateFlow<String> = firebaseService.databaseUrl
    val firebaseProjectId: StateFlow<String> = firebaseService.projectId

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

    // Network Status & Real-time Network Observer
    private val _isOnline = MutableStateFlow(NetworkHelper.isOnline(application))
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Preferences & Custom API Key
    private val prefs = application.getSharedPreferences("kodemamas_ai_prefs", android.content.Context.MODE_PRIVATE)
    private val _customApiKey = MutableStateFlow(prefs.getString("custom_gemini_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    // Role-Based Access Control (RBAC) & Admin Route Protection
    val isAdmin: StateFlow<Boolean> = userProfile
        .map { SecurityUtils.hasAdminAccess(it?.role, it?.email) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Masked API key flow for secure presentation
    val maskedApiKey: StateFlow<String> = _customApiKey
        .map { key -> SecurityUtils.maskApiKey(key.ifBlank { GeminiService.getEffectiveApiKey() }) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "••••••••")

    // Cryptographic Authentication & Password Management State
    private val _isAuthenticated = MutableStateFlow(prefs.getBoolean("user_authenticated", true))
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun setSecurePassword(plainTextPassword: String): Boolean {
        if (plainTextPassword.length < 6) return false
        val hashResult = SecurityUtils.hashPassword(plainTextPassword)
        prefs.edit()
            .putString("auth_pwd_hash", hashResult.hashBase64)
            .putString("auth_pwd_salt", hashResult.saltBase64)
            .putBoolean("has_set_password", true)
            .apply()
        _isAuthenticated.value = true
        return true
    }

    fun verifyAndLogin(candidatePassword: String): Boolean {
        val storedHash = prefs.getString("auth_pwd_hash", null) ?: return false
        val storedSalt = prefs.getString("auth_pwd_salt", null) ?: return false
        val valid = SecurityUtils.verifyPassword(candidatePassword, storedHash, storedSalt)
        if (valid) {
            _isAuthenticated.value = true
        }
        return valid
    }

    fun logout() {
        _isAuthenticated.value = false
    }

    // Google Search Grounding & Gemini AI Status
    private val _isGoogleSearchEnabled = MutableStateFlow(prefs.getBoolean("google_search_grounding", true))
    val isGoogleSearchEnabled: StateFlow<Boolean> = _isGoogleSearchEnabled.asStateFlow()

    private val _selectedGeminiModel = MutableStateFlow(
        prefs.getString("selected_gemini_model", GeminiService.MODEL_GEMINI_3_5_FLASH) ?: GeminiService.MODEL_GEMINI_3_5_FLASH
    )
    val selectedGeminiModel: StateFlow<String> = _selectedGeminiModel.asStateFlow()

    // Theme Mode: "SYSTEM", "DARK", "LIGHT"
    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Active AI Assistant Language (Defaults to "auto", supports all 12 SA official languages)
    private val _aiLanguageCode = MutableStateFlow(prefs.getString("ai_language_code", "auto") ?: "auto")
    val aiLanguageCode: StateFlow<String> = _aiLanguageCode.asStateFlow()

    fun setAiLanguageCode(code: String) {
        val clean = if (code.isBlank()) "auto" else code.lowercase().trim()
        prefs.edit().putString("ai_language_code", clean).apply()
        _aiLanguageCode.value = clean
        GeminiService.setActiveLanguageCode(clean)
    }

    // Subscription & Plan details: "FREE", "STANDARD" (R99/yr), "PREMIUM" (R299/yr)
    private val _currentPlanTier = MutableStateFlow(
        prefs.getString("user_plan_tier", "FREE") ?: "FREE"
    )
    val currentPlanTier: StateFlow<String> = _currentPlanTier.asStateFlow()

    private val _paymentReference = MutableStateFlow(
        prefs.getString("payment_reference", "") ?: ""
    )
    val paymentReference: StateFlow<String> = _paymentReference.asStateFlow()

    val capitecAccountNumber = "2121743886"
    val capitecBankName = "Capitec Bank"
    val capitecBranchCode = "470010"
    val standardPlanPrice = "R99/year"
    val premiumPlanPrice = "R299/year"

    fun activatePlan(tier: String, reference: String = "") {
        val cleanTier = when (tier.uppercase()) {
            "STANDARD" -> "STANDARD"
            "PREMIUM" -> "PREMIUM"
            else -> "FREE"
        }
        prefs.edit()
            .putString("user_plan_tier", cleanTier)
            .putString("payment_reference", reference.trim())
            .putLong("plan_activation_time", System.currentTimeMillis())
            .apply()
        _currentPlanTier.value = cleanTier
        _paymentReference.value = reference.trim()

        viewModelScope.launch {
            val isPrem = (cleanTier == "PREMIUM")
            repository.updatePremiumStatus(isPrem)
        }
    }

    fun downgradeToFree() {
        activatePlan("FREE", "")
    }

    // User Rating & In-App Feedback
    private val _userRating = MutableStateFlow(prefs.getInt("user_rating", 0))
    val userRating: StateFlow<Int> = _userRating.asStateFlow()

    private val _feedbackSubmitted = MutableStateFlow(false)
    val feedbackSubmitted: StateFlow<Boolean> = _feedbackSubmitted.asStateFlow()

    // Chat states
    private val _aiGenerating = MutableStateFlow(false)
    val aiGenerating: StateFlow<Boolean> = _aiGenerating.asStateFlow()

    private val _mentorTyping = MutableStateFlow(false)
    val mentorTyping: StateFlow<Boolean> = _mentorTyping.asStateFlow()

    // Selected challenge in Daily Challenge View
    private val _activeChallenge = MutableStateFlow<CodingChallenge?>(null)
    val activeChallenge: StateFlow<CodingChallenge?> = _activeChallenge.asStateFlow()

    init {
        GeminiService.setCustomApiKey(_customApiKey.value)
        GeminiService.setActiveModel(_selectedGeminiModel.value)
        GeminiService.setGoogleSearchGrounding(_isGoogleSearchEnabled.value)
        GeminiService.setActiveLanguageCode(_aiLanguageCode.value)

        viewModelScope.launch {
            NetworkHelper.observeNetwork(application).collect { online ->
                _isOnline.value = online
            }
        }

        viewModelScope.launch {
            // Populate database if empty
            repository.prepopulateDatabaseIfEmpty()

            // Ensure the AI Helper has the requested system request discussion from the screenshot
            launch {
                aiChats.collect { chats ->
                    if (chats.none { it.messageText.contains("Key professionals") }) {
                        repository.insertChatMessage(
                            MentorChat(
                                isAi = true,
                                isUser = false,
                                messageText = """Key professionals who use it include:
1. **Business & Systems Analysts:**
They examine it to see if the idea is realistic (feasibility analysis) and start planning requirements.
2. **IT Steering Committee & Project Managers:** Decision-makers who review it to say, *"Yes, let's fund and build this!"* or *"Not right now."*
3. **Developers & Designers:** They reference it to understand the big-picture purpose before building.

Think like a tech leader: If you were creating a system request to solve a challenge in your own community, what problem would you address at your local spaza shop, clinic, or school?"""
                            )
                        )
                    }
                }
            }

            // Pull the preferred language code from database
            userProfile.collect { profile ->
                profile?.let {
                    _currentLanguageCode.value = it.languageCode
                }
            }
        }
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    fun toggleNetworkMode() {
        _isOnline.value = !_isOnline.value
    }

    fun changeLanguage(langCode: String) {
        viewModelScope.launch {
            repository.updateUserLanguage(langCode)
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
        viewModelScope.launch {
            val lessons = allLessons.value
            for (l in lessons) {
                repository.updateLessonDownloaded(l.id, true)
            }
            repository.updateDownloadedOfflineStatus(true)
            // Add downloading XP reward
            repository.updateProfile(userProfile.value!!.copy(xp = userProfile.value!!.xp + 40, hasDownloadedOffline = true))
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

        when (currentLesson.category) {
            "HTML" -> {
                if (currentCode.contains("<h1>") && currentCode.contains("</h1>") && currentCode.lowercase().contains("spaza")) {
                    _simulatorOutput.value = "🚀 Web Emulator Preview:\n✨ Successfully Rendered Header!\nHeading size h1: \"Mam's Spaza Shop\" with warm gold colors."
                    _simulatorSuccess.value = true
                } else if (currentCode.contains("<ul>") && currentCode.contains("</ul>")) {
                    _simulatorOutput.value = "🚀 Web Emulator Preview:\n🛒 Spaza Product Inventory list generated!\nFound elements: Bread, Milk, Rooibos Tea."
                    _simulatorSuccess.value = true
                } else {
                    _simulatorOutput.value = "Web Preview Output:\n--------------------\n" + currentCode + "\n--------------------\nTip: Make sure to wrap headings in <h1>...</h1> or make lists using <ul> and <li>!"
                    _simulatorSuccess.value = false
                }
            }
            "CSS" -> {
                if (currentCode.contains("background-color") && currentCode.contains("color")) {
                    _simulatorOutput.value = "🎨 CSS styling compiled:\n✅ Background set to deep midnight #121212!\n✅ Accent color painted Gold (#FFD700)!"
                    _simulatorSuccess.value = true
                } else {
                    _simulatorOutput.value = "CSS compiler output:\nModified style sheets rules. Use background-color and color to configure colors!"
                    _simulatorSuccess.value = false
                }
            }
            "JavaScript" -> {
                if (currentCode.contains("calculateTotal") && currentCode.contains("18.50")) {
                    _simulatorOutput.value = "⚙️ JavaScript Output:\nR85.00\n\n✅ Code execution compiled!\nCalculates 2 Blue Ribbon bread & 3 Clover milks perfectly (2*18.5 + 3*16 = 37 + 48 = 85)."
                    _simulatorSuccess.value = true
                } else {
                    _simulatorOutput.value = "⚙️ JavaScript Console:\nRunning script...\nResult: Undefined or code incomplete. Write the calculateTotal function!"
                    _simulatorSuccess.value = false
                }
            }
            "Python" -> {
                if (currentCode.contains("temp > 30") && currentCode.contains("print")) {
                    _simulatorOutput.value = "🐍 Python Terminal Output:\nWarning: High Heat! Increase irrigation x2.\n\n✅ Algorithm completed successfully!"
                    _simulatorSuccess.value = true
                } else {
                    _simulatorOutput.value = "🐍 Python IDLE Console:\nError: IndentationError or missing conditional comparison condition temperature > 30."
                    _simulatorSuccess.value = false
                }
            }
        }
    }

    fun completeStep() {
        viewModelScope.launch {
            val currentLesson = _currentActiveLesson.value ?: return@launch
            val steps = _currentActiveSteps.value
            val currentIdx = _currentStepIndex.value
            
            if (currentIdx == steps.size - 1) {
                // Lesson steps finished -> Load interactive quiz questions
                repository.getQuizForLesson(currentLesson.id).collect { questions ->
                    _activeQuizQuestions.value = questions
                    _quizQuestionIndex.value = 0
                    _selectedAnswerIndex.value = -1
                    _quizChecked.value = false
                    _quizCorrect.value = false
                    _quizScore.value = 0
                    _quizFinished.value = false
                    
                    // Trigger navigation to quiz state
                    _currentActiveSteps.value = emptyList() // clear steps to open quiz
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

        if (currentQIdx == questions.size - 1) {
            // End of Quiz
            _quizFinished.value = true
            // Save user progress in Database! This grants XP and unlocks the next lesson.
            viewModelScope.launch {
                val lessonId = _currentActiveLesson.value?.id ?: return@launch
                val passed = _quizScore.value >= (questions.size / 2.0)
                repository.saveUserProgress(
                    lessonId = lessonId,
                    stepIndex = 1, // dummy value marking done
                    completed = passed,
                    quizCompleted = true,
                    score = _quizScore.value
                )
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
        val sanitized = SecurityUtils.sanitizeInput(content, maxLength = 1000)
        if (sanitized.isEmpty()) return

        // Rate limiting: Maximum 6 forum posts per minute
        if (!SecurityUtils.RateLimiter.isAllowed("forum_post", maxRequests = 6, windowMillis = 60_000L)) {
            return
        }

        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val newPost = DiscussionPost(
                id = "post_${System.currentTimeMillis()}",
                author = SecurityUtils.sanitizePlainText(profile.name + " (" + (if (profile.role == "Mama") "Mama" else "Student") + ")", maxLength = 80),
                role = SecurityUtils.sanitizePlainText(profile.role, maxLength = 40),
                content = sanitized,
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

    fun setCustomApiKey(key: String) {
        val trimmed = SecurityUtils.sanitizePlainText(key.trim(), maxLength = 128)
        prefs.edit().putString("custom_gemini_api_key", trimmed).apply()
        _customApiKey.value = trimmed
        GeminiService.setCustomApiKey(trimmed)
    }

    fun toggleGoogleSearch(enabled: Boolean? = null) {
        val next = enabled ?: !_isGoogleSearchEnabled.value
        _isGoogleSearchEnabled.value = next
        prefs.edit().putBoolean("google_search_grounding", next).apply()
        GeminiService.setGoogleSearchGrounding(next)
    }

    fun setSelectedGeminiModel(model: String) {
        _selectedGeminiModel.value = model
        prefs.edit().putString("selected_gemini_model", model).apply()
        GeminiService.setActiveModel(model)
    }

    fun searchGoogleDatabase(query: String) {
        if (query.isBlank()) return
        sendAiChat("Search Google database and explain: $query")
    }

    // AI chat - send message (Online Gemini AI + Google Search Database Grounding + Unrestricted local engine)
    fun sendAiChat(messageText: String) {
        if (messageText.trim().isEmpty()) return
        viewModelScope.launch {
            val userMsg = MentorChat(isAi = true, isUser = true, messageText = messageText)
            repository.insertChatMessage(userMsg)

            _aiGenerating.value = true

            val currentLang = _aiLanguageCode.value
            val langDirective = if (currentLang != "auto") {
                "ACTIVE SOUTH AFRICAN LANGUAGE: The user has selected language code '$currentLang'. You MUST deliver your complete answer in that language, removing the English barrier entirely."
            } else {
                "MULTILINGUAL INSTRUCTION: You support all 12 South African official languages (isiZulu, isiXhosa, Afrikaans, Sepedi, Setswana, Sesotho, Xitsonga, siSwati, Tshivenda, isiNdebele, SASL, English). Detect the language used by the user and respond in that exact language with rich cultural and technical fluency, eliminating the English barrier."
            }

            val systemPrompt = """You are KodeMamas AI Assistant, built specifically to eliminate the English language barrier in computer science and tech education across South African communities.
You thoroughly explain and answer ANY question asked by the user without arbitrary restrictions.

$langDirective

Scope & Capabilities:
- Answer questions on ALL subjects: programming, computer science, software engineering, mathematics, science, technology, career coaching, job readiness, CV/resume building, business, general knowledge, languages (especially all 12 South African official languages), and everyday problem-solving.
- When writing code or technical explanations, provide comprehensive, production-ready, clear examples with explanatory walkthroughs in their chosen mother tongue.
- Ground your answers in accurate, real-time facts from Google Search database when applicable.
- Do NOT refuse to answer, do NOT restrict your knowledge to only beginner coding, and do NOT artificially limit your response.
- Provide in-depth insights for advanced inquiries, and clear clarity for fundamentals. Support South African township, rural, and urban contexts naturally whenever relevant."""

            val aiResponse = GeminiService.generateResponse(
                prompt = messageText,
                systemInstruction = systemPrompt,
                enableGoogleSearch = _isGoogleSearchEnabled.value,
                isOnline = _isOnline.value,
                languageCode = currentLang
            )
            repository.insertChatMessage(MentorChat(isAi = true, isUser = false, messageText = aiResponse))
            _aiGenerating.value = false
        }
    }

    fun clearAiMessages() {
        viewModelScope.launch {
            repository.clearAiChats()
            repository.insertChatMessage(
                MentorChat(
                    isAi = true,
                    isUser = false,
                    messageText = """Key professionals who use it include:
1. **Business & Systems Analysts:**
They examine it to see if the idea is realistic (feasibility analysis) and start planning requirements.
2. **IT Steering Committee & Project Managers:** Decision-makers who review it to say, *"Yes, let's fund and build this!"* or *"Not right now."*
3. **Developers & Designers:** They reference it to understand the big-picture purpose before building.

Think like a tech leader: If you were creating a system request to solve a challenge in your own community, what problem would you address at your local spaza shop, clinic, or school?"""
                )
            )
        }
    }

    fun updateProfileName(newName: String) {
        val cleanName = SecurityUtils.sanitizePlainText(newName, maxLength = 80)
        if (cleanName.isBlank()) return
        val current = userProfile.value ?: return
        viewModelScope.launch {
            repository.updateProfile(current.copy(name = cleanName))
        }
    }

    fun toggleDataSaving(enabled: Boolean) {
        val current = userProfile.value ?: return
        viewModelScope.launch {
            repository.updateProfile(current.copy(dataSavingMode = enabled))
        }
    }

    // 1-on-1 mentorship chat with tech mentor
    fun sendMentorChat(messageText: String) {
        if (messageText.trim().isEmpty()) return
        viewModelScope.launch {
            val userMsg = MentorChat(isAi = false, isUser = true, messageText = messageText)
            repository.insertChatMessage(userMsg)

            _mentorTyping.value = true
            delay(2000) // Realistic typing status

            val replies = listOf(
                "That is a great question! I recommend reviewing Lesson 1 for foundational HTML and CSS structure first, it will clarify that syntax.",
                "Excellent progress! Remember to save your lessons offline so you can practice even when loadshedding strikes or data is low. Let me know if you'd like your CV draft reviewed!",
                "Ngiyabonga for your question! You are building solid algorithmic thinking. Keep practicing the daily coding challenges!",
                "Dumelang & Sanibonani! I am reviewing your code submissions. Solo founder Nokwazi Nobuhle Xaba in Bloemfontein is cheering you on. Keep building and innovating!"
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

    fun claimPremiumUpgrade() {
        activatePlan("PREMIUM", "PROMO_TRIAL")
    }

    fun cancelPremium() {
        activatePlan("FREE", "")
    }

    fun setThemeMode(mode: String) {
        val validMode = when (mode.uppercase()) {
            "DARK", "LIGHT" -> mode.uppercase()
            else -> "SYSTEM"
        }
        prefs.edit().putString("theme_mode", validMode).apply()
        _themeMode.value = validMode
    }

    fun submitRating(rating: Int, comment: String = "") {
        val bounded = rating.coerceIn(1, 5)
        val cleanComment = SecurityUtils.sanitizeInput(comment, maxLength = 500)
        prefs.edit().putInt("user_rating", bounded).apply()
        _userRating.value = bounded
        if (cleanComment.isNotBlank()) {
            viewModelScope.launch {
                repository.addDiscussionPost(
                    DiscussionPost(
                        id = "rating_${System.currentTimeMillis()}",
                        author = SecurityUtils.sanitizePlainText(userProfile.value?.name ?: "Community Learner", maxLength = 80),
                        role = SecurityUtils.sanitizePlainText(userProfile.value?.role ?: "Student", maxLength = 40),
                        content = "⭐ Rated KodeMamas $bounded/5 Stars: \"$cleanComment\"",
                        timestamp = System.currentTimeMillis(),
                        likes = 5,
                        commentCount = 1,
                        languageCode = currentLanguageCode.value
                    )
                )
            }
        }
    }

    fun submitFeedback(category: String, message: String) {
        val cleanCat = SecurityUtils.sanitizePlainText(category, maxLength = 50)
        val cleanMsg = SecurityUtils.sanitizeInput(message, maxLength = 1000)
        if (cleanMsg.isBlank()) return
        if (!SecurityUtils.RateLimiter.isAllowed("feedback_submit", maxRequests = 5, windowMillis = 60_000L)) {
            return
        }
        viewModelScope.launch {
            repository.addDiscussionPost(
                DiscussionPost(
                    id = "feedback_${System.currentTimeMillis()}",
                    author = SecurityUtils.sanitizePlainText(userProfile.value?.name ?: "Community Learner", maxLength = 80),
                    role = SecurityUtils.sanitizePlainText(userProfile.value?.role ?: "Student", maxLength = 40),
                    content = "[$cleanCat] $cleanMsg",
                    timestamp = System.currentTimeMillis(),
                    likes = 1,
                    commentCount = 0,
                    languageCode = currentLanguageCode.value
                )
            )
            _feedbackSubmitted.value = true
        }
    }

    fun dismissFeedbackNotice() {
        _feedbackSubmitted.value = false
    }

    // ---------------------- FIREBASE PEOPLE OPERATIONS ----------------------
    fun syncFirebasePeople() {
        viewModelScope.launch {
            firebaseService.syncWithFirebase()
        }
    }

    fun addPersonToFirebase(person: PersonEntity) {
        viewModelScope.launch {
            firebaseService.pushPersonToFirebase(person)
        }
    }

    fun togglePersonFavorite(id: String) {
        viewModelScope.launch {
            firebaseService.toggleFavorite(id)
        }
    }

    fun updateFirebaseConfig(url: String, projectId: String) {
        // Protect admin routes: Verify user has administrative access
        val profile = userProfile.value
        if (!SecurityUtils.hasAdminAccess(profile?.role, profile?.email)) {
            android.util.Log.w("SecurityAdmin", "Unauthorized attempt to modify Firebase backend configuration")
            return
        }
        firebaseService.updateFirebaseConfig(url, projectId)
    }

    fun resetFirebaseConfig() {
        firebaseService.resetToDefaults()
    }

    fun publishCurrentUserToFirebase(
        role: String = "Mama in Tech",
        township: String = "Bloemfontein Hub",
        province: String = "Free State",
        bio: String = "Passionate about learning coding and offline digital skills at KodeMamas.",
        skills: String = "HTML5, CSS3, Python, Android",
        isMentor: Boolean = false
    ) {
        val current = userProfile.value ?: return
        val safeEmailId = (if (current.email.isNotBlank()) current.email else "user_${System.currentTimeMillis()}")
            .replace("@", "_")
            .replace(".", "_")

        val person = PersonEntity(
            id = safeEmailId,
            name = current.name,
            email = current.email,
            role = role,
            townshipOrCity = township,
            province = province,
            primaryLanguage = currentLanguageCode.value,
            bio = bio,
            skills = skills,
            isMentorAvailable = isMentor,
            xp = current.xp,
            streak = current.streak,
            badge = if (current.isPremium) "Premium Graduate 👑" else "Township Learner ⭐",
            lastActive = System.currentTimeMillis(),
            isVerified = true,
            isFavorite = false,
            syncedWithFirebase = true
        )
        viewModelScope.launch {
            firebaseService.pushPersonToFirebase(person)
        }
    }
}
