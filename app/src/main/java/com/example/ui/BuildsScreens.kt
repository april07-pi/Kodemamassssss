package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalKodeMamasColors
import com.example.ui.theme.ThemeCardBorder
import com.example.ui.theme.ThemeGold
import com.example.ui.theme.ThemeIndigo
import com.example.ui.theme.ThemeSurfaceDark

data class ProjectTemplate(
    val id: String,
    val title: String,
    val language: String,
    val description: String,
    val codeSnippet: String
)

val SA_PROJECT_TEMPLATES = listOf(
    ProjectTemplate(
        id = "spaza_calc",
        title = "Spaza Shop POS Calculator",
        language = "JavaScript",
        description = "Calculate daily bread, milk, and cool drink receipts with change computation.",
        codeSnippet = """// Spaza Shop Point-of-Sale System
function calculateTillReceipt(items) {
  let subtotal = 0;
  items.forEach(item => {
    subtotal += item.price * item.qty;
    console.log(`• ${'$'}{item.name} x${'$'}{item.qty} = R${'$'}{item.price * item.qty}`);
  });
  const vatRate = 0.15;
  const vatAmount = subtotal * vatRate;
  const total = subtotal;
  console.log('---');
  console.log(`Subtotal: R${'$'}{subtotal.toFixed(2)}`);
  console.log(`Incl. 15% VAT: R${'$'}{vatAmount.toFixed(2)}`);
  console.log(`Total Payable: R${'$'}{total.toFixed(2)}`);
  return total;
}

const basket = [
  { name: "Albany Brown Bread", price: 17.50, qty: 2 },
  { name: "Clover Fresh Milk 2L", price: 32.00, qty: 1 },
  { name: "Eggs 6-Pack", price: 21.00, qty: 1 }
];

calculateTillReceipt(basket);"""
    ),
    ProjectTemplate(
        id = "spaza_storefront",
        title = "Township Storefront Webpage",
        language = "HTML",
        description = "Mobile-friendly webpage for local community groceries.",
        codeSnippet = """<!DOCTYPE html>
<html>
<head>
  <style>
    body { font-family: sans-serif; background: #0F172A; color: #F8FAFC; padding: 20px; }
    .card { background: #1E293B; border-radius: 12px; padding: 16px; border: 1px solid #334155; }
    h1 { color: #FBBF24; margin-bottom: 4px; }
    .badge { background: #6D28D9; color: white; padding: 4px 8px; border-radius: 6px; font-size: 12px; }
  </style>
</head>
<body>
  <div class="card">
    <span class="badge">Soweto Hub</span>
    <h1>Mama Ruth's Spaza</h1>
    <p>Fresh daily bakery, maize meal, airtime & prepaid electricity.</p>
    <ul>
      <li>Airtime: Vodacom, MTN, Telkom</li>
      <li>Fresh Milk & Bread daily at 06:00</li>
    </ul>
  </div>
</body>
</html>"""
    ),
    ProjectTemplate(
        id = "kasi_crops",
        title = "Free State Smart Crop Monitor",
        language = "Python",
        description = "Soil moisture and solar alert rules for community vegetable gardens.",
        codeSnippet = """# Free State Community Garden Sensor Engine
soil_moisture = 28  # percentage
soil_temp = 34      # degrees Celsius
is_windy = False

print(f"Sensor Diagnostics: Soil {soil_moisture}% | Temp {soil_temp}°C")

if soil_moisture < 30 and soil_temp > 30:
    print("ACTION: High Heat Alert! Activate drip irrigation for 25 minutes.")
elif soil_moisture >= 60:
    print("STATUS: Soil saturation optimal. Irrigation suspended to save water.")
else:
    print("STATUS: Crop climate normal. Routine schedule active.")"""
    ),
    ProjectTemplate(
        id = "brand_styling",
        title = "KodeMamas Vibrant Theme",
        language = "CSS",
        description = "CSS variables and responsive styling for high contrast mobile reading.",
        codeSnippet = """:root {
  --primary: #6D28D9;
  --accent: #FBBF24;
  --bg-dark: #111827;
  --text-light: #F9FAFB;
}

body {
  background-color: var(--bg-dark);
  color: var(--text-light);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto;
  margin: 0;
  padding: 16px;
}

.title {
  color: var(--accent);
  font-size: 24px;
  font-weight: bold;
}"""
    )
)

@Composable
fun BuildsTab(viewModel: MainViewModel, langCode: String) {
    val colors = LocalKodeMamasColors.current
    val clipboardManager = LocalClipboardManager.current
    val editorText by viewModel.editorText.collectAsState()
    val simulatorOutput by viewModel.simulatorOutput.collectAsState()
    val simulatorSuccess by viewModel.simulatorSuccess.collectAsState()

    var selectedLang by remember { mutableStateOf("JavaScript") }
    var selectedTemplateId by remember { mutableStateOf<String?>("spaza_calc") }
    var activeOutputTab by remember { mutableStateOf(0) } // 0: Output, 1: Details

    // Initialize with a practical starter if empty
    LaunchedEffect(Unit) {
        if (editorText.isBlank()) {
            val defaultTpl = SA_PROJECT_TEMPLATES.first()
            selectedLang = defaultTpl.language
            selectedTemplateId = defaultTpl.id
            viewModel.updateEditorText(defaultTpl.codeSnippet)
        }
    }

    // Quick syntax keys that mobile phone keyboards hide behind symbols menu
    val quickSyntaxKeys = listOf("<", ">", "/", "=", "\"", "'", "{", "}", "(", ")", ";", ":", "$", "+", "-", "*")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. BRANDED HERO BANNER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .testTag("builds_header_card"),
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceDark),
            border = BorderStroke(1.dp, Color(0xFF374151))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ThemeGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "KodeMamas Code Studio",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "Mobile Compiler & Sandbox",
                                color = ThemeGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))

                    // Zero Data / Offline pill
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Offline Ready",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Build and experiment with real code for township businesses, agriculture, and web pages right on your device.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // 2. PROJECT TEMPLATES SELECTOR
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "African Real-World Templates",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Load & Edit",
                    fontSize = 11.sp,
                    color = ThemeIndigo,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(SA_PROJECT_TEMPLATES) { tpl ->
                    val isSelected = selectedTemplateId == tpl.id
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                selectedTemplateId = tpl.id
                                selectedLang = tpl.language
                                viewModel.updateEditorText(tpl.codeSnippet)
                            }
                            .testTag("tpl_${tpl.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ThemeIndigo.copy(alpha = 0.08f) else colors.surface
                        ),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) ThemeIndigo else colors.cardBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = when (tpl.language) {
                                        "HTML" -> Color(0xFFFF5722).copy(alpha = 0.15f)
                                        "CSS" -> Color(0xFF1973E8).copy(alpha = 0.15f)
                                        "JavaScript" -> ThemeGold.copy(alpha = 0.25f)
                                        else -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = tpl.language,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (tpl.language) {
                                            "HTML" -> Color(0xFFFF5722)
                                            "CSS" -> Color(0xFF1973E8)
                                            "JavaScript" -> Color(0xFF92400E)
                                            else -> Color(0xFF047857)
                                        }
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = ThemeIndigo,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tpl.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tpl.description,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                maxLines = 2,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. LANGUAGE TABS (HTML, CSS, JS, Python)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("JavaScript", "Python", "HTML", "CSS").forEach { lang ->
                val isSelected = selectedLang == lang
                Button(
                    onClick = {
                        selectedLang = lang
                        // If template doesn't match, load language default
                        val matchingTpl = SA_PROJECT_TEMPLATES.firstOrNull { it.language == lang }
                        if (matchingTpl != null) {
                            selectedTemplateId = matchingTpl.id
                            viewModel.updateEditorText(matchingTpl.codeSnippet)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lang_tab_$lang"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) ThemeIndigo else colors.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(
                        text = if (lang == "JavaScript") "JS" else lang,
                        color = if (isSelected) Color.White else colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 4. SMART MOBILE CODE EDITOR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .testTag("code_editor_card"),
            colors = CardDefaults.cardColors(containerColor = ThemeSurfaceDark),
            border = BorderStroke(1.dp, Color(0xFF374151))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header of editor
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$selectedLang LIVE SCRIPT",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val tpl = SA_PROJECT_TEMPLATES.firstOrNull { it.id == selectedTemplateId }
                                if (tpl != null) viewModel.updateEditorText(tpl.codeSnippet)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = ThemeGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.updateEditorText("") },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mobile Code Editor Text Field
                TextField(
                    value = editorText,
                    onValueChange = { viewModel.updateEditorText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("code_editor_input"),
                    textStyle = TextStyle(
                        color = ThemeGold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = ThemeGold,
                        unfocusedTextColor = ThemeGold,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        cursorColor = ThemeGold,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // QUICK SYNTAX TOOLBAR FOR MOBILE PHONE KEYBOARDS
                Text(
                    text = "Quick Mobile Keys (Tap to insert)",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSyntaxKeys.forEach { key ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateEditorText(editorText + key)
                                },
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    color = ThemeGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // RUN ACTION BUTTON
                Button(
                    onClick = { viewModel.runSimulatorCode() },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("run_code_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Execute in Simulator",
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 5. COMPUTER MONITOR PREVIEW (DESKTOP VIEWPORT & TERMINAL)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = ThemeGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Desktop Computer Preview",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Live PC View",
                    color = Color(0xFF34D399),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        ComputerDesktopPreview(
            code = editorText,
            language = selectedLang,
            consoleOutput = simulatorOutput,
            title = SA_PROJECT_TEMPLATES.firstOrNull { it.id == selectedTemplateId }?.title ?: "Desktop Simulator",
            modifier = Modifier.fillMaxWidth()
        )

        // 6. DETAILED TERMINAL & COMPILER OUTPUT DISPLAY
        if (simulatorOutput.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("compiler_output_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (simulatorSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                ),
                border = BorderStroke(
                    1.dp,
                    if (simulatorSuccess) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = if (simulatorSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (simulatorSuccess) Color(0xFF059669) else Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (simulatorSuccess) "OUTPUT GENERATED (SUCCESS)" else "COMPILER NOTICE",
                                fontWeight = FontWeight.Black,
                                color = if (simulatorSuccess) Color(0xFF047857) else Color(0xFFB91C1C),
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(simulatorOutput))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Output",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SelectionContainer {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp)),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = simulatorOutput,
                                color = if (simulatorSuccess) Color(0xFF34D399) else Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(88.dp))
    }
}
