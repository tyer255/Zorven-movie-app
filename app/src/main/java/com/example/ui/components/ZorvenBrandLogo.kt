package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZorvenCrimson
import com.example.ui.theme.ZorvenTextPrimary

/**
 * Custom architectural vector monogram ("Z") and wordmark ("ZORVÉN").
 * Constructed with precise geometric ribbon planes — zero AI sparkles, zero generic boxes.
 */
@Composable
fun ZorvenMonogramIcon(
    size: Dp = 26.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Top horizontal architectural bar (sharp parallelogram)
        val topBar = Path().apply {
            moveTo(w * 0.10f, h * 0.12f)
            lineTo(w * 0.90f, h * 0.12f)
            lineTo(w * 0.78f, h * 0.29f)
            lineTo(w * 0.10f, h * 0.29f)
            close()
        }

        // Bottom horizontal architectural bar
        val bottomBar = Path().apply {
            moveTo(w * 0.22f, h * 0.71f)
            lineTo(w * 0.90f, h * 0.71f)
            lineTo(w * 0.90f, h * 0.88f)
            lineTo(w * 0.10f, h * 0.88f)
            close()
        }

        // Central folded ribbon diagonal slash (overlaps top-right and bottom-left with subtle shadow depth)
        val diagonalSlash = Path().apply {
            moveTo(w * 0.70f, h * 0.12f)
            lineTo(w * 0.90f, h * 0.12f)
            lineTo(w * 0.30f, h * 0.88f)
            lineTo(w * 0.10f, h * 0.88f)
            close()
        }

        // Draw top and bottom structural bars in deep cinema crimson
        drawPath(
            path = topBar,
            color = Color(0xFFB80710)
        )
        drawPath(
            path = bottomBar,
            color = Color(0xFFB80710)
        )

        // Draw subtle fold shadow under the diagonal ribbon
        val shadowTop = Path().apply {
            moveTo(w * 0.62f, h * 0.29f)
            lineTo(w * 0.78f, h * 0.29f)
            lineTo(w * 0.68f, h * 0.42f)
            close()
        }
        drawPath(
            path = shadowTop,
            color = Color(0xFF5A0207)
        )

        // Draw primary diagonal ribbon with restrained studio luminance
        drawPath(
            path = diagonalSlash,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF1F2D),
                    ZorvenCrimson,
                    Color(0xFFC90812)
                ),
                start = Offset(w * 0.85f, h * 0.12f),
                end = Offset(w * 0.15f, h * 0.88f)
            )
        )
    }
}

@Composable
fun ZorvenBrandWordmark(
    monogramSize: Dp = 26.dp,
    fontSize: TextUnit = 19.sp,
    showMonogram: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showMonogram) {
            ZorvenMonogramIcon(size = monogramSize)
        }
        Text(
            text = "ZORVÉN",
            style = TextStyle(
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                letterSpacing = 3.2.sp,
                color = ZorvenTextPrimary
            )
        )
    }
}
