package com.bticket.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** État de connexion partagé par toute l'application. */
object AuthState {
    var profile by mutableStateOf<UserProfile?>(null)
        private set

    /** Vrai tant que Firebase n'a pas indiqué si quelqu'un est connecté. */
    var loading by mutableStateOf(true)
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var started = false
    private var signingUp = false

    fun start() {
        if (started) return
        started = true
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                profile = null
                loading = false
            } else {
                scope.launch {
                    val loaded = try {
                        Repo.loadProfile(user.uid)
                    } catch (e: Exception) {
                        null
                    }
                    // Pendant une inscription, le profil n'existe pas encore : on ne l'écrase pas.
                    if (loaded != null || !signingUp) profile = loaded
                    loading = false
                }
            }
        }
    }

    suspend fun signIn(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        val cred = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val uid = cred.user?.uid ?: throw IllegalStateException("Connexion impossible.")
        val loaded = Repo.loadProfile(uid)
        if (loaded == null) {
            auth.signOut()
            throw IllegalStateException("Aucun profil bTicket n'est associé à ce compte.")
        }
        profile = loaded
    }

    suspend fun signUpBuyer(email: String, password: String, fullName: String, phone: String) {
        signingUp = true
        try {
            val auth = FirebaseAuth.getInstance()
            val cred = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = cred.user?.uid ?: throw IllegalStateException("Inscription impossible.")
            Repo.createBuyerProfile(uid, email.trim(), fullName.trim(), phone.trim())
            profile = UserProfile(uid, email.trim(), fullName.trim(), phone.trim(), "buyer", "approved")
        } finally {
            signingUp = false
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun friendlyError(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidUserException -> "Aucun compte ne correspond à cet e-mail."
        is FirebaseAuthInvalidCredentialsException -> "E-mail ou mot de passe incorrect."
        is FirebaseAuthUserCollisionException -> "Un compte existe déjà avec cet e-mail."
        is FirebaseAuthWeakPasswordException -> "Mot de passe trop faible (6 caractères minimum)."
        is FirebaseNetworkException -> "Pas de connexion internet."
        else -> e.message ?: "Une erreur est survenue."
    }
}
