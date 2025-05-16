package com.example.oralenglishgpt.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.res.colorResource
import com.example.oralenglishgpt.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun MainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colorResource(R.color.dark_primary),
            secondary = colorResource(R.color.dark_secondary),
            tertiary = colorResource(R.color.dark_tertiary),
            background = colorResource(R.color.dark_background),
            surface = colorResource(R.color.dark_surface),
            onPrimary = colorResource(R.color.dark_onPrimary),
            onSecondary = colorResource(R.color.dark_onSecondary),
            onBackground = colorResource(R.color.dark_onBackground),
            onSurface = colorResource(R.color.dark_onSurface)
        )
    } else {
        lightColorScheme(
            primary = colorResource(R.color.light_primary),
            secondary = colorResource(R.color.light_secondary),
            tertiary = colorResource(R.color.light_tertiary),
            background = colorResource(R.color.light_background),
            surface = colorResource(R.color.light_surface),
            onPrimary = colorResource(R.color.light_onPrimary),
            onSecondary = colorResource(R.color.light_onSecondary),
            onBackground = colorResource(R.color.light_onBackground),
            onSurface = colorResource(R.color.light_onSurface)
        )
    }

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !darkTheme

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = colorScheme.surface,
            darkIcons = useDarkIcons
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}