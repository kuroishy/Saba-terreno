# Saba Terreno

Aplicación móvil Android para la gestión de incidencias y continuidad
operacional en una red de estacionamientos.

**Asignatura:** DSY1105 — Desarrollo de Aplicaciones Móviles
**Evaluación:** Parcial 2 — Encargo (entrega en parejas)
**Equipo:** _(completar número de equipo)_

| Integrante | RUT | Rol en el equipo |
|---|---|---|
| _(completar nombre y apellido)_ | | |
| _(completar nombre y apellido)_ | | |

---

## El problema

En un estacionamiento, cuando una barrera no sube o un cajero se traga los
billetes, el operador de turno pierde tiempo buscando a quién avisar y el
registro de la falla queda en un cuaderno o en un grupo de WhatsApp. No hay
trazabilidad, y la gerencia no tiene forma de ver el estado real de la red.

Saba Terreno resuelve eso: el operador reporta la falla desde el teléfono con
evidencia fotográfica, el ticket queda con historial, y cada parking muestra un
semáforo de estado que se calcula solo a partir de los tickets abiertos.

## Funcionalidades implementadas

- **Autenticación por RUT y contraseña**, con validación real de dígito
  verificador (módulo 11) y contraseñas almacenadas como hash SHA-256.
- **Cuatro roles con permisos distintos** (Operación terreno, IT, Gerencia,
  Proveedor externo). El proveedor externo no tiene visibilidad sobre los
  cajeros, porque manejan dinero.
- **Semáforo operacional por equipo y por parking**, calculado como la peor
  severidad entre los tickets abiertos de los equipos visibles para ese rol.
- **Reporte de incidencias** con catálogo de fallas según el tipo de equipo,
  severidad, comentario y foto de evidencia. Una falla *fuera de servicio*
  exige foto y una descripción con contexto suficiente.
- **Listado de incidencias** con filtro entre abiertas y cerradas.
- **Detalle de la incidencia** con la línea de pasos de su ciclo de vida
  (detección, reporte, escalado o intervención, cierre), la evidencia
  fotográfica y el historial completo de eventos. Según el rol permite
  registrar la intervención, agregar notas y cerrar la incidencia con una
  resolución validada. Al cerrarla, el semáforo del parking se recalcula solo.
- **Guías de contingencia** con pasos y criterio de escalamiento al SAU.
- **Ronda de turno** con tarjetas que se deslizan: se recorre equipo por
  equipo marcando conforme u observado, con deshacer, y al terminar se ofrece
  reportar cada equipo observado. El gesto no es obligatorio: los mismos
  resultados están disponibles con botones y descritos para accesibilidad.
- **Pauta de mantención semanal** firmada con huella digital (o el PIN del
  teléfono como alternativa). Se registra una sola vez por semana y queda el
  historial de semanas anteriores; los perfiles que sólo consultan ven el
  cumplimiento sin poder registrarlo.
- **La sesión se recuerda** entre aperturas: al volver a abrir la app se
  retoma el parking en el que se estaba trabajando, sin pasar por el login.
- **Persistencia local** completa: los tickets, su historial, las mantenciones
  y las fotos sobreviven al cierre de la aplicación.

## Arquitectura

El proyecto sigue **MVVM** con tres capas separadas por paquete:

```
cl.duoc.sabaterreno
├── domain/                 Kotlin puro, sin dependencias de Android
│   ├── model/              Modelos y catálogos del negocio
│   ├── rules/              Semáforo y permisos por rol
│   └── validation/         RUT módulo 11 y validadores de formulario
├── data/
│   ├── local/              Room: entidades, DAOs, conversores, semilla
│   └── repo/               Repositorio y mapeo entidad ↔ dominio
├── ui/
│   ├── theme/              Paleta y tipografía
│   ├── componentes/        Componentes reutilizables
│   └── pantallas/          Pantallas Compose y sus ViewModel
└── nativo/                 Cámara (CameraX) y biometría
```

**La validación vive en el dominio, no en la interfaz.** Los validadores son
objetos sin estado que reciben los datos del formulario y devuelven un
`ResultadoValidacion` con la lista de campos que fallaron. El ViewModel sólo
orquesta: pide la validación y expone el resultado en su `UiState`. La pantalla
decide cómo dibujar el error. Por eso las reglas se prueban sin levantar un
`Activity`:

```kotlin
val r = TicketValidator.validar(
    equipoId = "barrera-out",
    falla = "Brazo no sube",
    severidad = Severidad.FUERA_DE_SERVICIO,
    observacion = "corta",
    fotoUri = null
)
assertFalse(r.valido)                      // falta evidencia y detalle
assertNotNull(r.mensajeDe(Campo.EVIDENCIA))
```

El flujo de datos es unidireccional: Room emite `Flow`, el repositorio los
traduce a modelos de dominio, el ViewModel los expone como `StateFlow` y
Compose se recompone solo.

## Recursos nativos

| Recurso | Dónde se usa | Manejo seguro |
|---|---|---|
| **Cámara** (CameraX) | Evidencia fotográfica del ticket | Permiso en runtime pedido en el momento de usarla, no al abrir la app. Las fotos se guardan en el almacenamiento **interno** (`filesDir/evidencias`), no en la galería: son evidencia operacional. |
| **Biometría** (BiometricPrompt) | Firma de la pauta de mantención | Sólo la confirmación del sistema marca la pauta como firmada. Si no hay hardware o huella inscrita, se explica el motivo en vez de fallar en silencio. |

Ambos degradan con elegancia: si la persona rechaza el permiso, el formulario
sigue siendo usable y sólo se le explica qué no podrá hacer.

## Pruebas

35 pruebas unitarias sobre el dominio y el estado de la ronda:

```bash
./gradlew test
```

Cubren el módulo 11 del RUT (dígito numérico, K y 0), las reglas de cada
validador, y el cálculo del semáforo — incluida una prueba de regresión que
verifica que un proveedor externo no ve un parking en rojo por un ticket de
cajero que no tiene permiso de ver.

## Cómo ejecutar

**Requisitos:** Android Studio Rabbit 2 (2026.2.2) o superior, JDK 17+,
Android SDK con API 37.

1. Clonar el repositorio y abrir la carpeta en Android Studio.
2. Esperar la sincronización de Gradle (descarga las dependencias).
3. Elegir un emulador con API 26 o superior, o conectar un dispositivo físico.
4. Ejecutar con **Run ▸ app**.

Desde la terminal:

```bash
./gradlew assembleDebug     # genera el APK de depuración
./gradlew installDebug      # lo instala en el dispositivo conectado
./gradlew test              # ejecuta las pruebas unitarias
```

### Perfiles de prueba

Los datos son ficticios. Los RUT son válidos según módulo 11 para que pasen la
validación real de la aplicación.

| Perfil | RUT | Contraseña |
|---|---|---|
| Operación terreno | 15.420.895-K | terreno2026 |
| Tecnología e IT | 10.234.577-0 | tisaba2026 |
| Gestión y dirección | 98.765.432-5 | gestion2026 |
| Proveedor externo | 13.456.789-9 | soporte2026 |

> Para probar la biometría, el emulador necesita una huella inscrita:
> *Settings ▸ Security ▸ Fingerprint*, y luego `adb -e emu finger touch 1`
> cuando la aplicación pida la huella.

## Stack

Kotlin · Jetpack Compose (Material 3) · Navigation Compose · Room · DataStore ·
CameraX · AndroidX Biometric · Gradle 9.8.2 con AGP 9.5.0
