package com.example.moviluz.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moviluz.R

class BienvenidaActivity : AppCompatActivity() {

    private var emailUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bienvenida)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el email del usuario pasado por Intent
        emailUsuario = intent.getStringExtra("usuario")

        val txtBienvenida = findViewById<TextView>(R.id.txtBienvenida)
        val txtServicio = findViewById<TextView>(R.id.txtServicio)

        // Saludo personalizado con el email recibido
        if (!emailUsuario.isNullOrEmpty()) {
            txtBienvenida.text = "¡Bienvenido, $emailUsuario!"
        } else {
            txtBienvenida.text = "¡Bienvenido a MoviLuz!"
        }

        txtServicio.text = "MoviLuz - Plataforma de Monitoreo IoT de Intensidad Lumínica en Tiempo Real.\nSupervisa los sensores de luz de tus instalaciones con control inteligente."
    }

    fun onPreferenciasClick(view: View) {
        val intent = Intent(this, PreferenciasActivity::class.java)
        intent.putExtra("usuario", emailUsuario)
        startActivity(intent)
    }

    fun onVerLecturasClick(view: View) {
        val intent = Intent(this, MedicionesActivity::class.java)
        intent.putExtra("usuario", emailUsuario)
        startActivity(intent)
    }
}
