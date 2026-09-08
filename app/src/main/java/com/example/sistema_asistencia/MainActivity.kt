package com.example.sistema_asistencia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

/**
 * Actividad del Menú Principal basada en el tema del proyecto:
 * Sistema de Control y Gestión de Asistencia de Estudiantes mediante Códigos QR.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a las tarjetas (cards) del menú basadas en el tema QR de Asistencia
        val cardEstudiantes = findViewById<MaterialCardView>(R.id.cardEstudiantes)
        val cardAsignaturas = findViewById<MaterialCardView>(R.id.cardAsignaturas)
        val cardCursos = findViewById<MaterialCardView>(R.id.cardCursos)
        val cardAsistencias = findViewById<MaterialCardView>(R.id.cardAsistencias)
        val cardConsultas = findViewById<MaterialCardView>(R.id.cardConsultas)

        // Módulo 1: Estudiantes
        cardEstudiantes.setOnClickListener {
            openDetail("Módulo Estudiantes", "Gestión de alumnos, datos institucionales y estados de matrícula.")
        }

        // Módulo 2: Asignaturas
        cardAsignaturas.setOnClickListener {
            openDetail("Módulo Asignaturas", "Administración de materias, códigos y planes de estudio.")
        }

        // Módulo 3: Cursos y Clases
        cardCursos.setOnClickListener {
            openDetail("Módulo Cursos y Clases", "Programación de clases, horarios y tolerancia de asistencia.")
        }

        // Módulo 4: Asistencias QR (Abre la pantalla de registro / escaneo)
        cardAsistencias.setOnClickListener {
            val intent = Intent(this, RegistroAsistenciaActivity::class.java)
            startActivity(intent)
        }

        // Módulo 5: Consultas y Reportes
        cardConsultas.setOnClickListener {
            openDetail("Módulo Consultas y Reportes", "Consulta histórica de asistencias, ausencias y llegadas tardías por QR.")
        }
    }

    /**
     * Método auxiliar para abrir la pantalla de detalle genérica para los módulos del sistema de asistencia.
     */
    private fun openDetail(titulo: String, descripcion: String) {
        val intent = Intent(this, DetalleModuloActivity::class.java).apply {
            putExtra("EXTRA_TITULO", titulo)
            putExtra("EXTRA_DESCRIPCION", descripcion)
        }
        startActivity(intent)
    }
}
