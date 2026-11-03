package cl.duoc.sabaterreno.data.local

import androidx.room.TypeConverter
import cl.duoc.sabaterreno.domain.model.EstadoTicket
import cl.duoc.sabaterreno.domain.model.RespuestaPauta
import cl.duoc.sabaterreno.domain.model.ResultadoRonda
import cl.duoc.sabaterreno.domain.model.Rol
import cl.duoc.sabaterreno.domain.model.Severidad

/** Conversión de los tipos del dominio a columnas simples de SQLite. */
class Convertidores {

    @TypeConverter fun severidadATexto(v: Severidad): String = v.name
    @TypeConverter fun textoASeveridad(v: String): Severidad = Severidad.valueOf(v)

    @TypeConverter fun estadoATexto(v: EstadoTicket): String = v.name
    @TypeConverter fun textoAEstado(v: String): EstadoTicket = EstadoTicket.valueOf(v)

    @TypeConverter fun rolATexto(v: Rol): String = v.name
    @TypeConverter fun textoARol(v: String): Rol = Rol.valueOf(v)

    @TypeConverter
    fun listaATexto(v: List<String>): String = v.joinToString(",")

    @TypeConverter
    fun textoALista(v: String): List<String> =
        if (v.isBlank()) emptyList() else v.split(",")

    @TypeConverter
    fun respuestasATexto(v: Map<String, RespuestaPauta>): String =
        v.entries.joinToString(";") { "${it.key}=${it.value.name}" }

    @TypeConverter
    fun textoARespuestas(v: String): Map<String, RespuestaPauta> =
        if (v.isBlank()) emptyMap()
        else v.split(";").mapNotNull { par ->
            val (k, valor) = par.split("=").let { it.getOrNull(0) to it.getOrNull(1) }
            if (k != null && valor != null) k to RespuestaPauta.valueOf(valor) else null
        }.toMap()

    @TypeConverter
    fun rondaATexto(v: Map<String, ResultadoRonda>): String =
        v.entries.joinToString(";") { "${it.key}=${it.value.name}" }

    @TypeConverter
    fun textoARonda(v: String): Map<String, ResultadoRonda> =
        if (v.isBlank()) emptyMap()
        else v.split(";").mapNotNull { par ->
            val partes = par.split("=")
            val k = partes.getOrNull(0)
            val valor = partes.getOrNull(1)
            if (k != null && valor != null) k to ResultadoRonda.valueOf(valor) else null
        }.toMap()
}
