
package com.example.sistema_asistencia

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DocentesActivity : AppCompatActivity() {

    private data class Docente(
        val id: Long,
        val nombre: String,
        val apellido: String,
        val email: String,
        val estado: Int
    ) {
        override fun toString(): String {
            val estadoTexto =
                if (estado == 1) "Activo" else "Inactivo"

            return "$nombre $apellido\n$email\nEstado: $estadoTexto"
        }
    }

    private val docentes = mutableListOf<Docente>()
    private lateinit var adapter: ArrayAdapter<Docente>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_docentes)

        val listView = findViewById<ListView>(
            R.id.listViewDocentes
        )

        val tvSinDocentes = findViewById<TextView>(
            R.id.tvSinDocentes
        )

        val btnNuevo = findViewById<MaterialButton>(
            R.id.btnNuevoDocente
        )

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            docentes
        )

        listView.adapter = adapter
        listView.emptyView = tvSinDocentes

        // CREAR
        btnNuevo.setOnClickListener {
            startActivity(
                Intent(this, CrearDocenteActivity::class.java)
            )
        }

        // EDITAR
        listView.setOnItemClickListener { _, _, position, _ ->

            val docente = docentes[position]

            val intent = Intent(
                this,
                CrearDocenteActivity::class.java
            )

            intent.putExtra("id_docente", docente.id)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDocentes()
    }

    // ==================================
    // SELECT DE DOCENTES
    // ==================================

    private fun cargarDocentes() {

        val resultado = mutableListOf<Docente>()

        try {
            BaseDatos(this).abrir().use { db ->

                val consulta = """
                    SELECT
                        d.id_docente,
                        u.nombre,
                        u.apellido,
                        u.email,
                        CASE
                            WHEN d.estado = 1 AND u.estado = 1
                            THEN 1
                            ELSE 0
                        END AS estado
                    FROM docentes d
                    INNER JOIN usuarios u
                        ON d.usu_id_usuario = u.id_usuario
                    ORDER BY u.apellido, u.nombre
                """.trimIndent()

                db.rawQuery(consulta, null).use { cursor ->

                    while (cursor.moveToNext()) {

                        resultado.add(
                            Docente(
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

            docentes.clear()
            docentes.addAll(resultado)
            adapter.notifyDataSetChanged()

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al cargar docentes: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
