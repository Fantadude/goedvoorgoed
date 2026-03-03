package com.example.goedvoorgoed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.goedvoorgoed.ui.theme.GoedvoorgoedTheme

/**
 * Main entry point for iOS (and potentially other platforms)
 * This is a simplified version for cross-platform compatibility.
 * 
 * For full functionality on Android, use MainActivity.kt in composeApp module
 * which contains the complete UI implementation with Android-specific features.
 */
@Composable
fun App() {
    GoedvoorgoedTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Simplified placeholder UI for iOS
            // Full implementation would need platform-agnostic resources
            Text(
                text = "Goed Voor Goed\n\nWelkom bij onze app!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxSize(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
