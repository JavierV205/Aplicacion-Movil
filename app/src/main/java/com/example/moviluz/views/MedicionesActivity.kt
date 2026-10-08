package com.example.moviluz.views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviluz.R
import com.example.moviluz.adapters.LightMeasurementAdapter
import com.example.moviluz.models.LightMeasurement
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MedicionesActivity : AppCompatActivity() {

    companion object {
        const val COLECCION_MEDICIONES = "mediciones_luz"
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: LightMeasurementAdapter

    private lateinit var recyclerMediciones: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEstadoVacio: LinearLayout
    private lateinit var txtTotalMediciones: TextView
    private lateinit var spnFiltroUbicacion: Spinner

    private var listenerRegistration: ListenerRegistration? = null
    private var todasLasMediciones: List<LightMeasurement> = emptyList()
    private var filtroActual: String = "Todas"
    private var emailUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mediciones)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainMediciones)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailUsuario = intent.getStringExtra("usuario")
        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences(PreferenciasActivity.PREFS_NAME, Context.MODE_PRIVATE)

        inicializarVistas()
        configurarRecyclerView()
        configurarFiltro()
        escucharMedicionesEnTiempoReal()
    }

    private fun inicializarVistas() {
        recyclerMediciones = findViewById(R.id.recyclerMediciones)
        progressBar = findViewById(R.id.progressBarMediciones)
        layoutEstadoVacio = findViewById(R.id.layoutEstadoVacio)
        txtTotalMediciones = findViewById(R.id.txtTotalMediciones)
        spnFiltroUbicacion = findViewById(R.id.spnFiltroUbicacion)

        val btnVolver = findViewById<ImageButton>(R.id.btnVolverMediciones)
        val btnPreferencias = findViewById<ImageButton>(R.id.btnAbrirPreferencias)
        val fabAgregar = findViewById<FloatingActionButton>(R.id.fabAgregarMedicion)

        btnVolver.setOnClickListener {
            finish()
        }

        btnPreferencias.setOnClickListener {
            val intent = Intent(this, PreferenciasActivity::class.java)
            intent.putExtra("usuario", emailUsuario)
            startActivity(intent)
        }

        fabAgregar.setOnClickListener {
            mostrarDialogoCrearOEditar(null)
        }
    }

    private fun configurarRecyclerView() {
        adapter = LightMeasurementAdapter(
            measurements = emptyList(),
            onEditClickListener = { medicion ->
                mostrarDialogoCrearOEditar(medicion)
            },
            onDeleteClickListener = { medicion ->
                confirmarEliminacion(medicion)
            }
        )
        recyclerMediciones.layoutManager = LinearLayoutManager(this)
        recyclerMediciones.adapter = adapter
    }

    private fun configurarFiltro() {
        val opcionesFiltro = arrayOf("Todas") + PreferenciasActivity.UBICACIONES_DISPONIBLES
        val adapterFiltro = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            opcionesFiltro
        )
        spnFiltroUbicacion.adapter = adapterFiltro

        spnFiltroUbicacion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filtroActual = opcionesFiltro[position]
                aplicarFiltro()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Suscripción en tiempo real a la colección "mediciones_luz" de Firestore
     */
    private fun escucharMedicionesEnTiempoReal() {
        progressBar.visibility = View.VISIBLE

        listenerRegistration = db.collection(COLECCION_MEDICIONES)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(this, "Error al sincronizar datos: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    todasLasMediciones = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(LightMeasurement::class.java)
                    }
                    aplicarFiltro()
                }
            }
    }

    private fun aplicarFiltro() {
        val listaFiltrada = if (filtroActual == "Todas") {
            todasLasMediciones
        } else {
            todasLasMediciones.filter { it.location.equals(filtroActual, ignoreCase = true) }
        }

        adapter.updateList(listaFiltrada)
        txtTotalMediciones.text = "Lecturas: ${listaFiltrada.size}"

        if (listaFiltrada.isEmpty()) {
            layoutEstadoVacio.visibility = View.VISIBLE
            recyclerMediciones.visibility = View.GONE
        } else {
            layoutEstadoVacio.visibility = View.GONE
            recyclerMediciones.visibility = View.VISIBLE
        }
    }

    /**
     * Diálogo para Crear (si medicion == null) o Editar (si medicion != null)
     */
    private fun mostrarDialogoCrearOEditar(medicionExistente: LightMeasurement?) {
        val builder = AlertDialog.Builder(this)
        val vistaDialogo = LayoutInflater.from(this).inflate(R.layout.dialog_light_measurement, null)

        val edtDeviceId = vistaDialogo.findViewById<EditText>(R.id.edtDialogDeviceId)
        val edtIntensity = vistaDialogo.findViewById<EditText>(R.id.edtDialogIntensity)
        val spnLocation = vistaDialogo.findViewById<Spinner>(R.id.spnDialogLocation)

        val adapterSpinner = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            PreferenciasActivity.UBICACIONES_DISPONIBLES
        )
        spnLocation.adapter = adapterSpinner

        val esEdicion = medicionExistente != null
        val titulo = if (esEdicion) "Editar Medición de Luz" else "Nueva Medición de Luz"

        if (esEdicion) {
            edtDeviceId.setText(medicionExistente?.deviceId)
            edtIntensity.setText(medicionExistente?.lightIntensity.toString())
            val indiceUbicacion = PreferenciasActivity.UBICACIONES_DISPONIBLES.indexOf(medicionExistente?.location)
            if (indiceUbicacion >= 0) {
                spnLocation.setSelection(indiceUbicacion)
            }
        } else {
            // Sugerir ID de dispositivo y ubicación predeterminada desde SharedPreferences
            edtDeviceId.setText("ESP32_LDR_01")
            val ubicacionPredeterminada = prefs.getString(
                PreferenciasActivity.KEY_UBICACION_FAVORITA,
                PreferenciasActivity.UBICACIONES_DISPONIBLES[0]
            )
            val indice = PreferenciasActivity.UBICACIONES_DISPONIBLES.indexOf(ubicacionPredeterminada)
            if (indice >= 0) {
                spnLocation.setSelection(indice)
            }
        }

        builder.setTitle(titulo)
            .setView(vistaDialogo)
            .setPositiveButton(if (esEdicion) "Actualizar" else "Guardar", null) // Se sobreescribe para validar antes de cerrar
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()

        // Sobrescribir listener positivo para evitar que el diálogo se cierre en caso de error
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val deviceId = edtDeviceId.text.toString().trim()
            val intensidadStr = edtIntensity.text.toString().trim()
            val ubicacion = spnLocation.selectedItem.toString()

            if (deviceId.isEmpty()) {
                edtDeviceId.error = "Ingresa el ID del dispositivo"
                return@setOnClickListener
            }

            if (intensidadStr.isEmpty()) {
                edtIntensity.error = "Ingresa la intensidad de luz"
                return@setOnClickListener
            }

            val intensidad = intensidadStr.toDoubleOrNull()
            if (intensidad == null || intensidad < 0) {
                edtIntensity.error = "Ingresa un número válido mayor o igual a 0"
                return@setOnClickListener
            }

            if (esEdicion) {
                actualizarMedicionEnFirestore(medicionExistente?.id ?: "", deviceId, intensidad, ubicacion)
            } else {
                guardarMedicionEnFirestore(deviceId, intensidad, ubicacion)
            }

            alertDialog.dismiss()
        }
    }

    /**
     * Operación CREATE en Firestore
     */
    private fun guardarMedicionEnFirestore(deviceId: String, intensidad: Double, ubicacion: String) {
        val nuevaMedicion = hashMapOf(
            "deviceId" to deviceId,
            "lightIntensity" to intensidad,
            "location" to ubicacion,
            "timestamp" to Timestamp.now()
        )

        db.collection(COLECCION_MEDICIONES)
            .add(nuevaMedicion)
            .addOnSuccessListener {
                Toast.makeText(this, "Medición registrada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar medición: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Operación UPDATE en Firestore
     */
    private fun actualizarMedicionEnFirestore(id: String, deviceId: String, intensidad: Double, ubicacion: String) {
        if (id.isEmpty()) {
            Toast.makeText(this, "Error: Identificador de documento no válido", Toast.LENGTH_SHORT).show()
            return
        }

        val actualizacion = hashMapOf<String, Any>(
            "deviceId" to deviceId,
            "lightIntensity" to intensidad,
            "location" to ubicacion
        )

        db.collection(COLECCION_MEDICIONES).document(id)
            .update(actualizacion)
            .addOnSuccessListener {
                Toast.makeText(this, "Medición actualizada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar medición: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Operación DELETE en Firestore con Diálogo de Confirmación
     */
    private fun confirmarEliminacion(medicion: LightMeasurement) {
        val id = medicion.id
        if (id.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se encontró el ID del documento", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("¿Eliminar medición?")
            .setMessage("¿Estás seguro de que deseas eliminar el registro de '${medicion.location}' (${medicion.lightIntensity} Lux)? Esta acción no se puede deshacer.")
            .setIcon(R.drawable.ic_delete_24)
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection(COLECCION_MEDICIONES).document(id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Medición eliminada exitosamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar medición: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}
