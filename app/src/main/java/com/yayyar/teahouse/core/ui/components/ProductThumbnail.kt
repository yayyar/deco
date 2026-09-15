package com.yayyar.teahouse.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import java.io.File

@Composable
fun ProductThumbnail(
    imageUri: String?,
    fallbackImageUri: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIcon: ImageVector = Icons.Default.Checkroom,
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    contentDescription: String? = null
) {
    val effectiveUri = rememberEffectiveImageModel(imageUri, fallbackImageUri)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(placeholderColor),
        contentAlignment = Alignment.Center
    ) {
        if (effectiveUri != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(effectiveUri)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(placeholderColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = placeholderIcon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(size * 0.5f)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(placeholderColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = placeholderIcon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(size * 0.5f)
                        )
                    }
                }
            )
        } else {
            Icon(
                imageVector = placeholderIcon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

private fun rememberEffectiveImageModel(imageUri: String?, fallbackImageUri: String?): Any? {
    val candidate = when {
        !imageUri.isNullOrBlank() -> imageUri
        !fallbackImageUri.isNullOrBlank() -> fallbackImageUri
        else -> return null
    }

    return if (candidate.startsWith("http") || candidate.startsWith("content://")) {
        candidate
    } else {
        File(candidate)
    }
}
