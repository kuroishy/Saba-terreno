package cl.duoc.sabaterreno.domain.validation

/**
 * Contrato de validación. Vive en el dominio y no conoce la interfaz: devuelve
 * qué campo falló y por qué, y es la pantalla la que decide cómo dibujarlo.
 * Esto es lo que permite probar las reglas sin levantar un Activity.
 */

/** Identifica el campo de un formulario sin acoplarse a ningún componente visual. */
enum class Campo {
    RUT, CLAVE, EQUIPO, FALLA, SEVERIDAD, OBSERVACION, EVIDENCIA, FIRMA, RESOLUCION, ITEMS
}

/** Error de un campo, con un mensaje legible para la persona que lo ve. */
data class ErrorCampo(val campo: Campo, val mensaje: String)

/** Resultado de validar un formulario completo. */
data class ResultadoValidacion(val errores: List<ErrorCampo> = emptyList()) {

    val valido: Boolean get() = errores.isEmpty()

    fun mensajeDe(campo: Campo): String? = errores.firstOrNull { it.campo == campo }?.mensaje

    fun tieneError(campo: Campo): Boolean = errores.any { it.campo == campo }

    companion object {
        val OK = ResultadoValidacion()

        /** Construye el resultado descartando las comprobaciones que pasaron. */
        fun de(vararg posibles: ErrorCampo?) = ResultadoValidacion(posibles.filterNotNull())
    }
}

/** Azúcar para escribir reglas como `condicion exigeQue Campo.X con "mensaje"`. */
internal fun exigir(condicion: Boolean, campo: Campo, mensaje: String): ErrorCampo? =
    if (condicion) null else ErrorCampo(campo, mensaje)
