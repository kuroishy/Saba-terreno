package cl.duoc.sabaterreno.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.sabaterreno.data.local.Catalogo
import cl.duoc.sabaterreno.data.local.SesionStore
import cl.duoc.sabaterreno.data.repo.SabaRepository
import cl.duoc.sabaterreno.domain.rules.Permisos
import cl.duoc.sabaterreno.nativo.Biometria
import cl.duoc.sabaterreno.ui.pantallas.EstadoScreen
import cl.duoc.sabaterreno.ui.pantallas.IncidenciasScreen
import cl.duoc.sabaterreno.ui.pantallas.LoginScreen
import cl.duoc.sabaterreno.ui.pantallas.LoginViewModel
import cl.duoc.sabaterreno.ui.pantallas.MantencionScreen
import cl.duoc.sabaterreno.ui.pantallas.MantencionViewModel
import cl.duoc.sabaterreno.ui.pantallas.NuevoTicketScreen
import cl.duoc.sabaterreno.ui.pantallas.NuevoTicketViewModel
import cl.duoc.sabaterreno.ui.pantallas.RondaScreen
import cl.duoc.sabaterreno.ui.pantallas.RondaViewModel
import cl.duoc.sabaterreno.ui.pantallas.SelectorParking
import cl.duoc.sabaterreno.ui.pantallas.SoporteScreen
import cl.duoc.sabaterreno.ui.pantallas.TicketScreen
import cl.duoc.sabaterreno.ui.pantallas.TicketViewModel

/** Destinos con pestaña en la barra inferior. */
private enum class Seccion(val ruta: String, val etiqueta: String, val icono: ImageVector) {
    ESTADO("estado", "Estado", Icons.Default.Traffic),
    INCIDENCIAS("incidencias", "Incidencias", Icons.Default.Warning),
    SOPORTE("soporte", "Soporte", Icons.Default.SupportAgent),
    MANTENCION("mantencion", "Mantención", Icons.Default.Build)
}

private const val RUTA_NUEVO = "nuevo"
private const val RUTA_TICKET = "ticket/{id}"
private const val RUTA_RONDA = "ronda"

private fun rutaTicket(id: String) = "ticket/$id"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SabaApp(repo: SabaRepository, store: SesionStore) {
    val sesionVm: SesionViewModel = viewModel(factory = SesionViewModel.factory(repo, store))
    val sesion by sesionVm.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Mientras se lee la sesión recordada no se muestra nada: hacerlo
    // provocaría un parpadeo del login antes de restaurarla.
    if (sesion.restaurando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Sin sesión: login y elección de parking.
    val usuario = sesion.usuario
    if (usuario == null) {
        val loginVm: LoginViewModel = viewModel(factory = LoginViewModel.factory(repo))
        LoginScreen(loginVm) { sesionVm.iniciarSesion(it) }
        return
    }
    val parking = sesion.parking
    if (parking == null) {
        val tickets by repo.tickets().collectAsStateWithLifecycle(emptyList())
        SelectorParking(
            nombre = usuario.nombre.substringBefore(' '),
            parkings = sesion.parkingsDisponibles,
            tickets = tickets,
            equiposVisibles = sesion.equiposVisibles(Catalogo.equipos),
            alElegir = sesionVm::elegirParking
        )
        return
    }

    var menuAbierto by remember { mutableStateOf(false) }
    val nav = rememberNavController()
    val entradaActual by nav.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination
    val tickets by repo.ticketsDe(parking.id).collectAsStateWithLifecycle(emptyList())
    val ultimaRonda by repo.ultimaRonda(parking.id).collectAsStateWithLifecycle(null)
    val equipos = sesion.equiposVisibles(Catalogo.equipos)
    val capacidades = sesion.capacidades
    val visibles = tickets.filter { t ->
        Permisos.puedeVer(usuario.rol, t, Catalogo.equipos)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(parking.nombre, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${usuario.nombre} · ${usuario.rol.etiqueta}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (rutaActual?.route in setOf(RUTA_NUEVO, RUTA_TICKET, RUTA_RONDA)) {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        if (sesion.parkingsDisponibles.size > 1) {
                            DropdownMenuItem(
                                text = { Text("Cambiar estacionamiento") },
                                onClick = { menuAbierto = false; sesionVm.cambiarParking() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = { menuAbierto = false; sesionVm.cerrarSesion() }
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (rutaActual?.route !in setOf(RUTA_NUEVO, RUTA_TICKET, RUTA_RONDA)) {
                NavigationBar {
                    Seccion.entries.forEach { seccion ->
                        val seleccionada = rutaActual?.hierarchy?.any { it.route == seccion.ruta } == true
                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = {
                                nav.navigate(seccion.ruta) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                val abiertas = visibles.count { it.abierto }
                                if (seccion == Seccion.INCIDENCIAS && abiertas > 0) {
                                    BadgedBox(badge = { Badge { Text("$abiertas") } }) {
                                        Icon(seccion.icono, contentDescription = null)
                                    }
                                } else {
                                    Icon(seccion.icono, contentDescription = null)
                                }
                            },
                            label = { Text(seccion.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { relleno ->
        Box(Modifier.fillMaxSize().padding(relleno)) {
            NavHost(
                navController = nav,
                startDestination = Seccion.ESTADO.ruta,
                // Transición compartida entre pestañas: fluidez, no decoración.
                enterTransition = { fadeIn(tween(220)) },
                exitTransition = { fadeOut(tween(180)) }
            ) {
                composable(Seccion.ESTADO.ruta) {
                    EstadoScreen(
                        tickets = visibles,
                        parkingId = parking.id,
                        equiposVisibles = equipos,
                        puedeReportar = capacidades?.crearTicket == true,
                        ultimaRonda = ultimaRonda,
                        alReportar = { nav.navigate(RUTA_NUEVO) },
                        alIniciarRonda = { nav.navigate(RUTA_RONDA) },
                        alAbrirEquipo = { equipo ->
                            // Si el equipo ya tiene una incidencia abierta se
                            // muestra esa, en vez de duplicar el reporte.
                            val abierto = visibles.firstOrNull { it.equipoId == equipo.id && it.abierto }
                            if (abierto != null) nav.navigate(rutaTicket(abierto.id))
                            else nav.navigate(RUTA_NUEVO)
                        }
                    )
                }
                composable(Seccion.INCIDENCIAS.ruta) {
                    IncidenciasScreen(tickets = visibles, alAbrir = { nav.navigate(rutaTicket(it.id)) })
                }
                composable(Seccion.SOPORTE.ruta) {
                    SoporteScreen(
                        alLlamar = {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:+56200000000"))
                            )
                        }
                    )
                }
                composable(Seccion.MANTENCION.ruta) {
                    val mantVm: MantencionViewModel = viewModel(
                        factory = MantencionViewModel.factory(
                            repo = repo,
                            parkingId = parking.id,
                            puedeRegistrar = capacidades?.registrarMantencion == true
                        ),
                        key = parking.id
                    )
                    val activity = context as FragmentActivity
                    MantencionScreen(
                        vm = mantVm,
                        autor = usuario.nombre,
                        alPedirFirma = {
                            Biometria.firmar(
                                activity = activity,
                                titulo = "Firmar pauta de mantención",
                                subtitulo = "Confirma tu identidad para dejar constancia",
                                alExito = mantVm::firmaConfirmada,
                                alFallar = mantVm::firmaRechazada
                            )
                        }
                    )
                }
                composable(
                    RUTA_RONDA,
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(260))
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(220))
                    }
                ) {
                    val rondaVm: RondaViewModel =
                        viewModel(factory = RondaViewModel.factory(repo, equipos))
                    RondaScreen(
                        vm = rondaVm,
                        parkingId = parking.id,
                        autor = usuario.nombre,
                        alReportar = { nav.navigate(RUTA_NUEVO) },
                        alTerminar = { nav.popBackStack() }
                    )
                }
                composable(
                    RUTA_TICKET,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(260))
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(220))
                    }
                ) { entrada ->
                    val id = entrada.arguments?.getString("id").orEmpty()
                    val ticketVm: TicketViewModel =
                        viewModel(factory = TicketViewModel.factory(repo, id), key = id)
                    TicketScreen(
                        vm = ticketVm,
                        capacidades = capacidades ?: Permisos.de(usuario.rol),
                        autor = usuario.nombre
                    )
                }
                composable(
                    RUTA_NUEVO,
                    enterTransition = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(260))
                    },
                    exitTransition = {
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(220))
                    }
                ) {
                    val nuevoVm: NuevoTicketViewModel =
                        viewModel(factory = NuevoTicketViewModel.factory(repo))
                    NuevoTicketScreen(
                        vm = nuevoVm,
                        equiposVisibles = equipos,
                        parkingId = parking.id,
                        autor = usuario.nombre,
                        alCrear = { nav.popBackStack() }
                    )
                }
            }
        }
    }
}
