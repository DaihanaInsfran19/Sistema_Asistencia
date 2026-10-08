
package com.example.sistema_asistencia

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PasswordUtils {

    private const val ITERACIONES = 310000

    fun generarHash(password: String): String {

        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val mac = Mac.getInstance("HmacSHA256")

        val clave = SecretKeySpec(
            password.toByteArray(Charsets.UTF_8),
            "HmacSHA256"
        )

        mac.init(clave)

        val primerBloque = salt + byteArrayOf(0, 0, 0, 1)

        var u = mac.doFinal(primerBloque)
        val resultado = u.copyOf()

        repeat(ITERACIONES - 1) {
            u = mac.doFinal(u)

            for (i in resultado.indices) {
                resultado[i] = (
                        resultado[i].toInt() xor u[i].toInt()
                        ).toByte()
            }
        }

        val saltBase64 = Base64.encodeToString(
            salt,
            Base64.NO_WRAP
        )

        val hashBase64 = Base64.encodeToString(
            resultado,
            Base64.NO_WRAP
        )

        return "pbkdf2_sha256" +
                "$" + ITERACIONES +
                "$" + saltBase64 +
                "$" + hashBase64
    }
}
