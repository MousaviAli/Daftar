package com.parsaplanner.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import com.parsaplanner.app.ui.theme.AccentGold
import com.parsaplanner.app.ui.theme.AccentTerracotta

/** A warmer, more premium-feeling FAB than the flat Material default: a soft terracotta→gold gradient with a real drop shadow. */
@Composable
fun GradientFab(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .shadow(elevation = 10.dp, shape = CircleShape, ambientColor = AccentTerracotta, spotColor = AccentTerracotta)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(AccentTerracotta, AccentGold)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onPrimary)
    }
}

/** Consistent soft elevation used for all content cards across the app. */
@Composable
fun luxuryCardElevation() = CardDefaults.cardElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
