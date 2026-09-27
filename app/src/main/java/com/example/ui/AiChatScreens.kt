package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiService
import com.example.data.SecurityUtils
import com.example.ui.theme.LocalKodeMamasColors
import com.example.ui.theme.ThemeCardBorder
import com.example.ui.theme.ThemeGold
import com.example.ui.theme.ThemeIndigo
import com.example.ui.theme.ThemeSurfaceDark

// ---------------------- HELPER: FORMATTED CHAT MESSAGE ----------------------
@Composable
fun FormattedChatMessage(text: String) {
    val context = LocalContext.current
    val isGrounded = text.contains("Grounded with Google Search Database") || text.contains("Google Search Database & Real-Time Web Grounding")
    val lines = text.split("\n")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (isGrounded) {
            Surface(
                color = Color(0xFFF1F5FD),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFD0E1FD)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A73E8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Grounded with Google Search Database & Gemini AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF174EA6)
                    )
                }
            }
        }

        var inCodeBlock = false
        val codeBuffer = StringBuilder()

        for (line in lines) {
            val trimmedLine = line.trim()

            if (trimmedLine.startsWith("```")) {
                if (inCodeBlock) {
                    // End of code block: render code card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = codeBuffer.toString().trimEnd(),
                            color = ThemeGold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    codeBuffer.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeBuffer.append(line).append("\n")
                continue
            }

            if (trimmedLine.isBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
            } else if (trimmedLine.startsWith("•") && (trimmedLine.contains("http://") || trimmedLine.contains("https://"))) {
                val urlMatcher = Regex("""https?://[^\s)\]]+""")
                val match = urlMatcher.find(trimmedLine)
                val url = match?.value

                if (url != null) {
                    val label = trimmedLine.substringBefore(url).trim()
                    val annotatedString = buildAnnotatedString {
                        append(label)
                        append(" ")
                        val start = length
                        append(url)
                        val end = length
                        addStyle(
                            style = SpanStyle(
                                color = Color(0xFF1A73E8),
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            ),
                            start = start,
                            end = end
                        )
                        addStringAnnotation(
                            tag = "URL",
                            annotation = url,
                            start = start,
                            end = end
                        )
                    }

                    ClickableText(
                        text = annotatedString,
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                        },
                        style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color(0xFF111827))
                    )
                } else {
                    Text(text = trimmedLine, fontSize = 13.sp, color = Color(0xFF111827))
                }
            } else {
                // Parse bold (**text**) and italic (*text*)
                Text(
                    text = buildAnnotatedString {
                        var i = 0
                        while (i < line.length) {
                            if (i + 1 < line.length && line[i] == '*' && line[i + 1] == '*') {
                                val end = line.indexOf("**", i + 2)
                                if (end != -1) {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF111827))) {
                                        append(line.substring(i + 2, end))
                                    }
                                    i = end + 2
                                    continue
                                }
                            } else if (line[i] == '*' && (i + 1 == line.length || line[i + 1] != '*')) {
                                val end = line.indexOf('*', i + 1)
                                if (end != -1) {
                                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Color(0xFF374151))) {
                                        append(line.substring(i + 1, end))
                                    }
                                    i = end + 1
                                    continue
                                }
                            }
                            append(line[i])
                            i++
                        }
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF111827),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ---------------------- TAB 3: AI ASSISTANT / CHATBOT ----------------------
@Composable
fun AiChatTab(viewModel: MainViewModel, langCode: String) {
    val colors = LocalKodeMamasColors.current
    val chats by viewModel.aiChats.collectAsState()
    val isGenerating by viewModel.aiGenerating.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isGoogleSearchEnabled by viewModel.isGoogleSearchEnabled.collectAsState()
    val selectedModel by viewModel.selectedGeminiModel.collectAsState()
    val aiLanguageCode by viewModel.aiLanguageCode.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showKeyDialog by remember { mutableStateOf(false) }
    var keyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(chats.size, isGenerating) {
        if (chats.isNotEmpty()) {
            listState.animateScrollToItem(chats.size - 1)
        }
    }

    val saLanguages = listOf(
        "auto" to "🌍 Auto-Detect (All 12 SA)",
        "zu" to "🇿🇦 isiZulu",
        "xh" to "🇿🇦 isiXhosa",
        "af" to "🇿🇦 Afrikaans",
        "nso" to "🇿🇦 Sepedi",
        "tn" to "🇿🇦 Setswana",
        "st" to "🇿🇦 Sesotho",
        "ts" to "🇿🇦 Xitsonga",
        "ss" to "🇿🇦 siSwati",
        "ve" to "🇿🇦 Tshivenda",
        "nr" to "🇿🇦 isiNdebele",
        "sasl" to "🤟 SASL (Sign)",
        "en" to "🇬🇧 English"
    )

    val promptSuggestions = listOf(
        "Explain variables in isiZulu 💡",
        "How do I build a Spaza shop POS? 🛒",
        "Fix my HTML tag error 🛠️",
        "What is a Python loop? 🐍"
    )

    // SETTINGS DIALOG
    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = ThemeGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Engine & Search Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Mama Ruth AI connects Google Search Database grounding with Google Gemini AI models to provide verified answers in South African languages.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )

                    // Connectivity Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOnline) "Network: Online (Google Search Active)" else "Network: Offline (Offline Knowledge Mode)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Google Search Grounding Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google Search Grounding",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeGold
                            )
                            Text(
                                text = "Query Google's live web database for real-time citations",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = isGoogleSearchEnabled,
                            onCheckedChange = { viewModel.toggleGoogleSearch(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ThemeGold,
                                checkedTrackColor = ThemeIndigo
                            )
                        )
                    }

                    // Gemini Model Selector
                    Text(
                        text = "Active Gemini Model",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            GeminiService.MODEL_GEMINI_3_5_FLASH to "3.5 Flash",
                            GeminiService.MODEL_GEMINI_3_1_PRO to "3.1 Pro",
                            GeminiService.MODEL_GEMINI_FLASH_LATEST to "Flash Latest"
                        ).forEach { (modelId, label) ->
                            FilterChip(
                                selected = selectedModel == modelId,
                                onClick = { viewModel.setSelectedGeminiModel(modelId) },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ThemeGold,
                                    selectedLabelColor = Color(0xFF111827),
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Text(
                        text = if (customApiKey.isNotBlank()) "✅ Custom Gemini Key: ${SecurityUtils.maskApiKey(customApiKey)}" else "⚡ Using Built-in Gemini Intelligence",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (customApiKey.isNotBlank()) Color(0xFF34D399) else ThemeGold
                    )
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Custom Gemini API Key (Optional)", color = ThemeGold, fontSize = 11.sp) },
                        placeholder = { Text("Paste AI Studio API key...", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isKeyVisible) "Hide Key" else "Reveal Key",
                                    tint = ThemeGold
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ThemeGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = ThemeGold
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCustomApiKey(keyInput)
                        showKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeGold)
                ) {
                    Text("Save Settings", color = Color(0xFF111827), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        keyInput = ""
                        viewModel.setCustomApiKey("")
                        showKeyDialog = false
                    }
                ) {
                    Text("Clear / Default", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = ThemeSurfaceDark
        )
    }

    // MAIN AI CHAT INTERFACE
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // 1. TOP STATUS & SETTINGS BAR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .testTag("ai_status_bar"),
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceDark),
            border = BorderStroke(1.dp, Color(0xFF374151))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
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
                                .background(ThemeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Mama Ruth AI",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Pan-African Coding Tutor",
                                color = ThemeGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Online badge
                        Surface(
                            color = if (isOnline) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) "Live" else "Local",
                                    color = if (isOnline) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Settings button
                        IconButton(
                            onClick = { showKeyDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "AI Settings",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grounding & Model row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedModel == GeminiService.MODEL_GEMINI_3_1_PRO) "Model: Gemini 3.1 Pro" else "Model: Gemini 3.5 Flash",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    FilterChip(
                        selected = isGoogleSearchEnabled,
                        onClick = { viewModel.toggleGoogleSearch() },
                        label = {
                            Text(
                                text = if (isGoogleSearchEnabled) "Google Grounding: ON" else "Google Grounding: OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ThemeGold,
                            selectedLabelColor = Color(0xFF111827),
                            selectedLeadingIconColor = Color(0xFF111827),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.White.copy(alpha = 0.8f),
                            iconColor = Color.White.copy(alpha = 0.8f)
                        ),
                        border = null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. SOUTH AFRICAN LANGUAGE SELECTOR BAR
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(saLanguages) { (code, label) ->
                val isSelected = aiLanguageCode == code
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setAiLanguageCode(code) },
                    label = {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ThemeIndigo,
                        selectedLabelColor = Color.White,
                        containerColor = colors.surface,
                        labelColor = colors.textPrimary
                    ),
                    border = BorderStroke(1.dp, if (isSelected) ThemeIndigo else colors.cardBorder)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. CHAT MESSAGE STREAM
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chats.isEmpty()) {
                // Empty state with prompts
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(ThemeIndigo.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👵🏾", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sawubona! I am Mama Ruth",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your multilingual coding companion. Ask in your language or try a quick starter below:",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    promptSuggestions.forEach { prompt ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.sendAiChat(prompt)
                                },
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.cardBorder)
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ThemeIndigo,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(chats) { chat ->
                        val isUser = chat.isUser
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(ThemeIndigo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("MR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Surface(
                                modifier = Modifier
                                    .widthIn(max = 290.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                    ),
                                color = if (isUser) ThemeIndigo else colors.surface,
                                border = BorderStroke(1.dp, if (isUser) ThemeIndigo else colors.cardBorder)
                            ) {
                                Box(modifier = Modifier.padding(12.dp)) {
                                    if (isUser) {
                                        Text(
                                            text = chat.messageText,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    } else {
                                        FormattedChatMessage(text = chat.messageText)
                                    }
                                }
                            }
                        }
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                modifier = Modifier.padding(start = 36.dp, top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = ThemeIndigo,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOnline && isGoogleSearchEnabled) "Consulting Google Database & Gemini AI..." else "Mama Ruth is thinking...",
                                    color = ThemeIndigo,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. CAPSULE INPUT ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = "Ask Mama Ruth in your language...",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .testTag("ai_text_input"),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = ThemeIndigo
                ),
                shape = RoundedCornerShape(26.dp),
                singleLine = true
            )

            // Google Search Action Button
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.searchGoogleDatabase(textInput.trim())
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ThemeGold)
                    .testTag("ai_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Google Database",
                    tint = Color(0xFF111827),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Standard Send Button
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendAiChat(textInput.trim())
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(ThemeIndigo)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
