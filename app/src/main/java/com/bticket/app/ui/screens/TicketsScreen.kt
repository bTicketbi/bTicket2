package com.bticket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bticket.app.data.AuthState
import com.bticket.app.data.Repo
import com.bticket.app.data.TicketItem
import com.bticket.app.ui.components.formatBIF
import com.bticket.app.ui.components.formatDate

@Composable
fun TicketsScreen(onLogin: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val profile = AuthState.profile

    var tickets by remember { mutableStateOf<List<TicketItem>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profile?.id) {
        tickets = null
        error = null
        val uid = profile?.id
        if (uid != null) {
            try {
                tickets = Repo.myTickets(uid)
            } catch (e: Exception) {
                error = e.message ?: "Impossible de charger vos tickets."
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "Mes tickets",
                modifier = Modifier.statusBarsPadding().padding(top = 16.dp),
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                color = cs.onSurface,
            )
        }

        val list = tickets
        val err = error
        if (profile == null) {
            item {
                LoginPrompt(
                    title = "Connectez-vous pour voir vos tickets",
                    hint = "Vos achats apparaîtront ici, avec leur QR code d'entrée.",
                    onLogin = onLogin,
                )
            }
        } else if (err != null) {
            item { MessageCard(title = "Impossible de charger vos tickets", hint = err) }
        } else if (list == null) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cs.primary)
                }
            }
        } else if (list.isEmpty()) {
            item {
                MessageCard(
                    title = "Aucun ticket pour l'instant",
                    hint = "Vos tickets achetés apparaîtront ici.",
                )
            }
        } else {
            items(list, key = { it.id }) { t -> TicketCard(t) }
        }
    }
}

@Composable
private fun TicketCard(t: TicketItem) {
    val cs = MaterialTheme.colorScheme
    val (label, color) = when (t.status) {
        "used" -> "Utilisé" to cs.onSurfaceVariant
        "cancelled" -> "Annulé" to cs.error
        else -> "Valide" to cs.primary
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cs.surfaceContainerLowest)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = t.eventTitle,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = cs.onSurface,
            )
            Text(
                text = label,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
        Text("${formatDate(t.eventDate)} · ${t.eventTime}", fontSize = 13.sp, color = cs.onSurfaceVariant)
        Text(t.venue, fontSize = 13.sp, color = cs.onSurfaceVariant)
        Text(
            text = "${t.ticketTypeName} · ${formatBIF(t.price)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = cs.primary,
        )
    }
}

@Composable
fun LoginPrompt(title: String, hint: String, onLogin: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cs.surfaceContainerLow)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
        Text(hint, fontSize = 13.sp, color = cs.onSurfaceVariant)
        Button(onClick = onLogin) { Text("Se connecter ou créer un compte") }
    }
}
