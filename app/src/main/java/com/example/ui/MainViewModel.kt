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

    fun sendBuddyMessage(buddyId: String, text: String, sharedResourceTitle: String = "", sharedResourceCode: String = "") {
        viewModelScope.launch {
            val msg = BuddyMessage(
                buddyId = buddyId,
                senderId = "me",
                messageText = text,
                sharedResourceTitle = sharedResourceTitle,
                sharedResourceCode = sharedResourceCode
            )
            repository.insertBuddyMessage(msg)
            
            // Highlight interactive responses in native/South African flavors to make them super immersive
            delay(1500)
            val replyText = when {
                sharedResourceTitle.isNotEmpty() -> "Wow, thank you so much for sharing '${sharedResourceTitle}'! This will help me immensely in my storefront too. Siyabonga kakhulu!"
                text.lowercase().contains("molo") || text.lowercase().contains("hello") || text.lowercase().contains("yebo") || text.lowercase().contains("dumelang") -> 
                    "Dumela! Thank you for connecting with me. How are your lessons going? Let's check our code and build something great! 🇿🇦"
                text.lowercase().contains("help") || text.lowercase().contains("stuck") || text.lowercase().contains("code") || text.lowercase().contains("compiler") ->
                    "Don't worry sister! Double check your tags. Make sure your closing tags have the slash like </h1> or </ul>. I am always online to help review! 👩‍💻"
                else -> "This is awesome! Let's make sure we keep up our daily streak and finish our next coding challenges together. Step by step!"
            }
            val replyMsg = BuddyMessage(
                buddyId = buddyId,
                senderId = buddyId,
                messageText = replyText
            )
            repository.insertBuddyMessage(replyMsg)
        }
    }

    init {
        viewModelScope.launch {
            // Populate database if empty
            repository.prepopulateDatabaseIfEmpty()
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
                }
            }
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
        viewModelScope.launch {
            sharedPrefs.edit().putString("selected_language_code", langCode).apply()
            val existing = userProfile.value
            val updated = existing?.copy(
                name = name,
                role = role,
                languageCode = langCode,
                xp = newXp
            ) ?: UserProfile(
                id = 1,
                name = name,
                email = "${name.lowercase().replace(" ", "")}@kodemamas.org",
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
        viewModelScope.launch {
            val lessons = allLessons.value
            for (l in lessons) {
                repository.updateLessonDownloaded(l.id, true)
            }
            repository.updateDownloadedOfflineStatus(true)
            // Add downloading XP reward
            repository.updateProfile(userProfile.value!!.copy(xp = userProfile.value!!.xp + 40, hasDownloadedOffline = true))
            completeOnboardingPhase("admit")
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

        if (_simulatorSuccess.value) {
            completeOnboardingPhase("activate")
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
        if (content.trim().isEmpty()) return
        viewModelScope.launch {
            val profile = userProfile.value ?: return@launch
            val newPost = DiscussionPost(
                id = "post_${System.currentTimeMillis()}",
                author = profile.name + " (" + (if (profile.role == "Mama") "Mama" else "Student") + ")",
                role = profile.role,
                content = content,
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
        if (messageText.trim().isEmpty()) return
        viewModelScope.launch {
            val userMsg = MentorChat(isAi = true, isUser = true, messageText = messageText)
            repository.insertChatMessage(userMsg)

            _aiGenerating.value = true

            // Generate response
            if (!_isOnline.value) {
                delay(1200) // fast realistic delay offline simulation
                val fallbackReply = generateOfflineRecommendation(messageText)
                repository.insertChatMessage(MentorChat(isAi = true, isUser = false, messageText = fallbackReply))
                _aiGenerating.value = false
            } else {
                // Create context prompt
                val systemPrompt = "You are an empathetic, patient, and highly encouraging AI Tutor for KodeMamas. Your students are South African township mothers and young girls learning to code. You are fluent in all 11 official South African languages (English, Zulu, Xhosa, Afrikaans, Sepedi, Setswana, Sesotho, Xitsonga, siSwati, Tshivenda, isiNdebele) and South African Sign Language (SASL) notation. You MUST respond in the language the student addresses you in, or if they ask to explain something in any of the 12 languages, do so warmly. Explain the login/account creation screen (name input, selecting from 12 South African languages, and choosing role of Mama, Student, or Mentor) and explain the content after logging in (the Dashboard with 100-Day Onboarding Phase tracker and visual wins; the Learn tab with 10 interactive lessons; the Code compiler simulator; the Community forum; and study Buddies) in their requested language. Never write code for them directly; guide them step-by-step. Keep explanations short, simple, and under 150 words."
                val aiResponse = GeminiService.generateResponse(messageText, systemPrompt)
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
        if (messageText.trim().isEmpty()) return
        viewModelScope.launch {
            val userMsg = MentorChat(isAi = false, isUser = true, messageText = messageText)
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
