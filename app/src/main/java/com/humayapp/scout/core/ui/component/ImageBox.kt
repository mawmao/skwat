package com.humayapp.scout.core.ui.component

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.humayapp.scout.core.ui.theme.InputFieldTokens
import com.humayapp.scout.core.ui.theme.ScoutIcons
import com.humayapp.scout.core.ui.theme.ScoutTheme

/**
 * Displays an image inside a bordered, clipped box.
 *
 * - If [uri] is provided, the image is loaded asynchronously and cropped to fill the box.
 * - If [uri] is null, a placeholder image icon is shown instead.
 * - An optional [aspectRatio] can be enforced; pass 0f to let the parent decide size.
 *
 * This is mainly a reusable UI helper for showing picked images or empty image slots
 * with consistent styling (border, shape, placeholder).
 *
 * @param modifier Optional modifier for sizing, padding, etc.
 * @param uri Optional image URI to load and display.
 * @param aspectRatio Width / height ratio. Ignored if <= 0f.
 */
@Composable
fun ImageBox(
    modifier: Modifier = Modifier,
    uri: Uri? = null,
    aspectRatio: Float?,
) {
    val context = LocalContext.current
    val borderColor = InputFieldTokens.unfocusedColor
    val shape = ScoutTheme.shapes.cornerMedium

    val imageRequest = remember(uri, context) {
        if (uri != null) {
            ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build()
        } else null
    }

    Box(
        modifier = modifier
            .then(if (aspectRatio != null) Modifier.aspectRatio(aspectRatio) else Modifier)
            .border(1.dp, borderColor, shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(ScoutIcons.Image),
                contentDescription = null,
                tint = borderColor
            )
        }
    }
}
