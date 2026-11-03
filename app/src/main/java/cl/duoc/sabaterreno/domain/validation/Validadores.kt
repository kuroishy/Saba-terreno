package cl.duoc.sabaterreno.domain.validation

import cl.duoc.sabaterreno.domain.model.RespuestaPauta
import cl.duoc.sabaterreno.domain.model.Severidad

/**
 * Validadores de cada formulario. Son objetos sin estado que reciben los datos
 * del formulario y devuelven un [ResultadoValidacion]. No importan nada de
 * Android: la pantalla sólo consume el resultado.
 */

object LoginValidator {

    private const val LARGO_MINIMO_CLAVE = 6

    fun validar(rut: String, clave: String) = ResultadoValidacion.de(
        exigir(rut.isNotBlank(), Campo.RUT, "Ingresa tu RUT."),
        if (rut.isBlank()) null
        else exigir(Rut.esValido(rut), Campo.RUT, "El RUT no es válido. Revisa el dígito verificador."),
        exigir(clave.isNotBlank(), Campo.CLAVE, "Ingresa tu contraseña."),
        if (clave.isBlank()) null
        else exigir(
            clave.length >= LARGO_MINIMO_CLAVE,
            Campo.CLAVE,
            "La contraseña tiene al menos $LARGO_MINIMO_CLAVE caracteres."
        )
    )
}

object TicketValidator {

    const val OBSERVACION_MINIMA_CRITICA = 15

    /**
     * Reglas del reporte de falla. Dos reglas de negocio importantes:
     * una falla fuera de servicio exige foto de evidencia, y exige una
     * observación con contexto suficiente para que el SAU pueda actuar.
     */
    fun validar(
        equipoId: String?,
        falla: String?,
        severidad: Severidad?,
        observacion: String,
        fotoUri: String?
    ): ResultadoValidacion {
        val critica = severidad == Severidad.FUERA_DE_SERVICIO
        return ResultadoValidacion.de(
            exigir(!equipoId.isNullOrBlank(), Campo.EQUIPO, "Elige el equipo afectado."),
            exigir(!falla.isNullOrBlank(), Campo.FALLA, "Indica qué falla presenta el equipo."),
            exigir(severidad != null, Campo.SEVERIDAD, "Indica cómo quedó el servicio."),
            exigir(
                !critica || fotoUri != null,
                Campo.EVIDENCIA,
                "Una falla fuera de servicio necesita una foto de evidencia."
            ),
            exigir(
                !critica || observacion.trim().length >= OBSERVACION_MINIMA_CRITICA,
                Campo.OBSERVACION,
                "Describe la situación con al menos $OBSERVACION_MINIMA_CRITICA caracteres."
            )
        )
    }
}

object CierreValidator {

    const val RESOLUCION_MINIMA = 10

    fun validar(resolucion: String) = ResultadoValidacion.de(
        exigir(resolucion.isNotBlank(), Campo.RESOLUCION, "Escribe cómo se resolvió."),
        if (resolucion.isBlank()) null
        else exigir(
            resolucion.trim().length >= RESOLUCION_MINIMA,
            Campo.RESOLUCION,
            "Detalla la solución con al menos $RESOLUCION_MINIMA caracteres."
        )
    )
}

object PautaValidator {

    fun validar(
        totalItems: Int,
        respuestas: Map<String, RespuestaPauta>,
        observacion: String,
        firmada: Boolean
    ): ResultadoValidacion {
        val hayObservados = respuestas.values.any { it == RespuestaPauta.OBSERVADO }
        return ResultadoValidacion.de(
            exigir(
                respuestas.size == totalItems,
                Campo.ITEMS,
                "Marca cada ítem como conforme u observado."
            ),
            exigir(
                !hayObservados || observacion.isNotBlank(),
                Campo.OBSERVACION,
                "Detalla qué observaste en los ítems marcados."
            ),
            exigir(firmada, Campo.FIRMA, "Firma la pauta con tu huella para guardarla.")
        )
    }
}
