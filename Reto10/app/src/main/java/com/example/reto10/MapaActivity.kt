package com.example.reto10
import android.os.Bundle
import androidx.activity.ComponentActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        val mapView = MapView(this)
        setContentView(mapView)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        // Obtener coordenadas enviadas desde la otra actividad
        val lat = intent.getDoubleExtra("latitud", 0.0)
        val lon = intent.getDoubleExtra("longitud", 0.0)

        if (lat != 0.0 && lon != 0.0) {
            val geoPoint = GeoPoint(lat, lon)
            mapView.controller.setCenter(geoPoint)

            val marker = Marker(mapView)
            marker.position = geoPoint
            marker.title = "Ubicación seleccionada"
            mapView.overlays.add(marker)
        }
    }
}
