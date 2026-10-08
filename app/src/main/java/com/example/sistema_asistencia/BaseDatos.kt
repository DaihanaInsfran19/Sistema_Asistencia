package com.example.sistema_asistencia

import android.content.Context
import android.database.sqlite.SQLiteDatabase

/**
 * Abre una copia privada de la base SQLite ya existente.
 * El archivo original debe estar en app/src/main/assets/bd_app_asistencia.db.
 */
class BaseDatos(private val context: Context) {

    private val nombreBD = "bd_app_asistencia.db"

    fun abrir(): SQLiteDatabase {
        val archivo = context.getDatabasePath(nombreBD)

        // Solo se copia en la primera ejecución de la aplicación.
        if (!archivo.exists()) {
            archivo.parentFile?.mkdirs()
            context.assets.open(nombreBD).use { origen ->
                archivo.outputStream().use { destino ->
                    origen.copyTo(destino)
                }
            }
        }

        return SQLiteDatabase.openDatabase(
            archivo.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE
        ).apply {
            // Necesario por conexión para respetar las FK definidas en SQLite.
            setForeignKeyConstraintsEnabled(true)
        }
    }
}
