
package com.example.sistema_asistencia

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class EstudiantesActivity : AppCompatActivity() {

    private data class Estudiante(
        val id: Long,
        val nombre: String,
        val apellido: String,
        val email: String,
        val estado: Int
    ) {
        override fun toString(): String {
            val textoEstado =
                if (estado == 1) "Activo" else "Inactivo"

            return "$nombre $apellido\n$email\nEstado: $textoEstado"
        }
    }

    private val estudiantes = mutableListOf<Estudiante>()
    private lateinit var adapter: ArrayAdapter<Estudiante>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estudiantes)

        val listView = findViewById<ListView>(
            R.id.listViewEstudiantes
        )

        val tvSinEstudiantes = findViewById<TextView>(
            R.id.tvSinEstudiantes
        )

        val btnNuevo = findViewById<MaterialButton>(
            R.id.btnNuevoEstudiante
        )

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            estudiantes
        )

        listView.adapter = adapter
        listView.emptyView = tvSinEstudiantes

        // CREAR ESTUDIANTE
        btnNuevo.setOnClickListener {
            startActivity(
                Intent(this, CrearEstudianteActivity::class.java)
            )
        }

        // EDITAR ESTUDIANTE
        listView.setOnItemClickListener { _, _, position, _ ->

            val estudiante = estudiantes[position]

            val intent = Intent(
                this,
                CrearEstudianteActivity::class.java
            )

            intent.putExtra("id_estudiante", estudiante.id)

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarEstudiantes()
    }

    private fun cargarEstudiantes() {

        val resultado = mutableListOf<Estudiante>()

        try {
            BaseDatos(this).abrir().use { db ->

                val consulta = """
                    SELECT
                        e.id_estudiante,
                        u.nombre,
                        u.apellido,
                        u.email,
                        CASE
                            WHEN e.estado = 1 AND u.estado = 1
                            THEN 1
                            ELSE 0
                        END AS estado
                    FROM estudiantes e
                    INNER JOIN usuarios u
                        ON e.usu_id_usuario = u.id_usuario
                    ORDER BY u.apellido, u.nombre
                """.trimIndent()

                db.rawQuery(consulta, null).use { cursor ->

                    while (cursor.moveToNext()) {
                        resultado.add(
                            Estudiante(
                                id = cursor.getLong(0),
                                nombre = cursor.getString(1),
                                apellido = cursor.getString(2),
                                email = cursor.getString(3),
                                estado = cursor.getInt(4)
                            )
                        )
                    }
                }
            }

            estudiantes.clear()
            estudiantes.addAll(resultado)
            adapter.notifyDataSetChanged()

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al cargar estudiantes: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
