package com.example.trackingbnsp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
// Import model data yang sudah kita definisikan

class HomeActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvWelcome: TextView
    private lateinit var tvUserInfo: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvLocationPreview: TextView
    private lateinit var cardMap: CardView
    private lateinit var btnSaveLocation: Button
    private lateinit var btnHistory: Button
    private lateinit var btnProfile: Button
    private lateinit var btnLogout: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var currentUserId: String = ""

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        toolbar = findViewById(R.id.toolbar)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserInfo = findViewById(R.id.tvUserInfo)
        tvLocation = findViewById(R.id.tvLocation)
        tvLocationPreview = findViewById(R.id.tvLocationPreview)
        cardMap = findViewById(R.id.cardMap)
        btnSaveLocation = findViewById(R.id.btnSaveLocation)
        btnHistory = findViewById(R.id.btnHistory)
        btnProfile = findViewById(R.id.btnProfile)
        btnLogout = findViewById(R.id.btnLogout)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Home"

        loadUserData()
        checkLocationPermission()

        // Klik card untuk buka Google Maps
        cardMap.setOnClickListener {
            openGoogleMaps()
        }

        // Simpan lokasi ke database
        btnSaveLocation.setOnClickListener {
            saveLocationToDatabase()
        }

        // Lihat riwayat lokasi
        btnHistory.setOnClickListener {
            // FIX: Mengubah LocationHistory menjadi LocationHistoryActivity
            val intent = Intent(this, LocationHistoryActivity::class.java)
            startActivity(intent)
        }
// ... (Sisa fungsi lain tetap sama)
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
// ... (Fungsi loadUserData, checkLocationPermission, onRequestPermissionsResult, getCurrentLocation tetap sama)

    private fun loadUserData() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getString("userId", "") ?: ""

        if (currentUserId.isNotEmpty()) {
            database.getReference("users").child(currentUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        if (user != null) {
                            tvWelcome.text = "Selamat Datang, ${user.nama}!"
                            tvUserInfo.text = "Username: ${user.username}"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@HomeActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude

                    val latString = String.format("%.6f", currentLatitude)
                    val lngString = String.format("%.6f", currentLongitude)

                    tvLocation.text = "Lokasi Anda: $latString, $lngString"
                    tvLocationPreview.text = "Lat: $latString\nLng: $lngString\nKlik untuk membuka di Maps"
                } else {
                    tvLocation.text = "Tidak dapat mengambil lokasi"
                    tvLocationPreview.text = "Aktifkan GPS untuk melihat lokasi"
                    Toast.makeText(this, "Aktifkan GPS untuk mendapatkan lokasi", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun saveLocationToDatabase() {
        if (currentLatitude == 0.0 && currentLongitude == 0.0) {
            Toast.makeText(this, "Lokasi belum tersedia. Tunggu sebentar...", Toast.LENGTH_SHORT).show()
            getCurrentLocation()
            return
        }

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val locationId = database.getReference("locations").child(currentUserId).push().key ?: return

        val locationHistory = LocationHistory(
            id = locationId,
            userId = currentUserId,
            latitude = currentLatitude,
            longitude = currentLongitude,
            timestamp = System.currentTimeMillis(),
            alamat = "Lat: ${String.format("%.6f", currentLatitude)}, Lng: ${String.format("%.6f", currentLongitude)}"
        )

        database.getReference("locations")
            .child(currentUserId)
            .child(locationId)
            .setValue(locationHistory)
            .addOnSuccessListener {
                Toast.makeText(this, "Lokasi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                // Logika tambahan setelah sukses (misalnya update UI)
            }
            .addOnFailureListener { error ->
                // Pastikan Anda menangani kegagalan dengan baik
                Toast.makeText(this, " Gagal menyimpan: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun openGoogleMaps() {
        if (currentLatitude == 0.0 && currentLongitude == 0.0) {
            Toast.makeText(this, "Lokasi belum tersedia. Tunggu sebentar...", Toast.LENGTH_SHORT).show()
            getCurrentLocation()
            return
        }

        try {
            val gmmIntentUri = Uri.parse("geo:$currentLatitude,$currentLongitude?q=$currentLatitude,$currentLongitude(Lokasi Saya)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                openMapsInBrowser()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error membuka maps: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openMapsInBrowser() {
        val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$currentLatitude,$currentLongitude")
        val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
        startActivity(browserIntent)
    }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
    }
}