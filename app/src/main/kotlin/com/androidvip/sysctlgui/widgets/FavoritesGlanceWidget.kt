package com.androidvip.sysctlgui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.androidvip.sysctlgui.design.theme.onPrimaryContainerLight
import com.androidvip.sysctlgui.design.theme.primaryContainerLight
import com.androidvip.sysctlgui.design.theme.primaryLight
import com.androidvip.sysctlgui.domain.models.KernelParam
import com.androidvip.sysctlgui.domain.usecase.GetUserParamsUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FavoritesGlanceWidget : GlanceAppWidget(), KoinComponent {
    private val getUserParamsUseCase: GetUserParamsUseCase by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val favoriteParams = getUserParamsUseCase().filter { it.isFavorite }

        provideContent {
            FavoritesWidgetContent(params = favoriteParams)
        }
    }

    @Composable
    fun FavoritesWidgetContent(params: List<KernelParam>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(primaryContainerLight)
                .padding(16.dp),
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Text(
                text = "Favorite Kernel Params",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ColorProvider(onPrimaryContainerLight)
                ),
                modifier = GlanceModifier.padding(vertical = 8.dp)
            )
            Spacer(GlanceModifier.height(8.dp))

            if (params.isEmpty()) {
                Text("No favorite parameters yet.")
            } else {
                LazyColumn {
                    items(params) { param ->
                        FavoriteItem(param = param)
                        Spacer(GlanceModifier.height(4.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun FavoriteItem(param: KernelParam) {
        Column(
            modifier = GlanceModifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .clickable(
                    onClick = actionRunCallback<ViewKernelParamDetailsAction>(
                        parameters = actionParametersOf(kernelParamNameKey to param.name)
                    )
                )
        ) {
            Text(
                text = param.name,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(primaryLight)
                )
            )
            Text(
                text = param.value,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(primaryLight)
                )
            )
        }
    }
}
