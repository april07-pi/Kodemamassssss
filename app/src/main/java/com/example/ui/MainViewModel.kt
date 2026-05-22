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
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Chat states
    private val _aiGenerating = MutableStateFlow(false)
    val aiGenerating: StateFlow<Boolean> = _aiGenerating.asStateFlow()

    private val _mentorTyping = MutableStateFlow(false)
    val mentorTyping: StateFlow<Boolean> = _mentorTyping.asStateFlow()

    // Selected challenge in Daily Challenge View
    private val _activeChallenge = MutableStateFlow<CodingChallenge?>(null)
    val activeChallenge: StateFlow<CodingChallenge?> = _activeChallenge.asStateFlow()

    init {
        viewModelScope.launch {
            // Populate database if empty
            repository.prepopulateDatabaseIfEmpty()
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
                val systemPrompt = "You are KodeMamas AI chatbot, a warm, beginner-friendly South African tech mentor teaching mothers, students, and community members in township & rural areas to code from scratch in simple, culturally relatable ways. If possible, respond nicely, and include short South African touchpoints like 'Halala', 'Ngiyabonga', 'Sharp sharp', or Zulu/Xhosa basic phrases depending on what's asked. Keep coding blocks simple, short and easy for low-end mobile screens."
                val aiResponse = GeminiService.generateResponse(messageText, systemPrompt)
                repository.insertChatMessage(MentorChat(isAi = true, isUser = false, messageText = aiResponse))
                _aiGenerating.value = false
            }
        }
    }

    private fun generateOfflineRecommendation(query: String): String {
        val text = query.lowercase()
        return when {
            text.contains("html") -> "Sanibonani! I am in Offline Mode right now 🔌 Here is a fast local tip: HTML is built using Tags. An element like a heading is written with `<h1>Siyakwamukela!</h1>` which stands for Header 1. Connect to mobile data to ask me complex HTML layout questions!"
            text.contains("css") -> "Dumela mama/student! In Offline local storage, here is the answer: CSS styles websites! Use `body { background-color: purple; color: gold; }` to make your website match the gorgeous KodeMamas startup layout!"
            text.contains("javascript") || text.contains("js") -> "Hello sister! I am in Offline/Local caching. JS controls calculations and interactive elements. E.g. `const total = quantity * price;` is the classic formula to calculate Spaza shop inventory checkout totals."
            text.contains("python") -> "Hello! While offline, remember Python is loved for its simple structure. E.g. print(\"Harvesting Smart predictive analysis!\"). Python relies heavily on tabs/spaces indentation, so watch out for IndentationErrors!"
            else -> "Hello! I am in Offline Local mode. You can access all 4 language-translated interactive coding classes, read quizzes, and run local compilers offline! Connect to the internet for real-time generative help."
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

    // 1-on-1 mentorship chat with Akhona
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
                "Molo student! I am currently checking coding logs from Soweto. Your profile looks amazing. Continue building and creating!"
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
        viewModelScope.launch {
            repository.updatePremiumStatus(true)
        }
    }

    fun cancelPremium() {
        viewModelScope.launch {
            repository.updatePremiumStatus(false)
        }
    }
}
