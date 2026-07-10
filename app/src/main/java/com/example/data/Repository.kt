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
    val allBuddies: Flow<List<Buddy>> = db.buddyDao().getAllBuddies()

    fun getMessagesForBuddy(buddyId: String): Flow<List<BuddyMessage>> =
        db.buddyDao().getMessagesForBuddy(buddyId)

    suspend fun updateBuddyConnection(buddyId: String, connected: Boolean) {
        db.buddyDao().updateConnectionStatus(buddyId, connected)
    }

    suspend fun insertBuddyMessage(message: BuddyMessage) {
        db.buddyDao().insertBuddyMessage(message)
    }

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

    suspend fun updatePlusStatus(isPlus: Boolean) {
        db.userDao().updatePlusStatus(isPlus)
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
        if (existingProfile == null) {
            // 1. Enter default User Profile
            db.userDao().insertOrUpdateProfile(
                UserProfile(
                    name = "Nokwazi",
                    email = "nokwazi@kodemamas.org",
                    role = "Mama", // Mama, Student, Girl
                    languageCode = "en",
                    streak = 3,
                    xp = 180,
                    dataSavingMode = false,
                    isPremium = false,
                    isPlus = false,
                    hasDownloadedOffline = false
                )
            )
        }

        // Check if lessons are fully populated (at least 10 lessons required)
        val existingLessons = db.lessonDao().getAllLessonsList()
        if (existingLessons.size < 10) {
            // 2. Prepopulate 10 standard lessons (incorporating small business plan & coding foundations)
            val defaultLessons = listOf(
                Lesson("html_1", "Intro to HTML (Web Layout)", "Mam's Spaza Shop Storefront", "HTML", "Beginner", 10, isUnlocked = true, isDownloaded = true, orderIndex = 1),
                Lesson("css_2", "Adding Style with CSS", "Beautifying Your Online Catalog", "CSS", "Beginner", 15, isUnlocked = false, isDownloaded = false, orderIndex = 2),
                Lesson("js_3", "Interactive JS Calculations", "Calculating Bread & Veggie Orders", "JavaScript", "Beginner", 20, isUnlocked = false, isDownloaded = false, orderIndex = 3),
                Lesson("python_4", "Python Crop Agriculture Tracker", "Harvesting & Pricing Predictions", "Python", "Beginner", 25, isUnlocked = false, isDownloaded = false, orderIndex = 4),
                Lesson("html_5", "StoryBrand Landing Page Layout", "Create High-Converting Headers", "HTML", "Beginner", 12, isUnlocked = false, isDownloaded = false, orderIndex = 5),
                Lesson("css_6", "Visual Branding & Team Roles", "Color-Coding SOPs & Responsibilities", "CSS", "Beginner", 14, isUnlocked = false, isDownloaded = false, orderIndex = 6),
                Lesson("js_7", "Spaza Profit Margin & Cash Flow Calculator", "Recession-Proof Income Trackers", "JavaScript", "Beginner", 18, isUnlocked = false, isDownloaded = false, orderIndex = 7),
                Lesson("html_8", "Customer Feedback Form Setup", "Input Elements for Customer Lead Captures", "HTML", "Beginner", 15, isUnlocked = false, isDownloaded = false, orderIndex = 8),
                Lesson("python_9", "90-Day Predictive Revenue Planner", "Systems & Loop-Based Growth Trackers", "Python", "Beginner", 22, isUnlocked = false, isDownloaded = false, orderIndex = 9),
                Lesson("js_10", "SOP Task Automation Checklist Dashboard", "Scaling Spaza Systems Digitally", "JavaScript", "Beginner", 20, isUnlocked = false, isDownloaded = false, orderIndex = 10)
            )
            db.lessonDao().insertLessons(defaultLessons)

            // 3. Prepopulate Lesson Steps
            val defaultSteps = listOf(
                // HTML 1
                LessonStep("html_1_s1", "html_1", 1, "Welcome to HTML!", "HTML stands for HyperText Markup Language. It's the bone structure of every website! Today, we will code a responsive digital storefront for **Mam's Spaza shop** in Bloemfontein. Click next to start setting up our shelf structure.", "Siyakwamukela! HTML yakha amathambo ewebhusayithi. Namuhla sizokwakha isitolo sika-Mama esiku-inthanethi.", "", "READ", "Click Next to continue!"),
                LessonStep("html_1_s2", "html_1", 2, "Writing Our First Header (<h1>)", "The `<h1>` tag defines the main and largest heading on your page. Inside your editor layout, write `<h1>Mam's Spaza Shop</h1>` to give our website a grand, colorful banner at the top!", "Bhala isihloko esikhulu sesitolo usebenzisa u-`<h1>` ukuze sitolise iwebhusayithi yakho.", "<h1>Mam's Spaza Shop</h1>", "RUN_CODE", "Type <h1>Mam's Spaza Shop</h1> in the simulator."),
                LessonStep("html_1_s3", "html_1", 3, "Listing Product Offerings (<p> & <ul>)", "To show paragraph text, we use `<p>`. To display a list, we use `<ul>` (unordered list) alongside `<li>` (list item) tags. Let's list what we have: Bread, Milk, and Rooibos tea! Type a product listing below.", "Dala uhlu lwemikhiqizo njengesinkwa nobisi usebenzisa amathegi e-`<ul>` ne-`<li>`.", "<p>Our Fresh Daily Stock:</p>\n<ul>\n  <li>Blue Ribbon Bread</li>\n  <li>Fresh Clover Milk</li>\n  <li>Rooibos Tea Bags</li>\n</ul>", "RUN_CODE", "Add <p> and <ul> tags."),

                // CSS 2
                LessonStep("css_2_s1", "css_2", 1, "Styling Core Concepts", "CSS (Cascading Style Sheets) controls how elements look – colors, margins, fonts, and borders. Let's bring beautiful African warmth to Mam's shop with a Purple and Gold startup theme!", "I-CSS ishuna imibala nefomethi yewebhusayithi. Uhlelo lwethu lokusebenza luhlobe ngemibala yama-Khosi (Omnyama, Ophephuli noGolide).", "", "READ", "Learn CSS basic styling rule"),
                LessonStep("css_2_s2", "css_2", 2, "Modifying Colors (color & background)", "To set text color to gold, use `color: gold;`. To set a rich dark background, write `background-color: #121212;` inside the CSS body selector. Let's write the CSS styling rule now!", "Faka umbala wegolide no-background omnyama kwi-webhusayithi yakho.", "body {\n  background-color: #121212;\n  color: #FFD700; /* Gold */\n  font-family: sans-serif;\n}", "RUN_CODE", "Use background-color and color selectors."),

                // JavaScript 3
                LessonStep("js_3_s1", "js_3", 1, "What is JavaScript?", "JavaScript makes websites interactive! It allows us to calculate purchases, update total price, and save orders without refreshing. Let's code a quick cart calculator for Mam's Spaza Shop.", "U-JavaScript wenza isizindalwazi sisebenze. Sizobala intengo yesinkwa nezithelo.", "", "READ", "Understand JS variables and logic"),
                LessonStep("js_3_s2", "js_3", 2, "Writing Your First Calculator Function", "Let's write a function to calculate total order prices! We define variables with `const` and calculate the total. Type the JS function below:", "Bhala umsebenzi ka-JS obala i-total yamakhasimende ethu.", "function calculateTotal(breadQty, milkQty) {\n  const breadPrice = 18.50;\n  const milkPrice = 16.00;\n  return (breadQty * breadPrice) + (milkQty * milkPrice);\n}\nconsole.log(\"R\" + calculateTotal(2, 3));", "RUN_CODE", "Develop calculation formula in Rand"),

                // Python 4
                LessonStep("python_4_s1", "python_4", 1, "Python for Smart Farming", "Python is fantastic for calculating crop harvest and market prices! In rural South Africa, farmers use data to forecast crop yield. Today we will build a simplified crop pricing estimator in Python.", "I-Python iwusizo kakhulu ekubaleni isivuno semikhakha yezolimo ezabelweni zasekhaya.", "", "READ", "Basic python syntax and prints"),
                LessonStep("python_4_s2", "python_4", 2, "Conditional Pricing Estimator (if/else)", "Let's write an algorithm that checks temperature and tells us if corn needs more water: If temperature is > 30 degrees Celsius, crop watering must double. Type the script below:", "Bhala i-Python ehlola izinga lokushisa lenhlabathi.", "temp = 32\nif temp > 30:\n    print(\"Warning: High Heat! Increase irrigation x2.\")\nelse:\n    print(\"Normal climate. Maintain standard water flow.\")", "RUN_CODE", "Write conditional statements in Python."),

                // HTML 5 (StoryBrand Hero Page)
                LessonStep("html_5_s1", "html_5", 1, "Hero Section Title (<h1>)", "Marketing starts with a clear message. Under Donald Miller's StoryBrand, your customer is the Hero! Write a clear header <h1>Grow Your Business with KodeMamas</h1>.", "Ngaphansi kwe-StoryBrand, ikhasimende lakho iyi-Hero! Bhala isihloko esicacile.", "<h1>Grow Your Business with KodeMamas</h1>", "RUN_CODE", "Type <h1>Grow Your Business with KodeMamas</h1> in the simulator."),
                LessonStep("html_5_s2", "html_5", 2, "Call to Action Button", "Make it easy for customers to buy! Use <button> to create a grand 'Buy Now' action button on your page storefront.", "Yenza kube lula ukuthenga! Sebenzisa i-<button>.", "<button>Join Training Now</button>", "RUN_CODE", "Type <button>Join Training Now</button> in the simulator."),

                // CSS 6 (Branding and SOPs)
                LessonStep("css_6_s1", "css_6", 1, "Hiring for Roles & CSS Classes", "Hiring standard processes means documenting roles. Let's color-code our team roles list in the CSS dashboard layout: .mama gets Gold, .student gets Indigo!", "I-CSS ishuna ama-SOP. Ake sifake umbala ezindimeni: O-Mama bathola Igolide, Abafundi i-Indigo!", ".mama {\n  color: #FFD700; /* Gold */\n  font-weight: bold;\n}\n.student {\n  color: #4B0082; /* Indigo */\n}", "RUN_CODE", "Define class styling selectors."),

                // JavaScript 7 (Spaza Cash Flow & profit)
                LessonStep("js_7_s1", "js_7", 1, "Recession-Proof Pricing", "Know your numbers! Let's calculate cash reserves. Create an algorithm that adds income streams and subtracts expenses to output net profits in Rand.", "Bala imali egciniwe net ngomsebenzi obala inzuzo.", "const revenue = 15000;\nconst expenses = 9500;\nconst profit = revenue - expenses;\nconsole.log(\"R\" + profit);", "RUN_CODE", "Calculate total net profit in Rand."),

                // HTML 8 (Feedback form)
                LessonStep("html_8_s1", "html_8", 1, "Input Fields for Feedback", "Collect emails consistently to master your marketing. Let's create a customer email input element <input type=\"email\" placeholder=\"Enter your email\" />.", "Khoda into yokufaka i-imeyili.", "<input type=\"email\" placeholder=\"Enter your email\" />", "RUN_CODE", "Type <input type=\"email\" placeholder=\"Enter your email\" /> in the simulator."),

                // Python 9 (Revenue Forecast loop)
                LessonStep("python_9_s1", "python_9", 1, "90-Day Goal Forecaster", "Systems > Hustle. Let's write a Python loop to forecast sales growing by 15% each month for the next 3 months to make cash reserves resilient!", "Bhala iluphu ye-Python ebikezela ukukhula.", "sales = 5000\nfor month in [1, 2, 3]:\n    sales = sales * 1.15\n    print(f\"Month {month}: R{sales:.2f}\")", "RUN_CODE", "Write loop forecasting sales compounding."),

                // JavaScript 10 (SOP checklist loop)
                LessonStep("js_10_s1", "js_10", 1, "Automating Daily Checklist Steps", "Only scale what already works by documenting Standard Operating Procedures (SOPs). Let's write a JS loop that prints our 6-Step Plan.", "Bhala ama-SOP kwi-iluphu ye-JS ephrinta amaphuzu wethu we-6-Step Plan.", "const steps = [\"Lead\", \"Team\", \"Plan\", \"Experience\", \"Marketing\", \"Scale\"];\nsteps.forEach((step, i) => console.log((i+1) + \". \" + step));", "RUN_CODE", "Develop loops listing business SOPs.")
            )
            db.lessonStepDao().insertSteps(defaultSteps)

            // Prepopulate new Quizzes for Lessons 5-10 too!
            val defaultQuizzes = listOf(
                QuizQuestion("q_html_1", "html_1", "What tag is used to write the largest headers or website titles in HTML?", "Yiliphi ithegi elisetshenziselwa izihloko ezinkulu ku-HTML?", "<p>", "<h1>", "<img>", "<body>", 1, "<h1> is the largest heading tag. Lower headings use <h2> down to <h6>."),
                QuizQuestion("q_html_2", "html_1", "Which tag should you use to bundle individual list items (<li>) into an unnumbered list?", "Yiliphi ithegi elakha uhlu olungena-nombolo lomkhiqizo?", "<ul>", "<ol>", "<p>", "<a>", 0, "<ul> stands for Unordered List, which creates bullet points around list items (<li>)."),
                QuizQuestion("q_css_1", "css_2", "Which property is used in CSS to declare text colors?", "Iyiphi i-property eshintsha umbala wombhalo ku-CSS?", "font-color", "background-color", "color", "text-paint", 2, "The 'color' property regulates text color directly in Cascading Style Sheets."),
                QuizQuestion("q_js_1", "js_3", "How do we declare constant variables in modern JavaScript?", "Sizimisa kanjani izinto ezingashintshi (constants) ku-JS?", "var", "let", "const", "def", 2, "'const' is used for modern block-scoped, non-reassignable constant variables."),
                QuizQuestion("q_python_1", "python_4", "What is the correct indentation requirement for 'if' statements in Python code?", "Uhlelo lwe-Python lusebenzisa ini ukuze luhlukanise izinkomba (if block)?", "Curly braces {}", "Semi-colons ;", "Tabs or 4 Spaces", "Parentheses ()", 2, "Python uses whitespace/tabs indentation instead of curly braces to establish code blocks."),
                QuizQuestion("q_html_5", "html_5", "Under the StoryBrand framework, who is the hero of your business's marketing story?", "Ngaphansi kwe-StoryBrand, ubani oyi-Hero yendaba yakho yezokumaketha?", "The business founder", "The customer", "The product", "The investor", 1, "The customer is the true Hero of the StoryBrand framework. Your business acts as the Guide."),
                QuizQuestion("q_css_6", "css_6", "In CSS, what is the correct selector for a class named 'mama'?", "Ku-CSS, iyiphi inkomba efanele yekilasi elibizwa ngokuthi 'mama'?", "#mama", "mama", ".mama", "*mama", 2, "Classes in CSS are selected using a leading dot, e.g., '.mama'."),
                QuizQuestion("q_js_7", "js_7", "In JavaScript, if revenue = 100 and expenses = 40, what is the output of 'revenue - expenses'?", "Ku-JavaScript, uma inzuzo = 100 kanti izindleko = 40, yini imiphumela ye-'revenue - expenses'?", "140", "60", "revenue40", "Error", 1, "Subtracting 40 from 100 returns 60 as a standard mathematical operation."),
                QuizQuestion("q_html_8", "html_8", "Which HTML tag is used to create an input text field for customer feedback?", "Iyiphi ithegi ye-HTML esetshenziselwa ukudala inkundla yokufaka impendulo?", "<input>", "<button>", "<form>", "<text>", 0, "The <input> tag creates interactive controls for web-based forms to collect user inputs."),
                QuizQuestion("q_python_9", "python_9", "In Python, which keyword is used to loop over a range of numbers or list elements?", "Ku-Python, yiliphi igama elingukhiye elisetshenziselwa ukuzulazulela phezu kohlu?", "while", "for", "loop", "repeat", 1, "The 'for' keyword in Python executes loop iterations over iterable structures such as lists."),
                QuizQuestion("q_js_10", "js_10", "In business scaling systems, what does SOP stand for?", "Ezinhlelweni zokukala ibhizinisi, amagama athi SOP amele ini?", "System Order Protocol", "Standard Operating Procedure", "Sales Optimization Plan", "Source Of Profit", 1, "SOP stands for Standard Operating Procedure, which is a set of step-by-step instructions compiled to help workers carry out routine operations.")
            )
            db.quizDao().insertQuizQuestions(defaultQuizzes)
        }

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
                "post_1", "Mama Thandi (Bloemfontein)", "Mama",
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
                messageText = "Hello from Bloemfontein! I am Nokwazi Nobuhle Xaba, your founder and tech mentor. Welcome to KodeMamas! I am matched with you to review your CV, help you get job-ready, and answer coding questions. Leave a message here anytime!"
            )
        )

        // Seed buddies
        val seedBuddies = listOf(
            Buddy("buddy_thandi", "Mama Thandi", "Mama", "zu", 185, "mama_avatar", "Spaza Shops, Motherhood", "Intro to HTML", false),
            Buddy("buddy_simphiwe", "Simphiwe Sibanda", "Student", "xh", 310, "girl_avatar_1", "Agriculture, Tech Jobs", "Intro to CSS", false),
            Buddy("buddy_gontse", "Gontse Morake", "Student", "nso", 420, "girl_avatar_2", "Tech Jobs, NGOs", "JS Interactive Calculations", false),
            Buddy("buddy_annelize", "Annelize Badenhorst", "Mama", "af", 120, "mama_avatar", "Spaza Shops, Motherhood", "Intro to HTML", false),
            Buddy("buddy_lindiwe", "Lindiwe Cele", "Mama", "zu", 250, "mama_avatar", "Agriculture, Motherhood", "Intro to CSS", false),
            Buddy("buddy_thabo", "Thabo Mokoena", "Student", "st", 90, "girl_avatar_1", "Tech Jobs, Spaza Shops", "Intro to HTML", false),
            Buddy("buddy_abigail", "Abigail Smith", "Mentor", "en", 500, "girl_avatar_1", "Tech Jobs, NGOs", "Python Crop Agriculture Tracker", false),
            Buddy("buddy_puleng", "Puleng Mogotlane", "Student", "tn", 210, "girl_avatar_2", "Tech Jobs, NGOs", "Intro to HTML", false),
            Buddy("buddy_tinyiko", "Tinyiko Khosa", "Mama", "ts", 150, "mama_avatar", "Agriculture, Motherhood", "Intro to CSS", false),
            Buddy("buddy_nokuthula", "Nokuthula Dlamini", "Student", "ss", 330, "girl_avatar_1", "Tech Jobs, NGOs", "Intro to HTML", false),
            Buddy("buddy_rotshidzwa", "Rotshidzwa Nemukula", "Mama", "ve", 175, "mama_avatar", "Spaza Shops, Motherhood", "Intro to HTML", false),
            Buddy("buddy_nomvula", "Nomvula Mahlangu", "Student", "nr", 280, "girl_avatar_2", "Tech Jobs, Spaza Shops", "Intro to CSS", false),
            Buddy("buddy_sipho", "Sipho Ndlovu (SASL)", "Student", "sasl", 290, "girl_avatar_1", "Tech Jobs, NGOs", "Intro to HTML", false)
        )
        db.buddyDao().insertBuddies(seedBuddies)

        // Seed some starter chat messages for matching buddies
        db.buddyDao().insertBuddyMessage(
            BuddyMessage(
                buddyId = "buddy_thandi",
                senderId = "buddy_thandi",
                messageText = "Yebo mama! I saw you are also running a spaza shop. How is the HTML storefront lesson going? Can you check my code?",
                timestamp = System.currentTimeMillis() - 7200000
            )
        )
        db.buddyDao().insertBuddyMessage(
            BuddyMessage(
                buddyId = "buddy_simphiwe",
                senderId = "buddy_simphiwe",
                messageText = "Molo sister! I am interested in agricultural tech too. Are you learning CSS styling for crop trackers?",
                timestamp = System.currentTimeMillis() - 7200000
            )
        )
    }
}
