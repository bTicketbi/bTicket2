package com.bticket.app.ui.components

import java.util.Locale

fun formatBIF(amount: Long): String {
    val grouped = String.format(Locale.FRANCE, "%,d", amount)
        .replace('\u202f', ' ')
        .replace('\u00a0', ' ')
    return "$grouped BIF"
}

private val MONTHS = arrayOf(
    "janv.", "févr.", "mars", "avr.", "mai", "juin",
    "juil.", "août", "sept.", "oct.", "nov.", "déc.",
)

/** « 2026-11-20 » → « 20 ». */
fun dayOf(iso: String): String = iso.split("-").getOrNull(2)?.take(2) ?: ""

/** « 2026-11-20 » → « nov. ». */
fun monthOf(iso: String): String {
    val m = iso.split("-").getOrNull(1)?.toIntOrNull() ?: return ""
    return MONTHS.getOrElse(m - 1) { "" }
}

/** « 2026-11-20 » → « 20 nov. 2026 ». */
fun formatDate(iso: String): String {
    val parts = iso.split("-")
    if (parts.size < 3) return iso
    return "${dayOf(iso)} ${monthOf(iso)} ${parts[0]}"
}
