package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

val AVATAR_COLORS = listOf(
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF97316), // Orange
    Color(0xFF6366F1)  // Indigo
)

@Composable
fun StudentAvatar(
    photoUri: String?,
    studentName: String,
    avatarPreset: Int = 0,
    size: Dp = 48.dp,
    borderColor: Color? = null,
    borderWidth: Dp = 2.dp,
    modifier: Modifier = Modifier
) {
    val initial = studentName.trim().take(1).uppercase()
    val bgColor = AVATAR_COLORS.getOrElse(avatarPreset % AVATAR_COLORS.size) { AVATAR_COLORS[0] }

    var boxModifier = modifier
        .size(size)
        .clip(CircleShape)
    
    if (borderColor != null) {
        boxModifier = boxModifier.border(borderWidth, borderColor, CircleShape)
    }

    Box(
        modifier = boxModifier.background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNull_or_empty_uri()) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Student Photo of $studentName",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (initial.isNotEmpty()) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Student Icon",
                tint = Color.White,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

private fun String?.isNull_or_empty_uri(): Boolean {
    return this.isNullOrBlank() || this == "null"
}
