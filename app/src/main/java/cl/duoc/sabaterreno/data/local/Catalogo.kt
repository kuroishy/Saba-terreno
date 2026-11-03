package cl.duoc.sabaterreno.data.local

import cl.duoc.sabaterreno.domain.model.Equipo
import cl.duoc.sabaterreno.domain.model.ItemPauta
import cl.duoc.sabaterreno.domain.model.TipoEquipo

/**
 * Catálogos fijos del negocio: la dotación de equipos es igual en todos los
 * parkings de la red, y la pauta semanal está definida por operaciones.
 */
object Catalogo {

    val equipos = listOf(
        Equipo("barrera-in", "Barreras Entrada", TipoEquipo.BARRERA),
        Equipo("barrera-out", "Barreras Salida", TipoEquipo.BARRERA),
        Equipo("lpr", "Lectura LPR", TipoEquipo.LPR),
        Equipo("pos", "Terminales POS", TipoEquipo.POS),
        Equipo("cajero1", "Cajero N° 1", TipoEquipo.CAJERO),
        Equipo("cajero2", "Cajero N° 2", TipoEquipo.CAJERO)
    )

    fun equipo(id: String): Equipo? = equipos.firstOrNull { it.id == id }

    val itemsPauta = listOf(
        ItemPauta("lpr", "LPR", "Limpieza lentes LPR"),
        ItemPauta("goma", "Barreras", "Prueba goma barrera"),
        ItemPauta("pos", "POS", "Verificación papel POS"),
        ItemPauta("caj", "Cajeros", "Revisión de billetes y rollo de boletas"),
        ItemPauta("sen", "Accesos", "Señalética y luminaria de accesos")
    )
}

/** Guía de contingencia que la app muestra en Soporte. */
data class Guia(val titulo: String, val pasos: List<String>, val escalamiento: String)

object Guias {
    val todas = listOf(
        Guia(
            "Protocolo reinicio de cajeros",
            listOf(
                "Avisa al jefe de turno y activa el cobro manual si hay vehículos en cola.",
                "Revisa que no haya billetes atascados y que la bandeja de vuelto tenga monedas y billetes.",
                "Apaga el cajero desde el interruptor trasero, espera 30 segundos y vuelve a encenderlo.",
                "Espera la pantalla principal y haz una transacción de prueba.",
                "Si la falla sigue, reporta un ticket con foto y llama a soporte."
            ),
            "Escala al SAU si el cajero no vuelve en 10 minutos o si hay dinero retenido."
        ),
        Guia(
            "Contingencia barreras abiertas",
            listOf(
                "Deja a una persona en la salida para controlar el flujo de vehículos.",
                "Cobra con el terminal POS portátil o con el cajero más cercano y registra cada ticket.",
                "Anota la hora de inicio de la contingencia en las observaciones del ticket.",
                "Reporta la falla con foto del equipo y márcala como fuera de servicio.",
                "Cuando soporte confirme la reparación, prueba subida y bajada antes de retomar el flujo."
            ),
            "Escala de inmediato: una barrera abierta implica pérdida de control de acceso."
        ),
        Guia(
            "Lectura LPR intermitente",
            listOf(
                "Revisa si el lente tiene polvo, agua o reflejos y límpialo con paño seco.",
                "Verifica que la cámara no se haya movido de su posición.",
                "Prueba con tres vehículos distintos y anota cuántas patentes no se leen.",
                "Si el problema sigue, reporta un ticket degradado con evidencia del paso de un vehículo."
            ),
            "Escala si fallan más de 3 de 10 lecturas o si la cámara no muestra imagen."
        ),
        Guia(
            "Terminal POS sin conexión",
            listOf(
                "Revisa que el terminal tenga corriente y que el cable de red esté firme.",
                "Reinicia el terminal manteniendo el botón de encendido por 5 segundos.",
                "Prueba una transacción de bajo monto con una tarjeta de prueba.",
                "Mientras tanto, dirige los pagos a otro terminal o al cajero.",
                "Reporta el ticket con foto de la pantalla del terminal."
            ),
            "Escala si ningún terminal del parking puede procesar pagos."
        )
    )
}
