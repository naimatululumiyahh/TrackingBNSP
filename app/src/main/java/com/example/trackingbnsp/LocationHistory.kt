package com.example.trackingbnsp

import com.google.firebase.database.IgnoreExtraProperties
import java.sql.Date


@IgnoreExtraProperties
data class LocationHistory(
    var id: String? = null,
    var userId: String? = null,
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
    var timestamp: Long? = 0L,
    var alamat: String? = "Alamat tidak ditemukan"
) {

    fun getFormattedCoordinates(): String {
        val lat = String.format("%.6f", latitude ?: 0.0)
        val lon = String.format("%.6f", longitude ?: 0.0)
        val date = Date(timestamp ?: 0L)
        return "Lat: $lat, " +
                "Lon: $lon, " +
                "Date:$date"
    }
}