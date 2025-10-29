package com.example.trackingbnsp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etNama: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etNomorTelepon: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        etUsername = findViewById(R.id.etUsername)
        etNama = findViewById(R.id.etNama)
        etPassword = findViewById(R.id.etPassword)
        etNomorTelepon = findViewById(R.id.etNomorTelepon)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val nama = etNama.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val nomorTelepon = etNomorTelepon.text.toString().trim()

        if (username.isEmpty()) {
            etUsername.error = "Username tidak boleh kosong"
            return
        }

        if (nama.isEmpty()) {
            etNama.error = "Nama tidak boleh kosong"
            return
        }

        if (password.isEmpty() || password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            return
        }

        if (nomorTelepon.isEmpty()) {
            etNomorTelepon.error = "Nomor telepon tidak boleh kosong"
            return
        }

        val usersRef = database.getReference("users")
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegisterActivity, "Username sudah digunakan", Toast.LENGTH_SHORT).show()
                    } else {
                        createUserWithFirebase(username, nama, password, nomorTelepon)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun createUserWithFirebase(username: String, nama: String, password: String, nomorTelepon: String) {
        val email = "${username}@trackingbnsp.com"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val user = User(userId, username, nama, password, nomorTelepon)
                    database.getReference("users").child(userId).setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registrasi berhasil! Silakan login", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menyimpan data: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}