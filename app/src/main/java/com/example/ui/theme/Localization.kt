package com.example.ui.theme

object Localization {

    val languages = listOf(
        com.example.data.Language.ENGLISH,
        com.example.data.Language.ZULU,
        com.example.data.Language.XHOSA,
        com.example.data.Language.AFRIKAANS,
        com.example.data.Language.SEPEDI,
        com.example.data.Language.TSWANA,
        com.example.data.Language.SOTHO,
        com.example.data.Language.TSONGA,
        com.example.data.Language.SWAZI,
        com.example.data.Language.VENDA,
        com.example.data.Language.NDEBELE,
        com.example.data.Language.SIGN_LANGUAGE
    )

    fun getDynamicLessonTranslation(key: String, lang: String): String? {
        val lessonMap = LocalizationLessons.lessonMaps[lang] ?: LocalizationLessons.lessonMaps["en"]
        return lessonMap?.get(key)
    }

    fun translate(key: String, lang: String): String {
        // 1. Check lesson dictionary
        val lessonMap = LocalizationLessons.lessonMaps[lang] ?: LocalizationLessons.lessonMaps["en"]
        val lessonVal = lessonMap?.get(key)
        if (lessonVal != null) return lessonVal

        // 2. Check UI dictionary
        val uiMap = LocalizationUi.uiMaps[lang] ?: LocalizationUi.uiMaps["en"]
        val uiVal = uiMap?.get(key)
        if (uiVal != null) return uiVal

        // 3. Fallback to English in Lesson dictionary
        val enLesson = LocalizationLessons.lessonMaps["en"]?.get(key)
        if (enLesson != null) return enLesson

        // 4. Fallback to English in UI dictionary
        val enUi = LocalizationUi.uiMaps["en"]?.get(key)
        if (enUi != null) return enUi

        // 5. If key has prefix or suffix conventions, format cleanly
        return key.replace("_", " ").capitalize()
    }
}
