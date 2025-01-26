package com.example.reto9

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            return
        }


        // Set up user location overlay
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation() // This makes the map follow the user's location
        map.overlays.add(locationOverlay)
        map.controller.setZoom(20.0)
        locationOverlay.runOnFirstFix {
            val userLocation = locationOverlay.myLocation
            if (userLocation != null) {
                runOnUiThread {
                    fetchAndAddPOIs(userLocation.latitude, userLocation.longitude)
                }
            }
        }

        val latitudeInput: EditText = findViewById(R.id.latitude_input)
        val longitudeInput: EditText = findViewById(R.id.longitude_input)
        val goButton: Button = findViewById(R.id.coordinate_button)

        goButton.setOnClickListener {
            val latText = latitudeInput.text.toString()
            val lonText = longitudeInput.text.toString()

            if (latText.isEmpty() || lonText.isEmpty()) {
                Toast.makeText(this, "Please enter valid coordinates!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val latitude = latText.toDouble()
                val longitude = lonText.toDouble()

                // Center and zoom the map to the entered coordinates
                val geoPoint = GeoPoint(latitude, longitude)
                fetchAndAddPOIs(latitude, longitude)
                map.controller.animateTo(geoPoint)
                map.controller.setZoom(20.0)

                // Optionally add a marker at the entered coordinates
                val marker = Marker(map)
                marker.position = geoPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Custom Location"
                map.overlays.add(marker)

            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid coordinate format!", Toast.LENGTH_SHORT).show()
            }
        }
        val backToLocationButton: Button = findViewById(R.id.back_to_location_button)
        backToLocationButton.setOnClickListener {
            val myLocation = locationOverlay.myLocation
            if (myLocation != null) {
                map.controller.animateTo(myLocation)
                map.controller.setZoom(20.0)
                val userLocation = locationOverlay.myLocation
                if (userLocation != null) {
                    runOnUiThread {
                        fetchAndAddPOIs(userLocation.latitude, userLocation.longitude)
                    }
                }
                Toast.makeText(this, "Centered on your location!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Location not available!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun fetchAndAddPOIs(latitude: Double, longitude: Double) {
        val overpassUrl =
            "https://overpass-api.de/api/interpreter?data=[out:json];node(around:1000,$latitude,$longitude)[\"amenity\"];out;"

        val request = Request.Builder()
            .url(overpassUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to load POIs", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonResponse ->
                    try {
                        val jsonObject = JSONObject(jsonResponse)
                        val elements = jsonObject.getJSONArray("elements")

                        val pois = mutableListOf<GeoPoint>()
                        for (i in 0 until elements.length()) {
                            val element = elements.getJSONObject(i)
                            if (element.has("lat") && element.has("lon")) {
                                val lat = element.getDouble("lat")
                                val lon = element.getDouble("lon")
                                pois.add(GeoPoint(lat, lon))
                            }
                        }

                        runOnUiThread {
                            addPointsOfInterest(pois)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun addPointsOfInterest(pois: List<GeoPoint>) {
        for (point in pois) {
            val marker = Marker(map)
            marker.position = point
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Point of Interest"
            map.overlays.add(marker)
        }
    }


}
