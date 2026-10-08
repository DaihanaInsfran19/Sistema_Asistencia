package com.example.sistema_asistencia

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import java.util.Locale

/**
 * Actividad para registrar la asistencia y sesiones de clases mediante códigos QR.
 * Mantiene los campos solicitados adaptados al tema: Estudiante, Asignatura, Fecha, Tolerancia y Cantidad.
 */
class RegistroAsistenciaActivity : AppCompatActivity() {

    private data class EstudianteOpcion(
        val id: Long,
        val nombre: String
    ) {
        override fun toString(): String = nombre
    }

    // Modelo para mostrar asignaturas y conservar sus ID
    private data class AsignaturaOpcion(
        val id: Long,
        val codigo: String,
        val nombre: String
    ) {
        override fun toString(): String = "$nombre ($codigo)"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_asistencia)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Registro de Asistencia QR"

        // Referencias a los componentes de la interfaz
        val autoCompleteEstudiante = findViewById<AutoCompleteTextView>(R.id.autoCompleteEstudiante)
        val autoCompleteAsignatura = findViewById<AutoCompleteTextView>(R.id.autoCompleteAsignatura)
        val etFechaInicio = findViewById<TextInputEditText>(R.id.etFechaInicio)
        val etFechaDevolucion = findViewById<TextInputEditText>(R.id.etFechaDevolucion)
        val etCantidad = findViewById<TextInputEditText>(R.id.etCantidad)
        val btnRegistrarAsistencia = findViewById<MaterialButton>(R.id.btnRegistrarAsistencia)

        // Listas que se cargarán desde SQLite
        val estudiantes = mutableListOf<EstudianteOpcion>()
        val asignaturas = mutableListOf<AsignaturaOpcion>()

        // ==================================
        // 1. CONSULTAR BASE DE DATOS SQLITE
        // ==================================

        try {
            BaseDatos(this).abrir().use { db ->

                val consulta = """
                    SELECT e.id_estudiante, u.nombre, u.apellido
                    FROM estudiantes e
                    INNER JOIN usuarios u
                        ON e.usu_id_usuario = u.id_usuario
                    WHERE e.estado = 1 AND u.estado = 1
                    ORDER BY u.apellido, u.nombre
                """.trimIndent()

                db.rawQuery(consulta, null).use { cursor ->

                    val colId = cursor.getColumnIndexOrThrow("id_estudiante")
                    val colNombre = cursor.getColumnIndexOrThrow("nombre")
                    val colApellido = cursor.getColumnIndexOrThrow("apellido")

                    while (cursor.moveToNext()) {

                        val id = cursor.getLong(colId)
                        val nombre = cursor.getString(colNombre)
                        val apellido = cursor.getString(colApellido)

                        estudiantes.add(
                            EstudianteOpcion(
                                id = id,
                                nombre = "$nombre $apellido"
                            )
                        )
                    }
                }

                // CONSULTAR ASIGNATURAS
                val consultaAsignaturas = """
                    SELECT id_asignatura, codigo, nombre
                    FROM asignaturas
                    WHERE estado = 1
                    ORDER BY nombre
                """.trimIndent()

                db.rawQuery(consultaAsignaturas, null).use { cursor ->

                    val colId =
                        cursor.getColumnIndexOrThrow("id_asignatura")

                    val colCodigo =
                        cursor.getColumnIndexOrThrow("codigo")

                    val colNombre =
                        cursor.getColumnIndexOrThrow("nombre")

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(colId)
                        val codigo = cursor.getString(colCodigo)
                        val nombre = cursor.getString(colNombre)

                        asignaturas.add(
                            AsignaturaOpcion(
                                id = id,
                                codigo = codigo,
                                nombre = nombre
                            )
                        )
                    }
                }
            }

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al consultar SQLite: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }

        val adapterEstudiantes = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            estudiantes
        )

        autoCompleteEstudiante.setAdapter(adapterEstudiantes)

        val adapterAsignaturas = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            asignaturas
        )

        autoCompleteAsignatura.setAdapter(adapterAsignaturas)

        // Guardar el objeto seleccionado, no solo su nombre
        var estudianteSeleccionado: EstudianteOpcion? = null
        var asignaturaSeleccionada: AsignaturaOpcion? = null

        autoCompleteEstudiante.setOnItemClickListener {
                parent, _, position, _ ->

            estudianteSeleccionado =
                parent.getItemAtPosition(position) as? EstudianteOpcion
        }

        autoCompleteAsignatura.setOnItemClickListener {
                parent, _, position, _ ->

            asignaturaSeleccionada =
                parent.getItemAtPosition(position) as? AsignaturaOpcion
        }


        // Selector de fecha para Fecha de Clase
        etFechaInicio.setOnClickListener {
            showDatePicker(etFechaInicio)
        }

        // Selector de fecha para Límite de Tolerancia
        etFechaDevolucion.setOnClickListener {
            showDatePicker(etFechaDevolucion)
        }

        // Evento de guardado del registro de asistencia
        btnRegistrarAsistencia.setOnClickListener {
            val estudiante = autoCompleteEstudiante.text.toString().trim()
            val asignatura = autoCompleteAsignatura.text.toString().trim()
            val fechaInicio = etFechaInicio.text.toString().trim()
            val fechaDevolucion = etFechaDevolucion.text.toString().trim()
            val cantidad = etCantidad.text.toString().trim()

            if (estudiante.isEmpty() || asignatura.isEmpty() || fechaInicio.isEmpty() || fechaDevolucion.isEmpty() || cantidad.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos requeridos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "¡Asistencia registrada exitosamente por QR!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    /**
     * Muestra un diálogo de calendario (DatePicker) para seleccionar fechas.
     */
    private fun showDatePicker(editText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            editText.setText(formattedDate)
        }, year, month, day).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
