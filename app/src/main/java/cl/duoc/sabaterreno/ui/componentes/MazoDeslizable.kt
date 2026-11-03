package cl.duoc.sabaterreno.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.launch

/** Hacia qué lado se descartó la tarjeta. */
enum class Lado { IZQUIERDA, DERECHA }

/**
 * Mazo de tarjetas que se descartan arrastrando a izquierda o derecha.
 *
 * El gesto es la forma natural de revisar una lista de equipos caminando por
 * el estacionamiento con una mano ocupada, pero **no es la única**: la pantalla
 * que lo usa ofrece además botones, y cada tarjeta expone su acción por
 * accesibilidad. El arrastre acompaña con rotación y con un sello que aparece
 * según la dirección, para que se entienda qué va a pasar antes de soltar.
 *
 * @param indice tarjeta visible actualmente; al cambiar desde fuera (deshacer)
 *               la posición se reinicia sola.
 * @param forzar cuando la pantalla pide descartar con un botón en vez del dedo.
 */
@Composable
fun <T> MazoDeslizable(
    elementos: List<T>,
    indice: Int,
    etiquetaIzquierda: String,
    etiquetaDerecha: String,
    colorIzquierda: Color,
    colorDerecha: Color,
    alDescartar: (indice: Int, lado: Lado) -> Unit,
    modifier: Modifier = Modifier,
    forzar: Lado? = null,
    forzado: () -> Unit = {},
    contenido: @Composable BoxScope.(T) -> Unit
) {
    if (indice >= elementos.size) return

    val alcance = rememberCoroutineScope()
    val densidad = LocalDensity.current
    val anchoDescarte = with(densidad) { 420.dp.toPx() }
    val umbral = with(densidad) { 110.dp.toPx() }

    // Un Animatable por tarjeta: al avanzar el índice, la nueva entra centrada.
    val desplazamiento = remember(indice) { Animatable(0f) }

    // El Animatable sólo se actualiza dentro de una corrutina, así que al
    // soltar el dedo su valor todavía puede ir atrasado. Este acumulador lleva
    // el arrastre de forma síncrona y es el que decide si se supera el umbral.
    var acumulado by remember(indice) { mutableFloatStateOf(0f) }

    // Descarte pedido por botón: misma animación que el gesto.
    LaunchedEffect(forzar, indice) {
        val lado = forzar ?: return@LaunchedEffect
        val destino = if (lado == Lado.DERECHA) anchoDescarte else -anchoDescarte
        acumulado = destino
        desplazamiento.animateTo(destino, tween(240))
        alDescartar(indice, lado)
        forzado()
    }

    Box(modifier.fillMaxWidth()) {
        // La tarjeta siguiente asoma detrás para dar sensación de pila.
        elementos.getOrNull(indice + 1)?.let { siguiente ->
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 0.93f
                        scaleY = 0.93f
                        translationY = 28f
                        alpha = 0.75f
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) { contenido(siguiente) }
        }

        val actual = elementos[indice]
        val x = desplazamiento.value
        val progreso = (abs(x) / umbral).coerceIn(0f, 1f)

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = x
                    rotationZ = x / 36f
                }
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
                .pointerInput(indice) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val recorrido = acumulado
                            alcance.launch {
                                if (abs(recorrido) > umbral) {
                                    val lado = if (recorrido > 0) Lado.DERECHA else Lado.IZQUIERDA
                                    val destino = if (lado == Lado.DERECHA) anchoDescarte else -anchoDescarte
                                    desplazamiento.animateTo(destino, tween(200))
                                    alDescartar(indice, lado)
                                } else {
                                    // No alcanzó el umbral: vuelve a su sitio.
                                    acumulado = 0f
                                    desplazamiento.animateTo(0f, tween(220))
                                }
                            }
                        },
                        onDragCancel = {
                            acumulado = 0f
                            alcance.launch { desplazamiento.animateTo(0f, tween(220)) }
                        },
                        onHorizontalDrag = { _, delta ->
                            acumulado += delta
                            alcance.launch { desplazamiento.snapTo(acumulado) }
                        }
                    )
                }
                .semantics {
                    contentDescription =
                        "Tarjeta ${indice + 1} de ${elementos.size}. " +
                        "Desliza a la derecha para $etiquetaDerecha, a la izquierda para $etiquetaIzquierda."
                },
            contentAlignment = Alignment.Center
        ) {
            contenido(actual)

            // Sellos: aparecen a medida que el arrastre se acerca al umbral.
            Sello(
                texto = etiquetaDerecha,
                color = colorDerecha,
                rotacion = -12f,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
                    .alpha(if (x > 0) progreso else 0f)
            )
            Sello(
                texto = etiquetaIzquierda,
                color = colorIzquierda,
                rotacion = 12f,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .alpha(if (x < 0) progreso else 0f)
            )
        }
    }
}

@Composable
private fun Sello(texto: String, color: Color, rotacion: Float, modifier: Modifier = Modifier) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.titleLarge,
        color = color,
        modifier = modifier
            .graphicsLayer { rotationZ = rotacion }
            .border(4.dp, color, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 5.dp)
    )
}
