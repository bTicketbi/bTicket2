package com.bticket.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Tracés des icônes (grille 24×24, style Material). */
object IconPaths {
    const val HOME = "M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"
    const val TICKET = "M22,10V6c0,-1.1 -0.9,-2 -2,-2H4c-1.1,0 -1.99,0.9 -1.99,2v4c1.1,0 1.99,0.9 1.99,2s-0.89,2 -2,2v4c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2v-4c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2zM13,17.5h-2v-2h2v2zM13,13.5h-2v-2h2v2zM13,9.5h-2v-2h2v2z"
    const val ACCOUNT = "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,6c1.93,0 3.5,1.57 3.5,3.5S13.93,13 12,13s-3.5,-1.57 -3.5,-3.5S10.07,6 12,6zM12,20c-2.03,0 -4.43,-0.82 -6.14,-2.88C7.55,15.8 9.68,15 12,15s4.45,0.8 6.14,2.12C16.43,19.18 14.03,20 12,20z"
    const val SEARCH = "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z"
    const val LOCATION = "M12,2C8.13,2 5,5.13 5,9c0,5.25 7,13 7,13s7,-7.75 7,-13c0,-3.87 -3.13,-7 -7,-7zM12,11.5c-1.38,0 -2.5,-1.12 -2.5,-2.5s1.12,-2.5 2.5,-2.5 2.5,1.12 2.5,2.5 -1.12,2.5 -2.5,2.5z"
    const val CHAT = "M20,2H4c-1.1,0 -2,0.9 -2,2v18l4,-4h14c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2z"
}

@Composable
fun PathIcon(
    pathData: String,
    tint: Color,
    size: Dp = 24.dp,
    modifier: Modifier = Modifier,
) {
    val path = remember(pathData) { PathParser().parsePathString(pathData).toPath() }
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension / 24f
        withTransform({ scale(s, s, Offset.Zero) }) {
            drawPath(path, tint)
        }
    }
}
