package com.example.goedvoorgoed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Goed voor Goed Logo Composable
 * A custom logo representing the GOED voor GOED brand
 */
@Composable
fun GoedVoorGoedLogo(
    modifier: Modifier = Modifier,
    size: Int = 80
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF2A2A4A) else Color.White
    val innerBackgroundColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onPrimary
    val accentColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner circle with blue background
        Box(
            modifier = Modifier
                .size((size - 8).dp)
                .clip(CircleShape)
                .background(innerBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            // GOED Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GOED",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size / 4).sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Small decorative line
                Box(
                    modifier = Modifier
                        .size((size / 8).dp, 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )
            }
        }
    }
}

/**
 * Small logo for use in lists or cards
 */
@Composable
fun GoedVoorGoedLogoSmall(
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF2A2A4A) else Color.White
    val innerBackgroundColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(innerBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = textColor
            )
        }
    }
}
