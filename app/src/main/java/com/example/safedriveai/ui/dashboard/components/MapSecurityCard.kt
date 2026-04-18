package com.example.safedriveai.ui.dashboard.components

import android.R
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import androidx.compose.foundation.isSystemInDarkTheme
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.config.Configuration as OsmConfig // <--- Importante para el user agent
import androidx.core.content.ContextCompat
import com.example.safedriveai.ui.dashboard.CardBackground
import org.osmdroid.views.overlay.Marker

@SuppressLint("RememberReturnType")
@Composable
fun MapSecurityCard(
    currentLocation: Location?,
    modifier: Modifier = Modifier // Acepta el modifier que le pasamos
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme() // Detecta si el móvil está en modo oscuro

    // 1. Configuración Real: Identifica tu app ante los servidores de mapas
    remember {
        OsmConfig.getInstance().load(context, context.getSharedPreferences("osm_pref", 0))
        OsmConfig.getInstance().userAgentValue = context.packageName // Identificación obligatoria
    }

    // Recordamos el MapView para no recrearlo en cada recomposición
    val mapView = remember { MapView(context) }

    // Configuramos el marcador del coche
    val carMarker = remember {
        Marker(mapView).apply {
            title = "Tu Vehículo"
            icon = ContextCompat.getDrawable(context, R.drawable.ic_menu_compass)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }

    // Usamos el modifier que nos pasan (fillMaxSize del layout principal)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        // Especificamos explícitamente el tipo <MapView> para quitar los errores de compilación
        AndroidView<MapView>(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(17.5) // Zoom más cercano para conducción
                    overlays.add(carMarker) // Añadimos el coche al mapa

                    // --- MODO NOCHE PROFESIONAL ---
                    if (isDark) {
                        overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                    }
                }
            },
            modifier = Modifier.fillMaxSize(), // El mapa llena el Card
            update = { view ->
                // Actualizamos la posición del coche en tiempo real
                currentLocation?.let { loc ->
                    val point = GeoPoint(loc.latitude, loc.longitude)
                    carMarker.position = point
                    view.controller.animateTo(point) // Animación suave

                    if (loc.hasBearing()) {
                        view.mapOrientation = -loc.bearing // Rotamos el mapa según el rumbo
                    }
                }
            }
        )
    }
}