package com.example.trackingbnsp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trackingbnsp.databinding.ActivityLocationHistoryBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LocationHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationHistoryBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: LocationHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        setupToolbar()
        setupRecyclerView()
        loadLocationHistory()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Riwayat Lokasi"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = LocationHistoryAdapter()
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistory.adapter = adapter
    }

    private fun loadLocationHistory() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User ID tidak ditemukan.", Toast.LENGTH_SHORT).show()
            updateEmptyState(true)
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        database.getReference("locations").child(userId)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE

                    val locations = snapshot.children.mapNotNull {
                        it.getValue(LocationHistory::class.java)
                    }.asReversed()

                    adapter.submitList(locations)

                    updateEmptyState(locations.isEmpty())
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@LocationHistoryActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
                    updateEmptyState(true)
                }
            })
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}