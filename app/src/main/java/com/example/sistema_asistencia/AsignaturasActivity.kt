
package com.example.sistema_asistencia

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class AsignaturasActivity : AppCompatActivity() {

    private data class Asignatura(
        val id: Long,
        val codigo: String,
        val nombre: String,
        val estado: Int
    ) {
        override fun toString(): String {
            val textoEstado = if (estado == 1) {
                "Activa"
            } else {
                "Inactiva"
            }

            return "$nombre ($codigo)\nEstado: $textoEstado"
        }
    }

    private val asignaturas = mutableListOf<Asignatura>()
    private lateinit var adapter: ArrayAdapter<Asignatura>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignaturas)

        // Componentes del XML
        val listView = findViewById<ListView>(
            R.id.listViewAsignaturas
        )

        val tvSinAsignaturas = findViewById<TextView>(
            R.id.tvSinAsignaturas
        )

        // Abrir formulario para editar una asignatura
        listView.setOnItemClickListener { _, _, position, _ ->

            // Obtener la asignatura seleccionada
            val asignatura = asignaturas[position]

            // Abrir el formulario
            val intent = Intent(
                this,
                CrearAsignaturaActivity::class.java
            )

            // Enviar el ID de la asignatura
            intent.putExtra("id_asignatura", asignatura.id)

            startActivity(intent)
        }

        val btnNuevaAsignatura = findViewById<MaterialButton>(
            R.id.btnNuevaAsignatura
        )

        // Configurar lista
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            asignaturas
        )

        listView.adapter = adapter
        listView.emptyView = tvSinAsignaturas

        // Abrir pantalla de nueva asignatura
        btnNuevaAsignatura.setOnClickListener {
            val intent = Intent(
                this,
                CrearAsignaturaActivity::class.java
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarAsignaturas()
    }

    // ==================================
    // CONSULTAR ASIGNATURAS EN SQLITE
    // ==================================

    private fun cargarAsignaturas() {

        val resultado = mutableListOf<Asignatura>()

        try {
            BaseDatos(this).abrir().use { db ->

                val consulta = """
                    SELECT
                        id_asignatura,
                        codigo,
                        nombre,
                        estado
                    FROM asignaturas
                    ORDER BY nombre
                """.trimIndent()

                db.rawQuery(consulta, null).use { cursor ->

                    val colId = cursor.getColumnIndexOrThrow(
                        "id_asignatura"
                    )

                    val colCodigo = cursor.getColumnIndexOrThrow(
                        "codigo"
                    )

                    val colNombre = cursor.getColumnIndexOrThrow(
                        "nombre"
                    )

                    val colEstado = cursor.getColumnIndexOrThrow(
                        "estado"
                    )

                    while (cursor.moveToNext()) {

                        resultado.add(
                            Asignatura(
                                id = cursor.getLong(colId),
                                codigo = cursor.getString(colCodigo),
                                nombre = cursor.getString(colNombre),
                                estado = cursor.getInt(colEstado)
                            )
                        )
                    }
                }
            }

            asignaturas.clear()
            asignaturas.addAll(resultado)
            adapter.notifyDataSetChanged()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Error al cargar asignaturas: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}