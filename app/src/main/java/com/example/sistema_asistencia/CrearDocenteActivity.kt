
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

class CrearDocenteActivity : AppCompatActivity() {

    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etClave: TextInputEditText
    private lateinit var etConfirmar: TextInputEditText

    private lateinit var tilClave: TextInputLayout
    private lateinit var switchEstado: SwitchCompat
    private lateinit var btnGuardar: MaterialButton

    private var idDocente: Long = -1L
    private var emailOriginal: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_docente)

        etNombre = findViewById(R.id.etNombreDocente)
        etApellido = findViewById(R.id.etApellidoDocente)
        etEmail = findViewById(R.id.etEmailDocente)
        etClave = findViewById(R.id.etClaveDocente)
        etConfirmar = findViewById(R.id.etConfirmarClave)

        tilClave = findViewById(R.id.tilClaveDocente)
        switchEstado = findViewById(R.id.switchEstadoDocente)
        btnGuardar = findViewById(R.id.btnGuardarDocente)

        val tvTitulo = findViewById<TextView>(
            R.id.tvTituloDocente
        )

        idDocente = intent.getLongExtra("id_docente", -1L)

        if (idDocente > 0) {

            // MODO EDICIÓN
            tvTitulo.text = "Editar Docente"
            btnGuardar.text = "Guardar cambios"
            switchEstado.visibility = View.VISIBLE

            tilClave.hint = "Nueva contraseña (opcional)"

            cargarDocente()

        } else {

            // MODO CREACIÓN
            tvTitulo.text = "Nuevo Docente"
            btnGuardar.text = "Crear docente"
            switchEstado.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            guardarDocente()
        }
    }

    // =====================================
    // CONSULTAR DOCENTE PARA EDITAR
    // =====================================

    private fun cargarDocente() {

        try {
            BaseDatos(this).abrir().use { db ->

                val consulta = """
                    SELECT
                        u.nombre,
                        u.apellido,
                        u.email,
                        CASE
                            WHEN u.estado = 1 AND d.estado = 1
                            THEN 1
                            ELSE 0
                        END AS estado
                    FROM docentes d
                    INNER JOIN usuarios u
                        ON d.usu_id_usuario = u.id_usuario
                    WHERE d.id_docente = ?
                """.trimIndent()

                db.rawQuery(
                    consulta,
                    arrayOf(idDocente.toString())
                ).use { cursor ->

                    if (cursor.moveToFirst()) {

                        etNombre.setText(cursor.getString(0))
                        etApellido.setText(cursor.getString(1))

                        emailOriginal = cursor.getString(2)
                        etEmail.setText(emailOriginal)

                        switchEstado.isChecked =
                            cursor.getInt(3) == 1

                    } else {
                        throw IllegalStateException(
                            "Docente no encontrado"
                        )
                    }
                }
            }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al cargar: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    // =====================================
    // INSERT / UPDATE
    // =====================================

    private fun guardarDocente() {

        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()

        val email = etEmail.text.toString()
            .trim()
            .lowercase(Locale.ROOT)

        val password = etClave.text.toString()
        val confirmar = etConfirmar.text.toString()

        // Crear siempre activo; editar permite cambiarlo
        val estado = if (
            idDocente > 0 && !switchEstado.isChecked
        ) 0 else 1

        // VALIDACIONES
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

        if (idDocente < 0 && password.isBlank()) {
            etClave.error = "Ingrese una contraseña"
            return
        }

        if (password.isNotEmpty()) {

            if (password.length < 8 || password.isBlank()) {
                etClave.error = "Mínimo 8 caracteres"
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

        // Hash y escritura fuera del hilo principal
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

                        // Verificar email duplicado si es nuevo
                        // o si se está cambiando el correo.
                        if (
                            idDocente < 0 ||
                            !email.equals(
                                emailOriginal,
                                ignoreCase = true
                            )
                        ) {

                            db.rawQuery(
                                """
                                SELECT 1
                                FROM usuarios
                                WHERE LOWER(email) = LOWER(?)
                                LIMIT 1
                                """.trimIndent(),
                                arrayOf(email)
                            ).use { cursor ->

                                if (cursor.moveToFirst()) {
                                    throw IllegalArgumentException(
                                        "El correo ya está registrado"
                                    )
                                }
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

                        if (idDocente > 0) {

                            // =====================
                            // UPDATE
                            // =====================

                            val filasUsuario = db.update(
                                "usuarios",
                                valoresUsuario,
                                """
                                id_usuario = (
                                    SELECT usu_id_usuario
                                    FROM docentes
                                    WHERE id_docente = ?
                                )
                                """.trimIndent(),
                                arrayOf(idDocente.toString())
                            )

                            if (filasUsuario == 0) {
                                throw IllegalStateException(
                                    "Usuario no encontrado"
                                )
                            }

                            val valoresDocente = ContentValues().apply {
                                put("estado", estado)
                            }

                            val filasDocente = db.update(
                                "docentes",
                                valoresDocente,
                                "id_docente = ?",
                                arrayOf(idDocente.toString())
                            )

                            if (filasDocente == 0) {
                                throw IllegalStateException(
                                    "Docente no encontrado"
                                )
                            }

                        } else {

                            // =====================
                            // INSERT
                            // =====================

                            // Obtener el rol DOCENTE
                            val idRol = db.rawQuery(
                                """
                                SELECT id_rol
                                FROM roles
                                WHERE nombre = 'DOCENTE'
                                LIMIT 1
                                """.trimIndent(),
                                null
                            ).use { cursor ->

                                if (!cursor.moveToFirst()) {
                                    throw IllegalStateException(
                                        "No existe el rol DOCENTE"
                                    )
                                }

                                cursor.getLong(0)
                            }

                            valoresUsuario.put("rol_id_rol", idRol)

                            // Crear usuario
                            val idUsuario = db.insertOrThrow(
                                "usuarios",
                                null,
                                valoresUsuario
                            )

                            // Crear docente vinculado
                            val valoresDocente = ContentValues().apply {
                                put("usu_id_usuario", idUsuario)
                                put("estado", 1)
                            }

                            db.insertOrThrow(
                                "docentes",
                                null,
                                valoresDocente
                            )
                        }

                        db.setTransactionSuccessful()

                    } finally {
                        db.endTransaction()
                    }
                }

                runOnUiThread {

                    if (!isFinishing && !isDestroyed) {

                        val mensaje = if (idDocente > 0) {
                            "Docente actualizado correctamente"
                        } else {
                            "Docente creado correctamente"
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
