package com.bticket.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bticket.app.ui.components.IconPaths
import com.bticket.app.ui.components.PathIcon
import com.bticket.app.ui.screens.AccountScreen
import com.bticket.app.ui.screens.AuthScreen
import com.bticket.app.ui.screens.HomeScreen
import com.bticket.app.ui.screens.TicketsScreen
import kotlinx.coroutines.launch

private const val SAFI_URL = "https://cinetv257.github.io/CINETV/assistant.html"

private class TabItem(val label: String, val icon: String)

private val TABS = listOf(
    TabItem("Événements", IconPaths.HOME),
    TabItem("Mes tickets", IconPaths.TICKET),
    TabItem("Compte", IconPaths.ACCOUNT),
)

/**
 * Structure principale :
 *  - 3 onglets sur lesquels on glisse du doigt (gauche/droite) ;
 *  - barre du bas masquée quand on fait défiler vers le bas, réaffichée en remontant ;
 *  - bulle Safi qui ouvre la page de l'assistant.
 */
@Composable
fun MainScaffold() {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { TABS.size })
    var barVisible by remember { mutableStateOf(true) }
    var showAuth by remember { mutableStateOf(false) }

    // Détecte le sens du défilement vertical de n'importe quel écran à l'intérieur du pager.
    val scrollWatcher = remember {
        object : NestedScrollConnection {
            private var acc = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy != 0f) {
                    if ((acc > 0f && dy < 0f) || (acc < 0f && dy > 0f)) acc = 0f
                    acc += dy
                    if (acc < -40f) {
                        barVisible = false
                    } else if (acc > 40f) {
                        barVisible = true
                    }
                }
                return Offset.Zero
            }
        }
    }

    // La barre réapparaît à chaque changement d'onglet.
    LaunchedEffect(pagerState.currentPage) { barVisible = true }

    Box(Modifier.fillMaxSize().nestedScroll(scrollWatcher)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 2,
        ) { page ->
            when (page) {
                0 -> HomeScreen()
                1 -> TicketsScreen(onLogin = { showAuth = true })
                else -> AccountScreen(onLogin = { showAuth = true })
            }
        }

        // La bulle Safi descend avec la barre quand celle-ci se cache.
        val bubbleBottom by animateDpAsState(
            targetValue = if (barVisible) 92.dp else 20.dp,
            label = "safiBubble",
        )
        SafiBubble(
            Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = bubbleBottom),
        )

        AnimatedVisibility(
            visible = barVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            BottomBar(
                selected = pagerState.currentPage,
                onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            )
        }

        if (showAuth) {
            BackHandler { showAuth = false }
            AuthScreen(onClose = { showAuth = false })
        }
    }
}

@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .background(cs.surface.copy(alpha = 0.97f)),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(cs.outlineVariant.copy(alpha = 0.5f)))
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            TABS.forEachIndexed { index, tab ->
                val active = index == selected
                val tint = if (active) cs.primary else cs.onSurfaceVariant
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { onSelect(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(width = 32.dp, height = 3.dp)
                            .clip(CircleShape)
                            .background(if (active) cs.primary else cs.surface.copy(alpha = 0f)),
                    )
                    Box(Modifier.height(8.dp))
                    PathIcon(tab.icon, tint, 24.dp)
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = tint,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SafiBubble(modifier: Modifier) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    Box(
        modifier
            .size(56.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(cs.primary)
            .clickable { openSafi(context) },
        contentAlignment = Alignment.Center,
    ) {
        PathIcon(IconPaths.CHAT, cs.onPrimary, 26.dp)
    }
}

/** Ouvre la page de Safi dans une fenêtre intégrée (Chrome Custom Tab). */
private fun openSafi(context: Context) {
    val uri = Uri.parse(SAFI_URL)
    try {
        val colors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(0xFF111318.toInt())
            .build()
        CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colors)
            .setShowTitle(true)
            .build()
            .launchUrl(context, uri)
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e2: Exception) {
            // Aucun navigateur disponible : on ne fait rien.
        }
    }
}
