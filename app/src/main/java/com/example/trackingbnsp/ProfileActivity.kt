package com.example.trackingbnsp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etUsername: TextInputEditText
    private lateinit var etNama: TextInputEditText
    private lateinit var etNomorTelepon: TextInputEditText
    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        toolbar = findViewById(R.id.toolbar)
        etUsername = findViewById(R.id.etUsername)
        etNama = findViewById(R.id.etNama)
        etNomorTelepon = findViewById(R.id.etNomorTelepon)
        btnEdit = findViewById(R.id.btnEdit)
        btnSave = findViewById(R.id.btnSave)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        loadUserData()

        btnEdit.setOnClickListener {
            enableEditing(true)
        }

        btnSave.setOnClickListener {
            saveUserData()
        }
    }

    private fun loadUserData() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "") ?: ""

        if (userId.isNotEmpty()) {
            database.getReference("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        currentUser = snapshot.getValue(User::class.java)
                        if (currentUser != null) {
                            etUsername.setText(currentUser!!.username)
                            etNama.setText(currentUser!!.nama)
                            etNomorTelepon.setText(currentUser!!.nomorTelepon)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ProfileActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun enableEditing(enabled: Boolean) {
        etNama.isEnabled = enabled
        etNomorTelepon.isEnabled = enabled

        if (enabled) {
            btnEdit.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
        } else {
            btnEdit.visibility = View.VISIBLE
            btnSave.visibility = View.GONE
        }
    }

    private fun saveUserData() {
        val nama = etNama.text.toString().trim()
        val nomorTelepon = etNomorTelepon.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return
        }

        if (nomorTelepon.isEmpty()) {
            etNomorTelepon.error = "Nomor telepon tidak boleh kosong"
            return
        }

        if (currentUser != null) {
            currentUser!!.nama = nama
            currentUser!!.nomorTelepon = nomorTelepon

            database.getReference("users").child(currentUser!!.id)
                .setValue(currentUser)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile berhasil diupdate!", Toast.LENGTH_SHORT).show()
                    enableEditing(false)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal update profile: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}