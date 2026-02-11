package com.androidvip.sysctlgui.design.utils

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp

@Composable
@ReadOnlyComposable
fun isLandscape(): Boolean {
    return LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
fun StatusBarProtection(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    height: Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
) {
    val gradient = Brush.verticalGradient(listOf(color, Color.Transparent))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(gradient)
    )
}

@Composable
fun AnimatedStrikeThroughIcon(
    icon: ImageVector,
    isOff: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val painter = rememberVectorPainter(icon)
    val backgroundColor = MaterialTheme.colorScheme.background
    val animationProgress by animateFloatAsState(
        targetValue = if (isOff) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "StrikeThruAnimation"
    )

    Box(
        modifier = modifier.drawWithCache {
            onDrawWithContent {
                with(painter) {
                    draw(size = size, colorFilter = ColorFilter.tint(tint))
                }

                if (animationProgress > 0f) {
                    val strokeWidth = size.width * 0.08f
                    val start = Offset(
                        x = size.width * 0.2f,
                        y = size.height * 0.2f
                    )
                    val end = Offset(
                        x = lerp(start.x, size.width * 0.8f, animationProgress),
                        y = lerp(start.y, size.height * 0.8f, animationProgress)
                    )

                    drawLine(
                        color = backgroundColor,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth * 3,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = tint,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    )
}
