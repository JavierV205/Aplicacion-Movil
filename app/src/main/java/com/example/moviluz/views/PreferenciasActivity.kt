package com.example.moviluz.views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moviluz.R
import com.google.android.material.materialswitch.MaterialSwitch

class PreferenciasActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "PreferenciasMoviLuz"
        const val KEY_UBICACION_FAVORITA = "ubicacion_favorita"
        const val KEY_UMBRAL_MINIMO = "umbral_minimo"
        const val KEY_INTERVALO_MUESTREO = "intervalo_muestreo"
        const val KEY_ALERTAS_ACTIVAS = "alertas_activas"

        // Ubicaciones de monitoreo acordadas para MoviLuz
        val UBICACIONES_DISPONIBLES = arrayOf(
            "Invernadero",
            "Oficina Central",
            "Laboratorio IoT",
            "Pasillo Principal"
        )
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var spnUbicacion: Spinner
    private lateinit var edtUmbral: EditText
    private lateinit var edtIntervalo: EditText
    private lateinit var switchAlertas: MaterialSwitch
    private var emailUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preferencias)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainPreferencias)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailUsuario = intent.getStringExtra("usuario")
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        spnUbicacion = findViewById(R.id.spnUbicacionPreferida)
        edtUmbral = findViewById(R.id.edtUmbralMinimo)
        edtIntervalo = findViewById(R.id.edtIntervaloMuestreo)
        switchAlertas = findViewById(R.id.switchAlertas)

        val btnVolver = findViewById<ImageButton>(R.id.btnVolverPreferencias)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarPreferencias)
        val btnIrAMediciones = findViewById<Button>(R.id.btnIrAMediciones)

        configurarSpinner()
        cargarPreferencias()

        btnVolver.setOnClickListener {
            finish()
        }

        btnGuardar.setOnClickListener {
            guardarPreferencias()
        }

        btnIrAMediciones.setOnClickListener {
            val intent = Intent(this, MedicionesActivity::class.java)
            intent.putExtra("usuario", emailUsuario)
            startActivity(intent)
        }
    }

    private fun configurarSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            UBICACIONES_DISPONIBLES
        )
        spnUbicacion.adapter = adapter
    }

    private fun cargarPreferencias() {
        val ubicacionGuardada = prefs.getString(KEY_UBICACION_FAVORITA, UBICACIONES_DISPONIBLES[0])
        val umbralGuardado = prefs.getFloat(KEY_UMBRAL_MINIMO, 300.0f)
        val intervaloGuardado = prefs.getInt(KEY_INTERVALO_MUESTREO, 5)
        val alertasActivas = prefs.getBoolean(KEY_ALERTAS_ACTIVAS, true)

        val indiceUbicacion = UBICACIONES_DISPONIBLES.indexOf(ubicacionGuardada)
        if (indiceUbicacion >= 0) {
            spnUbicacion.setSelection(indiceUbicacion)
        }

        edtUmbral.setText(umbralGuardado.toString())
        edtIntervalo.setText(intervaloGuardado.toString())
        switchAlertas.isChecked = alertasActivas
    }

    private fun guardarPreferencias() {
        val umbralTexto = edtUmbral.text.toString().trim()
        val intervaloTexto = edtIntervalo.text.toString().trim()

        if (umbralTexto.isEmpty()) {
            edtUmbral.error = "Ingresa un umbral válido"
            return
        }

        val umbral = umbralTexto.toFloatOrNull()
        if (umbral == null || umbral < 0) {
            edtUmbral.error = "El umbral debe ser un número positivo"
            return
        }

        if (intervaloTexto.isEmpty()) {
            edtIntervalo.error = "Ingresa un intervalo válido"
            return
        }

        val intervalo = intervaloTexto.toIntOrNull()
        if (intervalo == null || intervalo <= 0) {
            edtIntervalo.error = "El intervalo debe ser mayor a 0"
            return
        }

        val ubicacionSeleccionada = spnUbicacion.selectedItem.toString()
        val alertas = switchAlertas.isChecked

        prefs.edit()
            .putString(KEY_UBICACION_FAVORITA, ubicacionSeleccionada)
            .putFloat(KEY_UMBRAL_MINIMO, umbral)
            .putInt(KEY_INTERVALO_MUESTREO, intervalo)
            .putBoolean(KEY_ALERTAS_ACTIVAS, alertas)
            .apply()

        Toast.makeText(this, "Preferencias guardadas exitosamente", Toast.LENGTH_SHORT).show()
    }
}
