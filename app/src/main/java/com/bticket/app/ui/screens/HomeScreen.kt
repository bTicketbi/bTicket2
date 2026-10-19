package com.bticket.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.bticket.app.R
import com.bticket.app.data.EventItem
import com.bticket.app.data.Repo
import com.bticket.app.ui.components.IconPaths
import com.bticket.app.ui.components.PathIcon
import com.bticket.app.ui.components.dayOf
import com.bticket.app.ui.components.formatBIF
import com.bticket.app.ui.components.formatDate
import com.bticket.app.ui.components.monthOf

private val CATEGORIES = listOf("Tous", "Concert", "Festival", "Sport", "Conférence", "Théâtre", "Autre")

@Composable
fun HomeScreen() {
    val cs = MaterialTheme.colorScheme

    var events by remember { mutableStateOf<List<EventItem>?>(null) }
    var prices by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }
    var reload by remember { mutableIntStateOf(0) }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tous") }
    var selected by remember { mutableStateOf<EventItem?>(null) }

    LaunchedEffect(reload) {
        error = null
        events = null
        try {
            val list = Repo.listPublicEvents()
            events = list
            prices = try {
                Repo.minPrices(list.map { it.id })
            } catch (e: Exception) {
                emptyMap()
            }
        } catch (e: Exception) {
            error = e.message ?: "Impossible de charger les événements."
        }
    }

    val list = events
    val query = search.trim().lowercase()
    val filtered = list.orEmpty().filter { e ->
        (category == "Tous" || e.category == category) &&
            (query.isEmpty() || "${e.title} ${e.venue} ${e.city} ${e.organizerName}".lowercase().contains(query))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { AppHeader() }

        item {
            Text(
                text = buildAnnotatedString {
                    append("Votre prochaine ")
                    withStyle(SpanStyle(color = cs.primary)) { append("sortie") }
                    append(" commence ici")
                },
                fontSize = 27.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = cs.onSurface,
            )
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it.take(80) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Concert, festival, artiste, ville…") },
                leadingIcon = { PathIcon(IconPaths.SEARCH, cs.primary, 22.dp) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORIES) { c ->
                    val active = c == category
                    Box(
                        Modifier
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(if (active) cs.primary else cs.surfaceContainerLow)
                            .clickable { category = c }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = c,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) cs.onPrimary else cs.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "TOUS LES ÉVÉNEMENTS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = cs.onSurface,
                )
                if (list != null) {
                    Text(
                        text = "${filtered.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurfaceVariant,
                    )
                }
            }
        }

        val err = error
        if (err != null) {
            item {
                MessageCard(
                    title = "Impossible de charger les événements",
                    hint = err,
                    actionLabel = "Réessayer",
                    onAction = { reload++ },
                )
            }
        } else if (list == null) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cs.primary)
                }
            }
        } else if (filtered.isEmpty()) {
            item {
                MessageCard(
                    title = "Aucun événement disponible",
                    hint = "Les événements approuvés par bTicket apparaîtront ici.",
                )
            }
        } else {
            items(filtered, key = { it.id }) { event ->
                EventCard(event = event, minPrice = prices[event.id], onClick = { selected = event })
            }
        }
    }

    val ev = selected
    if (ev != null) {
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Fermer") } },
            title = { Text(ev.title, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${formatDate(ev.date)} · ${ev.time}", fontWeight = FontWeight.SemiBold)
                    Text("${ev.venue}, ${ev.city}", color = cs.onSurfaceVariant)
                    if (ev.description.isNotBlank()) {
                        Text(ev.description, maxLines = 8, overflow = TextOverflow.Ellipsis)
                    }
                    Text(
                        text = "L'achat de tickets arrive à la prochaine étape.",
                        color = cs.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
        )
    }
}

@Composable
fun AppHeader() {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_symbol),
            contentDescription = null,
            modifier = Modifier.size(width = 38.dp, height = 32.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = cs.onSurface)) { append("b") }
                withStyle(SpanStyle(color = cs.primary)) { append("ticket") }
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun EventCard(event: EventItem, minPrice: Long?, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape)
            .clip(shape)
            .background(cs.surfaceContainerLowest)
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .background(cs.surfaceVariant),
        ) {
            if (event.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                        ),
                    ),
            )
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(dayOf(event.date), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111318))
                Text(
                    monthOf(event.date).uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111318),
                )
            }
            Text(
                text = event.category,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = event.time,
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        TicketDivider()

        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PathIcon(IconPaths.LOCATION, cs.onSurfaceVariant, 16.dp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${event.venue}, ${event.city}",
                        fontSize = 14.sp,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = event.organizerName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (minPrice != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("DÈS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
                    Text(
                        formatBIF(minPrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = cs.onSurface,
                    )
                }
            }
        }
    }
}

/** Séparation « billet » : ligne pointillée avec deux encoches sur les bords. */
@Composable
private fun TicketDivider() {
    val cs = MaterialTheme.colorScheme
    val dashColor = cs.outlineVariant
    val notchColor = cs.background
    Box(Modifier.fillMaxWidth().height(20.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val y = size.height / 2f
            drawLine(
                color = dashColor,
                start = Offset(16.dp.toPx(), y),
                end = Offset(size.width - 16.dp.toPx(), y),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx())),
            )
        }
        Box(
            Modifier
                .size(20.dp)
                .offset(x = (-10).dp)
                .background(notchColor, CircleShape),
        )
        Box(
            Modifier
                .size(20.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 10.dp)
                .background(notchColor, CircleShape),
        )
    }
}

@Composable
fun MessageCard(
    title: String,
    hint: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cs.surfaceContainerLow)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
        Text(hint, fontSize = 13.sp, color = cs.onSurfaceVariant)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(4.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
