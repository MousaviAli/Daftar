package com.parsaplanner.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

private data class DrawnStroke(val path: Path, val color: Color, val widthPx: Float)

private val penColors = listOf(
    Color(0xFF3A2E27), Color(0xFFC1613A), Color(0xFF6E7F52), Color(0xFFD9A441), Color(0xFF2E5A88)
)

/**
 * A simple sketch/handwriting canvas. Reads pressure from the pointer event when a stylus
 * is used (thicker line under more pressure); falls back to a fixed width for touch/mouse.
 * On save, the drawing is flattened to a PNG in the app's private storage and returned as a Uri
 * so it can be attached exactly like any other image.
 */
@Composable
fun DrawingScreen(onCancel: () -> Unit, onSave: (Uri) -> Unit) {
    val context = LocalContext.current
    val strokes = remember { mutableStateListOf<DrawnStroke>() }
    var currentColor by remember { mutableStateOf(penColors.first()) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("رسم با قلم") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) }) {
                        Icon(Icons.Filled.Undo, contentDescription = "واگرد")
                    }
                    IconButton(onClick = {
                        val bitmap = renderStrokesToBitmap(strokes, canvasSize)
                        val uri = saveBitmapToFile(context, bitmap)
                        onSave(uri)
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "ذخیره")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                penColors.forEach { color ->
                    Box(
                        Modifier
                            .size(if (color == currentColor) 30.dp else 24.dp)
                            .background(color, CircleShape)
                            .pointerInput(color) {
                                detectTapGestures(onTap = { currentColor = color })
                            }
                    )
                }
            }
        }
    ) { padding ->
        Canvas(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFFFFDF8))
                .pointerInput(currentColor) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val path = Path().apply { moveTo(down.position.x, down.position.y) }
                        val pressure = down.pressure.takeIf { !it.isNaN() && it > 0f } ?: 1f
                        val stroke = DrawnStroke(path, currentColor, (2f + pressure * 6f).coerceIn(2f, 14f))
                        strokes.add(stroke)
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (change.pressed) {
                                path.lineTo(change.position.x, change.position.y)
                                change.consume()
                                strokes[strokes.lastIndex] = stroke
                            } else {
                                break
                            }
                        }
                    }
                }
        ) {
            canvasSize = size
            strokes.forEach { stroke ->
                drawPath(
                    path = stroke.path,
                    color = stroke.color,
                    style = Stroke(width = stroke.widthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

private fun renderStrokesToBitmap(strokes: List<DrawnStroke>, size: Size): Bitmap {
    val width = size.width.toInt().coerceAtLeast(1)
    val height = size.height.toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(android.graphics.Color.parseColor("#FFFFFDF8"))

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }
    strokes.forEach { stroke ->
        paint.color = android.graphics.Color.argb(
            (stroke.color.alpha * 255).toInt(),
            (stroke.color.red * 255).toInt(),
            (stroke.color.green * 255).toInt(),
            (stroke.color.blue * 255).toInt()
        )
        paint.strokeWidth = stroke.widthPx
        canvas.drawPath(stroke.path.asAndroidPath(), paint)
    }
    return bitmap
}

private fun saveBitmapToFile(context: android.content.Context, bitmap: Bitmap): Uri {
    val dir = File(context.filesDir, "sketches").apply { mkdirs() }
    val file = File(dir, "sketch_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
    return Uri.fromFile(file)
}
