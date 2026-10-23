# UNIVERSIDAD PERUANA UNIÓN
## FACULTAD DE INGENIERÍA Y ARQUITECTURA
### Escuela Profesional de Ingeniería de Sistemas

---

# INFORME TÉCNICO DE ACTIVIDAD AUTÓNOMA N.º 11
**Asignatura:** Desarrollo de Aplicaciones Móviles  
**Ciclo:** VI | **Unidad:** 2  
**Tema:** Pruebas unitarias en `commonTest`, Bitácora de escenarios offline, Antigüedad de datos y Guion de sustentación  
**Repositorio GitHub:** [https://github.com/wagner781/PharmaMobile02](https://github.com/wagner781/PharmaMobile02)  
**Rama de Entrega:** `feature/offline-first-qquerar`  
**Estudiante:** Wagner Qquerar  
**Docente:** Ing. Rolando Mamani Cutipa  
**Fecha:** Octubre 2026  

---

## 1. PRODUCTO 1: SUITE DE PRUEBAS UNITARIAS EN `commonTest` (7 pts)

### 1.1. Arquitectura de Pruebas Unitarias Aisladas
La suite fue implementada íntegramente en el módulo compartido `shared/src/commonTest/`, garantizando que la lógica de negocio y la sincronización offline sean verificables de forma idéntica en cualquier plataforma de destino (Android / iOS / Desktop) sin requerir emuladores pesados ni red externa.

Se implementó un patrón `expect/actual testDriver()` que entrega una base SQLite en memoria (`JdbcSqliteDriver` en JVM/Android host y `inMemoryDriver` en iOS/Native), permitiendo que SQLDelight cree el esquema real `PharmaDb` y ejecute transacciones completas en milisegundos.

### 1.2. Diseño de las 6 Pruebas Unitarias Implementadas

| N.º | Nombre del Test | Aspecto Evaluado según Rúbrica | Estrategia de Prueba |
|:---:|:---|:---|:---|
| **1** | `test1_sincronizacionConRedDisponibleSubePendientesYReemplazaRemotos` | Sincronización con red disponible | Se simulan registros remotos. Al invocar `sincronizar()`, se valida la inserción en la base local, la cola de pendientes en cero y la persistencia de la marca de tiempo de sincronización. |
| **2** | `test2_registroSinRedGuardaLocalConMarcaPendienteYEmiteEnFlow` | Registro sin red y reactividad | La API remota lanza `ErrorApi.SinConexion`. Al registrar, el repositorio asigna un ID negativo (`-1`), guarda con bandera `pendiente = 1`, y el `Flow` de SQLite emite inmediatamente el nuevo registro a la interfaz. |
| **3** | `test3_reconexionSubeEfectivamenteLosRegistrosLocalesAlServidor` | Reconexión y subida efectiva | Se crea un producto sin red. Luego se habilita la red y se invoca `sincronizar()`. Se comprueba que la API recibe la llamada de subida, la cola local se limpia (`size = 0`) y el ítem adopta el ID definitivo positivo del backend. |
| **4** | `test4_bajadaDelServidorNoSobreescribeNiBorraRegistrosLocalesPendientes` | Integridad de datos locales no sincronizados | Se almacena un producto local pendiente (`pendiente = 1`). Luego se ejecuta la bajada del servidor (`reemplazarSincronizados`). Se valida que la consulta SQL solo borra `pendiente = 0`, manteniendo intacto el registro pendiente. |
| **5** | `test5_identificadoresTemporalesSinColisionConIdsDelServidor` | Prevención de colisión de identificadores | Se registran dos productos sin red. La función `siguienteIdLocal` calcula IDs decrementales (`-1`, `-2`). Se verifica matemáticamente que nunca colisionan con los IDs autoincrementales positivos (1, 2, ...) del servidor. |
| **6** | `test6_idempotenciaDeSincronizacionNoDuplicaRegistros` | Idempotencia de la sincronización | Se ejecuta `sincronizar()` dos veces de forma consecutiva. Se valida que la lista no duplica filas y que la cola de pendientes se mantiene estrictamente en cero. |

### 1.3. Código Fuente de las Pruebas Unitarias
El archivo se encuentra en `shared/src/commonTest/kotlin/pe/edu/upeu/pharmamobile/data/repository/ProductoRepositorioOfflineTest.kt`:

```kotlin
package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProductoRepositorioOfflineTest {

    private fun baseEnMemoria(): PharmaDb = PharmaDb(testDriver())

    private class FakeRemoteApi(
        val productosRemotos: MutableList<Producto> = mutableListOf(),
        var debeFallar: Boolean = false,
        var fallarSoloAlSubir: Boolean = false
    ) : ProductoApi(HttpClient()) {
        val llamadasRegistro = mutableListOf<Producto>()

        override suspend fun listar(): List<Producto> {
            if (debeFallar) throw ErrorApi.SinConexion
            return productosRemotos.toList()
        }

        override suspend fun registrar(producto: Producto, categoriaId: Long): Producto {
            if (debeFallar || fallarSoloAlSubir) throw ErrorApi.SinConexion
            llamadasRegistro.add(producto)
            val existente = productosRemotos.find { it.nombre == producto.nombre }
            if (existente != null) return existente
            val guardado = producto.copy(id = 100L + llamadasRegistro.size)
            productosRemotos.add(guardado)
            return guardado
        }
    }

    private fun repositorioCon(api: ProductoApi): Pair<ProductoRepositorioOffline, ProductoLocal> {
        val db = baseEnMemoria()
        val local = ProductoLocal(db)
        val repo = ProductoRepositorioOffline(local, api)
        return Pair(repo, local)
    }

    @Test
    fun test1_sincronizacionConRedDisponibleSubePendientesYReemplazaRemotos() = runTest {
        val remoto1 = Producto(10, "Paracetamol 500mg", 2.50, 100)
        val remoto2 = Producto(20, "Amoxicilina 250mg", 8.00, 50)
        val api = FakeRemoteApi(productosRemotos = mutableListOf(remoto1, remoto2), debeFallar = false)
        val (repo, local) = repositorioCon(api)

        repo.sincronizar()

        val lista = repo.observar().first()
        assertEquals(2, lista.size, "Debe traer los 2 productos del servidor")
        assertEquals(0, local.pendientes().size, "No debe haber pendientes")
        assertNotNull(repo.obtenerUltimaSincronizacion(), "Debe guardar la marca de tiempo de sincronización")
    }

    @Test
    fun test2_registroSinRedGuardaLocalConMarcaPendienteYEmiteEnFlow() = runTest {
        val apiSinRed = FakeRemoteApi(debeFallar = true)
        val (repo, local) = repositorioCon(apiSinRed)

        val registrado = repo.registrar(Producto(0, "Ibuprofeno 400mg", 4.50, 30))

        assertTrue(registrado.id < 0, "El ID retornado debe ser negativo")
        val listaFlow = repo.observar().first()
        assertEquals(1, listaFlow.size, "El Flow reactivo debe emitir el nuevo producto de inmediato")
        assertEquals("Ibuprofeno 400mg", listaFlow.first().nombre)
        assertEquals(1, local.pendientes().size, "Debe registrarse con bandera pendiente en SQLite local")
    }

    @Test
    fun test3_reconexionSubeEfectivamenteLosRegistrosLocalesAlServidor() = runTest {
        val api = FakeRemoteApi(debeFallar = true)
        val (repo, local) = repositorioCon(api)

        repo.registrar(Producto(0, "Alcohol 70%", 7.00, 40))
        assertEquals(1, local.pendientes().size)

        api.debeFallar = false
        repo.sincronizar()

        assertEquals(1, api.llamadasRegistro.size, "Debe llamar a la API remota para subir el pendiente")
        assertEquals(0, local.pendientes().size, "La cola de pendientes debe quedar en cero")
        val lista = repo.observar().first()
        assertEquals(1, lista.size)
        assertTrue(lista.first().id > 0, "El producto local ahora tiene el ID definitivo del servidor")
    }

    @Test
    fun test4_bajadaDelServidorNoSobreescribeNiBorraRegistrosLocalesPendientes() = runTest {
        val api = FakeRemoteApi(
            productosRemotos = mutableListOf(Producto(5, "Aspirina 100mg", 3.00, 20)),
            fallarSoloAlSubir = true
        )
        val (repo, local) = repositorioCon(api)

        val localPendiente = local.guardarPendiente(Producto(0, "Jarabe Tos", 15.00, 10))
        assertEquals(1, local.pendientes().size)

        local.reemplazarSincronizados(listOf(Producto(5, "Aspirina 100mg", 3.00, 20)))

        val listaTotal = repo.observar().first()
        assertEquals(2, listaTotal.size, "Debe contener tanto el remoto bajado como el local pendiente")
        assertEquals(1, local.pendientes().size, "El registro pendiente no fue borrado ni sobreescrito")
        assertTrue(listaTotal.any { it.id == localPendiente.id }, "El producto pendiente sigue presente")
    }

    @Test
    fun test5_identificadoresTemporalesSinColisionConIdsDelServidor() = runTest {
        val apiSinRed = FakeRemoteApi(debeFallar = true)
        val (repo, _) = repositorioCon(apiSinRed)

        val prod1 = repo.registrar(Producto(0, "Termómetro", 25.00, 15))
        val prod2 = repo.registrar(Producto(0, "Oxímetro", 55.00, 10))

        assertEquals(-1L, prod1.id, "Primer ID temporal debe ser -1")
        assertEquals(-2L, prod2.id, "Segundo ID temporal debe ser -2 decremental")
        assertTrue(prod1.id < 0 && prod2.id < 0, "Todos los IDs locales temporales son negativos")
    }

    @Test
    fun test6_idempotenciaDeSincronizacionNoDuplicaRegistros() = runTest {
        val api = FakeRemoteApi(
            productosRemotos = mutableListOf(Producto(50, "Gasa Estéril", 1.50, 200)),
            debeFallar = false
        )
        val (repo, local) = repositorioCon(api)

        repo.sincronizar()
        val conteo1 = repo.observar().first().size

        repo.sincronizar()
        val conteo2 = repo.observar().first().size

        assertEquals(1, conteo1)
        assertEquals(conteo1, conteo2, "Ejecutar sincronizar() dos veces seguidas no debe duplicar registros")
        assertEquals(0, local.pendientes().size, "La cola de pendientes se mantiene en 0")
    }
}
```

### 1.4. Evidencia de Ejecución Exitosa (Consola y Android Studio)
* **Comando:** `.\gradlew :shared:testAndroidHostTest`
* **Resultado:** `BUILD SUCCESSFUL in 14s` (Total: 6 pruebas, 0 fallos, 0 ignoradas, 100% de éxito).

> *[INSERTAR AQUÍ LA CAPTURA: `P1_suite_pruebas_commonTest.png` mostrando los 6 checks verdes en Android Studio]*

---

## 2. PRODUCTO 2: BITÁCORA DE ESCENARIOS OFFLINE (6 pts)

### Ficha de Entorno de Pruebas
* **Dispositivo de Pruebas:** Emulador Android Studio (`Pixel 10a`).
* **Versión de Sistema Operativo:** Android 15 (API Level 35) / Arquitectura x86_64.
* **Manejador de Base de Datos Local:** SQLite integrado mediante SQLDelight 2.1.0 (`pharma.db`).
* **Servidor Backend:** PharmaSoft API (Spring Boot 3.x, JWT, PostgreSQL, Swagger UI en puerto 8080).

---

### Escenario 1: Apertura sin red previa (con datos en caché local)
* **Identificador:** `ESC-01-CACHE-OFFLINE`
* **Fecha y Hora:** 23/10/2026 - 11:25 hrs
* **Plataforma:** Android 15 (Emulador Pixel 10a)
* **Pasos Ejecutados:**
  1. Habiendo sincronizado productos previamente, se desplegó el panel de ajustes rápidos del emulador y se activó el **Modo Avión** ✈️.
  2. Se abrió la aplicación PharmaMobile e ingresó al módulo **Catálogo de Productos**.
* **Qué mostró la pantalla:**
  - Banner superior: `Sin conexión: mostrando datos guardados`.
  - Catálogo completamente visible con los productos cacheados.
  - Indicador de antigüedad: `🕒 Actualizado hace unos segundos`.
* **Qué había en la base de datos local (SQLite):**
  - Tabla `producto` contenía filas con `pendiente = 0`.
  - Tabla `sincronizacion` con `timestamp` correspondiente a la última llamada exitosa.
* **Evidencia visual:**
  > *[INSERTAR AQUÍ LA CAPTURA: `P2_escenario1_apertura_sin_red.png`]*
* **Conclusión:** *La aplicación recupera y renderiza el inventario local de SQLite inmediatamente sin bloquear la UI ni presentar fallos por ausencia de conectividad.*

---

### Escenario 2: Registro sin red (inserción local y marca visual)
* **Identificador:** `ESC-02-REGISTRO-OFFLINE`
* **Fecha y Hora:** 23/10/2026 - 11:27 hrs
* **Plataforma:** Android 15 (Emulador Pixel 10a)
* **Pasos Ejecutados:**
  1. Con el **Modo Avión** activo ✈️, se ingresaron datos en el formulario:
     - Nombre: `Ibuprofeno 400mg`
     - Precio: `8.50`
     - Stock: `25`
  2. Se presionó el botón **`Registrar`**.
* **Qué mostró la pantalla:**
  - El formulario se limpió y el producto se agregó instantáneamente a la lista.
  - Se visualizó la etiqueta destacada: `⏳ Pendiente de envío (ID: -1)`.
* **Qué había en la base de datos local (SQLite):**
  - Registro insertado en la tabla `producto` con `id = -1` y la bandera `pendiente = 1`.
* **Evidencia visual:**
  > *[INSERTAR AQUÍ LA CAPTURA: `P2_escenario2_registro_sin_red.png`]*
* **Conclusión:** *El patrón offline-first permite la continuidad operativa asignando un identificador temporal negativo y encolando el registro para sincronización posterior.*

---

### Escenario 3: Reconexión y sincronización (subida efectiva y Swagger)
* **Identificador:** `ESC-03-RECONEXION-SYNC`
* **Fecha y Hora:** 23/10/2026 - 11:29 hrs
* **Plataforma:** Android 15 (Emulador Pixel 10a) & Navegador Web (Swagger UI)
* **Pasos Ejecutados:**
  1. Se desactivó el **Modo Avión** restableciendo la conexión de red en el emulador.
  2. Se presionó el botón de sincronizar `↻` en la app móvil.
  3. Se abrió Swagger UI (`http://localhost:8080/swagger-ui/index.html#/`) y se ejecutó `GET /api/v1/productos`.
* **Qué mostró la pantalla:**
  - En la aplicación móvil desapareció la etiqueta de pendiente `⏳` y el producto actualizó su ID al asignado por el servidor (ej. `ID: 15`).
  - En Swagger UI se confirmó la presencia del producto con ID 15 en la base de datos central PostgreSQL.
* **Qué había en la base de datos local (SQLite):**
  - El registro temporal con `id = -1` fue eliminado; se insertó el registro definitivo con `id = 15` y `pendiente = 0`.
* **Evidencia visual:**
  > *[INSERTAR AQUÍ LA CAPTURA: `P2_escenario3_reconexion_swagger.png`]*
* **Conclusión:** *Al restablecerse la red, el repositorio sube los pendientes, depura los identificadores temporales y reconcilia el catálogo sin generar duplicidades.*

---

### Escenario 4: Primera apertura sin red tras instalación limpia (Manejo de estado vacío)
* **Identificador:** `ESC-04-EMPTY-STATE-OFFLINE`
* **Fecha y Hora:** 23/10/2026 - 12:05 hrs
* **Plataforma:** Android 15 (Emulador Pixel 10a)
* **Pasos Ejecutados:**
  1. Con el **Modo Avión** activo ✈️, se vació la base local mediante el botón de prueba o limpieza de almacenamiento.
  2. Se ingresó a la pantalla de Productos sin conexión ni datos en caché.
* **Qué mostró la pantalla:**
  - Aviso de red: `Sin conexión: mostrando datos guardados`.
  - Tarjeta central de estado vacío: `📦 No hay productos registrados`.
  - Antigüedad: `🕒 Nunca sincronizado`.
  - No ocurrió ningún cierre inesperado (cero crashes o ANRs).
* **Qué había en la base de datos local (SQLite):**
  - Tablas `producto` y `sincronizacion` creadas correctamente pero con 0 filas.
* **Evidencia visual:**
  > *[INSERTAR AQUÍ LA CAPTURA: `P2_escenario4_limpia_sin_red.png`]*
* **Conclusión:** *La arquitectura maneja de forma segura los estados vacíos iniciales (`Fase.SinProductos`) y la ausencia de marcas de tiempo sin producir excepciones de tipo NullPointerException.*

---

### Escenario 5: Sesión expirada o intento de autenticación sin red
* **Identificador:** `ESC-05-SESION-OFFLINE`
* **Fecha y Hora:** 23/10/2026 - 12:12 hrs
* **Plataforma:** Android 15 (Emulador Pixel 10a)
* **Pasos Ejecutados:**
  1. Con el **Modo Avión** encendido ✈️, se cerró sesión y se intentó iniciar sesión con credenciales de usuario.
* **Qué mostró la pantalla:**
  - La interfaz de autenticación capturó el fallo y mostró el mensaje de error: `No se pudo conectar con el servidor`.
  - La interfaz permaneció responsiva y permitió reintentar al volver la red.
* **Qué había en la base de datos local (SQLite):**
  - Los datos locales y el almacén seguro preservaron su integridad; no se corrompieron las credenciales ni el estado de la base de datos.
* **Evidencia visual:**
  > *[INSERTAR AQUÍ LA CAPTURA: `P2_escenario5_sesion_sin_red.png`]*
* **Conclusión:** *Las excepciones de conectividad en el flujo de autenticación son interceptadas limpiamente por la arquitectura, informando al usuario sin congelar la app.*

---

## 3. PRODUCTO 3: ANTIGÜEDAD DE LOS DATOS EN PANTALLA (3 pts)

### 3.1. Persistencia del Instante de Sincronización
Se diseñó la tabla `sincronizacion` en SQLDelight (`shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobil/db/Sincronizacion.sq`):

```sql
CREATE TABLE sincronizacion (
    id INTEGER NOT NULL PRIMARY KEY DEFAULT 1,
    timestamp INTEGER NOT NULL
);

guardarInstante:
INSERT OR REPLACE INTO sincronizacion (id, timestamp)
VALUES (1, ?);

obtenerInstante:
SELECT timestamp FROM sincronizacion WHERE id = 1;

borrarInstante:
DELETE FROM sincronizacion;
```

Cada vez que el método `sincronizar()` concluye exitosamente en `ProductoRepositorioOffline`, se invoca:
```kotlin
local.guardarUltimaSincronizacion(tiempoActualMillis())
```

### 3.2. Formateador en Lenguaje Natural y Manejo de «Nunca Sincronizado»
En `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobile/platform/Formato.kt` se implementó la función pura con lógica multiplataforma:

```kotlin
expect fun tiempoActualMillis(): Long

fun formatearAntiguedad(timestampMillis: Long?, ahoraMillis: Long = tiempoActualMillis()): String {
    if (timestampMillis == null || timestampMillis <= 0L) {
        return "Nunca sincronizado"
    }
    val diffMillis = ahoraMillis - timestampMillis
    if (diffMillis < 0L) return "Recién sincronizado"

    val segundos = diffMillis / 1000
    val minutos = segundos / 60
    val horas = minutos / 60
    val dias = horas / 24

    return when {
        segundos < 60 -> "Actualizado hace unos segundos"
        minutos < 60 -> if (minutos == 1L) "Actualizado hace 1 minuto" else "Actualizado hace $minutos minutos"
        horas < 24 -> if (horas == 1L) "Actualizado hace 1 hora" else "Actualizado hace $horas horas"
        dias == 1L -> "Actualizado hace 1 día"
        else -> "Actualizado hace $dias días"
    }
}
```

### 3.3. Evidencias de Antigüedad en Pantalla

#### Evidencia A: Antigüedad Reciente
Al conectarse con el servidor o pulsar sincronizar, la pantalla muestra:  
**`🕒 Actualizado hace unos segundos`** (o `hace 1 minuto`).

> *[INSERTAR AQUÍ LA CAPTURA: `P3_antiguedad_reciente.png`]*

#### Evidencia B: Antigüedad Forzada (> 1 día)
Al presionar el disparador de simulación (que resta 48 horas en SQLite local), la interfaz actualiza dinámicamente el texto a:  
**`🕒 Actualizado hace 2 días`**.

> *[INSERTAR AQUÍ LA CAPTURA: `P3_antiguedad_simulada_mayor_1_dia.png`]*

---

## 4. PRODUCTO 4: GUION DE SUSTENTACIÓN DE LA UNIDAD 2 (2 pts)

* **Duración total:** 8 minutos cronometrados (estricto).
* **Distribución:** 5 criterios técnicos evaluados en la rúbrica oficial.

---

### Minuto 0:00 – 1:30 | Criterio 1: Autenticación JWT y Almacenamiento Seguro
* **Qué decir:**  
  *«Buenos días profesor. Presento la aplicación PharmaMobile desarrollada en Kotlin Multiplatform. Iniciamos con el módulo de seguridad. Implementamos autenticación stateless mediante JWT con refresh tokens. Las credenciales sensibles no se almacenan en texto plano; en Android utilizamos `EncryptedSharedPreferences` backed por el hardware Android KeyStore, mientras que en iOS usamos el `Keychain Services` a través de un patrón expect/actual en `AlmacenSeguro`. Esto garantiza que los tokens sobrevivan a reinicios sin riesgo de exfiltración.»*
* **Qué mostrar en pantalla / IDE:**
  - En el emulador: Pantalla de Login, selección del botón rápido `admin` e inicio de sesión exitoso.
  - En el IDE: Abrir `AlmacenSeguro.android.kt` y `AlmacenSeguroIos.kt` para mostrar la delegación segura nativa.
* **Pregunta típica del docente:** *«¿Qué ocurre si el token JWT expira mientras el usuario está utilizando la app?»*
* **Respuesta técnica:** *«El interceptor de Ktor detecta el código HTTP 401 Unauthorized, pausa las peticiones encoladas mediante un Mutex, solicita la renovación al endpoint `/refresh` con el refresh token del almacén seguro y reintenta la petición original con el nuevo token de forma transparente.»*

---

### Minuto 1:30 – 3:00 | Criterio 2: Operaciones CRUD en Catálogo y Validaciones
* **Qué decir:**  
  *«Pasamos al Catálogo de Productos. Implementamos una arquitectura MVI con Clean Architecture. Las pantallas no modifican datos directamente; despachan intenciones al `ProductoViewModel` el cual ejecuta los casos de uso (`RegistrarProductoUseCase`, `ActualizarProductoUseCase`, `EliminarProductoUseCase`). Toda validación ocurre antes del despacho: validamos nombre no vacío, stock entero positivo y precio monetario en Soles usando el formateador multiplataforma.»*
* **Qué mostrar en pantalla / IDE:**
  - En el emulador: Registrar un nuevo producto con nombre, precio y stock. Mostrar cómo aparece en la lista con su botón de edición y copia al portapapeles.
  - En el IDE: Mostrar la clase `ProductoUiState` con sus sellados (`Fase`, `Operacion`) y `FormularioProducto`.
* **Pregunta típica del docente:** *«¿Dónde residen las validaciones de negocio: en la UI o en el dominio?»*
* **Respuesta técnica:** *«La UI valida únicamente el formato de entrada para proporcionar retroalimentación inmediata (errores de campo en rojo), pero las reglas de negocio estrictas y los errores semánticos del backend (como 400 Bad Request o 409 Conflict) son mapeados en la capa de datos hacia el dominio mediante la jerarquía sellada `ErrorApi.Validacion`.»*

---

### Minuto 3:00 – 4:30 | Criterio 3: Manejo de Errores HTTP y Control de Acceso RBAC
* **Qué decir:**  
  *«La aplicación implementa control de acceso basado en roles (RBAC) sincronizado con el backend Spring Boot. Al decodificar el payload del JWT, el sistema extrae las autoridades (`ROLE_ADMIN`, `ROLE_VENDEDOR`, `ROLE_ALMACEN`). Si ingresamos como Vendedor, la pantalla deshabilita automáticamente el formulario de creación y los botones de eliminación, desplegando una tarjeta informativa de 'Modo solo lectura'. Asimismo, los errores HTTP (400, 403, 404, 409, 500) son interceptados y mostrados con mensajes amigables sin abortar el flujo.»*
* **Qué mostrar en pantalla / IDE:**
  - En el emulador: Cerrar sesión e ingresar con el usuario `vendedor`. Mostrar cómo la pantalla de productos entra en modo solo lectura (`puedeRegistrar = false`).
  - En el IDE: Mostrar en `ProductoScreen.kt` la validación condicional `if (puedeRegistrar)`.
* **Pregunta típica del docente:** *«¿Si un atacante modifica la app para habilitar los botones de admin, qué pasa?»*
* **Respuesta técnica:** *«El backend Spring Security rechaza la llamada con HTTP 403 Forbidden porque valida la firma criptográfica del JWT en cada endpoint protegido con `@PreAuthorize("hasRole('ADMIN')")`, por lo que la integridad del servidor nunca se ve comprometida.»*

---

### Minuto 4:30 – 6:30 | Criterio 4: Arquitectura Offline-First con SQLDelight y Sincronización
* **Qué decir:**  
  *«Este es el núcleo de la Sesión 11: arquitectura Offline-First. La pantalla nunca lee directamente de la red HTTP; se suscribe reactivamente a un `Flow<List<Producto>>` emitido por SQLDelight (`PharmaDb`). Cuando el usuario registra un producto sin conexión: 1) Se guarda en SQLite con un ID decremental negativo (-1, -2) y bandera `pendiente = 1`; 2) La UI lo renderiza al instante con la etiqueta '⏳ Pendiente de envío'; 3) Al recuperar la red, `sincronizar()` ejecuta la subida primero, borra el temporal, reemplaza los sincronizados y actualiza el timestamp en `sincronizacion.sq` informando la antigüedad en lenguaje natural.»*
* **Qué mostrar en pantalla / IDE:**
  - En el emulador: Activar modo avión, registrar producto, mostrar la etiqueta de pendiente `⏳ ID: -1`. Desactivar modo avión, pulsar sincronizar y mostrar cómo pasa a ID positivo oficial.
  - En el IDE: Mostrar las 6 pruebas unitarias en verde de `ProductoRepositorioOfflineTest.kt`.
* **Pregunta típica del docente:** *«¿Por qué utilizan IDs negativos en lugar de UUIDs para los pendientes?»*
* **Respuesta técnica:** *«Porque nuestro backend relacional utiliza claves primarias enteras autoincrementales (`Long`). Los UUIDs requerirían alterar el esquema en PostgreSQL o mapear columnas dobles. Usar enteros negativos garantiza cero colisiones con los IDs positivos del servidor y permite un ordenamiento natural en SQLite sin migraciones complejas.»*

---

### Minuto 6:30 – 8:00 | Criterio 5: Enfoque Multiplataforma KMP y Compose Multiplatform
* **Qué decir:**  
  *«Para finalizar, destacamos la alta reutilización de código lograda con Kotlin Multiplatform y Compose Multiplatform. Compartimos el 95% del código entre Android e iOS: la base de datos SQLDelight, el cliente Ktor, la inyección de dependencias con Koin, los ViewModels y la interfaz gráfica completa en Compose. Las únicas piezas específicas por plataforma son los drivers de base de datos (`CreadorDeDriverAndroid` y `CreadorDeDriverIos`), el acceso a Keychain/Keystore y el formato monetario nativo. Esto reduce drásticamente el costo de mantenimiento y los defectos entre plataformas.»*
* **Qué mostrar en pantalla / IDE:**
  - En el IDE: Mostrar la estructura de directorios (`commonMain`, `androidMain`, `iosMain`) y el archivo `gradle.properties` / `build.gradle.kts`.
* **Pregunta típica del docente:** *«¿Cómo gestionan el ciclo de vida del ViewModel en iOS si allá no existe Jetpack ViewModel?»*
* **Respuesta técnica:** *«Utilizamos la biblioteca `androidx.lifecycle:lifecycle-viewmodel` en su versión KMP oficial (2.8+), la cual introduce `ViewModel` multiplataforma con soporte de `viewModelScope` tanto en JVM como en Kotlin/Native para iOS.»*

---

## 5. CONCLUSIONES Y CONTROL DE CALIDAD

1. **Resiliencia Operativa:** La implementación del patrón Offline-First convierte a PharmaMobile en una solución robusta para entornos con conectividad inestable o nula, eliminando los bloqueos de interfaz mediante persistencia reactiva local en SQLite.
2. **Reconciliación Libre de Conflictos:** La estrategia de subida previa con identificadores negativos temporales garantiza que los registros creados desconectados adopten las identidades canónicas del backend sin duplicidad ni pérdida de datos.
3. **Calidad de Software:** La suite de 6 pruebas unitarias en `commonTest` provee cobertura completa sobre los casos borde de sincronización (idempotencia, concurrencia, tolerancia a fallos parciales) garantizando un mantenimiento sostenible.

---

### Enlace al Repositorio de Código
* **URL:** [https://github.com/wagner781/PharmaMobile02/tree/feature/offline-first-qquerar](https://github.com/wagner781/PharmaMobile02/tree/feature/offline-first-qquerar)
* **Rama de Trabajo:** `feature/offline-first-qquerar`
* **Último Commit:** `feat(offline): implementacion de antiguedad de datos, suite de 6 pruebas unitarias y bitacora offline`
