package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Lesson
import com.example.ui.theme.*

/**
 * Modern African EdTech Curriculum Screen.
 * Professional, clean, offline-first course listing with category filtering and progress tracking.
 */
@Composable
fun LearnTab(viewModel: MainViewModel, langCode: String) {
    val colors = LocalKodeMamasColors.current
    val lessons by viewModel.allLessons.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val completedLessonIds = remember(progressList) {
        progressList.filter { it.isCompleted }.map { it.lessonId }.toSet()
    }

    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val categories = listOf("All", "HTML", "CSS", "JavaScript", "Python", "Mobile Dev", "Data & AI", "Design")

    val filteredLessons = remember(lessons, selectedCategory) {
        if (selectedCategory == "All") lessons
        else lessons.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val totalLessons = lessons.size.coerceAtLeast(1)
    val completedCount = completedLessonIds.size
    val progressFraction = (completedCount.toFloat() / totalLessons.toFloat()).coerceIn(0f, 1f)
    val allDownloaded = lessons.isNotEmpty() && lessons.all { it.isDownloaded }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Heading
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Localization.translate("interactive_curriculum", langCode),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
                Text(
                    text = Localization.translate("curriculum_subtitle", langCode),
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Curriculum Progress & Offline Ready Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ThemeGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = ThemeGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = Localization.translate("curriculum_progress", langCode),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "$completedCount of ${lessons.size} modules mastered",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Percentage Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ThemeIndigo.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(progressFraction * 100).toInt()}% Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeIndigo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ThemeGold,
                    trackColor = colors.cardBorder
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Offline Fast Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (allDownloaded) Icons.Default.CloudDone else Icons.Default.OfflinePin,
                            contentDescription = null,
                            tint = if (allDownloaded) Color(0xFF059669) else colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (allDownloaded) Localization.translate("all_offline_saved", langCode) else Localization.translate("offline_ready", langCode),
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }

                    if (!allDownloaded) {
                        TextButton(
                            onClick = { viewModel.downloadAllLessons() },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = Localization.translate("download_all", langCode),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeIndigo
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                val count = if (cat == "All") lessons.size else lessons.count { it.category.equals(cat, ignoreCase = true) }
                val displayLabel = when (cat) {
                    "All" -> Localization.translate("all_categories", langCode)
                    "HTML" -> Localization.translate("cat_html", langCode)
                    "CSS" -> Localization.translate("cat_css", langCode)
                    "JavaScript" -> Localization.translate("cat_js", langCode)
                    "Python" -> Localization.translate("cat_python", langCode)
                    "Mobile Dev" -> Localization.translate("cat_mobile_dev", langCode)
                    "Data & AI" -> Localization.translate("cat_data_ai", langCode)
                    "Design" -> Localization.translate("cat_design", langCode)
                    else -> cat
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSelectedCategoryFilter(cat) },
                    label = {
                        Text(
                            text = "$displayLabel ($count)",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ThemeIndigo,
                        selectedLabelColor = Color.White,
                        containerColor = colors.surface,
                        labelColor = colors.textSecondary
                    ),
                    border = BorderStroke(1.dp, if (isSelected) ThemeIndigo else colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_chip_$cat")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lesson Cards List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredLessons) { lesson ->
                val isUnlocked = lesson.isUnlocked || lesson.id in listOf("html_1", "html_2", "css_2", "js_3", "python_4", "mobile_1", "ai_1", "design_1")
                val isCompleted = lesson.id in completedLessonIds

                LessonItemCard(
                    lesson = lesson,
                    isUnlocked = isUnlocked,
                    isCompleted = isCompleted,
                    colors = colors,
                    langCode = langCode,
                    onSelect = {
                        viewModel.selectLesson(lesson)
                    },
                    onToggleDownload = {
                        viewModel.toggleSingleLessonDownload(lesson.id)
                    }
                )
            }

            if (filteredLessons.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No modules found in this category.",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Lesson Item Card adhering to African EdTech visual design.
 */
@Composable
private fun LessonItemCard(
    lesson: Lesson,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    colors: KodeMamasColors,
    langCode: String = "en",
    onSelect: () -> Unit,
    onToggleDownload: () -> Unit
) {
    val categoryBadgeBg = when (lesson.category) {
        "HTML" -> Color(0xFFFF5722).copy(alpha = 0.12f)
        "CSS" -> Color(0xFF2563EB).copy(alpha = 0.12f)
        "JavaScript" -> Color(0xFFD97706).copy(alpha = 0.12f)
        else -> Color(0xFF059669).copy(alpha = 0.12f)
    }

    val categoryBadgeText = when (lesson.category) {
        "HTML" -> Color(0xFFE64A19)
        "CSS" -> Color(0xFF1D4ED8)
        "JavaScript" -> Color(0xFFB45309)
        else -> Color(0xFF047857)
    }

    val localizedTitle = Localization.getLessonTitle(lesson.id, langCode)
    val localizedSubtitle = Localization.getLessonSubtitle(lesson.id, langCode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = isUnlocked) { onSelect() }
            .testTag("lesson_card_${lesson.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) colors.surface else colors.surface.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            1.dp,
            if (isCompleted) Color(0xFF059669).copy(alpha = 0.4f)
            else if (isUnlocked) colors.cardBorder
            else colors.cardBorder.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Category Tag Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isUnlocked) categoryBadgeBg else Color.Gray.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (lesson.category) {
                            "HTML" -> "HTML"
                            "CSS" -> "CSS"
                            "JavaScript" -> "JS"
                            else -> "PY"
                        },
                        fontWeight = FontWeight.Black,
                        color = if (isUnlocked) categoryBadgeText else Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Lesson Titles & Category Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = lesson.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) ThemeIndigo else Color.Gray
                        )
                        Text(text = "•", fontSize = 10.sp, color = colors.textSecondary)
                        Text(
                            text = "${lesson.durationMinutes} mins",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                        Text(text = "•", fontSize = 10.sp, color = colors.textSecondary)
                        Text(
                            text = lesson.difficulty,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = localizedTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) colors.textPrimary else Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = localizedSubtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isUnlocked) ThemeIndigo else Color.LightGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Download Toggle Icon Button (48dp touch target)
                IconButton(
                    onClick = onToggleDownload,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("download_lesson_${lesson.id}")
                ) {
                    Icon(
                        imageVector = if (lesson.isDownloaded) Icons.Default.CheckCircle else Icons.Default.FileDownload,
                        contentDescription = if (lesson.isDownloaded) "Downloaded for offline" else "Download for offline",
                        tint = if (lesson.isDownloaded) Color(0xFF059669) else colors.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Status Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF059669).copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Localization.translate("lesson_completed", langCode),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                } else if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ThemeGold.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = Localization.translate("unlocked", langCode),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = Localization.translate("locked", langCode),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // CTA Button
                if (isUnlocked) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (isCompleted) Localization.translate("continue_lesson", langCode) else Localization.translate("start_lesson", langCode),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeIndigo
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = ThemeIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Studio / Lesson Simulator.
 * Features:
 * - Clear Lesson Title & Category
 * - Step Objective & Concept
 * - Short digestible explanation
 * - South African local language translation card
 * - Monospace Code Snippet block with syntax highlight and "Copy / Insert" actions
 * - Interactive Live Mobile Code Editor
 * - Immediate compiler output feedback
 * - Step-by-step progress tracking
 * - Integrated Quiz Assessment
 */
@Composable
fun ActiveLessonSimulator(viewModel: MainViewModel, langCode: String) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val colors = LocalKodeMamasColors.current

    val lesson by viewModel.currentActiveLesson.collectAsState()
    val steps by viewModel.currentActiveSteps.collectAsState()
    val stepIndex by viewModel.currentStepIndex.collectAsState()

    val editorText by viewModel.editorText.collectAsState()
    val simulatorOutput by viewModel.simulatorOutput.collectAsState()
    val simulatorSuccess by viewModel.simulatorSuccess.collectAsState()

    // Quiz states
    val quizQuestions by viewModel.activeQuizQuestions.collectAsState()
    val quizIndex by viewModel.quizQuestionIndex.collectAsState()
    val selectedAns by viewModel.selectedAnswerIndex.collectAsState()
    val quizChecked by viewModel.quizChecked.collectAsState()
    val quizCorrect by viewModel.quizCorrect.collectAsState()
    val quizScore by viewModel.quizScore.collectAsState()
    val quizFinished by viewModel.quizFinished.collectAsState()

    val step = steps.getOrNull(stepIndex)

    // Calculate overall lesson progress
    val progressFraction = remember(steps, stepIndex, quizQuestions, quizIndex, quizFinished) {
        if (quizFinished) 1f
        else if (quizQuestions.isNotEmpty()) {
            val total = quizQuestions.size.coerceAtLeast(1)
            (quizIndex + 1).toFloat() / total.toFloat()
        } else if (steps.isNotEmpty()) {
            val total = steps.size.coerceAtLeast(1)
            (stepIndex + 1).toFloat() / total.toFloat()
        } else 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Upper Simulator Navigation Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ThemeIndigo)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { viewModel.closeActiveLesson() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_exit_lesson")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit lesson",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = lesson?.title ?: "Interactive Studio",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Course: ${lesson?.category ?: "Coding"}",
                                color = ThemeGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (lesson?.difficulty != null) {
                                Text(text = " • ", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                Text(
                                    text = lesson?.difficulty ?: "Beginner",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Step count pill
                if (steps.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Step ${stepIndex + 1} of ${steps.size}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (quizQuestions.isNotEmpty() && !quizFinished) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ThemeGold.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Quiz ${quizIndex + 1} of ${quizQuestions.size}",
                            color = ThemeGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Top Linear Progress Bar
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = ThemeGold,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }

        // Main Scrollable Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (step != null) {
                // 1. CONCEPT & EXPLANATION CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ThemeIndigo.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "STEP ${step.stepNumber}: OBJECTIVE",
                                    color = ThemeIndigo,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ThemeGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (step.completionRequirement == "RUN_CODE") "Hands-on Code" else "Core Concept",
                                    color = Color(0xFF92400E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = step.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = step.description,
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            lineHeight = 19.sp
                        )

                        // Local South African Language Translation
                        if (langCode != "en") {
                            val localizedStepText = Localization.getStepDescription(
                                stepId = step.id,
                                lang = langCode,
                                englishDesc = step.description,
                                zuluDesc = step.descriptionLocalized
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = ThemeIndigo.copy(alpha = 0.04f)),
                                border = BorderStroke(1.dp, ThemeIndigo.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🇿🇦", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = Localization.translate("in_your_language", langCode),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = ThemeIndigo
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = localizedStepText,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. CODE EXAMPLE / SYNTAX BOX (if step provides codeSnippet)
                if (step.codeSnippet.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        border = BorderStroke(1.dp, Color(0xFF374151))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = ThemeGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "EXAMPLE SYNTAX (${lesson?.category ?: "CODE"})",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Copy / Insert buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(step.codeSnippet))
                                            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = "Copy",
                                            fontSize = 10.sp,
                                            color = ThemeGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (step.completionRequirement == "RUN_CODE") {
                                        FilledTonalButton(
                                            onClick = {
                                                viewModel.updateEditorText(step.codeSnippet)
                                                Toast.makeText(context, "Inserted into editor!", Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = ThemeGold,
                                                contentColor = Color.Black
                                            )
                                        ) {
                                            Text(
                                                text = "Insert into Editor",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Code content
                            SelectionContainer {
                                Text(
                                    text = step.codeSnippet,
                                    color = ThemeGold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // 3. INTERACTIVE MOBILE CODE EDITOR
                if (step.completionRequirement == "RUN_CODE") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        border = BorderStroke(1.dp, ThemeGold.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE MOBILE EDITOR",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = lesson?.category ?: "CODE",
                                    color = ThemeGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Code input text field
                            TextField(
                                value = editorText,
                                onValueChange = { viewModel.updateEditorText(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .testTag("input_code_editor"),
                                textStyle = TextStyle(
                                    color = ThemeGold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A),
                                    cursorColor = ThemeGold,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Hint Box
                            if (step.answerHint.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = ThemeGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hint: " + step.answerHint,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Run Button (48dp min touch target)
                            Button(
                                onClick = { viewModel.runSimulatorCode() },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_run_simulator_code"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = Localization.translate("submit_code", langCode),
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // 4. SIMULATOR COMPILER OUTPUT
                    if (simulatorOutput.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = if (simulatorSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (simulatorSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (simulatorSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (simulatorSuccess) Color(0xFF059669) else Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (simulatorSuccess) "OUTPUT GENERATED • SUCCESS" else "OUTPUT • ADJUSTMENT REQUIRED",
                                        fontWeight = FontWeight.Black,
                                        color = if (simulatorSuccess) Color(0xFF059669) else Color(0xFFDC2626),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = simulatorOutput,
                                    color = Color(0xFF1F2937),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // 5. STEP NAVIGATION (Previous / Next)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (stepIndex > 0) {
                        OutlinedButton(
                            onClick = { viewModel.setStepIndex(stepIndex - 1) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_step_prev"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ThemeIndigo)
                        ) {
                            Text(text = "Previous", color = ThemeIndigo, fontWeight = FontWeight.Bold)
                        }
                    }

                    val canProceed = step.completionRequirement == "READ" || simulatorSuccess
                    Button(
                        onClick = { viewModel.completeStep() },
                        modifier = Modifier
                            .weight(if (stepIndex > 0) 1.6f else 1f)
                            .height(48.dp)
                            .testTag("btn_step_next"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canProceed) ThemeIndigo else Color.Gray
                        ),
                        enabled = canProceed,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (stepIndex == steps.size - 1) "Launch Assessment ⭐" else "Next Step →",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            } else if (quizQuestions.isNotEmpty()) {
                // ---------------------- QUIZ ASSESSMENT MODE ----------------------
                if (!quizFinished) {
                    val activeQ = quizQuestions.getOrNull(quizIndex)
                    if (activeQ != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            border = BorderStroke(1.dp, colors.cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ThemeGold.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "QUESTION ${quizIndex + 1} OF ${quizQuestions.size}",
                                            color = Color(0xFF92400E),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Text(
                                        text = "Assessment Mode",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = activeQ.question,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    lineHeight = 22.sp
                                )

                                if (langCode != "en") {
                                    val localizedQ = Localization.getQuizQuestion(
                                        quizId = activeQ.id,
                                        lang = langCode,
                                        englishQ = activeQ.question,
                                        zuluQ = activeQ.questionLocalized
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = localizedQ,
                                        color = ThemeIndigo,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Answer Options (A, B, C, D)
                        val options = listOf(activeQ.optionA, activeQ.optionB, activeQ.optionC, activeQ.optionD)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            options.forEachIndexed { optIndex, rawText ->
                                val isSelected = selectedAns == optIndex
                                val isCorrectOption = optIndex == activeQ.correctAnswerIndex

                                val optionBorder = if (quizChecked) {
                                    if (isCorrectOption) Color(0xFF10B981)
                                    else if (isSelected) Color(0xFFEF4444)
                                    else colors.cardBorder
                                } else {
                                    if (isSelected) ThemeIndigo else colors.cardBorder
                                }

                                val optionBg = if (quizChecked) {
                                    if (isCorrectOption) Color(0xFFECFDF5)
                                    else if (isSelected) Color(0xFFFEF2F2)
                                    else colors.surface
                                } else {
                                    if (isSelected) ThemeIndigo.copy(alpha = 0.08f) else colors.surface
                                }

                                val optionLetter = when (optIndex) {
                                    0 -> "A"
                                    1 -> "B"
                                    2 -> "C"
                                    else -> "D"
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(enabled = !quizChecked) {
                                            viewModel.selectQuizAnswer(optIndex)
                                        }
                                        .testTag("quiz_opt_$optIndex"),
                                    colors = CardDefaults.cardColors(containerColor = optionBg),
                                    border = BorderStroke(if (isSelected || (quizChecked && isCorrectOption)) 2.dp else 1.dp, optionBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (quizChecked && isCorrectOption) Color(0xFF10B981)
                                                    else if (quizChecked && isSelected) Color(0xFFEF4444)
                                                    else if (isSelected) ThemeIndigo
                                                    else colors.cardBorder.copy(alpha = 0.5f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = optionLetter,
                                                color = if (isSelected || (quizChecked && isCorrectOption)) Color.White else colors.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = rawText,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = colors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (quizChecked) {
                                            if (isCorrectOption) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Correct",
                                                    tint = Color(0xFF059669),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Cancel,
                                                    contentDescription = "Incorrect",
                                                    tint = Color(0xFFDC2626),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Explanation Feedback Box
                        if (quizChecked) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (quizCorrect) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                                ),
                                border = BorderStroke(1.dp, if (quizCorrect) Color(0xFF10B981) else Color(0xFFEF4444))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (quizCorrect) "Halala! Correct Answer! 🎉" else "Hawu! Not quite right.",
                                        color = if (quizCorrect) Color(0xFF047857) else Color(0xFFB91C1C),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = Localization.getQuizExplanation(activeQ.id, langCode, activeQ.explanation),
                                        color = Color(0xFF1F2937),
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }

                        // Quiz Action Buttons (48dp height min touch target)
                        if (!quizChecked) {
                            Button(
                                onClick = { viewModel.checkQuizAnswer() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_verify_answer"),
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                shape = RoundedCornerShape(14.dp),
                                enabled = selectedAns != -1
                            ) {
                                Text(text = Localization.translate("submit_answer", langCode), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.nextQuizStep() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_next_quiz_step"),
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = if (quizIndex == quizQuestions.size - 1) Localization.translate("quiz_passed", langCode) else Localization.translate("next_question", langCode),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    // QUIZ CELEBRATION CARD
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(2.dp, ThemeGold)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🏆", fontSize = 54.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = Localization.translate("congrats", langCode),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = ThemeIndigo,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You scored $quizScore of ${quizQuestions.size} correct, advancing your South African tech portfolio!",
                                textAlign = TextAlign.Center,
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { viewModel.closeActiveLesson() },
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_back_to_curriculum")
                            ) {
                                Text(
                                    text = "Back to Curriculum Map",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
