package com.example.sistema_asistencia

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Actividad encargada de gestionar el inicio de sesión de los usuarios (Estudiantes, Docentes, Administradores).
 * Permite ingresar credenciales y valida el acceso al menú principal.
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referencias a los componentes de la interfaz
        val etUsuario = findViewById<TextInputEditText>(R.id.etUsuario)
        val etContrasena = findViewById<TextInputEditText>(R.id.etContrasena)
        val btnAcceso = findViewById<MaterialButton>(R.id.btnAcceso)

        // Datos de ejemplo basados en la base de datos del proyecto
        etUsuario.setText("carlos.benitez@universidad.edu")
        etContrasena.setText("password123")

        // Configuración del evento de clic en el botón de acceso
        btnAcceso.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            // Validación de campos vacíos
            if (usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                // Simulación de acceso exitoso
                Toast.makeText(this, "¡Bienvenido, Docente Carlos Benítez!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cierra la pantalla de login para que al presionar atrás no regrese aquí
            }
        }
    }
}
