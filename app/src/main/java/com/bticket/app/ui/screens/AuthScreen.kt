package com.bticket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bticket.app.data.AuthState
import kotlinx.coroutines.launch

/** Connexion et inscription acheteur (e-mail + mot de passe). */
@Composable
fun AuthScreen(onClose: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    var signUp by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        if (busy) return
        if (email.isBlank() || password.isBlank() || (signUp && fullName.isBlank())) {
            error = "Veuillez remplir tous les champs obligatoires."
            return
        }
        busy = true
        error = null
        scope.launch {
            try {
                if (signUp) {
                    AuthState.signUpBuyer(email, password, fullName, phone)
                } else {
                    AuthState.signIn(email, password)
                }
                onClose()
            } catch (e: Exception) {
                error = AuthState.friendlyError(e)
            } finally {
                busy = false
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(cs.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onClose) { Text("← Retour") }

        Text(
            text = if (signUp) "Créer un compte" else "Connexion",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = cs.onSurface,
        )
        Text(
            text = if (signUp) {
                "Créez votre compte acheteur pour retrouver vos tickets."
            } else {
                "Connectez-vous avec votre e-mail et votre mot de passe."
            },
            fontSize = 14.sp,
            color = cs.onSurfaceVariant,
        )

        if (signUp) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nom complet") },
                singleLine = true,
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Téléphone (facultatif)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("E-mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mot de passe") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        val err = error
        if (err != null) {
            Text(err, color = cs.error, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = { submit() },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = cs.onPrimary,
                )
            } else {
                Text(
                    text = if (signUp) "Créer mon compte" else "Se connecter",
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        TextButton(
            onClick = {
                signUp = !signUp
                error = null
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (signUp) "J'ai déjà un compte" else "Pas encore de compte ? Créer un compte")
        }

        Text(
            text = "Vous êtes organisateur ? L'inscription organisateur se fait pour l'instant sur le site bTicket.",
            fontSize = 12.sp,
            color = cs.onSurfaceVariant,
        )
    }
}
