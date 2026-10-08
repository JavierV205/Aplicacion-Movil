package com.example.moviluz.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moviluz.R
import com.example.moviluz.models.LightMeasurement
import java.text.SimpleDateFormat
import java.util.Locale

class LightMeasurementAdapter(
    private var measurements: List<LightMeasurement> = emptyList(),
    private val onEditClickListener: (LightMeasurement) -> Unit,
    private val onDeleteClickListener: (LightMeasurement) -> Unit
) : RecyclerView.Adapter<LightMeasurementAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun updateList(newList: List<LightMeasurement>) {
        measurements = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_light_measurement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = measurements[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = measurements.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtLocation: TextView = itemView.findViewById(R.id.txtItemLocation)
        private val txtDeviceId: TextView = itemView.findViewById(R.id.txtItemDeviceId)
        private val txtIntensity: TextView = itemView.findViewById(R.id.txtItemIntensity)
        private val txtTimestamp: TextView = itemView.findViewById(R.id.txtItemTimestamp)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditarMedicion)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnEliminarMedicion)

        fun bind(measurement: LightMeasurement) {
            txtLocation.text = measurement.location.ifEmpty { "Sin ubicación" }
            txtDeviceId.text = "Sensor: ${measurement.deviceId.ifEmpty { "Desconocido" }}"
            txtIntensity.text = String.format(Locale.US, "%.1f Lux", measurement.lightIntensity)

            val dateStr = if (measurement.timestamp != null) {
                dateFormat.format(measurement.timestamp)
            } else {
                "Reciente"
            }
            txtTimestamp.text = dateStr

            btnEdit.setOnClickListener {
                onEditClickListener(measurement)
            }

            btnDelete.setOnClickListener {
                onDeleteClickListener(measurement)
            }
        }
    }
}
