package com.example.sistema_asistencia

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class GenericDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val titulo = intent.getStringExtra("EXTRA_TITULO") ?: "Módulo"
        val descripcion = intent.getStringExtra("EXTRA_DESCRIPCION") ?: "Descripción"

        supportActionBar?.title = titulo

        val tvTituloModulo = findViewById<TextView>(R.id.tvTituloModulo)
        val tvDescripcionModulo = findViewById<TextView>(R.id.tvDescripcionModulo)
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)

        tvTituloModulo.text = titulo
        tvDescripcionModulo.text = descripcion

        btnVolver.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
