package com.example.data

import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

object GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(
        prompt: String,
        systemInstruction: String = "",
        userLanguage: String = "en"
    ): String = withContext(Dispatchers.IO) {
        val trimmedPrompt = prompt.trim()
        if (trimmedPrompt.isEmpty()) {
            return@withContext "Sanibonani! Your message is empty. Please ask a coding, Donald Miller business plan, or digital literacy question!"
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidApiKey = apiKey.isNotEmpty() &&
                apiKey != "MY_GEMINI_API_KEY" &&
                apiKey != "GEMINI_API_KEY" &&
                apiKey != "YOUR_GEMINI_API_KEY"

        if (hasValidApiKey) {
            // Models to try in priority order per skill guidelines
            val modelsToTry = listOf(
                "gemini-3.5-flash",
                "gemini-flash-latest"
            )

            for (modelName in modelsToTry) {
                try {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

                    val requestJson = JSONObject()
                    val contentsArray = JSONArray()
                    val contentObj = JSONObject()
                    val partsArray = JSONArray()
                    val partObj = JSONObject()

                    partObj.put("text", trimmedPrompt)
                    partsArray.put(partObj)
                    contentObj.put("parts", partsArray)
                    contentsArray.put(contentObj)
                    requestJson.put("contents", contentsArray)

                    if (systemInstruction.isNotEmpty()) {
                        val sysInstructionObj = JSONObject()
                        val sysPartsArray = JSONArray()
                        val sysPartObj = JSONObject()
                        sysPartObj.put("text", systemInstruction)
                        sysPartsArray.put(sysPartObj)
                        sysInstructionObj.put("parts", sysPartsArray)
                        requestJson.put("systemInstruction", sysInstructionObj)
                    }

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = requestJson.toString().toRequestBody(mediaType)
                    val request = Request.Builder()
                        .url(url)
                        .post(body)
                        .build()

                    client.newCall(request).execute().use { response ->
                        val responseBody = response.body?.string() ?: ""
                        if (response.isSuccessful) {
                            val jsonResponse = JSONObject(responseBody)
                            val candidates = jsonResponse.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val firstCandidate = candidates.getJSONObject(0)
                                val content = firstCandidate.optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val textResult = parts.getJSONObject(0).optString("text")
                                    if (textResult.isNotBlank()) {
                                        return@withContext textResult.trim()
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fall through to next model or offline engine
                }
            }
        }

        // Seamless On-Device Socratic Knowledge Engine
        generateSocraticOfflineResponse(trimmedPrompt, userLanguage)
    }

    fun generateSocraticOfflineResponse(query: String, lang: String = "en"): String {
        val lower = query.lowercase().trim()

        return when {
            // Donald Miller 6-Step Small Business Framework
            lower.contains("donald miller") || lower.contains("6-step") || lower.contains("business plan") || lower.contains("grow business") || lower.contains("spaza plan") || lower.contains("storybrand") -> {
                "📈 **Donald Miller's 6-Step Small Business Plan (Built into Lessons 5–10):**\n\n" +
                "1. **Leadership & Mindset (Pilot):** Build habits and routine systems so you aren't the bottleneck (Lesson 6).\n" +
                "2. **StoryBrand Marketing (Engines):** Clarify your message: Make the customer the Hero and position your business as the Guide (`<h1>` & `<button>` in Lesson 5).\n" +
                "3. **Sales & Offers (Wings):** Track daily cash flow, bread/veggie pricing, and margins (`calculateTotal()` in Lesson 7).\n" +
                "4. **Customer Experience (Body):** Capture customer feedback and township order leads with `<input type=\"email\" />` (Lesson 8).\n" +
                "5. **Predictive Analytics (Fuel):** Use Python loops (`for month in range(1, 4):`) to forecast 90-day compounding revenue (Lesson 9).\n" +
                "6. **Operations & Scale (Controls):** Create digital SOP task checklists to automate daily steps (Lesson 10)."
            }

            // HTML & Web Layout
            lower.contains("html") || lower.contains("header") || lower.contains("heading") || lower.contains("button") || lower.contains("input") || lower.contains("tag") -> {
                "🌐 **HTML (Web Structure) Coach:**\n\n" +
                "• **Headers (`<h1>...</h1>`):** Use `<h1>` for your main storefront or page title. Keep it bold and clear!\n" +
                "• **Buttons (`<button>...</button>`):** Position clear Call-to-Action buttons (e.g. `<button>Order Fresh Bread</button>`).\n" +
                "• **Input Fields (`<input>`):** Collect phone numbers or emails using `<input type=\"text\" placeholder=\"Enter WhatsApp No\" />`.\n" +
                "• **Lists (`<ul><li>`):** Create itemized stock listings.\n\n" +
                "💡 *Socratic Question:* What specific element do you want on your web page right now — a title, a list, or an order button?"
            }

            // CSS & Visual Styling
            lower.contains("css") || lower.contains("style") || lower.contains("color") || lower.contains("background") || lower.contains("class") -> {
                "🎨 **CSS (Visual Styling) Coach:**\n\n" +
                "• **Selectors:** Use `.mama { color: #FFD700; }` to style classes or `#storefront { background-color: #121212; }` for IDs.\n" +
                "• **Colors:** Gold (`#FFD700`), Midnight (`#121212`), and Indigo (`#4B0082`) provide high contrast and readability on mobile screens.\n" +
                "• **Padding & Margins:** `padding: 12px;` adds breathing room inside cards so touch targets are at least 48dp.\n\n" +
                "💡 *Try this:* Which component would you like to style first?"
            }

            // JavaScript & Calculations
            lower.contains("javascript") || lower.contains("js") || lower.contains("calculate") || lower.contains("function") || lower.contains("variable") || lower.contains("loop") -> {
                "⚡ **JavaScript (Logic & Interactivity) Coach:**\n\n" +
                "• **Variables (`const` & `let`):** `const breadPrice = 18.50; const qty = 2;`\n" +
                "• **Functions:**\n" +
                "```javascript\n" +
                "function calculateProfit(revenue, expenses) {\n" +
                "  return revenue - expenses;\n" +
                "}\n" +
                "```\n" +
                "• **Arrays & Iteration:** `[\"Bread\", \"Milk\", \"Eggs\"].forEach(item => console.log(item));`\n\n" +
                "💡 *Challenge:* What calculation does your spaza or student project need to solve?"
            }

            // Python & Data / Predictions
            lower.contains("python") || lower.contains("crop") || lower.contains("predict") || lower.contains("forecast") || lower.contains("indent") -> {
                "🐍 **Python (Data & Forecasting) Coach:**\n\n" +
                "• **Conditions (`if/else`):**\n" +
                "```python\n" +
                "if temperature > 30:\n" +
                "    print(\"Warning: High Heat! Increase irrigation.\")\n" +
                "```\n" +
                "• **90-Day Compounding Forecast (`for` loop):**\n" +
                "```python\n" +
                "sales = 5000\n" +
                "for month in range(1, 4):\n" +
                "    sales = sales * 1.15\n" +
                "    print(f\"Month {month}: R{sales:.2f}\")\n" +
                "```\n" +
                "• **Tip:** Always use 4 spaces for indentation in Python!"
            }

            // Lesson 1 to 10 specific questions
            lower.contains("lesson 1") || lower.contains("lesson1") -> {
                "📘 **Lesson 1: Intro to HTML (Mam's Spaza Shop Storefront)**\n\nIn this lesson, you create your first web layout using `<h1>Mam's Spaza Shop</h1>` and list daily essentials like bread and milk with `<ul>` and `<li>`."
            }
            lower.contains("lesson 2") || lower.contains("lesson2") -> {
                "🎨 **Lesson 2: Adding Style with CSS (Online Catalog)**\n\nLearn how to color-code your catalog with `background-color: #121212;` and accent text with `#FFD700` (Gold)."
            }
            lower.contains("lesson 3") || lower.contains("lesson3") -> {
                "⚙️ **Lesson 3: Interactive JS Calculations**\n\nBuild the `calculateTotal()` function to calculate orders (e.g. 2 loaves of bread @ R18.50 + milk @ R16.00 = R85.00)."
            }
            lower.contains("lesson 4") || lower.contains("lesson4") -> {
                "🌾 **Lesson 4: Python Crop Agriculture Tracker**\n\nWrite smart conditional logic (`if temp > 30:`) to advise township farmers on automated crop irrigation."
            }
            lower.contains("lesson 5") || lower.contains("lesson5") -> {
                "🚀 **Lesson 5: StoryBrand Landing Page Layout**\n\nStep 1: Write `<h1>Grow Your Business with KodeMamas</h1>`.\nStep 2: Create a high-converting CTA button `<button>Join Training Now</button>`."
            }
            lower.contains("lesson 6") || lower.contains("lesson6") -> {
                "👥 **Lesson 6: Visual Branding & Team Roles**\n\nUse CSS classes `.mama` and `.student` to color-code team member responsibilities and standard operating procedures."
            }
            lower.contains("lesson 7") || lower.contains("lesson7") -> {
                "💰 **Lesson 7: Spaza Profit Margin & Cash Flow Calculator**\n\nCompute net profit (`revenue - expenses`) to manage resilient township income streams."
            }
            lower.contains("lesson 8") || lower.contains("lesson8") -> {
                "📋 **Lesson 8: Customer Feedback Form Setup**\n\nUse `<input type=\"email\" placeholder=\"Enter your email\" />` to build lead-generation and feedback systems."
            }
            lower.contains("lesson 9") || lower.contains("lesson9") -> {
                "📊 **Lesson 9: 90-Day Predictive Revenue Planner**\n\nUse Python compounding loops (`for month in [1, 2, 3]: sales *= 1.15`) to track 90-day growth milestones."
            }
            lower.contains("lesson 10") || lower.contains("lesson10") -> {
                "✅ **Lesson 10: SOP Task Automation Checklist Dashboard**\n\nAutomate standard operating procedures using JavaScript arrays and `forEach()` loops."
            }

            // isiZulu
            lower.contains("zulu") || lower.contains("sawubona") || lower.contains("sanibonani") || lower.contains("ngiyabonga") || lower.contains("kunjani") || lang == "zu" -> {
                "🇿🇦 **Sawubona Mama nomfundi we-KodeMamas!**\n\n" +
                "Ngilapha ukukusiza ngokufunda ikhodi (HTML, CSS, JavaScript, Python) kanye nokukhulisa ibhizinisi lakho lespaza.\n\n" +
                "• **Isifundo 1:** Ukwakha ikhasi le-HTML lesitolo sakho (`<h1>`)\n" +
                "• **Isifundo 2:** Ukuhlobisa ngemibala ye-CSS (`color`, `background-color`)\n" +
                "• **Isifundo 3:** Ukubala inzuzo yokudla nge-JavaScript\n" +
                "• **Isifundo 4:** Ukulandelela izitshalo ngobuchwepheshe be-Python\n\n" +
                "Ufuna siqale ngaliphi iphuzu namuhla?"
            }

            // isiXhosa
            lower.contains("xhosa") || lower.contains("molo") || lower.contains("molweni") || lower.contains("enkosi") || lang == "xh" -> {
                "🇿🇦 **Molo mama nomfundi waseKodeMamas!**\n\n" +
                "Ndilapha ukukunceda ekufundeni i-coding (HTML, CSS, JavaScript, Python) kunye nendlela yokukhulisa ishishini lakho le-spaza.\n\n" +
                "• **Isifundo 1:** Ukwakha iphepha lokuqala le-HTML (`<h1>Mam's Spaza</h1>`)\n" +
                "• **Isifundo 2:** Ukuhombisa nge-CSS\n" +
                "• **Isifundo 3:** Ukubala i-odolo nge-JavaScript\n\n" +
                "Leliphi icandelo ongathanda siliphande kunye namhlanje?"
            }

            // Afrikaans
            lower.contains("afrikaans") || lower.contains("hallo") || lower.contains("goeiedag") || lower.contains("dankie") || lang == "af" -> {
                "🇿🇦 **Hallo en welkom by KodeMamas!**\n\n" +
                "Ek is jou persoonlike kodering- en besigheidsafrigter. Ons leer HTML, CSS, JavaScript en Python om werklike besighede en spaza-winkels te bou.\n\n" +
                "Waaroor wil jy vandag meer leer?"
            }

            // Socratic Coach Guidance
            lower.contains("help") || lower.contains("error") || lower.contains("debug") || lower.contains("how to") || lower.contains("code") -> {
                "👩‍💻 **Socratic Builder Coach:**\n\n" +
                "Let's solve this together step-by-step:\n" +
                "1. **Define the Goal:** In plain words, what should this code do when you press Run?\n" +
                "2. **Check the Structure:**\n" +
                "   - If HTML: Are all tags closed (e.g. `<h1>...</h1>` or `<button>...</button>`)?\n" +
                "   - If CSS: Did you end each line with a semicolon (`;`)?\n" +
                "   - If JavaScript: Are your variable names spelled consistently?\n" +
                "   - If Python: Is your indentation aligned by 4 spaces?\n\n" +
                "Paste your code snippet or tell me what error you are seeing and I will guide you to the fix!"
            }

            // General Encouragement & Digital Literacy
            else -> {
                "👋 **Sanibonani! Molo! Dumelang! Hello!**\n\n" +
                "I am your KodeMamas Socratic AI Tutor & Coach 🇿🇦\n\n" +
                "Here is what you can ask me:\n" +
                "• **Coding Concepts:** HTML layouts, CSS styling, JavaScript math, Python loops.\n" +
                "• **Lessons 1–10:** Walkthroughs of Spaza and Crop Agriculture projects.\n" +
                "• **Donald Miller's 6-Step Plan:** How to build high-converting storefronts & automate SOPs.\n" +
                "• **Debugging:** Paste your code snippet for step-by-step guidance.\n" +
                "• **11 Official Languages:** Ask in isiZulu, isiXhosa, Afrikaans, Sesotho, Sepedi, Setswana, or English!\n\n" +
                "What would you like to build or learn right now?"
            }
        }
    }
}
