package com.example.notesappandroidcompose.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    tertiary = LightBlue
)

private val LightColorScheme = lightColorScheme(
    primary = LightBlue,
    onPrimary = White,
    secondary = SecondaryBlue,
    onSecondary = White,
    background = BackgroundWhite,
    surface = BackgroundWhite,
    onSurface = Color.Black,
    surfaceVariant = SurfaceBlueTint,
    onSurfaceVariant = Color.DarkGray,
    primaryContainer = SecondaryBlue,
    onPrimaryContainer = Color.Black
)

@Composable
fun NotesAppAndroidComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to stick to requested theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        // Simple dark variant or just keep it light as requested
        LightColorScheme 
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
