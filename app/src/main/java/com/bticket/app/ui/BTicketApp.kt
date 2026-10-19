package com.bticket.app.ui

import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bticket.app.AppPrefs
import com.bticket.app.data.AuthState
import com.bticket.app.ui.splash.LogoSplash
import com.bticket.app.ui.theme.BTicketTheme

@Composable
fun BTicketApp() {
    val dark = AppPrefs.darkTheme
    val activity = LocalContext.current as ComponentActivity

    var animationDone by remember { mutableStateOf(false) }
    // L'animation reste affichée jusqu'à sa fin ET jusqu'à ce que la connexion soit vérifiée.
    val showSplash = !(animationDone && !AuthState.loading)

    // Pendant l'animation (fond sombre), les icônes de la barre d'état restent claires.
    val darkBars = dark || showSplash
    DisposableEffect(darkBars) {
        val style = if (darkBars) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        onDispose { }
    }

    BTicketTheme(dark = dark) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            MainScaffold()
            AnimatedVisibility(
                visible = showSplash,
                exit = fadeOut(animationSpec = tween(450)),
            ) {
                LogoSplash(onFinished = { animationDone = true })
            }
        }
    }
}
