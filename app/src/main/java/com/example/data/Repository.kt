package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class Repository(private val db: AppDatabase) {

    val userProfile: Flow<UserProfile?> = db.userDao().getUserProfile()
    val allLessons: Flow<List<Lesson>> = db.lessonDao().getAllLessons()
    val allProgress: Flow<List<UserProgress>> = db.progressDao().getAllProgress()
    val allChallenges: Flow<List<CodingChallenge>> = db.challengeDao().getAllChallenges()
    val allPosts: Flow<List<DiscussionPost>> = db.discussionDao().getAllPosts()
    val aiChats: Flow<List<MentorChat>> = db.chatDao().getAiChats()
    val mentorChats: Flow<List<MentorChat>> = db.chatDao().getMentorChats()

    fun getStepsForLesson(lessonId: String): Flow<List<LessonStep>> =
        db.lessonStepDao().getStepsForLesson(lessonId)

    fun getQuizForLesson(lessonId: String): Flow<List<QuizQuestion>> =
        db.quizDao().getQuizForLesson(lessonId)

    fun getProgressForLesson(lessonId: String): Flow<UserProgress?> =
        db.progressDao().getProgressForLesson(lessonId)

    suspend fun getLessonById(id: String): Lesson? = db.lessonDao().getLessonById(id)

    suspend fun updateProfile(profile: UserProfile) {
        db.userDao().insertOrUpdateProfile(profile)
    }

    suspend fun updateChallengeStatus(id: String, completed: Boolean) {
        db.challengeDao().updateChallengeStatus(id, completed)
        if (completed) {
            // Award XP for completing offline daily challenge
            db.userDao().updateXpAndStreak(xpGained = 20, newStreak = 1)
        }
    }

    suspend fun addDiscussionPost(post: DiscussionPost) {
        db.discussionDao().insertPost(post)
    }

    suspend fun incrementPostLikes(postId: String) {
        db.discussionDao().incrementLikes(postId)
    }

    suspend fun insertChatMessage(message: MentorChat) {
        db.chatDao().insertMessage(message)
    }

    suspend fun clearAiChats() {
        db.chatDao().clearChats(isAi = true)
    }

    suspend fun updateLessonDownloaded(lessonId: String, downloaded: Boolean) {
        db.lessonDao().updateDownloadedStatus(lessonId, downloaded)
    }

    suspend fun updateLessonUnlocked(lessonId: String, unlocked: Boolean) {
        db.lessonDao().updateUnlockedStatus(lessonId, unlocked)
    }

    suspend fun updateUserLanguage(langCode: String) {
        db.userDao().updateLanguage(langCode)
    }

    suspend fun updateDataSaving(enabled: Boolean) {
        db.userDao().updateDataSaving(enabled)
    }

    suspend fun updateDownloadedOfflineStatus(enabled: Boolean) {
        db.userDao().updateDownloadedOfflineStatus(enabled)
    }

    suspend fun updatePremiumStatus(isPremium: Boolean) {
        db.userDao().updatePremiumStatus(isPremium)
    }

    suspend fun saveUserProgress(
        lessonId: String,
        stepIndex: Int,
        completed: Boolean,
        quizCompleted: Boolean,
        score: Int = 0
    ) {
        val existing = db.progressDao().getProgressForLessonSynchronous(lessonId)
        val newProgress = UserProgress(
            lessonId = lessonId,
            currentStepIndex = stepIndex,
            isCompleted = completed || (existing?.isCompleted ?: false),
            quizCompleted = quizCompleted || (existing?.quizCompleted ?: false),
            score = if (score > 0) score else (existing?.score ?: 0),
            lastUpdated = System.currentTimeMillis()
        )
        db.progressDao().insertOrUpdateProgress(newProgress)

        // Give XP if completed
        if (completed && !(existing?.isCompleted ?: false)) {
            db.userDao().updateXpAndStreak(xpGained = 50, newStreak = 1)
            // auto-unlock next lesson
            unlockNextLesson(lessonId)
        }
        if (quizCompleted && !(existing?.quizCompleted ?: false)) {
            db.userDao().updateXpAndStreak(xpGained = 30, newStreak = 1)
        }
    }

    private suspend fun unlockNextLesson(currentLessonId: String) {
        val nextId = when (currentLessonId) {
            "html_1" -> "css_2"
            "css_2" -> "js_3"
            "js_3" -> "python_4"
            else -> ""
        }
        if (nextId.isNotEmpty()) {
            db.lessonDao().updateUnlockedStatus(nextId, unlocked = true)
        }
    }

    suspend fun prepopulateDatabaseIfEmpty() {
        val existingProfile = db.userDao().getUserProfileSynchronous()
        if (existingProfile != null) return // Already populated

        // 1. Enter default User Profile
        db.userDao().insertOrUpdateProfile(
            UserProfile(
                name = "Naledi",
                email = "naledi@kodemamas.org",
                role = "Mama", // Mama, Student, Girl
                languageCode = "en",
                streak = 3,
                xp = 180,
                dataSavingMode = false,
                isPremium = false,
                hasDownloadedOffline = false
            )
        )

        // 2. Prepopulate 4 standard lessons
        val defaultLessons = listOf(
            Lesson("html_1", "Intro to HTML (Web Layout)", "Mam's Spaza Shop Storefront", "HTML", "Beginner", 10, isUnlocked = true, isDownloaded = true, orderIndex = 1),
            Lesson("css_2", "Adding Style with CSS", "Beautifying Your Online Catalog", "CSS", "Beginner", 15, isUnlocked = false, isDownloaded = false, orderIndex = 2),
            Lesson("js_3", "Interactive JS Calculations", "Calculating Bread & Veggie Orders", "JavaScript", "Beginner", 20, isUnlocked = false, isDownloaded = false, orderIndex = 3),
            Lesson("python_4", "Python Crop Agriculture Tracker", "Harvesting & Pricing Predictions", "Python", "Beginner", 25, isUnlocked = false, isDownloaded = false, orderIndex = 4)
        )
        db.lessonDao().insertLessons(defaultLessons)

        // 3. Prepopulate Lesson Steps
        val defaultSteps = listOf(
            // HTML
            LessonStep(
                "html_1_s1", "html_1", 1,
                "Welcome to HTML!",
                "HTML stands for HyperText Markup Language. It's the bone structure of every website! Today, we will code a responsive digital storefront for **Mam's Spaza shop** in Soweto. Click next to start setting up our shelf structure.",
                "Siyakwamukela! HTML yakha amathambo ewebhusayithi. Namuhla sizokwakha isitolo sika-Mama esiku-inthanethi.",
                "", "READ", "Click Next to continue!"
            ),
            LessonStep(
                "html_1_s2", "html_1", 2,
                "Writing Our First Header (<h1>)",
                "The `<h1>` tag defines the main and largest heading on your page. Inside your editor layout, write `<h1>Mam's Spaza Shop</h1>` to give our website a grand, colorful banner at the top!",
                "Bhala isihloko esikhulu sesitolo usebenzisa u-`<h1>` ukuze sitolise iwebhusayithi yakho.",
                "<h1>Mam's Spaza Shop</h1>", "RUN_CODE", "Type <h1>Mam's Spaza Shop</h1> in the simulator."
            ),
            LessonStep(
                "html_1_s3", "html_1", 3,
                "Listing Product Offerings (<p> & <ul>)",
                "To show paragraph text, we use `<p>`. To display a list, we use `<ul>` (unordered list) alongside `<li>` (list item) tags. Let's list what we have: Bread, Milk, and Rooibos tea! Type a product listing below.",
                "Dala uhlu lwemikhiqizo njengesinkwa nobisi usebenzisa amathegi e-`<ul>` ne-`<li>`.",
                "<p>Our Fresh Daily Stock:</p>\n<ul>\n  <li>Blue Ribbon Bread</li>\n  <li>Fresh Clover Milk</li>\n  <li>Rooibos Tea Bags</li>\n</ul>", "RUN_CODE", "Add <p> and <ul> tags."
            ),

            // CSS
            LessonStep(
                "css_2_s1", "css_2", 1,
                "Styling Core Concepts",
                "CSS (Cascading Style Sheets) controls how elements look – colors, margins, fonts, and borders. Let's bring beautiful African warmth to Mam's shop with a Purple and Gold startup theme!",
                "I-CSS ishuna imibala nefomethi yewebhusayithi. Uhlelo lwethu lokusebenza luhlobe ngemibala yama-Khosi (Omnyama, Ophephuli noGolide).",
                "", "READ", "Learn CSS basic styling rule"
            ),
            LessonStep(
                "css_2_s2", "css_2", 2,
                "Modifying Colors (color & background)",
                "To set text color to gold, use `color: gold;`. To set a rich dark background, write `background-color: #121212;` inside the CSS body selector. Let's write the CSS styling rule now!",
                "Faka umbala wegolide no-background omnyama kwi-webhusayithi yakho.",
                "body {\n  background-color: #121212;\n  color: #FFD700; /* Gold */\n  font-family: sans-serif;\n}", "RUN_CODE", "Use background-color and color selectors."
            ),

            // JavaScript
            LessonStep(
                "js_3_s1", "js_3", 1,
                "What is JavaScript?",
                "JavaScript makes websites interactive! It allows us to calculate purchases, update total price, and save orders without refreshing. Let's code a quick cart calculator for Mam's Spaza Shop.",
                "U-JavaScript wenza isizindalwazi sisebenze. Sizobala intengo yesinkwa nezithelo.",
                "", "READ", "Understand JS variables and logic"
            ),
            LessonStep(
                "js_3_s2", "js_3", 2,
                "Writing Your First Calculator Function",
                "Let's write a function to calculate total order prices! We define variables with `const` and calculate the total. Type the JS function below:",
                "Bhala umsebenzi ka-JS obala i-total yamakhasimende ethu.",
                "function calculateTotal(breadQty, milkQty) {\n  const breadPrice = 18.50; // Rand (R)\n  const milkPrice = 16.00;  // Rand (R)\n  return (breadQty * breadPrice) + (milkQty * milkPrice);\n}\nconsole.log(\"R\" + calculateTotal(2, 3));", "RUN_CODE", "Develop calculation formula in Rand"
            ),

            // Python
            LessonStep(
                "python_4_s1", "python_4", 1,
                "Python for Smart Farming",
                "Python is fantastic for calculating crop harvest and market prices! In rural South Africa, farmers use data to forecast crop yield. Today we will build a simplified crop pricing estimator in Python.",
                "I-Python iwusizo kakhulu ekubaleni isivuno semikhakha yezolimo ezabelweni zasekhaya.",
                "", "READ", "Basic python syntax and prints"
            ),
            LessonStep(
                "python_4_s2", "python_4", 2,
                "Conditional Pricing Estimator (if/else)",
                "Let's write an algorithm that checks temperature and tells us if corn needs more water: If temperature is > 30 degrees Celsius, crop watering must double. Type the script below:",
                "Bhala i-Python ehlola izinga lokushisa lenhlabathi.",
                "temp = 32\nif temp > 30:\n    print(\"Warning: High Heat! Increase irrigation x2.\")\nelse:\n    print(\"Normal climate. Maintain standard water flow.\")", "RUN_CODE", "Write conditional statements in Python."
            )
        )
        db.lessonStepDao().insertSteps(defaultSteps)

        // 4. Prepopulate Quiz Questions
        val defaultQuizzes = listOf(
            // HTML Quiz
            QuizQuestion(
                "q_html_1", "html_1",
                "What tag is used to write the largest headers or website titles in HTML?",
                "Yiliphi ithegi elisetshenziselwa izihloko ezinkulu ku-HTML?",
                "<p>", "<h1>", "<img>", "<body>", 1, // index 1 is <h1>
                "<h1> is the largest heading tag. Lower headings use <h2> down to <h6>."
            ),
            QuizQuestion(
                "q_html_2", "html_1",
                "Which tag should you use to bundle individual list items (<li>) into an unnumbered list?",
                "Yiliphi ithegi elakha uhlu olungena-nombolo lomkhiqizo?",
                "<ul>", "<ol>", "<p>", "<a>", 0, // index 0 is <ul>
                "<ul> stands for Unordered List, which creates bullet points around list items (<li>)."
            ),

            // CSS Quiz
            QuizQuestion(
                "q_css_1", "css_2",
                "Which property is used in CSS to declare text colors?",
                "Iyiphi i-property eshintsha umbala wombhalo ku-CSS?",
                "font-color", "background-color", "color", "text-paint", 2,
                "The 'color' property regulates text color directly in Cascading Style Sheets."
            ),

            // JS Quiz
            QuizQuestion(
                "q_js_1", "js_3",
                "How do we declare constant variables in modern JavaScript?",
                "Sizimisa kanjani izinto ezingashintshi (constants) ku-JS?",
                "var", "let", "const", "def", 2,
                "'const' is used for modern block-scoped, non-reassignable constant variables."
            ),

            // Python Quiz
            QuizQuestion(
                "q_python_1", "python_4",
                "What is the correct indentation requirement for 'if' statements in Python code?",
                "Uhlelo lwe-Python lusebenzisa ini ukuze luhlukanise izinkomba (if block)?",
                "Curly braces {}", "Semi-colons ;", "Tabs or 4 Spaces", "Parentheses ()", 2,
                "Python uses whitespace/tabs indentation instead of curly braces to establish code blocks."
            )
        )
        db.quizDao().insertQuizQuestions(defaultQuizzes)

        // 5. Prepopulate Coding Challenges
        val defaultChallenges = listOf(
            CodingChallenge(
                "chall_1", "Mama's Bakery Order System",
                "Mama Thoko makes fresh township muffins in Umlazi. She charges R8 per muffin. Calculate total price for 12 muffins in JS.",
                "Create a variable 'pricePerMuffin' equal to 8, and 'quantity' equal to 12. Log the total R-value (quantity * pricePerMuffin) using console.log().",
                "const pricePerMuffin = 8;\nconst quantity = 12;\nconsole.log(pricePerMuffin * quantity);",
                "96", false, "Beginner"
            ),
            CodingChallenge(
                "chall_2", "Spaza Shop Discount Checker",
                "Write a Python script that prints 'Discount Approved!' if spending is over R150.",
                "Create a variable 'spending = 180'. Print 'Discount Approved!' if spending is greater than 150.",
                "spending = 180\nif spending > 150:\n    print(\"Discount Approved!\")",
                "Discount Approved!", false, "Easy"
            )
        )
        db.challengeDao().insertChallenges(defaultChallenges)

        // 6. Prepopulate Discussion Posts
        val defaultPosts = listOf(
            DiscussionPost(
                "post_1", "Mama Thandi (Soweto)", "Mama",
                "Hao everyone! I just launched my Spaza storefront after Lesson 2! I put my address and pictures of my fresh archar and bread on my HTML profile. Two customers called me today saying they saw my prices online! Ngiyabonga KodeMamas!",
                System.currentTimeMillis() - 3600000 * 2, 28, 6, "zu"
            ),
            DiscussionPost(
                "post_2", "Zola Ndlovu", "Mentor",
                "Fantastic job Mama Thandi! This is exactly why we built this. For anyone struggling with the CSS background color in Umlazi, remember to include the hashtag (#) before hex codes in your styling block, like background: #3c0c54.",
                System.currentTimeMillis() - 3600000 * 5, 14, 2, "en"
            ),
            DiscussionPost(
                "post_3", "Simphiwe (Khayelitsha)", "Student",
                "Is anybody learning Python here? I am a matric student trying to use Python to build a small weather prediction alert for my grandmother's community crop garden. Let's study together!",
                System.currentTimeMillis() - 3600000 * 12, 19, 8, "xh"
            )
        )
        for (post in defaultPosts) {
            db.discussionDao().insertPost(post)
        }

        // 7. Add primary greeting messages of mentor helper
        db.chatDao().insertMessage(
            MentorChat(
                isAi = true,
                isUser = false,
                messageText = "Sanibonani! Dumelang! Hello Mama, sister and student! 👋 I am your KodeMamas AI Assistant. You can ask me anything about HTML, CSS, JavaScript, or Python in your language. Let's make coding simple together! How can I help you today?"
            )
        )
        db.chatDao().insertMessage(
            MentorChat(
                isAi = false,
                isUser = false,
                messageText = "Hello from Johannesburg! I am Mentor Akhona, your human tech buddy. Welcome to KodeMamas! I am matched with you to review your CV, assist in getting job-ready, and answer coding blockages. Leave a message here anytime!"
            )
        )
    }
}
