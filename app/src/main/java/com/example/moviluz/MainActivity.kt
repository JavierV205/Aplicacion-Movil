package com.example.moviluz

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moviluz.views.BienvenidaActivity
import com.example.moviluz.views.RegistroActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var mostrandoPassword: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de Firebase Authentication
        auth = FirebaseAuth.getInstance()
    }

    /**
     * Alterna la visibilidad de la contraseña entre texto plano y asteriscos
     */
    fun onMostrarPasswordClick(view: View) {
        val edtPassword = findViewById<EditText>(R.id.edtPassword)

        if (!mostrandoPassword) {
            edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            mostrandoPassword = true
        } else {
            edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            mostrandoPassword = false
        }
        edtPassword.setSelection(edtPassword.text.length)
    }

    /**
     * Ejecuta las validaciones de login y la autenticación con Firebase
     */
    fun onIngresarClick(view: View) {
        val edtUsuario = findViewById<EditText>(R.id.edtUsuario)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)

        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()

        var esValido = true

        // Validar formato de email antes de enviar la petición
        if (usuario.isEmpty()) {
            edtUsuario.error = "Ingresa tu correo electrónico"
            esValido = false
        } else if (!usuario.contains("@") || !Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            edtUsuario.error = "Ingresa un email válido (ej: usuario@correo.com)"
            esValido = false
        } else {
            edtUsuario.error = null
        }

        // Validar contraseña
        if (password.isEmpty()) {
            edtPassword.error = "Ingresa tu contraseña"
            esValido = false
        } else if (password.length < 6) {
            edtPassword.error = "La contraseña debe tener al menos 6 caracteres"
            esValido = false
        } else {
            edtPassword.error = null
        }

        // Si la validación falla, no avanzar ni enviar petición a Firebase
        if (!esValido) {
            return
        }

        // Enviar petición a Firebase Authentication
        auth.signInWithEmailAndPassword(usuario, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Autenticación correcta: obtener el email y navegar a Bienvenida
                    val firebaseUser = auth.currentUser
                    val emailUsuario = firebaseUser?.email ?: usuario

                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, BienvenidaActivity::class.java)
                    intent.putExtra("usuario", emailUsuario)
                    startActivity(intent)
                } else {
                    // Capturar y mostrar el error de Firebase, impidiendo avanzar
                    val mensajeError = task.exception?.localizedMessage ?: "Error de autenticación"
                    Toast.makeText(this, "Error de Firebase: $mensajeError", Toast.LENGTH_LONG).show()
                    edtPassword.error = "Credenciales incorrectas"
                }
            }
    }

    /**
     * Limpia los campos de texto, errores visuales y casilla de verificación
     */
    fun onLimpiarClick(view: View) {
        val edtUsuario = findViewById<EditText>(R.id.edtUsuario)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val chkRecordarme = findViewById<CheckBox>(R.id.chkRecordarme)

        edtUsuario.setText("")
        edtPassword.setText("")
        edtUsuario.error = null
        edtPassword.error = null
        chkRecordarme.isChecked = false
    }

    /**
     * Navega a la pantalla de Registro de usuarios
     */
    fun onIrARegistroClick(view: View) {
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
    }
}