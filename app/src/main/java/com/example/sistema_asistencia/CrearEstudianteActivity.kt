
package com.example.sistema_asistencia

import android.content.ContentValues
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class CrearEstudianteActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etClave: TextInputEditText
    private lateinit var etConfirmar: TextInputEditText

    private lateinit var tilClave: TextInputLayout
    private lateinit var switchEstado: SwitchCompat
    private lateinit var btnGuardar: MaterialButton

    private var idEstudiante: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_estudiante)

        etNombre = findViewById(R.id.etNombreEstudiante)
        etApellido = findViewById(R.id.etApellidoEstudiante)
        etEmail = findViewById(R.id.etEmailEstudiante)
        etClave = findViewById(R.id.etClaveEstudiante)
        etConfirmar = findViewById(R.id.etConfirmarClave)

        tilClave = findViewById(R.id.tilClaveEstudiante)
        switchEstado = findViewById(R.id.switchEstadoEstudiante)
        btnGuardar = findViewById(R.id.btnGuardarEstudiante)

        val tvTitulo = findViewById<TextView>(
            R.id.tvTituloEstudiante
        )

        idEstudiante = intent.getLongExtra(
            "id_estudiante", -1L
        )

        if (idEstudiante > 0) {

            // MODO EDICIÓN
            tvTitulo.text = "Editar Estudiante"
            btnGuardar.text = "Guardar cambios"
            switchEstado.visibility = View.VISIBLE

            tilClave.hint = "Nueva contraseña (opcional)"

            cargarEstudiante()

        } else {

            // MODO CREACIÓN
            tvTitulo.text = "Nuevo Estudiante"
            btnGuardar.text = "Crear estudiante"
            switchEstado.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            guardarEstudiante()
        }
    }

    // ==================================
    // CARGAR ESTUDIANTE PARA EDITAR
    // ==================================

    private fun cargarEstudiante() {

        try {
            BaseDatos(this).abrir().use { db ->

                val consulta = """
                    SELECT
                        u.nombre,
                        u.apellido,
                        u.email,
                        CASE
                            WHEN u.estado = 1 AND e.estado = 1
                            THEN 1
                            ELSE 0
                        END AS estado
                    FROM estudiantes e
                    JOIN usuarios u
                        ON e.usu_id_usuario = u.id_usuario
                    WHERE e.id_estudiante = ?
                """.trimIndent()

                db.rawQuery(
                    consulta,
                    arrayOf(idEstudiante.toString())
                ).use { cursor ->

                    if (cursor.moveToFirst()) {
                        etNombre.setText(cursor.getString(0))
                        etApellido.setText(cursor.getString(1))
                        etEmail.setText(cursor.getString(2))

                        switchEstado.isChecked =
                            cursor.getInt(3) == 1

                    } else {
                        throw IllegalStateException(
                            "Estudiante no encontrado"
                        )
                    }
                }
            }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    // ==================================
    // INSERT / UPDATE
    // ==================================

    private fun guardarEstudiante() {

        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()

        val email = etEmail.text.toString()
            .trim()
            .lowercase(Locale.ROOT)

        val password = etClave.text.toString()
        val confirmar = etConfirmar.text.toString()

        val estado = if (
            idEstudiante > 0 && !switchEstado.isChecked
        ) 0 else 1

        // Validaciones
        if (nombre.isEmpty() || apellido.isEmpty()) {
            Toast.makeText(
                this,
                "Ingrese nombre y apellido",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Correo electrónico no válido"
            etEmail.requestFocus()
            return
        }

        // Contraseña requerida en creación
        if (idEstudiante < 0 && password.isBlank()) {
            etClave.error = "Ingrese una contraseña"
            return
        }

        // En edición, dejarla vacía conserva la anterior
        if (password.isNotEmpty()) {

            if (password.length < 8 || password.isBlank()) {
                etClave.error = "Mínimo 8 caracteres válidos"
                return
            }

            if (password != confirmar) {
                etConfirmar.error = "Las contraseñas no coinciden"
                return
            }

        } else if (confirmar.isNotEmpty()) {
            etConfirmar.error = "Ingrese primero la contraseña"
            return
        }

        btnGuardar.isEnabled = false

        // Ejecutar hashing y escritura fuera del hilo principal
        Thread {

            try {
                val nuevoHash = if (password.isNotEmpty()) {
                    PasswordUtils.generarHash(password)
                } else {
                    null
                }

                BaseDatos(applicationContext).abrir().use { db ->

                    db.beginTransaction()

                    try {
                        // Verificar correo duplicado
                        val consultaEmail = """
                            SELECT 1
                            FROM usuarios
                            WHERE LOWER(email) = LOWER(?)
                              AND id_usuario != COALESCE(
                                  (
                                      SELECT usu_id_usuario
                                      FROM estudiantes
                                      WHERE id_estudiante = ?
                                  ),
                                  -1
                              )
                            LIMIT 1
                        """.trimIndent()

                        db.rawQuery(
                            consultaEmail,
                            arrayOf(
                                email,
                                idEstudiante.toString()
                            )
                        ).use { cursor ->

                            if (cursor.moveToFirst()) {
                                throw IllegalArgumentException(
                                    "El correo ya está registrado"
                                )
                            }
                        }

                        val valoresUsuario = ContentValues().apply {
                            put("nombre", nombre)
                            put("apellido", apellido)
                            put("email", email)
                            put("estado", estado)

                            if (nuevoHash != null) {
                                put("clave_hash", nuevoHash)
                            }
                        }

                        if (idEstudiante > 0) {

                            // =====================
                            // UPDATE
                            // =====================

                            val filasUsuario = db.update(
                                "usuarios",
                                valoresUsuario,
                                """
                                id_usuario = (
                                    SELECT usu_id_usuario
                                    FROM estudiantes
                                    WHERE id_estudiante = ?
                                )
                                """.trimIndent(),
                                arrayOf(idEstudiante.toString())
                            )

                            if (filasUsuario == 0) {
                                throw IllegalStateException(
                                    "Usuario no encontrado"
                                )
                            }

                            val valoresEstudiante =
                                ContentValues().apply {
                                    put("estado", estado)
                                }

                            val filasEstudiante = db.update(
                                "estudiantes",
                                valoresEstudiante,
                                "id_estudiante = ?",
                                arrayOf(idEstudiante.toString())
                            )

                            if (filasEstudiante == 0) {
                                throw IllegalStateException(
                                    "Estudiante no encontrado"
                                )
                            }

                        } else {

                            // =====================
                            // INSERT
                            // =====================

                            // Buscar rol ESTUDIANTE
                            val idRol = db.rawQuery(
                                """
                                SELECT id_rol
                                FROM roles
                                WHERE nombre = 'ESTUDIANTE'
                                LIMIT 1
                                """.trimIndent(),
                                null
                            ).use { cursor ->

                                if (!cursor.moveToFirst()) {
                                    throw IllegalStateException(
                                        "No existe el rol ESTUDIANTE"
                                    )
                                }

                                cursor.getLong(0)
                            }

                            valoresUsuario.put(
                                "rol_id_rol",
                                idRol
                            )

                            // Insertar usuario
                            val idUsuario = db.insertOrThrow(
                                "usuarios",
                                null,
                                valoresUsuario
                            )

                            // Insertar estudiante vinculado
                            val valoresEstudiante =
                                ContentValues().apply {
                                    put("usu_id_usuario", idUsuario)
                                    put("estado", 1)
                                }

                            db.insertOrThrow(
                                "estudiantes",
                                null,
                                valoresEstudiante
                            )
                        }

                        db.setTransactionSuccessful()

                    } finally {
                        db.endTransaction()
                    }
                }

                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        val mensaje = if (idEstudiante > 0) {
                            "Estudiante actualizado correctamente"
                        } else {
                            "Estudiante creado correctamente"
                        }

                        Toast.makeText(
                            this,
                            mensaje,
                            Toast.LENGTH_LONG
                        ).show()

                        finish()
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        btnGuardar.isEnabled = true

                        Toast.makeText(
                            this,
                            "Error al guardar: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }.start()
    }
}
