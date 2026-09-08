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

        // Listas de datos de prueba basadas en la base de datos del proyecto (Estudiantes y Asignaturas)
        val estudiantes = arrayOf("Ana Gómez", "Luis Martínez", "María López", "Carlos Benítez")
        val asignaturas = arrayOf("Programación I (INF-101)", "Base de Datos", "Redes de Computadoras", "Matemática Discreta")

        // Adaptadores para los menús desplegables
        val adapterEstudiantes = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estudiantes)
        autoCompleteEstudiante.setAdapter(adapterEstudiantes)

        val adapterAsignaturas = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, asignaturas)
        autoCompleteAsignatura.setAdapter(adapterAsignaturas)

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
