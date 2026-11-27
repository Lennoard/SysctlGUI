package com.androidvip.sysctlgui.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.androidvip.sysctlgui.ui.main.MainViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun PredictiveBackContainer(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val animatable = remember { Animatable(0f) }

    PredictiveBackHandler(enabled = true) { progressFlow: Flow<BackEventCompat> ->
        try {
            progressFlow.collect { backEvent ->
                val p = backEvent.progress
                animatable.snapTo(p)
                mainViewModel.updatePredictiveBackProgress(p)
            }
            mainViewModel.updatePredictiveBackProgress(1f)
            onBack()
        } catch (_: CancellationException) {
            coroutineScope.launch {
                animatable.animateTo(0f, tween(200)) {
                    mainViewModel.updatePredictiveBackProgress(value)
                }
                mainViewModel.updatePredictiveBackProgress(0f)
            }
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            val p = animatable.value
            scaleX = 1f - (p * 0.06f)
            scaleY = 1f - (p * 0.06f)
            alpha = 1f - (p * 0.2f)
        },
        content = content
    )
}
