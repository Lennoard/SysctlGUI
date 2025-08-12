package com.androidvip.sysctlgui.ui.params.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.design.theme.SysctlGuiTheme

@Composable
internal fun ActionToggleButton(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    iconOnActive: Painter,
    iconOnInactive: Painter,
    contentDescription: String? = null,
    onToggle: (Boolean) -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.background
        },
        label = "FabContainerColor"
    )

    val defaultElevation by animateDpAsState(
        targetValue = if (isActive) 4.dp else 2.dp,
        label = "FabElevation"
    )

    FloatingActionButton(
        modifier = modifier,
        onClick = { onToggle(!isActive) },
        containerColor = containerColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = defaultElevation,
            pressedElevation = defaultElevation * 2
        ),
        shape = CircleShape,
    ) {
        AnimatedContent(
            targetState = isActive,
            label = "ActionToggleButtonAnimation",
            transitionSpec = {
                val enterTransition = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialScale = 1.25f
                ) + fadeIn(animationSpec = tween(durationMillis = 200))

                val exitTransition = scaleOut(
                    animationSpec = tween(durationMillis = 150),
                    targetScale = 1.25f
                ) + fadeOut(animationSpec = tween(durationMillis = 100))

                enterTransition togetherWith exitTransition
            }
        ) { isCurrentlyActive ->
            val iconTint = if (isCurrentlyActive) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Icon(
                painter = if (isCurrentlyActive) iconOnActive else iconOnInactive,
                contentDescription = "Toggle $contentDescription",
                tint = iconTint
            )
        }
    }
}

@Composable
internal fun FavoriteButton(
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    onFavoriteClick: (Boolean) -> Unit,
) {
    ActionToggleButton(
        modifier = modifier,
        isActive = isFavorite,
        iconOnActive = painterResource(R.drawable.ic_favorite),
        iconOnInactive = painterResource(R.drawable.ic_favorite_outlined),
        contentDescription = "favorite",
        onToggle = onFavoriteClick
    )
}
@Composable
internal fun TaskerButton(
    modifier: Modifier = Modifier,
    isTaskerParam: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ActionToggleButton(
        modifier = modifier,
        isActive = isTaskerParam,
        iconOnActive = painterResource(R.drawable.ic_tasker),
        iconOnInactive = painterResource(R.drawable.ic_tasker_outlined),
        contentDescription = "tasker param",
        onToggle = onToggle
    )
}

@PreviewLightDark
@Composable
private fun FavoriteButtonStatesPreview() {
    SysctlGuiTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FavoriteButton(
                    isFavorite = false,
                    onFavoriteClick = {}
                )
                FavoriteButton(
                    isFavorite = true,
                    onFavoriteClick = {}
                )
                TaskerButton(
                    isTaskerParam = false,
                    onToggle = {}
                )
                TaskerButton(
                    isTaskerParam = true,
                    onToggle = {}
                )
            }
        }
    }
}

@Composable
@PreviewDynamicColors
private fun FavoriteButtonInteractivePreview() {
    SysctlGuiTheme(dynamicColor = true) {
        Surface {
            var isFavorite by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FavoriteButton(
                    isFavorite = isFavorite,
                    onFavoriteClick = { isFavorite = !isFavorite },
                )
                TaskerButton(
                    isTaskerParam = isFavorite,
                    onToggle = { isFavorite = !isFavorite }
                )
            }
        }
    }
}
