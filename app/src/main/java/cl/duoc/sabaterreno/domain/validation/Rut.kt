package cl.duoc.sabaterreno.domain.validation

/**
 * Validación de RUT chileno con dígito verificador (módulo 11).
 * Kotlin puro y sin dependencias: se prueba con tests unitarios.
 */
object Rut {

    /** Deja solo dígitos y la K final, en mayúscula. */
    fun normalizar(entrada: String): String =
        entrada.filter { it.isDigit() || it == 'k' || it == 'K' }.uppercase()

    /** Calcula el dígito verificador de un cuerpo numérico. */
    fun digitoVerificador(cuerpo: String): Char {
        var suma = 0
        var factor = 2
        for (c in cuerpo.reversed()) {
            suma += Character.getNumericValue(c) * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        return when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
    }

    /** true si el RUT tiene largo razonable y su dígito verificador cuadra. */
    fun esValido(entrada: String): Boolean {
        val limpio = normalizar(entrada)
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (cuerpo.length !in 7..8 || !cuerpo.all { it.isDigit() }) return false
        return digitoVerificador(cuerpo) == dv
    }

    /** Formatea 15420890K como 15.420.890-K. Devuelve la entrada si no es válida. */
    fun formatear(entrada: String): String {
        val limpio = normalizar(entrada)
        if (limpio.length < 2) return entrada
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$dv"
    }
}
