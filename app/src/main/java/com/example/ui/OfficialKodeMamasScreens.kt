package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// ---------------------------------------------------------------------------------
// ARTWORK: CUSTOM VECTOR ART & ILLUSTRATIONS (Compose Canvas)
// ---------------------------------------------------------------------------------

/**
 * Regal KodeMamas Monogram Logo: Interlaced gold & rose infinity/heart crown
 */
@Composable
fun KodeMamasMonogramLogo(modifier: Modifier = Modifier, sizeDp: Int = 44) {
    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val w = size.width
        val h = size.height
        
        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x60FBBF24), Color.Transparent),
                center = Offset(w / 2, h / 2),
                radius = w * 0.6f
            )
        )

        // Monogram Crown / Loop left
        val pathLeft = Path().apply {
            moveTo(w * 0.25f, h * 0.65f)
            cubicTo(w * 0.15f, h * 0.35f, w * 0.35f, h * 0.15f, w * 0.5f, h * 0.42f)
            cubicTo(w * 0.65f, h * 0.15f, w * 0.85f, h * 0.35f, w * 0.75f, h * 0.65f)
            cubicTo(w * 0.65f, h * 0.90f, w * 0.35f, h * 0.90f, w * 0.25f, h * 0.65f)
            close()
        }
        drawPath(
            path = pathLeft,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFBBF24), Color(0xFFFA4D89), Color(0xFFE02885))
            ),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Center jewel / node
        drawCircle(
            color = Color(0xFFFDE68A),
            radius = 3.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.42f)
        )
    }
}

/**
 * Screen 1 Hero Art: Authentic, beautifully rendered vector portrait of the South African Woman (Mama)
 * matching the official KodeMamas design: warm front-facing smile, expressive eyes, glowing melanin skin,
 * traditional sculptured pink/magenta & gold headwrap (doek/gele), radiant gold hoop earrings, and celestial halo.
 */
@Composable
fun OnboardingHeroIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.72f)
            .widthIn(max = 280.dp)
            .aspectRatio(1f)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Radiant Multi-Stop Celestial Halo Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFA4D89).copy(alpha = 0.85f),
                        Color(0xFFE02885).copy(alpha = 0.70f),
                        Color(0xFF8B1874).copy(alpha = 0.50f),
                        Color(0xFF3B0764).copy(alpha = 0.35f),
                        Color(0x00120320)
                    ),
                    center = Offset(w * 0.50f, h * 0.46f),
                    radius = w * 0.52f
                )
            )

            // 2. Concentric radiant aura rings & stardust
            drawCircle(
                color = Color(0xFFFA4D89).copy(alpha = 0.35f),
                radius = w * 0.47f,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = 0.40f),
                radius = w * 0.42f,
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFC084FC).copy(alpha = 0.25f),
                radius = w * 0.37f,
                style = Stroke(width = 1.dp.toPx())
            )

            // Floating gold & rose starlight sparkles
            drawCircle(color = Color(0xFFFDE68A), radius = 2.5.dp.toPx(), center = Offset(w * 0.15f, h * 0.22f))
            drawCircle(color = Color(0xFFFA4D89), radius = 2.dp.toPx(), center = Offset(w * 0.84f, h * 0.20f))
            drawCircle(color = Color(0xFFFBBF24), radius = 3.dp.toPx(), center = Offset(w * 0.88f, h * 0.44f))
            drawCircle(color = Color(0xFFFDE68A), radius = 2.dp.toPx(), center = Offset(w * 0.12f, h * 0.54f))
            drawCircle(color = Color(0xFFFFFFFF), radius = 1.5.dp.toPx(), center = Offset(w * 0.22f, h * 0.14f))
            drawCircle(color = Color(0xFFFBBF24), radius = 2.dp.toPx(), center = Offset(w * 0.78f, h * 0.12f))

            // 3. Township Skyline Silhouette (Bottom base in deep purple twilight)
            val skylinePath = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.86f)
                lineTo(w * 0.08f, h * 0.86f)
                lineTo(w * 0.12f, h * 0.80f)
                lineTo(w * 0.20f, h * 0.80f)
                lineTo(w * 0.24f, h * 0.87f)
                lineTo(w * 0.35f, h * 0.87f)
                lineTo(w * 0.38f, h * 0.81f)
                lineTo(w * 0.48f, h * 0.81f)
                lineTo(w * 0.52f, h * 0.85f)
                lineTo(w * 0.65f, h * 0.85f)
                lineTo(w * 0.70f, h * 0.79f)
                lineTo(w * 0.80f, h * 0.79f)
                lineTo(w * 0.85f, h * 0.86f)
                lineTo(w, h * 0.86f)
                lineTo(w, h)
                close()
            }
            drawPath(
                path = skylinePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x99240846), Color(0xFC0B0217))
                )
            )

            // 4. Shoulders & Royal Attire (Scoop neck blouse in magenta/violet gradient)
            val shoulderPath = Path().apply {
                moveTo(w * 0.10f, h)
                cubicTo(w * 0.14f, h * 0.80f, w * 0.28f, h * 0.77f, w * 0.36f, h * 0.76f)
                cubicTo(w * 0.42f, h * 0.84f, w * 0.58f, h * 0.84f, w * 0.64f, h * 0.76f)
                cubicTo(w * 0.72f, h * 0.77f, w * 0.86f, h * 0.80f, w * 0.90f, h)
                close()
            }
            drawPath(
                path = shoulderPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE02885), Color(0xFF7C3AED), Color(0xFF1E0638))
                )
            )

            // Gold piping along blouse neckline
            val necklinePath = Path().apply {
                moveTo(w * 0.36f, h * 0.76f)
                cubicTo(w * 0.42f, h * 0.84f, w * 0.58f, h * 0.84f, w * 0.64f, h * 0.76f)
            }
            drawPath(
                path = necklinePath,
                brush = Brush.horizontalGradient(listOf(Color(0xFFFDE68A), Color(0xFFFBBF24), Color(0xFFFDE68A))),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 5. African Beaded Gold Choker Necklace
            val chokerPath = Path().apply {
                moveTo(w * 0.41f, h * 0.72f)
                cubicTo(w * 0.46f, h * 0.75f, w * 0.54f, h * 0.75f, w * 0.59f, h * 0.72f)
            }
            drawPath(
                path = chokerPath,
                brush = Brush.horizontalGradient(listOf(Color(0xFFFBBF24), Color(0xFFFDE68A), Color(0xFFFBBF24))),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 6. Graceful Neck (Warm radiant African bronze tone)
            val neckPath = Path().apply {
                moveTo(w * 0.42f, h * 0.60f)
                lineTo(w * 0.40f, h * 0.76f)
                lineTo(w * 0.60f, h * 0.76f)
                lineTo(w * 0.58f, h * 0.60f)
                close()
            }
            drawPath(
                path = neckPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4A2012), Color(0xFF5A2A18), Color(0xFF38150B))
                )
            )

            // 7. Face Oval & Sculpted Jawline (Facing forward with welcoming warmth)
            val facePath = Path().apply {
                moveTo(w * 0.31f, h * 0.44f) // Left temple
                cubicTo(w * 0.30f, h * 0.54f, w * 0.33f, h * 0.63f, w * 0.43f, h * 0.70f) // Left cheek & jaw
                cubicTo(w * 0.47f, h * 0.73f, w * 0.53f, h * 0.73f, w * 0.57f, h * 0.70f) // Rounded chin
                cubicTo(w * 0.67f, h * 0.63f, w * 0.70f, h * 0.54f, w * 0.69f, h * 0.44f) // Right cheek & jaw
                close()
            }
            drawPath(
                path = facePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6B331A), // Warm bronze highlight at center
                        Color(0xFF542513),
                        Color(0xFF3B160A)  // Deep rich espresso contour
                    ),
                    center = Offset(w * 0.50f, h * 0.55f),
                    radius = w * 0.26f
                )
            )

            // Soft glowing rose-amber cheek blush
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x50FA4D89), Color.Transparent),
                    center = Offset(w * 0.38f, h * 0.57f),
                    radius = w * 0.08f
                ),
                radius = w * 0.08f,
                center = Offset(w * 0.38f, h * 0.57f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x50FA4D89), Color.Transparent),
                    center = Offset(w * 0.62f, h * 0.57f),
                    radius = w * 0.08f
                ),
                radius = w * 0.08f,
                center = Offset(w * 0.62f, h * 0.57f)
            )

            // 8. Iconic African Gold Hoop Earrings (Left and Right)
            // Left Gold Hoop Earring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFFFDE68A), Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFFDE68A)),
                    center = Offset(w * 0.28f, h * 0.57f)
                ),
                radius = 11.dp.toPx(),
                center = Offset(w * 0.28f, h * 0.57f),
                style = Stroke(width = 2.8.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = 1.5.dp.toPx(),
                center = Offset(w * 0.26f, h * 0.54f)
            )

            // Right Gold Hoop Earring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFFFDE68A), Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFFDE68A)),
                    center = Offset(w * 0.72f, h * 0.57f)
                ),
                radius = 11.dp.toPx(),
                center = Offset(w * 0.72f, h * 0.57f),
                style = Stroke(width = 2.8.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = 1.5.dp.toPx(),
                center = Offset(w * 0.74f, h * 0.54f)
            )

            // 9. Majestic Traditional Headwrap (Gele / Doek / Iduku)
            // Grand crowning volume framing the top of the head
            val geleCrown = Path().apply {
                moveTo(w * 0.24f, h * 0.44f)
                cubicTo(w * 0.12f, h * 0.32f, w * 0.16f, h * 0.12f, w * 0.38f, h * 0.08f)
                cubicTo(w * 0.50f, h * 0.05f, w * 0.64f, h * 0.06f, w * 0.76f, h * 0.14f)
                cubicTo(w * 0.88f, h * 0.22f, w * 0.88f, h * 0.34f, w * 0.76f, h * 0.44f)
                cubicTo(w * 0.68f, h * 0.42f, w * 0.50f, h * 0.41f, w * 0.32f, h * 0.42f)
                close()
            }
            drawPath(
                path = geleCrown,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFBE185D),
                        Color(0xFFE02885),
                        Color(0xFF9333EA),
                        Color(0xFF4A044E)
                    ),
                    start = Offset(w * 0.2f, h * 0.08f),
                    end = Offset(w * 0.8f, h * 0.44f)
                )
            )

            // Gele Sculptural Pleat Fold 1 (Sweeping golden & coral ribbon band)
            val geleFold1 = Path().apply {
                moveTo(w * 0.20f, h * 0.38f)
                cubicTo(w * 0.32f, h * 0.22f, w * 0.62f, h * 0.20f, w * 0.80f, h * 0.34f)
                cubicTo(w * 0.72f, h * 0.38f, w * 0.42f, h * 0.36f, w * 0.20f, h * 0.38f)
                close()
            }
            drawPath(
                path = geleFold1,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFBBF24), Color(0xFFFA4D89), Color(0xFFE02885))
                )
            )

            // Gele Sculptural Pleat Fold 2 (Top crest fold with delicate golden line)
            val geleFold2 = Path().apply {
                moveTo(w * 0.28f, h * 0.26f)
                cubicTo(w * 0.45f, h * 0.12f, w * 0.68f, h * 0.14f, w * 0.78f, h * 0.24f)
            }
            drawPath(
                path = geleFold2,
                brush = Brush.horizontalGradient(listOf(Color(0xFFFDE68A), Color(0xFFFBBF24))),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            val geleFold3 = Path().apply {
                moveTo(w * 0.24f, h * 0.18f)
                cubicTo(w * 0.40f, h * 0.08f, w * 0.62f, h * 0.09f, w * 0.72f, h * 0.16f)
            }
            drawPath(
                path = geleFold3,
                color = Color(0xE6FFFFFF),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Front Brow Band of Headwrap (Sits elegantly over forehead)
            val frontBand = Path().apply {
                moveTo(w * 0.27f, h * 0.44f)
                cubicTo(w * 0.38f, h * 0.40f, w * 0.62f, h * 0.40f, w * 0.73f, h * 0.44f)
                cubicTo(w * 0.68f, h * 0.47f, w * 0.32f, h * 0.47f, w * 0.27f, h * 0.44f)
                close()
            }
            drawPath(
                path = frontBand,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE02885), Color(0xFFFBBF24), Color(0xFFE02885))
                )
            )

            // Traditional South African Diamond Motif on Front Band
            drawCircle(color = Color(0xFFFDE68A), radius = 2.5.dp.toPx(), center = Offset(w * 0.50f, h * 0.43f))
            drawCircle(color = Color(0xFFFFFFFF), radius = 1.5.dp.toPx(), center = Offset(w * 0.42f, h * 0.425f))
            drawCircle(color = Color(0xFFFFFFFF), radius = 1.5.dp.toPx(), center = Offset(w * 0.58f, h * 0.425f))

            // 10. Expressive Arched Eyebrows
            // Left Eyebrow
            val leftBrow = Path().apply {
                moveTo(w * 0.37f, h * 0.475f)
                cubicTo(w * 0.41f, h * 0.455f, w * 0.45f, h * 0.458f, w * 0.47f, h * 0.478f)
            }
            drawPath(
                path = leftBrow,
                color = Color(0xFF1E0A04),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Right Eyebrow
            val rightBrow = Path().apply {
                moveTo(w * 0.53f, h * 0.478f)
                cubicTo(w * 0.55f, h * 0.458f, w * 0.59f, h * 0.455f, w * 0.63f, h * 0.475f)
            }
            drawPath(
                path = rightBrow,
                color = Color(0xFF1E0A04),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 11. Warm, Sparkling Almond Eyes
            // Left Eye
            val leftEyeWhite = Path().apply {
                moveTo(w * 0.365f, h * 0.510f)
                cubicTo(w * 0.400f, h * 0.490f, w * 0.445f, h * 0.495f, w * 0.465f, h * 0.515f)
                cubicTo(w * 0.440f, h * 0.528f, w * 0.395f, h * 0.525f, w * 0.365f, h * 0.510f)
                close()
            }
            drawPath(path = leftEyeWhite, color = Color(0xFFF1F5F9))

            // Left Upper Eyelash line
            val leftLash = Path().apply {
                moveTo(w * 0.360f, h * 0.512f)
                cubicTo(w * 0.400f, h * 0.488f, w * 0.445f, h * 0.492f, w * 0.470f, h * 0.515f)
            }
            drawPath(path = leftLash, color = Color(0xFF130602), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

            // Left Iris & Pupil
            drawCircle(
                color = Color(0xFF33160B),
                radius = 3.6.dp.toPx(),
                center = Offset(w * 0.420f, h * 0.508f)
            )
            drawCircle(
                color = Color(0xFF0F0401),
                radius = 2.0.dp.toPx(),
                center = Offset(w * 0.420f, h * 0.508f)
            )
            // Left Specular Sparkle (Alive highlight)
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = 1.2.dp.toPx(),
                center = Offset(w * 0.425f, h * 0.503f)
            )

            // Right Eye
            val rightEyeWhite = Path().apply {
                moveTo(w * 0.535f, h * 0.515f)
                cubicTo(w * 0.555f, h * 0.495f, w * 0.600f, h * 0.490f, w * 0.635f, h * 0.510f)
                cubicTo(w * 0.605f, h * 0.525f, w * 0.560f, h * 0.528f, w * 0.535f, h * 0.515f)
                close()
            }
            drawPath(path = rightEyeWhite, color = Color(0xFFF1F5F9))

            // Right Upper Eyelash line
            val rightLash = Path().apply {
                moveTo(w * 0.530f, h * 0.515f)
                cubicTo(w * 0.555f, h * 0.492f, w * 0.600f, h * 0.488f, w * 0.640f, h * 0.512f)
            }
            drawPath(path = rightLash, color = Color(0xFF130602), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

            // Right Iris & Pupil
            drawCircle(
                color = Color(0xFF33160B),
                radius = 3.6.dp.toPx(),
                center = Offset(w * 0.580f, h * 0.508f)
            )
            drawCircle(
                color = Color(0xFF0F0401),
                radius = 2.0.dp.toPx(),
                center = Offset(w * 0.580f, h * 0.508f)
            )
            // Right Specular Sparkle
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = 1.2.dp.toPx(),
                center = Offset(w * 0.585f, h * 0.503f)
            )

            // 12. Gentle Sculpted Nose Bridge & Nostril Wing Contours
            val noseBridge = Path().apply {
                moveTo(w * 0.49f, h * 0.495f)
                lineTo(w * 0.485f, h * 0.560f)
                cubicTo(w * 0.465f, h * 0.575f, w * 0.480f, h * 0.585f, w * 0.500f, h * 0.585f)
                cubicTo(w * 0.520f, h * 0.585f, w * 0.535f, h * 0.575f, w * 0.515f, h * 0.560f)
            }
            drawPath(
                path = noseBridge,
                color = Color(0x70260D05),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            )
            // Nose tip soft glow highlight
            drawCircle(
                color = Color(0x35FA4D89),
                radius = 2.dp.toPx(),
                center = Offset(w * 0.50f, h * 0.575f)
            )

            // 13. Beautiful, Warm Smiling Mouth & Lips
            // Bright white smile teeth
            val smileTeeth = Path().apply {
                moveTo(w * 0.450f, h * 0.638f)
                cubicTo(w * 0.480f, h * 0.648f, w * 0.520f, h * 0.648f, w * 0.550f, h * 0.638f)
                cubicTo(w * 0.530f, h * 0.655f, w * 0.470f, h * 0.655f, w * 0.450f, h * 0.638f)
                close()
            }
            drawPath(path = smileTeeth, color = Color(0xFFFFFBEB))

            // Upper Lip in warm berry-rose
            val upperLip = Path().apply {
                moveTo(w * 0.430f, h * 0.635f)
                cubicTo(w * 0.470f, h * 0.625f, w * 0.490f, h * 0.630f, w * 0.500f, h * 0.633f)
                cubicTo(w * 0.510f, h * 0.630f, w * 0.530f, h * 0.625f, w * 0.570f, h * 0.635f)
                cubicTo(w * 0.535f, h * 0.642f, w * 0.465f, h * 0.642f, w * 0.430f, h * 0.635f)
                close()
            }
            drawPath(path = upperLip, color = Color(0xFF9D174D))

            // Lower Lip in glowing raspberry with center highlight
            val lowerLip = Path().apply {
                moveTo(w * 0.435f, h * 0.638f)
                cubicTo(w * 0.465f, h * 0.665f, w * 0.535f, h * 0.665f, w * 0.565f, h * 0.638f)
                cubicTo(w * 0.540f, h * 0.672f, w * 0.460f, h * 0.672f, w * 0.435f, h * 0.638f)
                close()
            }
            drawPath(path = lowerLip, color = Color(0xFFBE185D))

            // Lip gloss highlight
            drawLine(
                color = Color(0x80F472B6),
                start = Offset(w * 0.485f, h * 0.662f),
                end = Offset(w * 0.515f, h * 0.662f),
                strokeWidth = 1.4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Warm corner dimples of smile
            drawLine(
                color = Color(0x603A1206),
                start = Offset(w * 0.425f, h * 0.632f),
                end = Offset(w * 0.420f, h * 0.638f),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0x603A1206),
                start = Offset(w * 0.575f, h * 0.632f),
                end = Offset(w * 0.580f, h * 0.638f),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Screen 2 Project Banner Artwork: Modern Black Mama coding on laptop with vibrant ideas
 */
@Composable
fun HomeProjectBannerIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(110.dp, 100.dp)) {
        val w = size.width
        val h = size.height

        // Background creative energy aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x80FA4D89), Color(0x309333EA), Color.Transparent),
                center = Offset(w * 0.65f, h * 0.5f),
                radius = w * 0.55f
            )
        )

        // Woman head with puff/braids
        drawCircle(
            color = Color(0xFF1F0D07),
            radius = 16.dp.toPx(),
            center = Offset(w * 0.65f, h * 0.34f)
        )
        // High puff bun
        drawCircle(
            color = Color(0xFF130603),
            radius = 14.dp.toPx(),
            center = Offset(w * 0.72f, h * 0.20f)
        )

        // Pink top / shirt
        val bodyPath = Path().apply {
            moveTo(w * 0.48f, h * 0.85f)
            lineTo(w * 0.54f, h * 0.50f)
            cubicTo(w * 0.60f, h * 0.48f, w * 0.70f, h * 0.48f, w * 0.76f, h * 0.50f)
            lineTo(w * 0.84f, h * 0.85f)
            close()
        }
        drawPath(
            path = bodyPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFA4D89), Color(0xFFE02885))
            )
        )

        // Open Laptop
        val laptopBase = Path().apply {
            moveTo(w * 0.20f, h * 0.82f)
            lineTo(w * 0.55f, h * 0.82f)
            lineTo(w * 0.58f, h * 0.85f)
            lineTo(w * 0.17f, h * 0.85f)
            close()
        }
        drawPath(path = laptopBase, color = Color(0xFFE5E7EB))

        val laptopScreen = Path().apply {
            moveTo(w * 0.38f, h * 0.55f)
            lineTo(w * 0.54f, h * 0.55f)
            lineTo(w * 0.52f, h * 0.82f)
            lineTo(w * 0.36f, h * 0.82f)
            close()
        }
        drawPath(path = laptopScreen, color = Color(0xFFD1D5DB))
        // Screen glow
        drawRect(
            brush = Brush.linearGradient(listOf(Color(0xFF67E8F9), Color(0xFFC084FC))),
            topLeft = Offset(w * 0.38f, h * 0.57f),
            size = Size(w * 0.14f, h * 0.22f)
        )

        // Floating sparkles
        drawCircle(color = Color(0xFFFBBF24), radius = 2.5.dp.toPx(), center = Offset(w * 0.28f, h * 0.35f))
        drawCircle(color = Color(0xFFFA4D89), radius = 3.dp.toPx(), center = Offset(w * 0.85f, h * 0.38f))
    }
}

/**
 * Screen 4 Mentorship Artwork: Duo of two African women collaborating at a laptop
 */
@Composable
fun MentorshipCollaborationIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        val w = size.width
        val h = size.height

        // Warm ambient glowing aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x60E02885), Color(0x307C22CE), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.45f
            )
        )

        // Line-art style duo collaborating
        // Mentor (Left)
        drawCircle(
            color = Color(0xFF381A12),
            radius = 18.dp.toPx(),
            center = Offset(w * 0.38f, h * 0.38f)
        )
        // Elegant headwrap on Mentor
        drawCircle(
            brush = Brush.linearGradient(listOf(Color(0xFFFA4D89), Color(0xFFFBBF24))),
            radius = 16.dp.toPx(),
            center = Offset(w * 0.35f, h * 0.28f)
        )

        // Mentee (Right)
        drawCircle(
            color = Color(0xFF261009),
            radius = 18.dp.toPx(),
            center = Offset(w * 0.62f, h * 0.42f)
        )
        // Hair puffs
        drawCircle(
            color = Color(0xFF130603),
            radius = 12.dp.toPx(),
            center = Offset(w * 0.68f, h * 0.30f)
        )

        // Collaborative desk & laptop between them
        val laptop = Path().apply {
            moveTo(w * 0.44f, h * 0.68f)
            lineTo(w * 0.56f, h * 0.68f)
            lineTo(w * 0.58f, h * 0.72f)
            lineTo(w * 0.42f, h * 0.72f)
            close()
        }
        drawPath(path = laptop, color = Color(0xFFF3F4F6))

        // Screen glowing towards them
        val screen = Path().apply {
            moveTo(w * 0.46f, h * 0.52f)
            lineTo(w * 0.54f, h * 0.52f)
            lineTo(w * 0.55f, h * 0.68f)
            lineTo(w * 0.45f, h * 0.68f)
            close()
        }
        drawPath(
            path = screen,
            brush = Brush.verticalGradient(listOf(Color(0xFFFA4D89), Color(0xFF8B5CF6)))
        )

        // Connecting golden guidance loop
        val loopPath = Path().apply {
            moveTo(w * 0.32f, h * 0.60f)
            cubicTo(w * 0.42f, h * 0.40f, w * 0.58f, h * 0.40f, w * 0.68f, h * 0.60f)
        }
        drawPath(
            path = loopPath,
            brush = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFFA4D89))),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

// ---------------------------------------------------------------------------------
// SCREEN 1: ONBOARDING / WELCOME SCREEN ("Let's Begin ->")
// ---------------------------------------------------------------------------------

@Composable
fun KodeMamasOnboardingView(
    langCode: String = "en",
    onBeginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0116))
    ) {
        // Decorative background subtle glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x35E02885), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.40f),
                    radius = w * 0.75f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x25FBBF24), Color.Transparent),
                    center = Offset(w * 0.8f, h * 0.15f),
                    radius = w * 0.5f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Top Brand Header with Tagline & Official South Africa Heritage Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KodeMamasMonogramLogo(sizeDp = 42)
                    Column {
                        Text(
                            text = "KODEMAMAS",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "Code. Create. Change Everything.",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFFA4D89),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // South African Heritage & Township Community Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF240E3A).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFFFA4D89).copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🇿🇦", fontSize = 13.sp)
                        Text(
                            text = com.example.ui.theme.Localization.translate("sisterhood_mamas", langCode),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFDE68A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Center Hero Portrait Illustration with Radiant Aura & Gele
            OnboardingHeroIllustration(modifier = Modifier.padding(vertical = 4.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Content Section: Heading, Value Highlights, Carousel, Button & Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = com.example.ui.theme.Localization.translate("onboarding_headline", langCode),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 23.sp,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Feature Highlights Pills (Zulu/Xhosa, Offline Mode, Mentorship)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1F0933),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "🗣️ 12 SA Languages", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1F0933),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "📶 " + com.example.ui.theme.Localization.translate("offline_first", langCode), fontSize = 11.sp, color = Color(0xFFFBBF24))
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1F0933),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "👭 " + com.example.ui.theme.Localization.translate("sisterhood", langCode), fontSize = 11.sp, color = Color(0xFFFA4D89))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Carousel indicator dots (active dot in radiant gradient)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFBBF24), Color(0xFFFA4D89))
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button: "Let's Begin ->" (vibrant magenta-to-coral gradient pill)
                Button(
                    onClick = onBeginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .testTag("onboarding_begin_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE02885), Color(0xFFFA4D89), Color(0xFFFF758C))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = com.example.ui.theme.Localization.translate("get_started", langCode),
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Begin",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer note
                Text(
                    text = "Learn in your language. At your pace. For your future.",
                    fontSize = 11.sp,
                    color = Color(0xFFC4B5DC).copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// SCREEN 2: HOME SCREEN ("Hello, Mama! 👋 Ready to build your future?")
// ---------------------------------------------------------------------------------

@Composable
fun KodeMamasHomeView(
    viewModel: MainViewModel,
    onNavigateToCourse: () -> Unit,
    onNavigateToMentorship: () -> Unit,
    onStartProject: () -> Unit,
    onOpenShowcase: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToAiChat: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val langCode by viewModel.currentLanguageCode.collectAsState()
    val allLessons by viewModel.allLessons.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val completedCount = allLessons.count { lesson ->
        progressList.any { it.lessonId == lesson.id && it.isCompleted }
    }
    val completedIds = remember(progressList) {
        progressList.filter { it.isCompleted }.map { it.lessonId }.toSet()
    }
    val activeLesson = remember(allLessons, completedIds) {
        allLessons.firstOrNull { it.id !in completedIds } ?: allLessons.firstOrNull()
    }
    val totalCount = allLessons.size.coerceAtLeast(1)
    val progressPercent = ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt()
    val progressFraction = (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f)
    val remainingCount = (allLessons.size - completedCount).coerceAtLeast(0)
    var selectedCategory by remember { mutableStateOf("Web Dev") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0219))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
    ) {
        // 1. Top Header: "Hello, Mama! 👋" & Avatar/Bell
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = com.example.ui.theme.Localization.translate("hello_mama", langCode),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = com.example.ui.theme.Localization.translate("ready_future", langCode),
                        color = Color(0xFFC4B5DC),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Notification Bell with badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B0630))
                            .border(1.dp, Color(0xFF3B1366), CircleShape)
                            .clickable { onOpenShowcase() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-6).dp, y = 6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFA4D89))
                        )
                    }

                    // Avatar with glowing ring
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(Color(0xFFFA4D89), Color(0xFFFBBF24), Color(0xFFFA4D89))
                                )
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22093F))
                            .clickable { onOpenSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userProfile?.name?.take(1)?.uppercase() ?: "M"),
                            color = Color(0xFFFDE68A),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // 2. Weekly Progress Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A062F)),
                border = BorderStroke(1.dp, Color(0xFF38105B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_progress_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Top motivating badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2D0D4E),
                        border = BorderStroke(1.dp, Color(0xFF4C1780))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "★",
                                color = Color(0xFFFBBF24),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = com.example.ui.theme.Localization.translate("keep_going", langCode),
                                color = Color(0xFFFDE68A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title & Percentage Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.ui.theme.Localization.translate("weekly_progress", langCode),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$progressPercent%",
                            color = Color(0xFFFA4D89),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gradient Progress Bar (Starts from 0 and fills dynamically)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2A0A47))
                    ) {
                        if (progressFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFE02885), Color(0xFFFA4D89))
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lessons completed sub-row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (completedCount == 0) "0 modules completed (Start learning!)" else "$completedCount of ${allLessons.size} completed",
                            color = Color(0xFFC4B5DC),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$remainingCount to go",
                            color = Color(0xFFC4B5DC),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 3. Continue Learning Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.example.ui.theme.Localization.translate("continue_learning", langCode),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = com.example.ui.theme.Localization.translate("view_all", langCode),
                        color = Color(0xFFFA4D89),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToCourse() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Continue Learning Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A062F)),
                    border = BorderStroke(1.dp, Color(0xFF38105B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (activeLesson != null) {
                                viewModel.startLesson(activeLesson)
                            } else {
                                onNavigateToCourse()
                            }
                        }
                        .testTag("continue_learning_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Code icon badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF4C1D95), Color(0xFF7C22CE))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "</>",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = com.example.ui.theme.Localization.getLessonTitle(activeLesson?.id ?: "html_1", langCode),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = com.example.ui.theme.Localization.getLessonSubtitle(activeLesson?.id ?: "html_1", langCode),
                                color = Color(0xFFC4B5DC),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Dynamic activity-driven percentage pill indicator (starts from 0%)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2E094E),
                            border = BorderStroke(1.dp, Color(0xFFFA4D89).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (completedCount == 0) "0% (Start)" else "$progressPercent%",
                                color = Color(0xFFFA4D89),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Explore Categories Section
        item {
            Column {
                Text(
                    text = com.example.ui.theme.Localization.translate("explore_categories", langCode),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val catWebDev = com.example.ui.theme.Localization.translate("cat_web_dev", langCode)
                val catMobileDev = com.example.ui.theme.Localization.translate("cat_mobile_dev", langCode)
                val catDataAi = com.example.ui.theme.Localization.translate("cat_data_ai", langCode)
                val catDesign = com.example.ui.theme.Localization.translate("cat_design", langCode)
                val catMore = com.example.ui.theme.Localization.translate("cat_more", langCode)

                val categories = listOf(
                    Triple(catWebDev, "</>", Color(0xFF7C22CE)),
                    Triple(catMobileDev, "📱", Color(0xFF0284C7)),
                    Triple(catDataAi, "🤖", Color(0xFF10B981)),
                    Triple(catDesign, "🎨", Color(0xFFF59E0B)),
                    Triple(catMore, "...", Color(0xFF6B7280))
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { (name, iconText, iconBg) ->
                        val isSelected = selectedCategory == name
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF2D0C4E) else Color(0xFF18052B),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFFA4D89) else Color(0xFF340E53)
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedCategory = name
                                    when (name) {
                                        catMobileDev, "Mobile Dev" -> {
                                            viewModel.setSelectedCategoryFilter("Mobile Dev")
                                            viewModel.closeCourseDetail()
                                            viewModel.selectTab("learn")
                                        }
                                        catDataAi, "Data & AI" -> {
                                            viewModel.closeCourseDetail()
                                            onNavigateToAiChat()
                                        }
                                        catDesign, "Design" -> {
                                            viewModel.setSelectedCategoryFilter("Design")
                                            viewModel.closeCourseDetail()
                                            viewModel.selectTab("learn")
                                        }
                                        catWebDev, "Web Dev" -> {
                                            viewModel.setSelectedCategoryFilter("HTML")
                                            viewModel.closeCourseDetail()
                                            viewModel.selectTab("learn")
                                        }
                                        else -> {
                                            viewModel.setSelectedCategoryFilter("All")
                                            viewModel.closeCourseDetail()
                                            viewModel.selectTab("learn")
                                        }
                                    }
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(iconBg.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = iconText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = name,
                                    color = if (isSelected) Color.White else Color(0xFFC4B5DC),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4b. Mama Ruth Multilingual AI Assistant Hero Banner (Connected to Gemini & Google Search Grounding)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0733)),
                border = BorderStroke(1.dp, Color(0xFFFA4D89).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAiChat() }
                    .testTag("home_ai_assistant_card")
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
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFE02885), Color(0xFFFBBF24))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = com.example.ui.theme.Localization.translate("mama_ruth_ai", langCode),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = com.example.ui.theme.Localization.translate("gemini_search_subtitle", langCode),
                                    color = Color(0xFFFDE68A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.ui.theme.Localization.translate("online_status", langCode).uppercase(),
                                    color = Color(0xFF34D399),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = com.example.ui.theme.Localization.translate("ai_banner_desc", langCode),
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language chips row (horizontally scrollable to avoid clipping on small devices)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(listOf("🇿🇦 isiZulu", "🇿🇦 isiXhosa", "🇿🇦 Afrikaans", "🇿🇦 Sepedi", "🇬🇧 English")) { langTag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF2E094E),
                                border = BorderStroke(1.dp, Color(0xFFFA4D89).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = langTag,
                                    color = Color(0xFFFDE68A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToAiChat,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFE02885), Color(0xFFFA4D89), Color(0xFFFBBF24))
                                    ),
                                    RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = com.example.ui.theme.Localization.translate("ask_in_language", langCode),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Project Banner Card: "Build projects. Solve real problems. Change your world."
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF22063D)),
                border = BorderStroke(1.dp, Color(0xFF481478)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_project_banner")
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val stackLayout = maxWidth < 420.dp
                    if (stackLayout) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = com.example.ui.theme.Localization.translate("start_project_banner", langCode),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 21.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onStartProject,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = com.example.ui.theme.Localization.translate("start_project_btn", langCode),
                                    color = Color(0xFF1B0630),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                HomeProjectBannerIllustration()
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = com.example.ui.theme.Localization.translate("start_project_banner", langCode),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onStartProject,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = com.example.ui.theme.Localization.translate("start_project_btn", langCode),
                                        color = Color(0xFF1B0630),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            HomeProjectBannerIllustration()
                        }
                    }
                }
            }
        }

        // Mentorship shortcut card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF150427)),
                border = BorderStroke(1.dp, Color(0xFF38105B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMentorship() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE02885).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = com.example.ui.theme.Localization.translate("mentorship_promo_title", langCode),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = com.example.ui.theme.Localization.translate("mentorship_promo_sub", langCode),
                            color = Color(0xFFC4B5DC),
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Mentorship",
                        tint = Color(0xFFFA4D89),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// SCREEN 3: COURSE DETAIL / LESSON SCREEN ("Introduction to Web Development")
// ---------------------------------------------------------------------------------

@Composable
fun KodeMamasCourseDetailView(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onStartLesson: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    var selectedCourseTab by remember { mutableStateOf("Curriculum") }
    val context = LocalContext.current
    val allLessons by viewModel.allLessons.collectAsState()
    val langCode by viewModel.currentLanguageCode.collectAsState()
    val progressList by viewModel.allProgress.collectAsState()
    val completedIds = remember(progressList) { progressList.filter { it.isCompleted }.map { it.lessonId }.toSet() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // Clean, crisp, high-contrast light canvas as shown in screenshot 3
            .statusBarsPadding()
    ) {
        // Top Navigation Bar (Back, Favorite, Share)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF111827)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFE02885) else Color(0xFF4B5563)
                    )
                }
                IconButton(onClick = {
                    Toast.makeText(context, "Course link copied to clipboard!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF4B5563)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Course Icon Badge & Category
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF7C22CE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "</>",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3E8FF)
                    ) {
                        Text(
                            text = "WEB DEVELOPMENT",
                            color = Color(0xFF7C22CE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Introduction to\nWeb Development",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Learn the fundamentals of HTML, CSS and JavaScript to build beautiful websites.",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chips row: 12 Lessons | Beginner | ⭐ 4.8 (320)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "📄 ${allLessons.size} Lessons",
                                fontSize = 11.sp,
                                color = Color(0xFF374151),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "🌱 Beginner",
                                fontSize = 11.sp,
                                color = Color(0xFF374151),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "⭐ 4.8 (320)",
                                fontSize = 11.sp,
                                color = Color(0xFF374151),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // "What you'll learn" Section
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "What you'll learn",
                            color = Color(0xFF111827),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val learningPoints = listOf(
                            "Build and structure web pages with HTML & CSS",
                            "Add interactivity with JavaScript & Python automation",
                            "Native Android Apps with Jetpack Compose (Mobile Dev)",
                            "Harness Gemini AI & Data Analytics for township businesses",
                            "Design accessible UI/UX with African color palettes"
                        )

                        learningPoints.forEach { point ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "✔",
                                    color = Color(0xFF7C22CE),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = point,
                                    color = Color(0xFF374151),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Highlight "Start Learning" Card with Floating Play Button
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartLesson() }
                        .testTag("course_start_learning_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6), Color(0xFF7C22CE))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "START LEARNING",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Lesson 1: Adding Style with CSS",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "60% Complete",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }

                            // Circular floating Play button
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Lesson",
                                    tint = Color(0xFF6D28D9),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Segmented Tabs: About | Curriculum | Reviews
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(4.dp)
                ) {
                    listOf("About", "Curriculum", "Reviews").forEach { tab ->
                        val isSelected = selectedCourseTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { selectedCourseTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color(0xFF111827) else Color(0xFF6B7280),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Curriculum Lesson List
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val curriculumList = if (allLessons.isNotEmpty()) allLessons else listOf(
                        com.example.data.Lesson("html_1", "Intro to HTML (Web Layout)", "Mam's Spaza Shop Storefront", "HTML", "Beginner", 10, isUnlocked = true, isDownloaded = true, orderIndex = 1)
                    )

                    curriculumList.forEach { lesson ->
                        val isCompleted = lesson.id in completedIds
                        val localizedTitle = com.example.ui.theme.Localization.getLessonTitle(lesson.id, langCode)
                        val localizedSubtitle = com.example.ui.theme.Localization.getLessonSubtitle(lesson.id, langCode)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.startLesson(lesson) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCompleted) Color(0xFFDCFCE7) else Color(0xFFF3F4F6)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Text("✓", color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("•", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = localizedTitle,
                                            color = Color(0xFF1F2937),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${lesson.category} • $localizedSubtitle",
                                            color = Color(0xFF6B7280),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Text(
                                    text = if (isCompleted) com.example.ui.theme.Localization.translate("lesson_completed", langCode) else "${lesson.durationMinutes}m",
                                    color = if (isCompleted) Color(0xFF16A34A) else Color(0xFF6B7280),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// SCREEN 4: MENTORSHIP SCREEN ("You don't have to do it alone.")
// ---------------------------------------------------------------------------------

@Composable
fun KodeMamasMentorshipProgramView(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onJoinProgram: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    val currentPlan by viewModel.currentPlanTier.collectAsState()
    val langCode by viewModel.currentLanguageCode.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0219)),
        containerColor = Color(0xFF0C0219),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFFA4D89) else Color.White.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "Mentorship program shared!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Sticky Bottom Pricing & Action Section: "R299 / month", "Join the Program ->"
            Surface(
                color = Color(0xFF140325),
                border = BorderStroke(1.dp, Color(0xFF38105B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = com.example.ui.theme.Localization.translate("monthly_price", langCode),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = com.example.ui.theme.Localization.translate("cancel_anytime", langCode),
                                color = Color(0xFFC4B5DC),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = onJoinProgram,
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .testTag("join_mentorship_program_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFE02885), Color(0xFFFA4D89), Color(0xFFFF758C))
                                        )
                                    )
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (currentPlan == "PREMIUM") com.example.ui.theme.Localization.translate("active_member", langCode) else com.example.ui.theme.Localization.translate("join_program", langCode),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Join",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = com.example.ui.theme.Localization.translate("safe_township", langCode),
                        color = Color(0xFFC4B5DC).copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // Header Badge & Title
            item {
                Column {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2E094E),
                        border = BorderStroke(1.dp, Color(0xFFFA4D89).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = com.example.ui.theme.Localization.translate("mentorship_title", langCode).uppercase(),
                            color = Color(0xFFFA4D89),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = com.example.ui.theme.Localization.translate("mentorship_promo_title", langCode),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = com.example.ui.theme.Localization.translate("mentorship_tagline", langCode),
                        color = Color(0xFFC4B5DC),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Artwork illustration of two women collaborating
            item {
                MentorshipCollaborationIllustration()
            }

            // 3 Core Benefit Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Benefit 1: 1-on-1 Mentorship
                    MentorshipBenefitCard(
                        icon = "💬",
                        iconColor = Color(0xFFE02885),
                        title = com.example.ui.theme.Localization.translate("one_on_one", langCode),
                        description = com.example.ui.theme.Localization.translate("one_on_one_desc", langCode)
                    )

                    // Benefit 2: Career Support
                    MentorshipBenefitCard(
                        icon = "💼",
                        iconColor = Color(0xFF10B981),
                        title = com.example.ui.theme.Localization.translate("careers", langCode),
                        description = com.example.ui.theme.Localization.translate("career_support_desc", langCode)
                    )

                    // Benefit 3: Community Access
                    MentorshipBenefitCard(
                        icon = "👥",
                        iconColor = Color(0xFFF97316),
                        title = com.example.ui.theme.Localization.translate("community", langCode),
                        description = com.example.ui.theme.Localization.translate("community_access_desc", langCode)
                    )
                }
            }
        }
    }
}

@Composable
fun MentorshipBenefitCard(
    icon: String,
    iconColor: Color,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18052B)),
        border = BorderStroke(1.dp, Color(0xFF38105B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color(0xFFC4B5DC),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// BOTTOM NAVIGATION BAR (Matching screenshot tabs)
// ---------------------------------------------------------------------------------

@Composable
fun OfficialKodeMamasBottomBar(
    currentTab: String,
    langCode: String = "en",
    onSelectTab: (String) -> Unit
) {
    val items = listOf(
        Pair("home", Icons.Default.Home to com.example.ui.theme.Localization.translate("dashboard", langCode)),
        Pair("learn", Icons.Default.School to com.example.ui.theme.Localization.translate("lessons", langCode)),
        Pair("ai_chat", Icons.Default.AutoAwesome to com.example.ui.theme.Localization.translate("ai_assistant", langCode)),
        Pair("projects", Icons.Default.Code to com.example.ui.theme.Localization.translate("builds", langCode)),
        Pair("community", Icons.Default.Forum to com.example.ui.theme.Localization.translate("community", langCode)),
        Pair("profile", Icons.Default.Person to com.example.ui.theme.Localization.translate("profile", langCode))
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .border(1.dp, Color(0xFF38105B), RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
        containerColor = Color(0xFF100322),
        tonalElevation = 6.dp
    ) {
        items.forEach { (route, iconAndLabel) ->
            val (icon, label) = iconAndLabel
            val isSelected = currentTab == route || (route == "profile" && currentTab == "mentorship")

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(route) },
                icon = {
                    if (route == "ai_chat") {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Color(0xFFFA4D89) else Color(0xFFFBBF24),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color(0xFFFA4D89) else Color(0xFFC4B5DC).copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFFFA4D89) else if (route == "ai_chat") Color(0xFFFDE68A) else Color(0xFFC4B5DC).copy(alpha = 0.7f),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = if (route == "ai_chat" && isSelected) Color(0xFF4C0535) else Color(0xFF2A0847)
                )
            )
        }
    }
}
