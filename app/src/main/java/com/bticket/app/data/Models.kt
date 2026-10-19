package com.bticket.app.data

/** Événement publié (collection Firestore « events », statut « approved »). */
data class EventItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val venue: String,
    val city: String,
    val date: String,      // AAAA-MM-JJ
    val time: String,      // HH:mm
    val imageUrl: String,
    val organizerId: String,
    val organizerName: String,
)

/** Ticket acheté (collection « tickets »). */
data class TicketItem(
    val id: String,
    val code: String,
    val eventId: String,
    val eventTitle: String,
    val eventDate: String,
    val eventTime: String,
    val venue: String,
    val ticketTypeName: String,
    val price: Long,
    val status: String,    // active | used | cancelled
)

/** Profil du compte (collection « users »). */
data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String,
    val role: String,      // buyer | organizer | scanner | admin
    val status: String,
)
