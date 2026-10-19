package com.bticket.app.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Accès à Firestore : mêmes collections que le site bTicket. */
object Repo {
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    private fun DocumentSnapshot.str(key: String): String = getString(key) ?: ""

    private fun DocumentSnapshot.long(key: String): Long = (get(key) as? Number)?.toLong() ?: 0L

    suspend fun listPublicEvents(): List<EventItem> {
        val snap = db.collection("events").whereEqualTo("status", "approved").get().await()
        return snap.documents
            .map { d ->
                EventItem(
                    id = d.id,
                    title = d.str("title"),
                    description = d.str("description"),
                    category = d.str("category"),
                    venue = d.str("venue"),
                    city = d.str("city"),
                    date = d.str("date"),
                    time = d.str("time"),
                    imageUrl = d.str("imageUrl"),
                    organizerId = d.str("organizerId"),
                    organizerName = d.str("organizerName"),
                )
            }
            .sortedBy { it.date }
    }

    /** Prix le plus bas de chaque événement (affiché « dès X »). */
    suspend fun minPrices(eventIds: List<String>): Map<String, Long> {
        val result = HashMap<String, Long>()
        for (chunk in eventIds.chunked(30)) {
            val snap = db.collection("ticketType").whereIn("eventId", chunk).get().await()
            for (d in snap.documents) {
                val eventId = d.getString("eventId") ?: continue
                val price = (d.get("price") as? Number)?.toLong() ?: continue
                val current = result[eventId]
                if (current == null || price < current) result[eventId] = price
            }
        }
        return result
    }

    suspend fun myTickets(uid: String): List<TicketItem> {
        val snap = db.collection("tickets").whereEqualTo("buyerUid", uid).get().await()
        return snap.documents
            .map { d ->
                TicketItem(
                    id = d.id,
                    code = d.str("code"),
                    eventId = d.str("eventId"),
                    eventTitle = d.str("eventTitle"),
                    eventDate = d.str("eventDate"),
                    eventTime = d.str("eventTime"),
                    venue = d.str("venue"),
                    ticketTypeName = d.str("ticketTypeName"),
                    price = d.long("price"),
                    status = d.getString("status") ?: "active",
                )
            }
            .sortedByDescending { it.eventDate }
    }

    suspend fun loadProfile(uid: String): UserProfile? {
        val d = db.collection("users").document(uid).get().await()
        if (!d.exists()) return null
        return UserProfile(
            id = d.id,
            email = d.str("email"),
            fullName = d.str("fullName"),
            phone = d.str("phone"),
            role = d.getString("role") ?: "buyer",
            status = d.getString("status") ?: "approved",
        )
    }

    suspend fun createBuyerProfile(uid: String, email: String, fullName: String, phone: String) {
        val data = hashMapOf<String, Any>(
            "email" to email,
            "fullName" to fullName,
            "role" to "buyer",
            "status" to "approved",
            "createdAt" to FieldValue.serverTimestamp(),
        )
        if (phone.isNotBlank()) data["phone"] = phone
        db.collection("users").document(uid).set(data).await()
    }
}
