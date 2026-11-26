package com.infinityat0.brainvita.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colours taken from the web version's style.css. */
@Immutable
data class BoardColors(
    val muted: Color,
    val board: Color,
    val boardEdge: Color,
    val hole: Color,
    val marble: Color,
    val marbleHighlight: Color,
    val marbleShade: Color,
    val accent: Color,
)

private val LightBoard = BoardColors(
    muted = Color(0xFF7A6E63),
    board = Color(0xFFB07A45),
    boardEdge = Color(0xFF8A5A2E),
    hole = Color(0xFF6E4520),
    marble = Color(0xFFD9822B),
    marbleHighlight = Color(0xFFFFD29A),
    marbleShade = Color(0xFF8C4A10),
    accent = Color(0xFF2F7D6D),
)

private val DarkBoard = LightBoard.copy(
    muted = Color(0xFFA3978A),
    board = Color(0xFF5B3F26),
    boardEdge = Color(0xFF3E2A18),
    hole = Color(0xFF24180D),
    accent = Color(0xFF5CC2AB),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF2F7D6D),
    onPrimary = Color.White,
    background = Color(0xFFF6F1E7),
    onBackground = Color(0xFF2B2520),
    surface = Color(0xFFF6F1E7),
    onSurface = Color(0xFF2B2520),
    surfaceContainer = Color(0xFFFFFAF2),
    secondaryContainer = Color(0xFFE9DFCD),
    onSecondaryContainer = Color(0xFF2B2520),
    outline = Color(0xFFD8CBB8),
    error = Color(0xFFB03A2E),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF5CC2AB),
    onPrimary = Color(0xFF10201C),
    background = Color(0xFF1D1A17),
    onBackground = Color(0xFFEEE6DB),
    surface = Color(0xFF1D1A17),
    onSurface = Color(0xFFEEE6DB),
    surfaceContainer = Color(0xFF2A2521),
    secondaryContainer = Color(0xFF3D3530),
    onSecondaryContainer = Color(0xFFEEE6DB),
    outline = Color(0xFF4A4038),
    error = Color(0xFFE5776B),
)

val LocalBoardColors = staticCompositionLocalOf { LightBoard }

@Composable
fun BrainvitaTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBoardColors provides if (dark) DarkBoard else LightBoard) {
        MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
    }
}
