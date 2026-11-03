package cl.duoc.sabaterreno.ui.pantallas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.duoc.sabaterreno.data.local.Guias
import cl.duoc.sabaterreno.ui.theme.AmarilloDegradado
import cl.duoc.sabaterreno.ui.theme.RojoFueraServicio

@Composable
fun SoporteScreen(alLlamar: () -> Unit) {
    var abierta by remember { mutableIntStateOf(-1) }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "¿Necesitas ayuda?",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        item {
            Button(
                onClick = alLlamar,
                colors = ButtonDefaults.buttonColors(containerColor = RojoFueraServicio),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                Text("  Llamar a soporte", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
        item {
            Text("Guías rápidas", style = MaterialTheme.typography.titleLarge)
        }
        items(Guias.todas.size) { indice ->
            val guia = Guias.todas[indice]
            Card(
                Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .clickable { abierta = if (abierta == indice) -1 else indice }
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(guia.titulo, style = MaterialTheme.typography.titleMedium)
                    AnimatedVisibility(visible = abierta == indice) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            guia.pasos.forEachIndexed { i, paso ->
                                Text("${i + 1}. $paso", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                guia.escalamiento,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AmarilloDegradado,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
