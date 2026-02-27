package com.humayapp.scout.feature.form.impl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.humayapp.scout.core.ui.theme.ScoutTheme

@Composable
fun <T> FormImagesLayout(
    items: List<T>,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    imageContent: @Composable (item: T, aspectRatio: Float?, modifier: Modifier) -> Unit
) {
    Column(modifier = modifier) {
        title?.let {
            it()
            Spacer(Modifier.height(ScoutTheme.spacing.smallMedium))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ScoutTheme.spacing.medium)
        ) {
            items.take(4).forEachIndexed { index, item ->
                imageContent(item, 1f, Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(ScoutTheme.spacing.medium))

        items.getOrNull(4)?.let { imageContent(it, null, Modifier.fillMaxWidth()) }

        Spacer(Modifier.height(ScoutTheme.spacing.medium))
    }
}
