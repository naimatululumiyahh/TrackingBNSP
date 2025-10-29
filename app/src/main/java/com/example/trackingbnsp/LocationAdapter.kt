package com.example.trackingbnsp

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

class LocationHistoryAdapter : ListAdapter<LocationHistory, LocationHistoryAdapter.LocationViewHolder>(LocationDiffCallback()) {

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCoordinates: TextView = view.findViewById(R.id.tvCoordinates)
        val tvAlamat: TextView = view.findViewById(R.id.tvAlamat)
        val btnOpenMap: ImageView = view.findViewById(R.id.btnOpenMap)

        fun bind(location: LocationHistory) {
            tvCoordinates.text = location.getFormattedCoordinates()
            tvAlamat.text = location.alamat

            btnOpenMap.setOnClickListener {
                openMap(location)
            }
        }

        private fun openMap(location: LocationHistory) {
            val context = itemView.context
            val lat = location.latitude ?: return
            val lon = location.longitude ?: return

            try {
                val gmmIntentUri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(Lokasi)")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                } else {
                    // Fallback: buka di browser
                    val browserUri = Uri.parse("https://maps.google.com/maps?q=$lat,$lon")
                    val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                    context.startActivity(browserIntent)
                }
            } catch (e: Exception) {
                // Handle error
                android.widget.Toast.makeText(context, "Gagal membuka peta.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location_history, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

class LocationDiffCallback : DiffUtil.ItemCallback<LocationHistory>() {
    override fun areItemsTheSame(oldItem: LocationHistory, newItem: LocationHistory): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: LocationHistory, newItem: LocationHistory): Boolean {
        return oldItem == newItem
    }
}