package com.bticket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bticket.app.AppPrefs
import com.bticket.app.data.AuthState

private fun roleLabel(role: String): String = when (role) {
    "admin" -> "Administrateur"
    "organizer" -> "Organisateur"
    "scanner" -> "Agent de scan"
    else -> "Acheteur"
}

@Composable
fun AccountScreen(onLogin: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val profile = AuthState.profile

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "Compte",
                modifier = Modifier.statusBarsPadding().padding(top = 16.dp),
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                color = cs.onSurface,
            )
        }

        if (profile == null) {
            item {
                LoginPrompt(
                    title = "Vous n'êtes pas connecté",
                    hint = "Connectez-vous pour acheter des tickets et retrouver vos achats.",
                    onLogin = onLogin,
                )
            }
        } else {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cs.surfaceContainerLowest)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = profile.fullName.ifBlank { "Mon compte" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = cs.onSurface,
                    )
                    Text(profile.email, fontSize = 14.sp, color = cs.onSurfaceVariant)
                    if (profile.phone.isNotBlank()) {
                        Text(profile.phone, fontSize = 14.sp, color = cs.onSurfaceVariant)
                    }
                    Text(
                        text = roleLabel(profile.role),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.primary,
                    )
                }
            }
            if (profile.role != "buyer") {
                item {
                    MessageCard(
                        title = "Espace ${roleLabel(profile.role).lowercase()}",
                        hint = "Cet espace arrive dans une prochaine étape de l'application. " +
                            "En attendant, utilisez le site bTicket.",
                    )
                }
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cs.surfaceContainerLowest)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Mode sombre", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface)
                Switch(checked = AppPrefs.darkTheme, onCheckedChange = { AppPrefs.setDark(it) })
            }
        }

        if (profile != null) {
            item {
                Button(
                    onClick = { AuthState.signOut() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.surfaceContainerHigh,
                        contentColor = cs.onSurface,
                    ),
                ) {
                    Text("Se déconnecter")
                }
            }
        }
    }
}
