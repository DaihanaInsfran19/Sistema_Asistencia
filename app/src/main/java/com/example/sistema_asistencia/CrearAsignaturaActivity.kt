
package com.example.sistema_asistencia

import android.content.ContentValues
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale
import androidx.appcompat.widget.SwitchCompat
import android.view.View

class CrearAsignaturaActivity : AppCompatActivity() {

    // Componentes de la interfaz
    private lateinit var etCodigo: TextInputEditText
    private lateinit var etNombre: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var btnGuardar: MaterialButton
    private lateinit var tvTitulo: TextView
    private lateinit var switchEstado: SwitchCompat

    // ID de la asignatura
    // -1 significa que estamos creando una nueva
    private var idAsignatura: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_asignatura)

        // Referencias a los componentes XML
        etCodigo = findViewById(R.id.etCodigoAsignatura)
        etNombre = findViewById(R.id.etNombreAsignatura)
        etDescripcion = findViewById(R.id.etDescripcionAsignatura)
        btnGuardar = findViewById(R.id.btnGuardarAsignatura)
        tvTitulo = findViewById(R.id.tvTituloFormulario)
        switchEstado = findViewById(R.id.switchEstadoAsignatura)

        // Comprobar si recibimos un ID para editar
        idAsignatura = intent.getLongExtra(
            "id_asignatura",
            -1L
        )


        if (idAsignatura > 0) {

            // MODO EDICIÓN
            tvTitulo.text = "Editar Asignatura"
            btnGuardar.text = "Guardar cambios"

            // Mostrar el interruptor de estado
            switchEstado.visibility = View.VISIBLE

            cargarAsignatura()

        } else {

            // MODO CREACIÓN
            tvTitulo.text = "Nueva Asignatura"
            btnGuardar.text = "Crear asignatura"

            // Ocultar el interruptor
            switchEstado.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            guardarAsignatura()
        }
    }

    // ==================================
    // CONSULTAR ASIGNATURA PARA EDITAR
    // ==================================

    private fun cargarAsignatura() {

        try {
            BaseDatos(this).abrir().use { db ->

                val consulta = """
                    SELECT codigo, nombre, descripcion,estado
                    FROM asignaturas
                    WHERE id_asignatura = ?
                """.trimIndent()

                db.rawQuery(
                    consulta,
                    arrayOf(idAsignatura.toString())
                ).use { cursor ->

                    if (cursor.moveToFirst()) {

                        // Cargar datos en el formulario
                        etCodigo.setText(
                            cursor.getString(0)
                        )

                        etNombre.setText(
                            cursor.getString(1)
                        )

                        etDescripcion.setText(
                            cursor.getString(2) ?: ""
                        )

                        switchEstado.isChecked = cursor.getInt(3) == 1
                    } else {

                        Toast.makeText(
                            this,
                            "No se encontró la asignatura",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()
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

    // ==================================
    // GUARDAR ASIGNATURA (INSERT / UPDATE)
    // ==================================

    private fun guardarAsignatura() {

        val codigo = etCodigo.text.toString()
            .trim()
            .uppercase(Locale.ROOT)

        val nombre = etNombre.text.toString().trim()

        val descripcion = etDescripcion.text.toString().trim()

        // Validaciones
        if (codigo.isEmpty()) {
            etCodigo.error = "Ingrese el código"
            etCodigo.requestFocus()
            return
        }

        if (nombre.isEmpty()) {
            etNombre.error = "Ingrese el nombre"
            etNombre.requestFocus()
            return
        }

        try {

            // Preparar los valores
            val valores = ContentValues().apply {
                put("codigo", codigo)
                put("nombre", nombre)
                put("descripcion", descripcion)
            }


            BaseDatos(this).abrir().use { db ->

                if (idAsignatura > 0) {

                    // UPDATE: permitir modificar el estado
                    val estado = if (switchEstado.isChecked) 1 else 0

                    valores.put("estado", estado)

                    val filasActualizadas = db.update(
                        "asignaturas",
                        valores,
                        "id_asignatura = ?",
                        arrayOf(idAsignatura.toString())
                    )

                    if (filasActualizadas == 0) {
                        throw IllegalStateException(
                            "No se encontró la asignatura"
                        )
                    }

                } else {

                    // INSERT: siempre crear como activa
                    valores.put("estado", 1)

                    db.insertOrThrow(
                        "asignaturas",
                        null,
                        valores
                    )
                }
            }

            // Mensaje según la operación
            val mensaje = if (idAsignatura > 0) {
                "Asignatura actualizada correctamente"
            } else {
                "Asignatura creada correctamente"
            }

            Toast.makeText(
                this,
                mensaje,
                Toast.LENGTH_LONG
            ).show()

            // Regresar al listado
            finish()

        } catch (e: SQLiteConstraintException) {

            Toast.makeText(
                this,
                "El código ya existe o los datos no son válidos",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Error al guardar: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
