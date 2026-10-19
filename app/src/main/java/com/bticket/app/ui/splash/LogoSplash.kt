package com.bticket.app.ui.splash

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/*
 * Animation du logo bTicket (portage de « bTicket - Animation du logo.html »).
 * Les tracés sont lus dans assets/logo_anim.json ; la chronologie reprend à l'identique
 * celle du fichier HTML : arrivée du panier (0,1 s), chute du ticket (1,0 s),
 * dessin de « bticket » (1,55 s), puis du slogan (2,55 s). Durée totale : 4,6 s.
 * Un appui n'importe où passe l'animation.
 */

private val GOLD = Color(0xFFD4AF38)
private val STAGE = Color(0xFF252A34)

private typealias Ease = (Float) -> Float

private object Easings {
    val lin: Ease = { t -> t }
    val p2out: Ease = { t -> 1f - (1f - t) * (1f - t) }
    val p2io: Ease = { t -> if (t < 0.5f) 2f * t * t else 1f - (-2f * t + 2f).pow(2) / 2f }
    val p3out: Ease = { t -> 1f - (1f - t).pow(3) }
    val expo: Ease = { t -> if (t >= 1f) 1f else 1f - 2f.pow(-10f * t) }
    val sineio: Ease = { t -> -(cos(PI.toFloat() * t) - 1f) / 2f }
    val back: Ease = { t ->
        val c1 = 2.2f
        val c3 = c1 + 1f
        1f + c3 * (t - 1f).pow(3) + c1 * (t - 1f).pow(2)
    }
    val bounce: Ease = { x ->
        val n = 7.5625f
        val d = 2.75f
        var t = x
        if (t < 1f / d) {
            n * t * t
        } else if (t < 2f / d) {
            t -= 1.5f / d
            n * t * t + 0.75f
        } else if (t < 2.5f / d) {
            t -= 2.25f / d
            n * t * t + 0.9375f
        } else {
            t -= 2.625f / d
            n * t * t + 0.984375f
        }
    }
    val elastic: Ease = { t ->
        if (t <= 0f) 0f
        else if (t >= 1f) 1f
        else 2f.pow(-10f * t) * sin((t * 10f - 0.75f) * (2f * PI.toFloat()) / 3f) + 1f
    }
}

/** Une forme : remplissage (règle pair/impair) + contours individuels pour le tracé au trait. */
private class Glyph(
    val fill: Path,
    val parts: List<Path>,
    val measures: List<PathMeasure>,
    val lengths: FloatArray,
    val total: Float,
    val cx: Float,
    val cy: Float,
)

/** Traînée de vitesse : trait arrondi (isLine) ou petit rectangle arrondi. */
private class SpeedEl(
    val isLine: Boolean,
    val x: Float,   // x1 pour un trait, x pour un rectangle
    val y: Float,
    val x2: Float,
    val w: Float,   // épaisseur du trait, ou largeur du rectangle
    val h: Float,
    val rx: Float,
)

private class LogoData(
    val speed: List<SpeedEl>,
    val cart: Glyph,
    val wheels: List<Glyph>,
    val ticket: Glyph,
    val dashes: List<Glyph>,
    val word: List<Glyph>,
    val idot: Glyph,
    val tag: List<Glyph>,
)

private fun buildGlyph(contours: JSONArray, cx: Float = 0f, cy: Float = 0f, measure: Boolean = false): Glyph {
    val fill = Path()
    fill.fillType = PathFillType.EvenOdd
    val parts = ArrayList<Path>()
    val measures = ArrayList<PathMeasure>()
    val lens = ArrayList<Float>()
    var total = 0f
    for (i in 0 until contours.length()) {
        val arr = contours.getJSONArray(i)
        val p = Path()
        var k = 0
        while (k + 1 < arr.length()) {
            val x = arr.getDouble(k).toFloat()
            val y = arr.getDouble(k + 1).toFloat()
            if (k == 0) p.moveTo(x, y) else p.lineTo(x, y)
            k += 2
        }
        p.close()
        fill.addPath(p)
        if (measure) {
            val pm = PathMeasure()
            pm.setPath(p, false)
            parts.add(p)
            measures.add(pm)
            lens.add(pm.length)
            total += pm.length
        }
    }
    return Glyph(fill, parts, measures, lens.toFloatArray(), total, cx, cy)
}

private fun JSONObject.f(key: String): Float = getDouble(key).toFloat()

private fun loadLogo(context: Context): LogoData {
    val text = context.assets.open("logo_anim.json").bufferedReader().use { it.readText() }
    val o = JSONObject(text)

    val sp = o.getJSONArray("speed")
    val speed = ArrayList<SpeedEl>()
    for (i in 0 until sp.length()) {
        val e = sp.getJSONObject(i)
        if (e.getString("t") == "line") {
            speed.add(SpeedEl(true, e.f("x1"), e.f("y1"), e.f("x2"), e.f("w"), 0f, 0f))
        } else {
            speed.add(SpeedEl(false, e.f("x"), e.f("y"), 0f, e.f("w"), e.f("h"), e.f("rx")))
        }
    }

    val wheelsJson = o.getJSONArray("wheels")
    val wheels = ArrayList<Glyph>()
    for (i in 0 until wheelsJson.length()) wheels.add(buildGlyph(wheelsJson.getJSONArray(i)))

    val dashesJson = o.getJSONArray("dashes")
    val dashes = ArrayList<Glyph>()
    for (i in 0 until dashesJson.length()) {
        val e = dashesJson.getJSONObject(i)
        dashes.add(buildGlyph(e.getJSONArray("c"), e.f("cx"), e.f("cy")))
    }

    val wordJson = o.getJSONArray("word")
    val word = ArrayList<Glyph>()
    for (i in 0 until wordJson.length()) word.add(buildGlyph(wordJson.getJSONArray(i), measure = true))

    val tagJson = o.getJSONArray("tag")
    val tag = ArrayList<Glyph>()
    for (i in 0 until tagJson.length()) tag.add(buildGlyph(tagJson.getJSONArray(i), measure = true))

    val idotJson = o.getJSONObject("idot")

    return LogoData(
        speed = speed,
        cart = buildGlyph(o.getJSONArray("cart")),
        wheels = wheels,
        ticket = buildGlyph(o.getJSONArray("ticket")),
        dashes = dashes,
        word = word,
        idot = buildGlyph(idotJson.getJSONArray("c"), idotJson.f("cx"), idotJson.f("cy")),
        tag = tag,
    )
}

private class Tr(val from: Float, val to: Float, val start: Float, val dur: Float, val ease: Ease)

/** Chronologie : mêmes valeurs que le moteur JavaScript du fichier HTML. */
private class Timeline(data: LogoData) {
    private val tracks = HashMap<String, HashMap<String, ArrayList<Tr>>>()
    val duration: Float

    private fun t(el: String, prop: String, from: Float, to: Float, start: Float, dur: Float, ease: Ease) {
        tracks.getOrPut(el) { HashMap() }.getOrPut(prop) { ArrayList() }.add(Tr(from, to, start, dur, ease))
    }

    /** Dessin au trait, puis remplissage, puis disparition du contour. */
    private fun draw(el: String, st: Float, dur: Float) {
        t(el, "do", 1f, 0f, st, dur, Easings.p2io)
        t(el, "fo", 0f, 1f, st + dur * 0.62f, 0.35f, Easings.lin)
        t(el, "so", 1f, 0f, st + dur * 0.9f, 0.22f, Easings.lin)
    }

    init {
        // Panier : arrivée à pleine vitesse, freinage, tangage, encaissement de l'impact
        t("cart", "x", -820f, 22f, 0.15f, 0.80f, Easings.expo)
        t("cart", "x", 22f, 0f, 0.95f, 0.35f, Easings.sineio)
        t("cart", "r", 0f, 5f, 0.45f, 0.35f, Easings.p2out)
        t("cart", "r", 5f, 0f, 0.80f, 0.75f, Easings.elastic)
        t("cart", "y", 0f, 4f, 1.24f, 0.07f, Easings.p2out)
        t("cart", "y", 4f, 0f, 1.31f, 0.55f, Easings.elastic)

        // Traînées : longues au début, elles se rétractent à leur taille finale
        val streak = floatArrayOf(340f, 300f, 260f)
        for (i in data.speed.indices) {
            val s = data.speed[i]
            val st = 0.10f + i * 0.05f
            if (s.isLine) {
                t("sl$i", "x1", s.x - streak[i], s.x, st, 0.95f, Easings.p3out)
            } else {
                t("sl$i", "x", s.x - streak[i], s.x, st, 0.95f, Easings.p3out)
                t("sl$i", "w", s.w + streak[i], s.w, st, 0.95f, Easings.p3out)
            }
            t("sl$i", "op", 0f, 1f, st, 0.12f, Easings.lin)
        }

        // Le ticket tombe dans le panier
        t("ticket", "op", 0f, 1f, 0.98f, 0.08f, Easings.lin)
        t("ticket", "y", -300f, 0f, 0.98f, 0.75f, Easings.bounce)
        t("ticket", "r", -50f, 0f, 0.98f, 0.55f, Easings.p3out)
        for (i in data.dashes.indices) {
            val st = 1.32f + i * 0.08f
            t("dash$i", "s", 0f, 1f, st, 0.38f, Easings.back)
            t("dash$i", "op", 0f, 1f, st, 0.05f, Easings.lin)
        }

        // « b » puis « ticket »
        draw("wg0", 1.55f, 0.70f)
        for (k in 1 until data.word.size) draw("wg$k", 1.70f + (k - 1) * 0.10f, 0.60f)
        val iStart = 1.70f + 0.10f + 0.35f
        t("idot", "op", 0f, 1f, iStart, 0.05f, Easings.lin)
        t("idot", "y", -90f, 0f, iStart, 0.75f, Easings.bounce)

        // Slogan
        for (i in data.tag.indices) draw("tg$i", 2.55f + i * 0.035f, 0.45f)

        var end = 0f
        for (m in tracks.values) {
            for (list in m.values) {
                list.sortBy { it.start }
                for (tr in list) end = max(end, tr.start + tr.dur)
            }
        }
        duration = max(end, 4.0f) + 0.6f
    }

    fun value(el: String, prop: String, time: Float, default: Float): Float {
        val list = tracks[el]?.get(prop) ?: return default
        var cur = list[0]
        if (time < cur.start) return cur.from
        for (tr in list) {
            if (time >= tr.start) cur = tr else break
        }
        val p = ((time - cur.start) / cur.dur).coerceIn(0f, 1f)
        return cur.from + (cur.to - cur.from) * cur.ease(p)
    }
}

private fun DrawScope.drawGlyphStroke(
    g: Glyph,
    color: Color,
    strokeWidth: Float,
    roundCap: Boolean,
    dashOffset: Float,
    fillAlpha: Float,
    strokeAlpha: Float,
    scratch: Path,
) {
    if (fillAlpha > 0.001f) drawPath(g.fill, color, alpha = fillAlpha)
    if (strokeAlpha > 0.001f) {
        val progress = (1f - dashOffset).coerceIn(0f, 1f)
        if (progress > 0f) {
            val limit = progress * g.total
            for (i in g.parts.indices) {
                val visible = min(g.lengths[i], limit)
                if (visible <= 0f) continue
                scratch.rewind()
                g.measures[i].getSegment(0f, visible, scratch, true)
                drawPath(
                    scratch,
                    color,
                    alpha = strokeAlpha,
                    style = Stroke(
                        width = strokeWidth,
                        cap = if (roundCap) StrokeCap.Round else StrokeCap.Butt,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }
}

private fun DrawScope.drawLogo(data: LogoData, tl: Timeline, t: Float, scratch: Path) {
    val vbW = 790f
    val vbH = 340f
    val sc = min(size.width * 0.9f / vbW, size.height * 0.5f / vbH)
    val ox = (size.width - vbW * sc) / 2f
    val oy = (size.height - vbH * sc) / 2f

    withTransform({
        translate(ox, oy)
        scale(sc, sc, Offset.Zero)
        translate(-120f, -335f)
    }) {
        // --- Panier + traînées + roues
        val cx = tl.value("cart", "x", t, 0f)
        val cy = tl.value("cart", "y", t, 0f)
        val cr = tl.value("cart", "r", t, 0f)
        withTransform({
            translate(cx, cy)
            rotate(cr, Offset(400f, 611f))
        }) {
            for (i in data.speed.indices) {
                val s = data.speed[i]
                val op = tl.value("sl$i", "op", t, 1f)
                if (op <= 0f) continue
                if (s.isLine) {
                    val x1 = tl.value("sl$i", "x1", t, s.x)
                    drawLine(
                        color = GOLD,
                        start = Offset(x1, s.y),
                        end = Offset(s.x2, s.y),
                        strokeWidth = s.w,
                        cap = StrokeCap.Round,
                        alpha = op,
                    )
                } else {
                    val x = tl.value("sl$i", "x", t, s.x)
                    val w = tl.value("sl$i", "w", t, s.w)
                    drawRoundRect(
                        color = GOLD,
                        topLeft = Offset(x, s.y),
                        size = Size(w, s.h),
                        cornerRadius = CornerRadius(s.rx, s.rx),
                        alpha = op,
                    )
                }
            }
            drawPath(data.cart.fill, GOLD)
            for (w in data.wheels) drawPath(w.fill, GOLD)
        }

        // --- Ticket qui tombe dans le panier
        val ticketOp = tl.value("ticket", "op", t, 1f)
        if (ticketOp > 0f) {
            val ty = tl.value("ticket", "y", t, 0f)
            val tr = tl.value("ticket", "r", t, 0f)
            withTransform({
                translate(0f, ty)
                rotate(tr, Offset(385f, 450f))
            }) {
                drawPath(data.ticket.fill, GOLD, alpha = ticketOp)
                for (i in data.dashes.indices) {
                    val d = data.dashes[i]
                    val dop = tl.value("dash$i", "op", t, 1f)
                    val ds = tl.value("dash$i", "s", t, 1f)
                    if (dop <= 0f || ds <= 0f) continue
                    withTransform({ scale(ds, ds, Offset(d.cx, d.cy)) }) {
                        drawPath(d.fill, GOLD, alpha = dop * ticketOp)
                    }
                }
            }
        }

        // --- « b » (blanc) puis « ticket » (or)
        for (k in data.word.indices) {
            drawGlyphStroke(
                g = data.word[k],
                color = if (k == 0) Color.White else GOLD,
                strokeWidth = 1.8f,
                roundCap = true,
                dashOffset = tl.value("wg$k", "do", t, 1f),
                fillAlpha = tl.value("wg$k", "fo", t, 0f),
                strokeAlpha = tl.value("wg$k", "so", t, 1f),
                scratch = scratch,
            )
        }
        val dotOp = tl.value("idot", "op", t, 1f)
        if (dotOp > 0f) {
            val dy = tl.value("idot", "y", t, 0f)
            withTransform({ translate(0f, dy) }) {
                drawPath(data.idot.fill, GOLD, alpha = dotOp)
            }
        }

        // --- Slogan « simple-rapide-sécurisé »
        for (k in data.tag.indices) {
            drawGlyphStroke(
                g = data.tag[k],
                color = Color.White,
                strokeWidth = 0.9f,
                roundCap = false,
                dashOffset = tl.value("tg$k", "do", t, 1f),
                fillAlpha = tl.value("tg$k", "fo", t, 0f),
                strokeAlpha = tl.value("tg$k", "so", t, 1f),
                scratch = scratch,
            )
        }
    }
}

/**
 * Écran de démarrage animé. [onFinished] est appelé à la fin de l'animation
 * (ou dès que l'utilisateur touche l'écran). Après la fin, l'image finale reste affichée.
 */
@Composable
fun LogoSplash(onFinished: () -> Unit) {
    val context = LocalContext.current
    val data = remember { loadLogo(context) }
    val timeline = remember { Timeline(data) }
    val scratch = remember { Path() }
    val time = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        time.animateTo(
            timeline.duration,
            tween((timeline.duration * 1000f).toInt(), easing = LinearEasing),
        )
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(STAGE)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onFinished() },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawLogo(data, timeline, time.value, scratch)
        }
    }
}
