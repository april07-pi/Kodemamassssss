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
    val allPeople: Flow<List<PersonEntity>> = db.personDao().getAllPeople()
    val allMentors: Flow<List<PersonEntity>> = db.personDao().getMentors()

    fun getPeopleByRole(role: String): Flow<List<PersonEntity>> =
        db.personDao().getPeopleByRole(role)

    suspend fun getPersonById(id: String): PersonEntity? =
        db.personDao().getPersonById(id)

    suspend fun insertPerson(person: PersonEntity) {
        db.personDao().insertOrUpdatePerson(person)
    }

    suspend fun togglePersonFavorite(id: String) {
        db.personDao().toggleFavorite(id)
    }

    suspend fun deletePerson(id: String) {
        db.personDao().deletePerson(id)
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
            "html_1" -> "html_2"
            "html_2" -> "html_3"
            "html_3" -> "css_2"
            "css_2" -> "css_3"
            "css_3" -> "css_4"
            "css_4" -> "js_3"
            "js_3" -> "js_4"
            "js_4" -> "js_5"
            "js_5" -> "python_4"
            "python_4" -> "python_5"
            "python_5" -> "python_6"
            "python_6" -> "mobile_1"
            "mobile_1" -> "mobile_2"
            "mobile_2" -> "mobile_3"
            "mobile_3" -> "ai_1"
            "ai_1" -> "ai_2"
            "ai_2" -> "ai_3"
            "ai_3" -> "design_1"
            "design_1" -> "design_2"
            "design_2" -> "design_3"
            else -> ""
        }
        if (nextId.isNotEmpty()) {
            db.lessonDao().updateUnlockedStatus(nextId, unlocked = true)
        }
    }

    suspend fun prepopulateDatabaseIfEmpty() {
        val existingProfile = db.userDao().getUserProfileSynchronous()
        if (existingProfile == null) {
            // 1. Enter default User Profile starting at 0%
            db.userDao().insertOrUpdateProfile(
                UserProfile(
                    name = "Learner",
                    email = "learner@kodemamas.org",
                    role = "Student", // Mama, Student, Girl
                    languageCode = "en",
                    streak = 0,
                    xp = 0,
                    dataSavingMode = false,
                    isPremium = false,
                    hasDownloadedOffline = true
                )
            )
        } else {
            val completedCount = db.progressDao().getCompletedCount()
            if (completedCount == 0 && (existingProfile.xp > 0 || existingProfile.streak > 0)) {
                db.userDao().resetXpAndStreak()
            }
        }

        val lessonCount = db.lessonDao().getLessonCount()
        val hasAiLesson = db.lessonDao().getLessonById("ai_1") != null
        if (lessonCount < 21 || !hasAiLesson) {
            // 2. Prepopulate all 21 comprehensive lessons across HTML, CSS, JavaScript, Python, Mobile Dev, Data & AI, and Design (all unlocked and offline-ready)
            val allLessons = listOf(
                // HTML Category
                Lesson("html_1", "Intro to HTML (Web Layout)", "Mam's Spaza Shop Storefront", "HTML", "Beginner", 10, isUnlocked = true, isDownloaded = true, orderIndex = 1),
                Lesson("html_2", "Interactive Forms & Customer Inputs", "Spaza Digital Order & Delivery Booking", "HTML", "Beginner", 15, isUnlocked = true, isDownloaded = true, orderIndex = 2),
                Lesson("html_3", "Semantic HTML & Township Media", "Adding Community Audio, Video & Gallery", "HTML", "Beginner", 18, isUnlocked = true, isDownloaded = true, orderIndex = 3),

                // CSS Category
                Lesson("css_2", "Adding Style with CSS", "Beautifying Your Online Catalog", "CSS", "Beginner", 15, isUnlocked = true, isDownloaded = true, orderIndex = 4),
                Lesson("css_3", "Flexbox & Grid Layouts", "Product Shelves & Responsive Cards", "CSS", "Intermediate", 20, isUnlocked = true, isDownloaded = true, orderIndex = 5),
                Lesson("css_4", "Responsive Mobile-First Design", "Media Queries for Low-End Smartphones", "CSS", "Intermediate", 22, isUnlocked = true, isDownloaded = true, orderIndex = 6),

                // JavaScript Category
                Lesson("js_3", "Interactive JS Calculations", "Calculating Bread & Veggie Orders", "JavaScript", "Beginner", 20, isUnlocked = true, isDownloaded = true, orderIndex = 7),
                Lesson("js_4", "DOM Manipulation & Event Listeners", "Adding to Cart & Real-Time Total", "JavaScript", "Intermediate", 25, isUnlocked = true, isDownloaded = true, orderIndex = 8),
                Lesson("js_5", "Async Data & Fetch APIs", "Live Township Weather & Produce Market Prices", "JavaScript", "Intermediate", 28, isUnlocked = true, isDownloaded = true, orderIndex = 9),

                // Python Category
                Lesson("python_4", "Python Crop Agriculture Tracker", "Harvesting & Pricing Predictions", "Python", "Beginner", 25, isUnlocked = true, isDownloaded = true, orderIndex = 10),
                Lesson("python_5", "Lists, Dictionaries & Store Inventory", "Spaza Stock Management Engine", "Python", "Intermediate", 25, isUnlocked = true, isDownloaded = true, orderIndex = 11),
                Lesson("python_6", "Functions & Automation", "Automated WhatsApp / SMS Receipt Generator", "Python", "Intermediate", 30, isUnlocked = true, isDownloaded = true, orderIndex = 12),

                // Mobile Dev Category
                Lesson("mobile_1", "Android & Compose Fundamentals", "Building Your First Mobile App Screen", "Mobile Dev", "Beginner", 18, isUnlocked = true, isDownloaded = true, orderIndex = 13),
                Lesson("mobile_2", "State & Interactive Mobile UI", "Township Cart & Real-Time Counter", "Mobile Dev", "Intermediate", 22, isUnlocked = true, isDownloaded = true, orderIndex = 14),
                Lesson("mobile_3", "Offline-First Mobile Architecture", "Room Database & Loadshedding Resilience", "Mobile Dev", "Intermediate", 25, isUnlocked = true, isDownloaded = true, orderIndex = 15),

                // Data & AI Category
                Lesson("ai_1", "AI & Prompt Engineering Basics", "Querying Gemini AI for Small Businesses", "Data & AI", "Beginner", 18, isUnlocked = true, isDownloaded = true, orderIndex = 16),
                Lesson("ai_2", "Data Analytics with Python & Pandas", "Predicting Spaza Grocery Demand", "Data & AI", "Intermediate", 22, isUnlocked = true, isDownloaded = true, orderIndex = 17),
                Lesson("ai_3", "AI Grounding & Live Search", "Fact-Checking with Real-Time Web Data", "Data & AI", "Intermediate", 25, isUnlocked = true, isDownloaded = true, orderIndex = 18),

                // Design Category
                Lesson("design_1", "UI/UX & Mobile Design Principles", "African Color Palettes & Accessibility", "Design", "Beginner", 15, isUnlocked = true, isDownloaded = true, orderIndex = 19),
                Lesson("design_2", "Figma to Code & Wireframing", "Translating Spaza Mockups to Compose", "Design", "Intermediate", 20, isUnlocked = true, isDownloaded = true, orderIndex = 20),
                Lesson("design_3", "Inclusive Design & Multi-Language UI", "Designing for 12 Languages & Non-Tech Mamas", "Design", "Intermediate", 22, isUnlocked = true, isDownloaded = true, orderIndex = 21)
            )
            db.lessonDao().insertLessons(allLessons)
        }

        // Ensure all lessons are unlocked and downloaded offline for immediate access
        db.lessonDao().unlockAndDownloadAllLessons()

            // 3. Prepopulate Lesson Steps for all 12 lessons
            val allSteps = listOf(
                // HTML 1
                LessonStep(
                    "html_1_s1", "html_1", 1,
                    "Welcome to HTML!",
                    "HTML stands for HyperText Markup Language. It's the bone structure of every website! Today, we will code a responsive digital storefront for Mam's Spaza shop in Soweto. Click next to start setting up our shelf structure.",
                    "Siyakwamukela! HTML yakha amathambo ewebhusayithi. Namuhla sizokwakha isitolo sika-Mama esiku-inthanethi.",
                    "", "READ", "Click Next to continue!"
                ),
                LessonStep(
                    "html_1_s2", "html_1", 2,
                    "Writing Our First Header (<h1>)",
                    "The <h1> tag defines the main and largest heading on your page. Inside your editor layout, write <h1>Mam's Spaza Shop</h1> to give our website a grand, colorful banner at the top!",
                    "Bhala isihloko esikhulu sesitolo usebenzisa u-<h1> ukuze sitolise iwebhusayithi yakho.",
                    "<h1>Mam's Spaza Shop</h1>", "RUN_CODE", "Type <h1>Mam's Spaza Shop</h1> in the simulator."
                ),
                LessonStep(
                    "html_1_s3", "html_1", 3,
                    "Listing Product Offerings (<p> & <ul>)",
                    "To show paragraph text, we use <p>. To display a list, we use <ul> (unordered list) alongside <li> (list item) tags. Let's list what we have: Bread, Milk, and Rooibos tea! Type a product listing below.",
                    "Dala uhlu lwemikhiqizo njengesinkwa nobisi usebenzisa amathegi e-<ul> ne-<li>.",
                    "<p>Our Fresh Daily Stock:</p>\n<ul>\n  <li>Blue Ribbon Bread</li>\n  <li>Fresh Clover Milk</li>\n  <li>Rooibos Tea Bags</li>\n</ul>", "RUN_CODE", "Add <p> and <ul> tags."
                ),

                // HTML 2
                LessonStep(
                    "html_2_s1", "html_2", 1,
                    "Interactive Forms & Customer Inputs",
                    "Forms allow township customers to place orders directly on your website! The <form> tag encapsulates input boxes such as customer names, phone numbers, and delivery addresses.",
                    "Amafomu avumela amakhasimende ukuthi a-ode ngqo kusizindalwazi sakho.",
                    "", "READ", "Understand form elements and customer order capture."
                ),
                LessonStep(
                    "html_2_s2", "html_2", 2,
                    "Creating Customer Text & Telephone Inputs",
                    "We use <label> to name each field, and <input> to let users type. For phone numbers, use <input type=\"tel\"> so mobile keyboards show numeric dials! Write an order form below.",
                    "Sebenzisa amathegi ka-<label> no-<input> ukuqoqa imininingwane yekhasimende.",
                    "<form>\n  <label>Your Name:</label>\n  <input type=\"text\" placeholder=\"Enter name\">\n  <label>Phone Number:</label>\n  <input type=\"tel\" placeholder=\"082 123 4567\">\n</form>", "RUN_CODE", "Create label and input tags."
                ),
                LessonStep(
                    "html_2_s3", "html_2", 3,
                    "Adding the Action Button (<button>)",
                    "Every great form needs an action button to submit the order! Use <button type=\"submit\">Place Spaza Order</button> to enable customers to finalize their purchase.",
                    "Faka inkinobho yokuthumela i-oda usebenzisa ithegi le-<button>.",
                    "<button type=\"submit\">Place Spaza Order</button>", "RUN_CODE", "Add a submit button to the form."
                ),

                // HTML 3
                LessonStep(
                    "html_3_s1", "html_3", 1,
                    "Semantic HTML Structure",
                    "Modern web standards use semantic tags like <header>, <main>, and <footer> instead of plain <div> tags. This improves accessibility, search ranking, and screen reader clarity for blind and low-vision users.",
                    "Sebenzisa amathegi asemthethweni njenge-<header>, <main>, kanye ne-<footer>.",
                    "", "READ", "Learn semantic web tags."
                ),
                LessonStep(
                    "html_3_s2", "html_3", 2,
                    "Adding Images with Alt Descriptions (<img>)",
                    "Display your bakery items and fresh fruits using the <img> tag! Remember to always include an `alt` attribute for accessibility.",
                    "Faka isithombe usebenzisa u-<img> ne-`alt` incazelo yezithombe.",
                    "<header>\n  <h1>Mam's Fresh Bakery</h1>\n</header>\n<main>\n  <img src=\"bread.jpg\" alt=\"Fresh baked township artisan bread loaf\">\n</main>", "RUN_CODE", "Include header, main, and accessible img tag."
                ),

                // CSS 2
                LessonStep(
                    "css_2_s1", "css_2", 1,
                    "Styling Core Concepts",
                    "CSS (Cascading Style Sheets) controls how elements look – colors, margins, fonts, and borders. Let's bring beautiful African warmth to Mam's shop with a Purple and Gold startup theme!",
                    "I-CSS ishuna imibala nefomethi yewebhusayithi.",
                    "", "READ", "Learn CSS basic styling rule"
                ),
                LessonStep(
                    "css_2_s2", "css_2", 2,
                    "Modifying Colors (color & background)",
                    "To set text color to gold, use color: gold;. To set a rich dark background, write background-color: #121212; inside the CSS body selector. Let's write the CSS styling rule now!",
                    "Faka umbala wegolide no-background omnyama kwi-webhusayithi yakho.",
                    "body {\n  background-color: #121212;\n  color: #FFD700; /* Gold */\n  font-family: sans-serif;\n}", "RUN_CODE", "Use background-color and color selectors."
                ),

                // CSS 3
                LessonStep(
                    "css_3_s1", "css_3", 1,
                    "Flexbox Product Shelves",
                    "Flexbox makes aligning items on a screen effortless! By declaring `display: flex;` on a container, child elements automatically align horizontally side-by-side like items on a supermarket shelf.",
                    "I-Flexbox ihlela izinto zibe seshalofini ngendlela ebukekayo nesheshayo.",
                    "", "READ", "Master flex containers and shelf alignment."
                ),
                LessonStep(
                    "css_3_s2", "css_3", 2,
                    "Spacing Elements Evenly (justify-content)",
                    "Use `justify-content: space-between;` to distribute product cards across the screen with equal margins between them.",
                    "Sebenzisa u-justify-content ukuze amakhadi emikhiqizo ahlalelane ngokulinganayo.",
                    ".product-shelf {\n  display: flex;\n  justify-content: space-between;\n  gap: 16px;\n}", "RUN_CODE", "Configure display: flex and justify-content."
                ),

                // CSS 4
                LessonStep(
                    "css_4_s1", "css_4", 1,
                    "Mobile-First Web Design",
                    "Over 85% of South African web traffic happens on mobile phones! Mobile-first design ensures your website loads fast, buttons are easy to tap, and text is readable on any screen size.",
                    "Klama iwebhusayithi yakho ibe lula ukusetshenziswa kumakhalekhukhwini.",
                    "", "READ", "Understand mobile viewport and responsive principles."
                ),
                LessonStep(
                    "css_4_s2", "css_4", 2,
                    "Writing CSS Media Queries (@media)",
                    "Media queries adapt CSS rules depending on screen width. If the screen is under 600px wide, stack products vertically for comfortable scrolling!",
                    "Sebenzisa i-media query ukushintsha ifomethi yezingcingo ezincane.",
                    "@media (max-width: 600px) {\n  .product-shelf {\n    flex-direction: column;\n    padding: 12px;\n  }\n}", "RUN_CODE", "Write a media query for mobile screens."
                ),

                // JavaScript 3
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
                    "Let's write a function to calculate total order prices! We define variables with const and calculate the total. Type the JS function below:",
                    "Bhala umsebenzi ka-JS obala i-total yamakhasimende ethu.",
                    "function calculateTotal(breadQty, milkQty) {\n  const breadPrice = 18.50; // Rand (R)\n  const milkPrice = 16.00;  // Rand (R)\n  return (breadQty * breadPrice) + (milkQty * milkPrice);\n}\nconsole.log(\"R\" + calculateTotal(2, 3));", "RUN_CODE", "Develop calculation formula in Rand"
                ),

                // JavaScript 4
                LessonStep(
                    "js_4_s1", "js_4", 1,
                    "DOM Element Selection & Click Events",
                    "The DOM (Document Object Model) represents HTML in code. With `document.querySelector`, JavaScript can listen for clicks on an 'Add to Cart' button and trigger instant updates.",
                    "I-DOM ivumela u-JavaScript ukulalela uma umsebenzisi ecofa inkinobho.",
                    "", "READ", "Understand addEventListener and event handling."
                ),
                LessonStep(
                    "js_4_s2", "js_4", 2,
                    "Live Cart Counter Update",
                    "Let's write code that increments the cart item badge each time a customer taps Buy! Type the click handler below:",
                    "Faka i-event listener yokwengeza inani lezimpahla ekolini.",
                    "const buyBtn = document.querySelector(\"#buy-btn\");\nlet cartCount = 0;\nbuyBtn.addEventListener(\"click\", () => {\n  cartCount += 1;\n  console.log(\"Cart updated: \" + cartCount + \" items\");\n});", "RUN_CODE", "Attach an addEventListener click listener."
                ),

                // JavaScript 5
                LessonStep(
                    "js_5_s1", "js_5", 1,
                    "Asynchronous JavaScript & APIs",
                    "In real-world applications, web stores fetch stock prices, township weather, and market produce from online databases using `fetch()` and `async/await` without freezing the screen.",
                    "Funda ukuthi izinhlelo zokusebenza zithola kanjani idatha eku-inthanethi nge-async/await.",
                    "", "READ", "Understand promises and async API data fetching."
                ),
                LessonStep(
                    "js_5_s2", "js_5", 2,
                    "Fetching Live Produce Market Rates",
                    "Let's write an async function that fetches market prices and logs the response in JSON format. Type the fetch function below:",
                    "Bhala i-async function ecela izintengo zemakethe ye-veggie.",
                    "async function getMarketPrices() {\n  const response = await fetch(\"https://api.kodemamas.org/market\");\n  const data = await response.json();\n  console.log(\"Fetched Maize price: R\" + data.maize);\n}", "RUN_CODE", "Write an async function using fetch and await."
                ),

                // Python 4
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
                ),

                // Python 5
                LessonStep(
                    "python_5_s1", "python_5", 1,
                    "Python Dictionaries for Store Stock",
                    "In Python, a dictionary `{key: value}` is the perfect tool for tracking store inventory: item names are keys, and stock quantities are values!",
                    "Izichazamazwi (dictionaries) ku-Python zikahle kakhulu ekuphatheni impahla yesitolo.",
                    "", "READ", "Understand Python key-value dictionaries."
                ),
                LessonStep(
                    "python_5_s2", "python_5", 2,
                    "Looping to Detect Low Stock",
                    "Write a loop that checks each item in Mam's Spaza shop: if stock is less than 5, print an alert to restock! Type the script below:",
                    "Sebenzisa i-loop ehlola ukuthi iyiphi impahla ephelayo esitolo.",
                    "stock = {\"maize\": 14, \"milk\": 3, \"bread\": 20}\nfor item, qty in stock.items():\n    if qty < 5:\n        print(f\"Alert: {item} is low ({qty} left)! Restock now.\")", "RUN_CODE", "Loop through stock dictionary and check low levels."
                ),

                // Python 6
                LessonStep(
                    "python_6_s1", "python_6", 1,
                    "Python Functions & Automation",
                    "Functions in Python let us write code once and run it thousands of times! We can automate receipt generation so customers immediately get an SMS or WhatsApp confirmation.",
                    "Sebenzisa imisebenzi (functions) ku-Python ukwakha amarisidi azithumelayo.",
                    "", "READ", "Learn Python def functions and parameters."
                ),
                LessonStep(
                    "python_6_s2", "python_6", 2,
                    "Building a Receipt Generator Function",
                    "Write a function `generate_receipt(customer, total)` that returns an official KodeMamas digital receipt message. Type the function below:",
                    "Bhala umsebenzi okhiqiza irisidi elisemthethweni lekhasimende.",
                    "def generate_receipt(customer, total):\n    receipt = f\"KodeMamas Spaza - Thanks {customer}! Total Paid: R{total:.2f}\"\n    return receipt\n\nprint(generate_receipt(\"Thandi\", 145.50))", "RUN_CODE", "Define generate_receipt function with return."
                ),

                // Mobile Dev 1
                LessonStep(
                    "mobile_1_s1", "mobile_1", 1,
                    "Why Mobile Development Matters in SA",
                    "Android powers over 85% of mobile devices across South African townships. In this module, you will learn to build native Android screens using Jetpack Compose, Google's modern declarative UI toolkit.",
                    "I-Android isebenza ezingcingweni ezingaphezu kuka-85% eNingizimu Afrika. Funda ukwakha izikrini nge-Jetpack Compose.",
                    "", "READ", "Learn why Android and Jetpack Compose matter."
                ),
                LessonStep(
                    "mobile_1_s2", "mobile_1", 2,
                    "Composable Functions & Text Elements",
                    "In Jetpack Compose, we build UI by declaring @Composable functions. Add a banner text for your township app using Text(\"Welcome to KodeMamas Mobile!\").",
                    "Ku-Jetpack Compose sisebenzisa i-@Composable ukwakha isikrini sethu.",
                    "@Composable\nfun WelcomeScreen() {\n    Text(\n        text = \"Welcome to KodeMamas Mobile!\",\n        color = Color.White\n    )\n}", "RUN_CODE", "Declare a Composable function with Text element."
                ),
                LessonStep(
                    "mobile_1_s3", "mobile_1", 3,
                    "Accessible Touch Targets (48dp)",
                    "To make apps accessible for elder mamas and high-sunlight outdoor usage, Material Design 3 requires buttons to have a minimum touch target size of 48.dp. Create an accessible order button!",
                    "Yakha inkinobho efinyeleleka kalula enamaphuzu okungenani angama-48dp.",
                    "Button(\n    onClick = { /* order placed */ },\n    modifier = Modifier\n        .fillMaxWidth()\n        .heightIn(min = 48.dp)\n) {\n    Text(\"Place Spaza Order\")\n}", "RUN_CODE", "Create Button with 48dp minimum touch target."
                ),

                // Mobile Dev 2
                LessonStep(
                    "mobile_2_s1", "mobile_2", 1,
                    "Understanding State in Compose",
                    "When data changes, Compose automatically recomposes only the UI elements that depend on that state. We use remember { mutableStateOf(...) } to hold reactive state.",
                    "Ku-Compose sisebenzisa i-state ukuze isikrini sizibuyekeze uma idatha ishintsha.",
                    "", "READ", "Understand reactive state and recomposition."
                ),
                LessonStep(
                    "mobile_2_s2", "mobile_2", 2,
                    "Interactive Counter with State",
                    "Build a bread order counter where tapping increment increases the quantity in real time. Write the reactive counter below:",
                    "Yakha isibali se-oda lesinkwa esibukhoma usebenzisa u-mutableStateOf.",
                    "var quantity by remember { mutableStateOf(1) }\nRow {\n    Button(onClick = { quantity++ }) { Text(\"+\") }\n    Text(\"Qty: \$quantity\")\n}", "RUN_CODE", "Implement stateful counter in Compose."
                ),
                LessonStep(
                    "mobile_2_s3", "mobile_2", 3,
                    "Efficient Lists with LazyColumn",
                    "Low-end smartphones have limited RAM. LazyColumn recycles views and only renders items currently on screen. Create a scrollable list of grocery products!",
                    "Sebenzisa i-LazyColumn ukuze uhlelo lusebenze ngokushesha kumafoni anenkumbulo encane.",
                    "LazyColumn {\n    items(productList) { item ->\n        ProductCard(item = item)\n    }\n}", "RUN_CODE", "Build LazyColumn list with items."
                ),

                // Mobile Dev 3
                LessonStep(
                    "mobile_3_s1", "mobile_3", 1,
                    "Loadshedding & Offline Resiliency",
                    "In South Africa, cell towers and mobile networks frequently drop during loadshedding. An offline-first mobile architecture stores all critical data in a local SQLite database using Room so users never lose work.",
                    "Ukwakheka kwe-Offline-first kusiza abasebenzisi ukuthi baqhubeke nokusebenza ngezikhathi zokucima kukagesi.",
                    "", "READ", "Learn offline-first mobile architecture."
                ),
                LessonStep(
                    "mobile_3_s2", "mobile_3", 2,
                    "Declaring Room Entities & DAOs",
                    "Define an @Entity for customer orders and a @Dao interface with @Insert and @Query functions.",
                    "Qoka i-Entity ne-DAO ukuze ulondoloze ama-oda ku-SQLite.",
                    "@Entity(tableName = \"orders\")\ndata class CustomerOrder(\n    @PrimaryKey val id: String,\n    val spazaName: String,\n    val totalAmount: Double\n)", "RUN_CODE", "Define Room Entity for customer orders."
                ),

                // Data & AI 1
                LessonStep(
                    "ai_1_s1", "ai_1", 1,
                    "Introduction to Generative AI & LLMs",
                    "Large Language Models like Google Gemini can understand natural language, summarize reports, translate between African languages, and write business copy. Prompt engineering is the skill of guiding AI models to produce accurate, high-value outputs.",
                    "I-Generative AI ne-Gemini zikuvumela ukuthi ubhale imilayezo nemiyalelo ngezilimi zasekhaya.",
                    "", "READ", "Learn LLM fundamentals and prompt engineering."
                ),
                LessonStep(
                    "ai_1_s2", "ai_1", 2,
                    "Persona & Context Prompting",
                    "Craft a role-based prompt giving the AI a persona, task, and formatting constraints to create a WhatsApp marketing announcement for fresh vegetable produce.",
                    "Yakha umyalo ocacile we-AI ozokhipha isikhangiso se-WhatsApp.",
                    "val prompt = \"\"\"Role: Friendly township retail marketing mentor.\nTask: Write a 3-sentence WhatsApp promo for fresh cabbage & spinach.\nLanguage: Conversational with warm Ubuntu greeting.\nFormat: Emoji bullet points with prices in Rand (R).\"\"\"", "RUN_CODE", "Define structured prompt template."
                ),

                // Data & AI 2
                LessonStep(
                    "ai_2_s1", "ai_2", 1,
                    "The Value of Data in Township Commerce",
                    "Spaza shops and local bakeries often lose money through stock spoilage or stockouts. Data analytics enables business owners to spot seasonal sales trends and order just enough inventory.",
                    "Ukuhlaziya idatha kusiza izitolo zasemalokishini ukuthi zazi ukuthi iyiphi impahla ethengwa kakhulu.",
                    "", "READ", "Understand data-driven business decisions."
                ),
                LessonStep(
                    "ai_2_s2", "ai_2", 2,
                    "Loading Datasets with Pandas",
                    "Use the Pandas library to read sales records and inspect statistical summaries like total revenue, mean transaction value, and top selling produce.",
                    "Funda idatha usebenzisa i-Pandas ubale isamba semali engenile.",
                    "import pandas as pd\ndf = pd.read_csv(\"spaza_sales.csv\")\nprint(df.describe())\ntotal_revenue = df[\"amount_rand\"].sum()\nprint(f\"Total Revenue: R{total_revenue:.2f}\")", "RUN_CODE", "Analyze sales DataFrame with Pandas."
                ),

                // Data & AI 3
                LessonStep(
                    "ai_3_s1", "ai_3", 1,
                    "Overcoming AI Hallucinations",
                    "AI models sometimes generate plausible-sounding but inaccurate facts. Grounding connects Gemini models directly to live search tools, allowing the AI to fetch verified facts and cite real-world sources.",
                    "I-AI Grounding iqinisekisa ukuthi izimpendulo zithathwe emithonjeni yangempela ephilayo.",
                    "", "READ", "Learn AI Grounding and Google Search tools."
                ),
                LessonStep(
                    "ai_3_s2", "ai_3", 2,
                    "Enabling Google Search Tool in Gemini",
                    "Configure the Gemini generative model with Google Search retrieval tools to query current market prices.",
                    "Lungiselela imodeli ye-Gemini ukuthi ihlanganiswe nosesho lwe-Google.",
                    "val modelConfig = GeminiConfig(\n    model = \"gemini-3.5-flash\",\n    tools = listOf(Tool.GoogleSearch()),\n    temperature = 0.2f\n)", "RUN_CODE", "Configure Gemini with Google Search tool."
                ),

                // Design 1
                LessonStep(
                    "design_1_s1", "design_1", 1,
                    "Design with Ubuntu & Cultural Warmth",
                    "Design is not just how something looks; it's how accessible and welcoming it feels. In South African communities, high-contrast colors, warm earthy tones, and spacious layouts ensure clarity even on low-cost screens under bright sunlight.",
                    "Ukuklama kudinga ukuthi kubhekelwe ukufinyelela kwabo bonke abantu nokukhanya kwelanga.",
                    "", "READ", "Understand inclusive visual design in Africa."
                ),
                LessonStep(
                    "design_1_s2", "design_1", 2,
                    "Accessible Color Contrast (WCAG AAA)",
                    "Use color combinations that achieve at least 7:1 contrast ratio for small text and 4.5:1 for headers. Define an accessible theme palette!",
                    "Qinisekisa ukuthi imibala ibonakala ngokucacile emalangeni.",
                    "val ThemeDarkPurple = Color(0xFF1E0635)\nval ThemeGold = Color(0xFFFBBF24)\n// Contrast ratio is > 10:1 (WCAG AAA Compliant)", "RUN_CODE", "Define high-contrast WCAG AAA palette."
                ),

                // Design 2
                LessonStep(
                    "design_2_s1", "design_2", 1,
                    "Bridging Design & Engineering",
                    "Designers create wireframes and UI mockups in tools like Figma. Software engineers inspect padding, margins, font sizes, and layout containers to translate pixels into clean Jetpack Compose code.",
                    "Ukufunda ukuguqula imidwebo ye-Figma ibe yikhodi ye-Jetpack Compose.",
                    "", "READ", "Learn Figma wireframes and Compose translation."
                ),
                LessonStep(
                    "design_2_s2", "design_2", 2,
                    "The 8dp Spacing Grid",
                    "Modern design systems rely on the 8dp grid (8dp, 16dp, 24dp, 32dp) for rhythm and consistency. Build a card with consistent padding.",
                    "Sebenzisa i-8dp grid ukuhlela izikhala ngendlela efanayo.",
                    "Card(\n    shape = RoundedCornerShape(16.dp),\n    modifier = Modifier.padding(16.dp)\n) {\n    Column(modifier = Modifier.padding(16.dp)) {\n        Text(\"Product Title\")\n        Spacer(modifier = Modifier.height(8.dp))\n        Text(\"Price: R25.00\")\n    }\n}", "RUN_CODE", "Implement card using 8dp grid spacing."
                ),

                // Design 3
                LessonStep(
                    "design_3_s1", "design_3", 1,
                    "Designing for Everyone",
                    "Inclusive design recognizes that people have different language backgrounds, digital literacy levels, and accessibility needs. A button that works well in English may truncate when translated into longer isiZulu or isiXhosa words.",
                    "Ukuklama okufanele bonke abantu kubhekelela izilimi zonke nezidingo ezahlukene.",
                    "", "READ", "Learn inclusive multi-language design principles."
                ),
                LessonStep(
                    "design_3_s2", "design_3", 2,
                    "Flexible Width Containers & Text Wrapping",
                    "Never hardcode fixed container widths for text buttons or cards. Allow text to wrap cleanly or use flexible sizing.",
                    "Ungafaki ubukhulu obumisiwe obungashintshi ukuze amagama ezilimi angasikwa.",
                    "Surface(\n    shape = RoundedCornerShape(16.dp),\n    modifier = Modifier.wrapContentSize()\n) {\n    Text(\n        text = buttonLabel,\n        maxLines = 2,\n        overflow = TextOverflow.Ellipsis,\n        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)\n    )\n}", "RUN_CODE", "Build flexible multi-language container."
                )
            )
            db.lessonStepDao().insertSteps(allSteps)

            // 4. Prepopulate Quiz Questions for all 12 lessons
            val allQuizzes = listOf(
                // HTML 1
                QuizQuestion(
                    "q_html_1_1", "html_1",
                    "What tag is used to write the largest headers or website titles in HTML?",
                    "Yiliphi ithegi elisetshenziselwa izihloko ezinkulu ku-HTML?",
                    "<p>", "<h1>", "<img>", "<body>", 1,
                    "<h1> is the largest heading tag. Lower headings use <h2> down to <h6>."
                ),
                QuizQuestion(
                    "q_html_1_2", "html_1",
                    "Which tag should you use to bundle individual list items (<li>) into an unnumbered list?",
                    "Yiliphi ithegi elakha uhlu olungena-nombolo lomkhiqizo?",
                    "<ul>", "<ol>", "<p>", "<a>", 0,
                    "<ul> stands for Unordered List, which creates bullet points around list items (<li>)."
                ),

                // HTML 2
                QuizQuestion(
                    "q_html_2_1", "html_2",
                    "Which tag wraps all interactive inputs and buttons when submitting customer orders?",
                    "Yiliphi ithegi eligoqa zonke izinto zokufaka i-oda?",
                    "<form>", "<div>", "<input>", "<table>", 0,
                    "<form> groups all order input fields together for submission."
                ),
                QuizQuestion(
                    "q_html_2_2", "html_2",
                    "Which input type should be used for customer telephone numbers?",
                    "Yiluphi uhlobo lwe-input olufanele izinombolo zocingo?",
                    "type=\"phone\"", "type=\"tel\"", "type=\"number\"", "type=\"digits\"", 1,
                    "<input type=\"tel\"> triggers numeric phone keypads on smartphones."
                ),

                // HTML 3
                QuizQuestion(
                    "q_html_3_1", "html_3",
                    "What attribute is required on <img> tags to describe images for accessibility?",
                    "Iyiphi i-attribute edingekayo ku-<img> ukuchaza isithombe kubantu abangaboni?",
                    "title", "alt", "desc", "src", 1,
                    "The 'alt' attribute gives screen readers descriptive text for visually impaired learners."
                ),
                QuizQuestion(
                    "q_html_3_2", "html_3",
                    "Which semantic tag should be used for the bottom section of a webpage?",
                    "Yiliphi ithegi elisemthethweni lengxenye engezansi yewebhusayithi?",
                    "<bottom>", "<footer>", "<end>", "<section>", 1,
                    "<footer> represents the footer/closing section of a web document."
                ),

                // CSS 2
                QuizQuestion(
                    "q_css_2_1", "css_2",
                    "Which property is used in CSS to declare text colors?",
                    "Iyiphi i-property eshintsha umbala wombhalo ku-CSS?",
                    "font-color", "background-color", "color", "text-paint", 2,
                    "The 'color' property regulates text color directly in Cascading Style Sheets."
                ),
                QuizQuestion(
                    "q_css_2_2", "css_2",
                    "How do you change the background color of an entire webpage?",
                    "Ushintsha kanjani umbala we-background yewebhusayithi yonke?",
                    "body { background-color: #121212; }", "body { color: black; }", "all { bg: dark; }", "screen { fill: black; }", 0,
                    "Targeting the 'body' selector with 'background-color' sets the canvas color."
                ),

                // CSS 3
                QuizQuestion(
                    "q_css_3_1", "css_3",
                    "Which CSS declaration turns a container into a flexible horizontal layout?",
                    "Yisiphi isimemezelo se-CSS esenza izinto zihambisane ngomugqa?",
                    "display: flex;", "display: block;", "display: inline;", "float: left;", 0,
                    "'display: flex;' activates Flexbox formatting on any element container."
                ),
                QuizQuestion(
                    "q_css_3_2", "css_3",
                    "Which property spaces items evenly with equal margins across a flex row?",
                    "Iyiphi i-property ehlukanisa izinto ngokulinganayo?",
                    "align-items: center;", "justify-content: space-between;", "margin: auto;", "gap: 0;", 1,
                    "'justify-content: space-between;' spaces children across the main horizontal axis."
                ),

                // CSS 4
                QuizQuestion(
                    "q_css_4_1", "css_4",
                    "Which CSS rule allows styles to change conditionally based on smartphone screen size?",
                    "Yimuphi umthetho we-CSS olungiselela amakhodi ezingcingo ezincane?",
                    "@import", "@media", "@screen", "@device", 1,
                    "@media queries allow responsive styles tailored to specific screen resolutions."
                ),
                QuizQuestion(
                    "q_css_4_2", "css_4",
                    "What is the recommended minimum touch target height for mobile action buttons?",
                    "Yiliphi izinga elincane elinconywayo lokucofa ngenkinobho efonini?",
                    "20px", "48px", "100px", "12px", 1,
                    "Material Design standards require a minimum touch target size of 48px."
                ),

                // JS 3
                QuizQuestion(
                    "q_js_3_1", "js_3",
                    "How do we declare constant variables in modern JavaScript?",
                    "Sizimisa kanjani izinto ezingashintshi (constants) ku-JS?",
                    "var", "let", "const", "def", 2,
                    "'const' is used for modern block-scoped, non-reassignable constant variables."
                ),
                QuizQuestion(
                    "q_js_3_2", "js_3",
                    "What operator calculates the product of item price and quantity in JavaScript?",
                    "Yiluphi uphawu olubala inani lokuthenga ngokuphindaphinda?",
                    "+", "*", "%", "/", 1,
                    "The asterisk '*' is the multiplication operator in JavaScript."
                ),

                // JS 4
                QuizQuestion(
                    "q_js_4_1", "js_4",
                    "Which method attaches an interaction listener when a customer clicks a button?",
                    "Iyiphi i-method elalela ukucofwa kwenkinobho ku-JS?",
                    "addEventListener()", "onClickEvent()", "listen()", "watchClick()", 0,
                    "'addEventListener(\"click\", callback)' handles user clicks and taps."
                ),
                QuizQuestion(
                    "q_js_4_2", "js_4",
                    "Which property changes the visible text inside an HTML element?",
                    "Iyiphi i-property ebuyekeza umbhalo obonakalayo ku-HTML?",
                    "textContent", "innerHTML", "value", "text()", 0,
                    "'textContent' safely updates the text inside a selected DOM node."
                ),

                // JS 5
                QuizQuestion(
                    "q_js_5_1", "js_5",
                    "Which keyword pauses execution inside an async function until data returns?",
                    "Yiliphi igama elilindisa umsebenzi we-async kuze kubuye idatha?",
                    "wait", "await", "pause", "defer", 1,
                    "'await' pauses the execution of an async function until a Promise settles."
                ),
                QuizQuestion(
                    "q_js_5_2", "js_5",
                    "What data interchange format is most commonly used by modern web APIs?",
                    "Yiyiphi ifomethi ejwayelekile yokudlulisa idatha eku-inthanethi?",
                    "XML", "JSON", "CSV", "TXT", 1,
                    "JSON (JavaScript Object Notation) is the universal format for web APIs."
                ),

                // Python 4
                QuizQuestion(
                    "q_python_4_1", "python_4",
                    "What is the correct indentation requirement for 'if' statements in Python code?",
                    "Uhlelo lwe-Python lusebenzisa ini ukuze luhlukanise izinkomba (if block)?",
                    "Curly braces {}", "Semi-colons ;", "Tabs or 4 Spaces", "Parentheses ()", 2,
                    "Python uses whitespace/tabs indentation instead of curly braces to establish code blocks."
                ),
                QuizQuestion(
                    "q_python_4_2", "python_4",
                    "Which built-in Python function displays text output to the screen or console?",
                    "Iyiphi i-function ye-Python ebonisa umbhalo esikrinini?",
                    "echo()", "display()", "print()", "show()", 2,
                    "'print()' outputs values directly to the console in Python."
                ),

                // Python 5
                QuizQuestion(
                    "q_python_5_1", "python_5",
                    "Which data structure stores items as key-value pairs in Python?",
                    "Iyiphi i-data structure egcina izinto ngamapheya (key-value)?",
                    "List", "Tuple", "Dictionary", "Set", 2,
                    "Dictionaries (dict) store data in key: value associations."
                ),
                QuizQuestion(
                    "q_python_5_2", "python_5",
                    "Which method iterates over both keys and values in a Python dictionary?",
                    "Iyiphi i-method ehlola kokubili amagama namanani esichazamazwini?",
                    ".keys()", ".values()", ".items()", ".all()", 2,
                    "The '.items()' method yields tuples of (key, value) pairs during loops."
                ),

                // Python 6
                QuizQuestion(
                    "q_python_6_1", "python_6",
                    "Which keyword is used to define a new function in Python?",
                    "Yiliphi igama elisetshenziselwa ukudala i-function entsha ku-Python?",
                    "function", "def", "func", "define", 1,
                    "'def' is the Python keyword for defining functions."
                ),
                QuizQuestion(
                    "q_python_6_2", "python_6",
                    "Which keyword sends a calculated result back to the caller of a function?",
                    "Yiliphi igama elibuyisela umphumela womsebenzi ku-Python?",
                    "send", "return", "output", "give", 1,
                    "'return' sends the output value back to the caller."
                ),

                // Mobile Dev 1
                QuizQuestion(
                    "q_mobile_1_1", "mobile_1",
                    "What is Jetpack Compose in Android development?",
                    "Kuyini i-Jetpack Compose ku-Android?",
                    "An XML layout parser", "A modern declarative UI toolkit in Kotlin", "A local SQLite database", "A background network service", 1,
                    "Jetpack Compose is Google's modern declarative UI toolkit for Android."
                ),
                QuizQuestion(
                    "q_mobile_1_2", "mobile_1",
                    "What is the recommended minimum touch target size according to Material Design 3 accessibility?",
                    "Liyini inani elincane elinconywayo lokucofa inkinobho?",
                    "24dp", "32dp", "48dp", "64dp", 2,
                    "Material 3 accessibility guidelines require touch targets to be at least 48x48dp."
                ),

                // Mobile Dev 2
                QuizQuestion(
                    "q_mobile_2_1", "mobile_2",
                    "Which Compose API is used to retain mutable state across recompositions?",
                    "Iyiphi i-API ye-Compose egcina i-state uma isikrini sizibuyekeza?",
                    "remember { mutableStateOf() }", "val state = staticState()", "useState()", "observeAsState()", 0,
                    "'remember { mutableStateOf(initialValue) }' preserves reactive state across Compose recompositions."
                ),
                QuizQuestion(
                    "q_mobile_2_2", "mobile_2",
                    "Why is 'LazyColumn' preferred over a standard 'Column' for large lists on mobile devices?",
                    "Kungani i-LazyColumn ingcono kune-Column ejwayelekile ezinhlwini ezinde?",
                    "It only renders visible items to conserve memory and RAM", "It forces the screen to rotate", "It only works with images", "It runs synchronously on the main thread", 0,
                    "LazyColumn renders only the items visible in the viewport, saving critical RAM on entry-level Android devices."
                ),

                // Mobile Dev 3
                QuizQuestion(
                    "q_mobile_3_1", "mobile_3",
                    "Which Jetpack library provides an abstraction layer over SQLite for local database persistence?",
                    "Iyiphi ilabhulali ye-Jetpack esetshenziselwa ukugcina idatha ku-SQLite?",
                    "Room", "Retrofit", "Glide", "WorkManager", 0,
                    "Room provides compile-time SQLite query verification and object mapping in modern Android."
                ),
                QuizQuestion(
                    "q_mobile_3_2", "mobile_3",
                    "What is a primary benefit of offline-first mobile architecture?",
                    "Yini inzuzo enkulu yokusebenzisa i-offline-first architecture?",
                    "The app works seamlessly during network outages and loadshedding", "It requires 5G connectivity", "It deletes old databases automatically", "It prevents users from taking screenshots", 0,
                    "Offline-first apps store data locally first, ensuring full usability even when network signals or electricity are unavailable."
                ),

                // Data & AI 1
                QuizQuestion(
                    "q_ai_1_1", "ai_1",
                    "What is Prompt Engineering in modern AI development?",
                    "Kuyini i-Prompt Engineering ekuthuthukisweni kwe-AI?",
                    "Writing hardware machine code", "Designing and refining text inputs to guide AI models effectively", "Training deep neural networks from scratch", "Installing GPU drivers", 1,
                    "Prompt engineering is the craft of structuring natural language instructions to steer AI models to desired outputs."
                ),
                QuizQuestion(
                    "q_ai_1_2", "ai_1",
                    "Which technique helps an LLM produce domain-accurate responses?",
                    "Iyiphi indlela esiza i-LLM ukuthi ikhiphe izimpendulo ezinembile?",
                    "Providing clear persona, context, and output format examples", "Using only one single word", "Writing prompt in ALL CAPS", "Deleting system instructions", 0,
                    "Giving the AI a clear role, domain context, and few-shot formatting examples yields the most accurate responses."
                ),

                // Data & AI 2
                QuizQuestion(
                    "q_ai_2_1", "ai_2",
                    "Which Python library is the industry standard for tabular data manipulation and analysis?",
                    "Iyiphi ilabhulali ye-Python ejwayelekile yokuhlaziya idatha yamatafula?",
                    "Pandas", "PyGame", "Flask", "Tkinter", 0,
                    "Pandas is the leading Python data manipulation library offering DataFrame and Series structures."
                ),
                QuizQuestion(
                    "q_ai_2_2", "ai_2",
                    "Which method in Pandas provides summary statistics like count, mean, min, and max?",
                    "Iyiphi i-method ku-Pandas ekhipha izibalo ezibalulekile njenge-mean ne-min?",
                    ".describe()", ".summarize()", ".stats()", ".calculate()", 0,
                    "The '.describe()' method returns key summary statistics for numerical columns in a DataFrame."
                ),

                // Data & AI 3
                QuizQuestion(
                    "q_ai_3_1", "ai_3",
                    "What is AI Grounding?",
                    "Kuyini ukuxhuma i-AI emithonjeni yangempela (Grounding)?",
                    "Connecting an AI model to external verifiable data sources like Google Search", "Connecting a device to an electrical earth ground", "Shutting down the AI model during night hours", "Deleting previous user chat logs", 0,
                    "AI Grounding anchors model responses in verifiable external data, reducing hallucinations and citing current facts."
                ),
                QuizQuestion(
                    "q_ai_3_2", "ai_3",
                    "How does grounding improve user trust in AI responses?",
                    "Kusiza kanjani ukuxhuma i-AI ekutheni abasebenzisi bayethembe?",
                    "By citing real-time verifiable sources and facts", "By making responses longer", "By generating random numbers", "By refusing to answer", 0,
                    "Providing verifiable sources and search citations lets users verify facts and trust business insights."
                ),

                // Design 1
                QuizQuestion(
                    "q_design_1_1", "design_1",
                    "Why is high color contrast critical for mobile applications in township communities?",
                    "Kungani ukuhlukana kwemibala okucacile kubalulekile ezinhlelweni zeselula?",
                    "It allows screens to remain readable outdoors in bright South African sunlight", "It makes the battery last 10 times longer", "It increases download speed", "It is only useful in dark rooms", 0,
                    "High contrast ensures high legibility under strong outdoor lighting and aids users with visual impairments."
                ),
                QuizQuestion(
                    "q_design_1_2", "design_1",
                    "What is visual hierarchy in UI design?",
                    "Kuyini ukuhleleka kokubonakalayo (visual hierarchy)?",
                    "Arranging elements to show their order of importance and guide the user's attention", "Sorting files in alphabetical order", "Adding as many bright colors as possible", "Making all text the exact same font size", 0,
                    "Visual hierarchy uses size, weight, color, and spacing to intuitively signal importance."
                ),

                // Design 2
                QuizQuestion(
                    "q_design_2_1", "design_2",
                    "What is the industry-standard spacing grid interval used by Material Design 3 and Figma?",
                    "Iyiphi igridi yesikhala ejwayelekile ku-Material Design ne-Figma?",
                    "8dp grid", "3dp grid", "11dp grid", "17dp grid", 0,
                    "The 8dp grid creates consistent visual rhythm and aligns with standard screen pixel densities."
                ),
                QuizQuestion(
                    "q_design_2_2", "design_2",
                    "When translating a vertical card mockup from Figma to Compose, which layout container should you use?",
                    "Uma uguqula ikhadi elimi mpo lisuka ku-Figma liya ku-Compose, usebenzisa ini?",
                    "Column", "Row", "LazyRow", "Spacer", 0,
                    "Column arranges items vertically, one after the other."
                ),

                // Design 3
                QuizQuestion(
                    "q_design_3_1", "design_3",
                    "Why must mobile UI containers avoid fixed widths when supporting multiple languages?",
                    "Kungani izakhiwo zokubamba umbhalo kungafanele zibe nobukhulu obungashintshi?",
                    "Translated phrases in languages like isiZulu or Sepedi may be longer than English and need room to wrap", "Because phones don't support width", "Fixed widths make the internet slower", "Languages always have the exact same number of letters", 0,
                    "Different languages vary significantly in phrase length; flexible containers prevent ugly text clipping."
                ),
                QuizQuestion(
                    "q_design_3_2", "design_3",
                    "What is the best practice for making navigation buttons clear to first-time tech learners?",
                    "Iyiphi indlela engcono yokwenza izinkinobho zokuhamba zicace kubafundi abasha?",
                    "Pair recognizable icons with localized descriptive text", "Use complex technical jargon", "Remove all text and use only abstract symbols", "Hide buttons inside deep nested menus", 0,
                    "Combining clear visual icons with descriptive localized labels provides the best accessibility and comprehension."
                )
            )
            db.quizDao().insertQuizQuestions(allQuizzes)

        // 5. Prepopulate Coding Challenges
        val defaultChallenges = listOf(
            CodingChallenge(
                "chall_1", "Township Bakery Order System",
                "A local community bakery charges R8 per muffin. Calculate total price for 12 muffins in JS.",
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
                "post_welcome", "Nokwazi Nobuhle Xaba", "Solo Founder & Mentor",
                "Sanibonani and welcome to the KodeMamas Community Circle! I founded this platform in Bloemfontein to empower women and youth through offline-first digital literacy and practical coding. Feel free to ask questions, share your code progress, and connect!",
                System.currentTimeMillis() - 3600000 * 2, 42, 10, "en"
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
                messageText = """Key professionals who use it include:
1. **Business & Systems Analysts:**
They examine it to see if the idea is realistic (feasibility analysis) and start planning requirements.
2. **IT Steering Committee & Project Managers:** Decision-makers who review it to say, *"Yes, let's fund and build this!"* or *"Not right now."*
3. **Developers & Designers:** They reference it to understand the big-picture purpose before building.

Think like a tech leader: If you were creating a system request to solve a challenge in your own community, what problem would you address at your local spaza shop, clinic, or school?"""
            )
        )
        db.chatDao().insertMessage(
            MentorChat(
                isAi = false,
                isUser = false,
                messageText = "Sanibonani and welcome to KodeMamas! I am Nokwazi Nobuhle Xaba, solo founder and lead tech mentor from Bloemfontein. Whether you need your CV reviewed, guidance on IT and Computer Science studies, or help solving coding bugs, I am here to support you. Leave a message here anytime!"
            )
        )
    }

    suspend fun resetProgressToZero() {
        db.progressDao().clearAllProgress()
        db.userDao().resetXpAndStreak()
    }
}
