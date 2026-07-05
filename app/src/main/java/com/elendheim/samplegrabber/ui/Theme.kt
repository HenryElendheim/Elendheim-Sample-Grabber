package com.elendheim.samplegrabber.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Amber = Color(0xFFFFBE45)
val AmberDim = Color(0xFFE0A231)
val Ink = Color(0xFF171102)
val Night = Color(0xFF0F0F12)
val NightRaised = Color(0xFF1A1A1F)
val NightHigh = Color(0xFF25252C)
val Fog = Color(0xFFE8E6E1)
val FogDim = Color(0xFFA9A79F)

private val GrabberColors = darkColorScheme(
    primary = Amber,
    onPrimary = Ink,
    primaryContainer = AmberDim,
    onPrimaryContainer = Ink,
    secondary = FogDim,
    onSecondary = Ink,
    background = Night,
    onBackground = Fog,
    surface = NightRaised,
    onSurface = Fog,
    surfaceVariant = NightHigh,
    onSurfaceVariant = FogDim,
    outline = FogDim
)

/** Dark mode first: the app always runs in its night palette. */
@Composable
fun GrabberTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GrabberColors,
        content = content
    )
}
