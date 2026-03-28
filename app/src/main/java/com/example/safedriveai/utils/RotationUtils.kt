package com.example.safedriveai.utils

import android.view.OrientationEventListener
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import com.example.safedriveai.sensors.SensorChecker

/**
 * Ángulos de rotación comunes basados en la orientación del dispositivo.
 */
enum class DeviceRotation(val angle: Float) {
    PORTRAIT(0f),
    LANDSCAPE_LEFT(90f),
    PORTRAIT_UPSIDE_DOWN(180f),
    LANDSCAPE_RIGHT(270f)
}

/**
 * Composable que escucha el sensor de orientación.
 */
@Composable
fun rememberDeviceRotation(): State<DeviceRotation> {
    val context = LocalContext.current
    val deviceRotation = remember { mutableStateOf(DeviceRotation.PORTRAIT) }

    DisposableEffect(context) {
        val missingHardware = SensorChecker.getMissingHardware(context)

        if (missingHardware.contains("Acelerómetro")) {
            return@DisposableEffect onDispose { }
        }

        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val newRotation = when (orientation) {
                    in 70 until 110 -> DeviceRotation.LANDSCAPE_RIGHT
                    in 160 until 200 -> DeviceRotation.PORTRAIT_UPSIDE_DOWN
                    in 250 until 290 -> DeviceRotation.LANDSCAPE_LEFT
                    in 340..360, in 0 until 20 -> DeviceRotation.PORTRAIT
                    else -> return
                }

                if (newRotation != deviceRotation.value) {
                    deviceRotation.value = newRotation
                }
            }
        }
        listener.enable()
        onDispose { listener.disable() }
    }
    return deviceRotation
}

/**
 * Contenedor que rota el contenido y ajusta su tamaño para simular horizontal.
 */
@Composable
fun RotationAwareContent(
    rotation: DeviceRotation,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(DpSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                containerSize = with(density) {
                    DpSize(coordinates.size.width.toDp(), coordinates.size.height.toDp())
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (containerSize != DpSize.Zero) {
            val isLandscape = rotation == DeviceRotation.LANDSCAPE_LEFT || rotation == DeviceRotation.LANDSCAPE_RIGHT

            val contentWidth = if (isLandscape) containerSize.height else containerSize.width
            val contentHeight = if (isLandscape) containerSize.width else containerSize.height

            Box(
                modifier = Modifier
                    .requiredSize(contentWidth, contentHeight)
                    .graphicsLayer {
                        rotationZ = rotation.angle
                    },
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}