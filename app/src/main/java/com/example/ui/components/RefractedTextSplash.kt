package com.example.ui.components

import android.graphics.CornerPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow

/**
 * Splash screen wrapper for ZORVÉN:
 * - Single metallic shine sweep across the wordmark (no repeat, no looping).
 * - Fast & punchy ~1.6s total duration, then immediately transitions into the Home page.
 * - Tap anywhere skips straight to the Home page instantly.
 */
@Composable
fun RefractedTextSplashScreen(
    text: String = "ZORVÉN",
    onSplashFinished: () -> Unit
) {
    var hasFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Fast, premium 1.6s intro: single metallic shine, then direct seamless entry to Home
        delay(1600L)
        if (!hasFinished) {
            hasFinished = true
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!hasFinished) {
                        hasFinished = true
                        onSplashFinished()
                    }
                }
            )
            .padding(horizontal = 24.dp)
            .testTag("refracted_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        RefractedText(
            text = text,
            theme = "dark",
            topColor = "auto",
            bottomColor = "auto",
            beamColor = "auto",
            spectrumWarmColor = "auto",
            spectrumMiddleColor = "auto",
            spectrumCoolColor = "auto",
            reveal = true,
            stagger = 0.07f,
            liftDuration = 0.36f,
            ease = "power4.out",
            beamFollowsLift = false,
            animateBeam = true,
            openingDuration = 1.25f,
            duration = 1.25f,
            intensity = 1.35f,
            dispersion = 0.95f,
            colorIntensity = 1.0f,
            beamPosition = 0.5f,
            fixedBeam = false,
            paused = false,
            cursorMovesBeam = false,
            fontSize = "400px",
            fontFamily = "'ReactDrops Square Display', 'Arial Black', sans-serif",
            fontWeight = 900,
            letterSpacing = "normal",
            extraBold = 2.25f,
            fontHeight = 80f,
            cornerRounding = 1f
        )
    }
}

/**
 * Native Compose optical metallic glass refraction implementation:
 * - Geometric Square Display glyphs with square 'O' and sharp accented 'É'
 * - `extraBold = 2.25` with `cornerRounding = 1` and `fontHeight = 80`
 * - SINGLE METALLIC SHINE: sweeps cleanly once from left to right across the glyphs and stops.
 * - Zero loops, zero second shines, fast transition into the Home page.
 */
@Composable
fun RefractedText(
    text: String = "ZORVÉN",
    theme: String = "dark",
    topColor: String = "auto",
    bottomColor: String = "auto",
    beamColor: String = "auto",
    spectrumWarmColor: String = "auto",
    spectrumMiddleColor: String = "auto",
    spectrumCoolColor: String = "auto",
    reveal: Boolean = true,
    stagger: Float = 0.07f,
    liftDuration: Float = 0.36f,
    ease: String = "power4.out",
    beamFollowsLift: Boolean = false,
    animateBeam: Boolean = true,
    openingDuration: Float = 1.25f,
    duration: Float = 1.25f,
    intensity: Float = 1.35f,
    dispersion: Float = 0.95f,
    colorIntensity: Float = 1.0f,
    beamPosition: Float = 0.5f,
    fixedBeam: Boolean = false,
    paused: Boolean = false,
    cursorMovesBeam: Boolean = false,
    fontSize: String = "400px",
    fontFamily: String = "'ReactDrops Square Display', 'Arial Black', sans-serif",
    fontWeight: Int = 900,
    letterSpacing: String = "normal",
    extraBold: Float = 2.25f,
    fontHeight: Float = 80f,
    cornerRounding: Float = 1f,
    modifier: Modifier = Modifier
) {
    var elapsedSec by remember { mutableFloatStateOf(0f) }
    var pointerBeamX by remember { mutableFloatStateOf(beamPosition) }

    LaunchedEffect(paused) {
        if (paused) return@LaunchedEffect
        val startNs = System.nanoTime()
        while (true) {
            withInfiniteAnimationFrameMillis {
                val nowNs = System.nanoTime()
                elapsedSec = (nowNs - startNs) / 1_000_000_000f
            }
        }
    }

    // Polished titanium & chrome metallic colors
    val isDark = theme.lowercase() != "light"
    val resolvedTopColor = remember(topColor, isDark) {
        parseColorOrDefault(topColor, if (isDark) Color(0xFFE2E8F0) else Color(0xFFFFFFFF))
    }
    val resolvedBottomColor = remember(bottomColor, isDark) {
        parseColorOrDefault(bottomColor, if (isDark) Color(0xFF4A5568) else Color(0xFFCBD5E1))
    }
    val resolvedBeamColor = remember(beamColor) {
        parseColorOrDefault(beamColor, Color(0xFFFFFFFF))
    }
    val resolvedWarmColor = remember(spectrumWarmColor) {
        parseColorOrDefault(spectrumWarmColor, Color(0xFFFFD166))
    }
    val resolvedMiddleColor = remember(spectrumMiddleColor) {
        parseColorOrDefault(spectrumMiddleColor, Color(0xFFFFFFFF))
    }
    val resolvedCoolColor = remember(spectrumCoolColor) {
        parseColorOrDefault(spectrumCoolColor, Color(0xFF90E0EF))
    }

    // SINGLE METALLIC SHINE:
    // Starts off-screen left (-0.35), sweeps smoothly across all letters between 0.10s and 1.25s,
    // and stays off-screen right (1.35) permanently. Strictly no loop, no repeat shine!
    val activeBeamPos = remember(
        elapsedSec,
        animateBeam,
        fixedBeam,
        cursorMovesBeam,
        pointerBeamX,
        beamPosition
    ) {
        when {
            cursorMovesBeam -> pointerBeamX.coerceIn(0f, 1f)
            fixedBeam || !animateBeam -> beamPosition.coerceIn(0f, 1f)
            else -> {
                val sweepStart = 0.10f
                val sweepEnd = 1.25f
                if (elapsedSec < sweepStart) {
                    -0.35f
                } else if (elapsedSec <= sweepEnd) {
                    val t = ((elapsedSec - sweepStart) / (sweepEnd - sweepStart)).coerceIn(0f, 1f)
                    // Silky smoothstep curve for single metallic sweep
                    val easedT = t * t * (3f - 2f * t)
                    -0.35f + 1.70f * easedT
                } else {
                    // Stays permanently past the right edge — never repeats!
                    1.35f
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (cursorMovesBeam) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            pointerBeamX = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        val heightScale = (fontHeight / 100f).coerceIn(0.45f, 1.25f)
        val canvasHeightDp = (maxWidth.value * 0.34f).coerceIn(130f, 260f).dp

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeightDp)
                .graphicsLayer {
                    // Seamless exit fade directly into the Home screen at ~1.42s - 1.60s
                    alpha = if (elapsedSec > 1.42f) {
                        (1f - ((elapsedSec - 1.42f) / 0.18f)).coerceIn(0f, 1f)
                    } else 1f
                }
        ) {
            val upperText = text.uppercase()
            val chars = upperText.toList()
            if (chars.isEmpty()) return@Canvas

            val charCount = chars.size
            val gapRatio = if (letterSpacing == "normal") 0.085f else 0.11f
            val totalUnits = charCount * 1.0f + (charCount - 1) * gapRatio
            val glyphWidth = (size.width * 0.94f) / totalUnits
            val naturalGlyphHeight = glyphWidth * 1.08f
            val glyphHeight = naturalGlyphHeight * heightScale
            val gapPx = glyphWidth * gapRatio

            val totalWordWidth = charCount * glyphWidth + (charCount - 1) * gapPx
            val startX = (size.width - totalWordWidth) / 2f
            val centerY = size.height / 2f
            val baseTopY = centerY - glyphHeight / 2f
            val baseBottomY = centerY + glyphHeight / 2f

            val worldBeamX = startX + totalWordWidth * activeBeamPos
            val worldBeamY = centerY

            val clipBottomY = baseBottomY + glyphHeight * 0.32f
            val clipTopY = baseTopY - glyphHeight * 0.35f

            clipRect(
                left = 0f,
                top = clipTopY,
                right = size.width,
                bottom = clipBottomY
            ) {
                chars.forEachIndexed { index, ch ->
                    val glyphLeft = startX + index * (glyphWidth + gapPx)
                    val glyphCenterX = glyphLeft + glyphWidth / 2f
                    val normalizedCharX = ((glyphCenterX - startX) / max(1f, totalWordWidth)).coerceIn(0f, 1f)

                    // Fast character lift: letters rise into view swiftly and stay firmly in place
                    val liftProgress = if (!reveal) {
                        1f
                    } else {
                        val staggerDelay = if (charCount > 1) {
                            (index.toFloat() / (charCount - 1).toFloat()) * stagger
                        } else 0f
                        val rawT = ((elapsedSec - staggerDelay) / max(0.01f, liftDuration)).coerceIn(0f, 1f)
                        applyEasing(rawT, ease)
                    }

                    val liftOffsetY = (1f - liftProgress) * (glyphHeight * 0.95f)
                    val glyphAlpha = (liftProgress * 1.35f).coerceIn(0f, 1f)

                    if (glyphAlpha > 0.005f) {
                        val glyphRect = Rect(
                            left = glyphLeft,
                            top = baseTopY + liftOffsetY,
                            right = glyphLeft + glyphWidth,
                            bottom = baseBottomY + liftOffsetY
                        )

                        val outerNativePath = buildSquareDisplayGlyphPath(
                            char = ch,
                            rect = glyphRect,
                            extraBold = extraBold,
                            insetFraction = 0f
                        )
                        val innerBevelNativePath = buildSquareDisplayGlyphPath(
                            char = ch,
                            rect = glyphRect,
                            extraBold = extraBold,
                            insetFraction = 0.075f
                        )

                        val outerComposePath = outerNativePath.asComposePath()
                        val innerComposePath = innerBevelNativePath.asComposePath()

                        val effectiveBeamY = if (beamFollowsLift) worldBeamY + liftOffsetY else worldBeamY

                        drawRefractedGlassGlyph(
                            outerPath = outerComposePath,
                            innerPath = innerComposePath,
                            outerNativePath = outerNativePath,
                            glyphRect = glyphRect,
                            normalizedCharX = normalizedCharX,
                            activeBeamPos = activeBeamPos,
                            beamCenter = Offset(worldBeamX, effectiveBeamY),
                            wordWidth = totalWordWidth,
                            glyphAlpha = glyphAlpha,
                            intensity = intensity,
                            dispersion = dispersion,
                            colorIntensity = colorIntensity,
                            extraBold = extraBold,
                            cornerRounding = cornerRounding,
                            topColor = resolvedTopColor,
                            bottomColor = resolvedBottomColor,
                            beamColor = resolvedBeamColor,
                            warmColor = resolvedWarmColor,
                            middleColor = resolvedMiddleColor,
                            coolColor = resolvedCoolColor
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawRefractedGlassGlyph(
    outerPath: androidx.compose.ui.graphics.Path,
    innerPath: androidx.compose.ui.graphics.Path,
    outerNativePath: Path,
    glyphRect: Rect,
    normalizedCharX: Float,
    activeBeamPos: Float,
    beamCenter: Offset,
    wordWidth: Float,
    glyphAlpha: Float,
    intensity: Float,
    dispersion: Float,
    colorIntensity: Float,
    extraBold: Float,
    cornerRounding: Float,
    topColor: Color,
    bottomColor: Color,
    beamColor: Color,
    warmColor: Color,
    middleColor: Color,
    coolColor: Color
) {
    val distFromBeam = normalizedCharX - activeBeamPos
    val absDist = abs(distFromBeam)
    // Gaussian falloff for metallic reflection
    val sigma = 0.18f
    val beamProximity = exp(-(distFromBeam * distFromBeam) / (2f * sigma * sigma))
    val localBeamEnergy = (beamProximity * intensity).coerceIn(0f, 1.9f)

    // 1. Clipped 3D Glass & Metallic Interior
    clipPath(path = outerPath, clipOp = ClipOp.Intersect) {
        val topGlassAlpha = ((0.28f + 0.45f * localBeamEnergy) * glyphAlpha).coerceIn(0f, 1f)
        val bottomGlassAlpha = ((0.20f + 0.35f * localBeamEnergy) * glyphAlpha).coerceIn(0f, 1f)

        // Dark metallic titanium body
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to topColor.copy(alpha = topGlassAlpha),
                0.18f to Color(0xFF1E222B).copy(alpha = 0.95f * glyphAlpha),
                0.52f to Color(0xFF0A0C10).copy(alpha = 0.98f * glyphAlpha),
                0.84f to Color(0xFF161920).copy(alpha = 0.95f * glyphAlpha),
                1.0f to bottomColor.copy(alpha = bottomGlassAlpha),
                startY = glyphRect.top,
                endY = glyphRect.bottom
            ),
            topLeft = glyphRect.topLeft,
            size = glyphRect.size
        )

        // Prismatic metallic dispersion ray (illuminates only as the beam passes)
        val dispersionSpreadPx = glyphRect.width * (0.28f * dispersion.coerceAtLeast(0.1f))
        val rayTiltX = distFromBeam * glyphRect.width * 0.95f
        val rayStart = Offset(
            x = beamCenter.x - dispersionSpreadPx - rayTiltX * 0.35f,
            y = beamCenter.y - glyphRect.height * 0.68f
        )
        val rayEnd = Offset(
            x = beamCenter.x + dispersionSpreadPx + rayTiltX * 0.35f,
            y = beamCenter.y + glyphRect.height * 0.68f
        )

        val specAlpha = (localBeamEnergy * colorIntensity * glyphAlpha).coerceIn(0f, 1f)
        if (specAlpha > 0.01f) {
            drawRect(
                brush = Brush.linearGradient(
                    0.0f to Color.Transparent,
                    0.18f to warmColor.copy(alpha = (0.75f * specAlpha).coerceIn(0f, 1f)),
                    0.40f to middleColor.copy(alpha = (0.92f * specAlpha).coerceIn(0f, 1f)),
                    0.50f to beamColor.copy(alpha = (1.0f * specAlpha).coerceIn(0f, 1f)),
                    0.62f to coolColor.copy(alpha = (0.88f * specAlpha).coerceIn(0f, 1f)),
                    0.82f to Color(0xFF4F46E5).copy(alpha = (0.40f * specAlpha).coerceIn(0f, 1f)),
                    1.0f to Color.Transparent,
                    start = rayStart,
                    end = rayEnd
                ),
                topLeft = glyphRect.topLeft,
                size = glyphRect.size,
                blendMode = BlendMode.Screen
            )
        }

        // Inner Recessed Face with Caustic Highlight
        clipPath(path = innerPath, clipOp = ClipOp.Intersect) {
            drawRect(
                brush = Brush.verticalGradient(
                    0.0f to Color(0xFF12151D).copy(alpha = 0.88f * glyphAlpha),
                    0.45f to Color(0xFF07090C).copy(alpha = 0.94f * glyphAlpha),
                    1.0f to Color(0xFF10131A).copy(alpha = 0.86f * glyphAlpha),
                    startY = glyphRect.top,
                    endY = glyphRect.bottom
                ),
                topLeft = glyphRect.topLeft,
                size = glyphRect.size
            )

            if (specAlpha > 0.01f) {
                val innerShiftX = -distFromBeam * glyphRect.width * 0.42f * dispersion
                val innerRayRadius = max(glyphRect.width * 1.15f, wordWidth * 0.18f)

                // Warm caustic lobe
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            warmColor.copy(alpha = (0.65f * specAlpha).coerceIn(0f, 0.95f)),
                            middleColor.copy(alpha = (0.35f * specAlpha).coerceIn(0f, 0.8f)),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = beamCenter.x + innerShiftX - dispersionSpreadPx * 0.42f,
                            y = beamCenter.y - glyphRect.height * 0.12f
                        ),
                        radius = innerRayRadius
                    ),
                    radius = innerRayRadius,
                    center = Offset(
                        x = beamCenter.x + innerShiftX - dispersionSpreadPx * 0.42f,
                        y = beamCenter.y - glyphRect.height * 0.12f
                    ),
                    blendMode = BlendMode.Plus
                )

                // Cool caustic lobe
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coolColor.copy(alpha = (0.68f * specAlpha).coerceIn(0f, 0.95f)),
                            Color(0xFF3B82F6).copy(alpha = (0.32f * specAlpha).coerceIn(0f, 0.75f)),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = beamCenter.x + innerShiftX + dispersionSpreadPx * 0.42f,
                            y = beamCenter.y + glyphRect.height * 0.14f
                        ),
                        radius = innerRayRadius
                    ),
                    radius = innerRayRadius,
                    center = Offset(
                        x = beamCenter.x + innerShiftX + dispersionSpreadPx * 0.42f,
                        y = beamCenter.y + glyphRect.height * 0.14f
                    ),
                    blendMode = BlendMode.Plus
                )

                // Core brilliant metallic white hotspot
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            beamColor.copy(alpha = (0.85f * specAlpha).coerceIn(0f, 1f)),
                            middleColor.copy(alpha = (0.50f * specAlpha).coerceIn(0f, 0.85f)),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = beamCenter.x + innerShiftX * 0.5f,
                            y = beamCenter.y
                        ),
                        radius = innerRayRadius * 0.72f
                    ),
                    radius = innerRayRadius * 0.72f,
                    center = Offset(
                        x = beamCenter.x + innerShiftX * 0.5f,
                        y = beamCenter.y
                    ),
                    blendMode = BlendMode.Plus
                )
            }
        }

        // Top Edge Specular Rim
        val topRimHeight = glyphRect.height * 0.09f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    beamColor.copy(alpha = ((0.35f + 0.65f * localBeamEnergy) * glyphAlpha).coerceIn(0f, 1f)),
                    topColor.copy(alpha = ((0.18f + 0.35f * localBeamEnergy) * glyphAlpha).coerceIn(0f, 0.85f)),
                    Color.Transparent
                ),
                startY = glyphRect.top,
                endY = glyphRect.top + topRimHeight
            ),
            topLeft = glyphRect.topLeft,
            size = glyphRect.size.copy(height = topRimHeight),
            blendMode = BlendMode.Screen
        )
    }

    // 2. Bevel Contour Metallic Edge Rim
    drawIntoCanvas { canvas ->
        val cornerRadiusPx = (cornerRounding * 3.2f).coerceAtLeast(1f)
        val strokeWidthPx = (extraBold * 1.45f).coerceIn(1.5f, 6f)

        val baseRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeJoin = Paint.Join.MITER
            strokeCap = Paint.Cap.SQUARE
            pathEffect = CornerPathEffect(cornerRadiusPx)
            val rimAlpha = ((0.28f + 0.60f * localBeamEnergy) * glyphAlpha).coerceIn(0f, 0.95f)
            color = topColor.copy(alpha = rimAlpha).toArgb()
        }
        canvas.nativeCanvas.drawPath(outerNativePath, baseRimPaint)

        // Gleaming metallic highlight along the glyph rim when the beam sweeps across
        if (absDist < 0.40f) {
            val hotspotRimAlpha = (((1f - absDist / 0.40f).pow(2)) * 0.88f * glyphAlpha).coerceIn(0f, 1f)
            val highlightColor = when {
                distFromBeam < -0.05f -> warmColor
                distFromBeam > 0.05f -> coolColor
                else -> beamColor
            }
            val beamRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = strokeWidthPx * 1.15f
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                pathEffect = CornerPathEffect(cornerRadiusPx * 1.2f)
                color = highlightColor.copy(alpha = hotspotRimAlpha).toArgb()
            }
            canvas.nativeCanvas.drawPath(outerNativePath, beamRimPaint)
        }
    }
}

/**
 * Builds custom ultra-heavy geometric vector paths matching 'ReactDrops Square Display'
 * (weight 900, `extraBold = 2.25`, with the signature Square Display uppercase 'O',
 * geometric 'Z', 'R', 'V', accented 'É', and 'N').
 */
private fun buildSquareDisplayGlyphPath(
    char: Char,
    rect: Rect,
    extraBold: Float,
    insetFraction: Float
): Path {
    val insetX = rect.width * insetFraction
    val insetY = rect.height * insetFraction
    val targetLeft = rect.left + insetX
    val targetTop = rect.top + insetY
    val targetW = (rect.width - insetX * 2f).coerceAtLeast(1f)
    val targetH = (rect.height - insetY * 2f).coerceAtLeast(1f)

    val sw = (26f + (extraBold - 1f) * 3.2f).coerceIn(22f, 33f)
    val p = Path()

    when (char) {
        'Z' -> {
            p.moveTo(4f, 14f)
            p.lineTo(96f, 14f)
            p.lineTo(96f, 14f + sw * 0.82f)
            p.lineTo(38f, 100f - sw * 0.82f)
            p.lineTo(96f, 100f - sw * 0.82f)
            p.lineTo(96f, 100f)
            p.lineTo(4f, 100f)
            p.lineTo(4f, 100f - sw * 0.82f)
            p.lineTo(62f, 14f + sw * 0.82f)
            p.lineTo(4f, 14f + sw * 0.82f)
            p.close()
        }

        'O' -> {
            p.fillType = Path.FillType.EVEN_ODD
            p.moveTo(16f, 14f)
            p.lineTo(84f, 14f)
            p.lineTo(96f, 26f)
            p.lineTo(96f, 88f)
            p.lineTo(84f, 100f)
            p.lineTo(16f, 100f)
            p.lineTo(4f, 88f)
            p.lineTo(4f, 26f)
            p.close()

            val innerL = 4f + sw
            val innerR = 96f - sw
            val innerT = 14f + sw * 0.82f
            val innerB = 100f - sw * 0.82f
            if (innerR > innerL && innerB > innerT) {
                p.moveTo(innerL, innerT)
                p.lineTo(innerR, innerT)
                p.lineTo(innerR, innerB)
                p.lineTo(innerL, innerB)
                p.close()
            }
        }

        'R' -> {
            p.fillType = Path.FillType.EVEN_ODD
            p.moveTo(4f, 14f)
            p.lineTo(82f, 14f)
            p.lineTo(95f, 25f)
            p.lineTo(95f, 54f)
            p.lineTo(82f, 64f)
            p.lineTo(74f, 64f)
            p.lineTo(97f, 100f)
            p.lineTo(66f, 100f)
            p.lineTo(47f, 66f)
            p.lineTo(31f, 66f)
            p.lineTo(31f, 100f)
            p.lineTo(4f, 100f)
            p.close()

            val cL = 31f
            val cR = 69f
            val cT = 32f
            val cB = 49f
            p.moveTo(cL, cT)
            p.lineTo(cR, cT)
            p.lineTo(cR, cB)
            p.lineTo(cL, cB)
            p.close()
        }

        'V' -> {
            p.moveTo(2f, 14f)
            p.lineTo(32f, 14f)
            p.lineTo(50f, 69f)
            p.lineTo(68f, 14f)
            p.lineTo(98f, 14f)
            p.lineTo(65f, 100f)
            p.lineTo(35f, 100f)
            p.close()
        }

        'É', 'E' -> {
            val barH = sw * 0.68f
            val midTop = 57f - barH / 2f
            val midBot = 57f + barH / 2f
            p.moveTo(6f, 16f)
            p.lineTo(94f, 16f)
            p.lineTo(94f, 16f + barH)
            p.lineTo(33f, 16f + barH)
            p.lineTo(33f, midTop)
            p.lineTo(86f, midTop)
            p.lineTo(86f, midBot)
            p.lineTo(33f, midBot)
            p.lineTo(33f, 100f - barH)
            p.lineTo(95f, 100f - barH)
            p.lineTo(95f, 100f)
            p.lineTo(6f, 100f)
            p.close()

            if (char == 'É') {
                p.moveTo(54f, 0f)
                p.lineTo(82f, 0f)
                p.lineTo(64f, 11.5f)
                p.lineTo(42f, 11.5f)
                p.close()
            }
        }

        'N' -> {
            val stemW = sw * 0.92f
            p.moveTo(4f, 14f)
            p.lineTo(4f + stemW * 1.15f, 14f)
            p.lineTo(96f - stemW, 67f)
            p.lineTo(96f - stemW, 14f)
            p.lineTo(96f, 14f)
            p.lineTo(96f, 100f)
            p.lineTo(96f - stemW * 1.15f, 100f)
            p.lineTo(4f + stemW, 47f)
            p.lineTo(4f + stemW, 100f)
            p.lineTo(4f, 100f)
            p.close()
        }

        else -> {
            p.addRect(10f, 14f, 90f, 100f, Path.Direction.CW)
        }
    }

    val matrix = Matrix().apply {
        setScale(targetW / 100f, targetH / 100f)
        postTranslate(targetLeft, targetTop)
    }
    p.transform(matrix)
    return p
}

private fun applyEasing(t: Float, ease: String): Float {
    val clamped = t.coerceIn(0f, 1f)
    return when (ease.lowercase()) {
        "power4.out" -> 1f - (1f - clamped).pow(4)
        "power3.out" -> 1f - (1f - clamped).pow(3)
        "power2.out" -> 1f - (1f - clamped).pow(2)
        else -> 1f - (1f - clamped).pow(4)
    }
}

private fun parseColorOrDefault(value: String, defaultColor: Color): Color {
    if (value.isBlank() || value.equals("auto", ignoreCase = true)) {
        return defaultColor
    }
    return try {
        Color(android.graphics.Color.parseColor(value.trim()))
    } catch (_: Exception) {
        defaultColor
    }
}
