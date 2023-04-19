package com.rosan.installer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.rosan.installer.R

@Composable
fun InstallerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = colorResource(R.color.dark_primary),
            primaryContainer = colorResource(R.color.dark_primary_container),
            secondary = colorResource(R.color.dark_secondary),
            tertiary = colorResource(R.color.dark_tertiary),
            error = colorResource(R.color.dark_error)
        )
        else -> lightColorScheme(
            primary = colorResource(R.color.light_primary),
            primaryContainer = colorResource(R.color.light_primary_container),
            secondary = colorResource(R.color.light_secondary),
            tertiary = colorResource(R.color.light_tertiary)
        )
    }
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = colorScheme.surface,
            darkIcons = !darkTheme
        )
        systemUiController.setNavigationBarColor(
            color = colorScheme.surface,
            darkIcons = !darkTheme
        )
    }
    /*val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.setStatusBarColor()
            (view.context as Activity).window.navigationBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
        }
    }*/

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}