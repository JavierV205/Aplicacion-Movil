package com.example.moviluz.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moviluz.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun mostrarEstadoCargando(cargando: Boolean) {
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val btnVolverLogin = findViewById<Button>(R.id.btnVolverLogin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarRegistro)

        btnRegistrar.isEnabled = !cargando
        btnVolverLogin.isEnabled = !cargando
        progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
    }

    /**
     * Valida que el email tenga un dominio válido (con arroba y extensión de dominio válida)
     */
    private fun esEmailValido(email: String): Boolean {
        if (!email.contains("@")) return false
        val partes = email.split("@")
        if (partes.size != 2 || partes[0].isEmpty() || partes[1].isEmpty()) return false
        val dominio = partes[1]
        if (!dominio.contains(".") || dominio.startsWith(".") || dominio.endsWith(".")) return false
        val extension = dominio.substringAfterLast(".")
        return extension.length >= 2 && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun onRegistrarClick(view: View) {
        val edtNombre = findViewById<EditText>(R.id.edtNombreRegistro)
        val edtEmail = findViewById<EditText>(R.id.edtEmailRegistro)
        val edtPassword = findViewById<EditText>(R.id.edtPasswordRegistro)
        val edtConfirmarPassword = findViewById<EditText>(R.id.edtConfirmarPassword)

        val nombre = edtNombre.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString()
        val confirmarPassword = edtConfirmarPassword.text.toString()

        var esValido = true

        // 1. Validar Nombre
        if (nombre.isEmpty()) {
            edtNombre.error = "Ingresa tu nombre completo"
            esValido = false
        } else {
            edtNombre.error = null
        }

        // 2. Validar Email: debe tener @ y dominio válido (ej: usuario@correo.com)
        if (email.isEmpty()) {
            edtEmail.error = "Ingresa tu correo electrónico"
            esValido = false
        } else if (!esEmailValido(email)) {
            edtEmail.error = "Ingresa un email con dominio válido (ej: usuario@correo.com)"
            esValido = false
        } else {
            edtEmail.error = null
        }

        // 3. Validar Contraseña: al menos 6 caracteres
        if (password.isEmpty()) {
            edtPassword.error = "Ingresa una contraseña"
            esValido = false
        } else if (password.length < 6) {
            edtPassword.error = "La contraseña debe tener al menos 6 caracteres"
            esValido = false
        } else {
            edtPassword.error = null
        }

        // 4. Validar Confirmación de Contraseña: coincidencia exacta
        if (confirmarPassword.isEmpty()) {
            edtConfirmarPassword.error = "Confirma tu contraseña"
            esValido = false
        } else if (password != confirmarPassword) {
            edtConfirmarPassword.error = "Las contraseñas no coinciden"
            esValido = false
        } else {
            edtConfirmarPassword.error = null
        }

        // Si alguna validación falla, no avanzar ni enviar petición a Firebase
        if (!esValido) {
            return
        }

        // Validaciones correctas: guardar en Firebase Authentication y Firestore
        mostrarEstadoCargando(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""

                    // Crear documento en la colección "usuarios" en Firestore
                    val datosUsuario = hashMapOf(
                        "uid" to uid,
                        "nombre" to nombre,
                        "email" to email,
                        "fechaRegistro" to System.currentTimeMillis()
                    )

                    Log.d("RegistroActivity", "Guardando usuario en Firestore UID: $uid")

                    db.collection("usuarios").document(uid)
                        .set(datosUsuario)
                        .addOnSuccessListener {
                            mostrarEstadoCargando(false)
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                            // Navegar a la pantalla de Bienvenida pasando el email por Intent
                            val intent = Intent(this, BienvenidaActivity::class.java)
                            intent.putExtra("usuario", email)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            mostrarEstadoCargando(false)
                            Log.e("RegistroActivity", "Error al guardar en Firestore", e)
                            Toast.makeText(this, "Error en Firestore: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    mostrarEstadoCargando(false)
                    val mensajeError = task.exception?.localizedMessage ?: "Error al registrar usuario"
                    Log.e("RegistroActivity", "Error en Firebase Auth: $mensajeError")
                    Toast.makeText(this, "Error de Firebase: $mensajeError", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun onVolverLoginClick(view: View) {
        finish()
    }
}
