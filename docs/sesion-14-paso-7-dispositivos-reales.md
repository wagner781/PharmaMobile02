# Sesión 14 · Paso 7. Pruebas en dispositivos Android reales

**Rama:** `feature/builds-14`

**Estado:** Se revisaron 24 capturas de dos teléfonos Android. La matriz identifica las comprobaciones que requieren evidencia adicional.

**APK de referencia:** `1.0.0` (`versionCode=1`), paquete `pe.edu.upeu.pharmamobile`.

## Preparación

1. Abrir `http://<IP-de-la-PC>:8080/v3/api-docs` desde cada teléfono conectado a la misma red que PharmaSoft. La respuesta JSON confirma que el teléfono llega a la API.
2. Generar el APK `release` firmado y minificado con la dirección confirmada. Registrar el commit compilado y el SHA-256 del archivo, y verificar su firma antes de copiarlo.
3. Copiar el mismo APK a ambos teléfonos. Abrirlo desde el gestor de archivos con el cable USB desconectado y permitir la instalación desde ese origen si Android lo solicita. Usar cuentas de prueba con permiso de edición y con rol vendedor.
4. Anotar marca, modelo y versión de Android de cada teléfono. Completar cada fila con el resultado y el enlace a la evidencia. Si hay un fallo, vincular su identificador de defecto.

En Windows se comprobó que PharmaSoft devuelve HTTP 200 en `/v3/api-docs` desde las dos interfaces de red activas de la PC. [Safari en el iPhone de Wagner](evidencias/sesion-14-paso-7/01-conectividad-pharmasoft-iphone.png) mostró el JSON `PharmaBackend API` desde `10.40.29.72:8080`. Es evidencia de conectividad en la red local.

El [APK del paso 4](sesion-14-paso-4-artefactos-android.md) usa `10.0.2.2:8080`, la dirección del backend para el emulador. El [paso 5](sesion-14-paso-5-minificacion.md) conserva sus resultados de emulación. Para crear el APK de la prueba física se suministra la dirección por propiedad de Gradle:

```powershell
.\gradlew.bat :androidApp:assembleRelease --no-configuration-cache '-PpharmaSoftBaseUrl=http://10.40.29.72:8080'
```

## APK para el recorrido físico

- **Archivo local:** `build/entrega/sesion-14/paso-7/Qquerar_ProyectoFinal_Android_PruebaFisica.apk`.
- **SHA-256 del APK:** `FDB8575AB9EB24FC5CDD24AEECFEBDAE134CEF75EF1928F1CAD1F75D2C26EC4B`.
- **Firma:** `apksigner` verificó el esquema v2 y confirmó el mismo certificado del paso 4. R8 generó el APK minificado.
- **Compilación del APK usado en las pruebas:** rama `feature/builds-14`, base `03755c2` y cambios de configuración del paso 7. El cierre registra las evidencias disponibles y deja las comprobaciones restantes para una revisión posterior.
- **Comprobación previa:** 24 pruebas de `testAndroidHostTest` pasaron. Se instaló el APK en un emulador temporal y se verificó el SHA-256 de la copia instalada. Con credenciales ficticias, PharmaSoft respondió HTTP 401 desde `10.40.29.72:8080` y la app mostró el mensaje esperado. [Captura](evidencias/sesion-14-paso-7/02-acceso-al-backend-emulador.png).

## Matriz de pruebas

| Dispositivo | Marca y modelo | Versión de Android | Fecha de prueba |
| --- | --- | --- | --- |
| Teléfono 1 | Honor (modelo por confirmar) | Android 14 | 11/11/2026 |
| Teléfono 2 | Samsung Galaxy A12 | Android 13 | 11/11/2026 |

Los resultados de ambos teléfonos se basan en las capturas aportadas. Los estados **Parcial** y **Sin verificar** indican comprobaciones que requieren evidencia adicional.

El [alcance congelado](sesion-14-paso-1-alcance.md) incluye la sincronización manual. La guía también pide observar el comportamiento al recuperar la red. Las capturas demuestran que se vació la cola, pero no permiten saber si se pulsó **Sincronizar** ni comprobar cuántas filas quedaron en el servidor.

| N.º | Caso | Resultado esperado | Teléfono 1 | Teléfono 2 |
| --- | --- | --- | --- | --- |
| 1 | Instalación desde el APK firmado | El instalador del sistema abre el archivo y la app aparece con su nombre e icono definitivos. | Parcial. La app abre. Falta documentar la instalación desde el archivo y el icono del lanzador (T1-01). | Cumple. El instalador confirmó la instalación de PharmaMobile y se comprobó que la app abre (T2-01 a T2-03). |
| 2 | Inicio de sesión correcto, credenciales inválidas y sin conexión | El acceso válido abre la app y los mensajes distinguen entre credenciales inválidas y falta de conexión. | Cumple. Acceso en la misma red y mensajes distintos para credenciales inválidas y error de conexión (T1-01 a T1-07). | Cumple. El catálogo abre con una sesión válida. Los mensajes distinguen credenciales inválidas y falta de conexión (T2-03 a T2-05, T2-07 y T2-08). |
| 3 | CRUD de productos y rechazo de validación | Se crea, lee, edita y elimina el producto. El mensaje del servidor aparece junto al campo que originó el rechazo. | Parcial. Alta y lectura visibles. Faltan edición, eliminación y rechazo de validación (T1-08 a T1-10). | Parcial. Se observan lectura, edición confirmada y alta local. Faltan baja y rechazo de validación (T2-05, T2-06 y T2-09). |
| 4 | Operación con rol vendedor sin privilegio | La app comunica la restricción y mantiene abierta la sesión. | Cumple en la app. El vendedor sigue en sesión y ve el catálogo en solo lectura (T1-05 y T1-11). | Cumple en la app. La cuenta vendedor consulta el catálogo en solo lectura con la sesión activa (T2-07 y T2-08). |
| 5 | Abrir, leer y crear en modo avión | El catálogo local se puede leer y el alta se muestra como pendiente. | Parcial. Catálogo visible y alta pendiente con ID −1. Falta comprobar la reapertura sin red (T1-12). | Parcial. Catálogo visible y alta pendiente en modo avión. Falta verificar la reapertura sin red (T2-09). |
| 6 | Recuperar conexión y sincronizar | El alta llega al servidor una sola vez, la cola queda vacía y la pantalla refleja el cambio. | Parcial. La cola pasó de 1 a 0. Falta confirmar el envío automático y la ausencia de duplicados en el servidor (T1-12 y T1-13). | Parcial. La cola pasó de 1 a 0. Falta confirmar el envío automático y la ausencia de duplicados en el servidor (T2-09 y T2-10). |
| 7 | Conceder, denegar y denegar permanentemente el permiso de notificaciones | Se gestionan los tres estados. La denegación permanente ofrece ir a Ajustes. | Parcial. Se ve el estado denegado y el diálogo. Faltan concesión y denegación permanente con acceso a Ajustes (T1-04 y T1-14). | Sin verificar. Faltan evidencias de los estados del permiso de notificaciones en este teléfono. |
| 8 | Cerrar sesión y volver a abrir | La sesión queda cerrada y se muestra la pantalla de acceso. | Sin verificar. Falta la evidencia de cierre de sesión y reapertura. | Sin verificar. Falta evidencia de cierre de sesión y reapertura. |

Para el modo avión, cargar primero el catálogo con red y apagar también el Wi-Fi si sigue activo. Usar nombres de prueba distintos por teléfono y comprobar en el servidor que la sincronización crea una sola fila por alta.

## Índice de capturas del primer teléfono

Las 14 capturas están ordenadas desde el inicio de sesión hasta el permiso de notificaciones. Cada identificador enlaza la captura con los casos de la matriz que ayuda a comprobar.

| Captura | Caso de la matriz | Qué se observa | Imagen o enlace |
| --- | --- | --- | --- |
| T1-01 | 1, 2 | Pantalla de acceso abierta en el teléfono. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-01-inicio-sesion.jpeg) |
| T1-02 | 2 | Indicador de carga durante el intento de acceso. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-02-estado-cargando.jpeg) |
| T1-03 | 2 | En la otra red, la app indicó que no pudo conectar con el servidor. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-03-servidor-no-alcanzable.jpeg) |
| T1-04 | 2, 7 | Se abrió Inicio en la misma red. La tarjeta mostraba el permiso denegado. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-04-acceso-misma-red.jpeg) |
| T1-05 | 2, 4 | El menú identifica al usuario vendedor y muestra el tema oscuro activado. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-05-menu-vendedor.jpeg) |
| T1-06 | 2 | La app mostró un mensaje específico para credenciales inválidas. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-06-credenciales-invalidas.jpeg) |
| T1-07 | 2 | Con modo avión activo, la app mostró un error de conexión. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-07-acceso-modo-avion.jpeg) |
| T1-08 | 3 | El catálogo muestra productos y PharmaSoft respondió. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-08-lectura-catalogo.jpeg) |
| T1-09 | 3 | Formulario de Doltoprofeno con precio S/ 5 y stock 10. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-09-formulario-producto.jpeg) |
| T1-10 | 3 | Se confirmó el alta de Doltoprofeno y el producto apareció en el catálogo. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-10-producto-guardado.jpeg) |
| T1-11 | 4 | El vendedor consultó el catálogo en modo solo lectura y sin controles de escritura. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-11-vendedor-solo-lectura.jpeg) |
| T1-12 | 5 | En modo avión, el catálogo siguió visible. El alta quedó pendiente con ID local −1. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-12-alta-sin-red.jpeg) |
| T1-13 | 6 | Tras reconectar, PharmaSoft respondió y la app mostró 0 pendientes y 0 fallidas. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-13-reconexion-sincronizacion.jpeg) |
| T1-14 | 7 | Android mostró las opciones Permitir y No permitir. | [Ver captura](evidencias/sesion-14-paso-7/telefono-1/T1-14-dialogo-notificaciones.jpeg) |

## Índice de capturas del segundo teléfono

Las dos primeras imágenes muestran el cuadro del instalador. Las demás siguen el orden del recorrido.

| Captura | Caso de la matriz | Qué se observa | Imagen o enlace |
| --- | --- | --- | --- |
| T2-01 | 1 | El instalador de Android muestra el progreso de instalación de PharmaMobile. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-01-instalacion.png) |
| T2-02 | 1 | Android confirma la instalación y ofrece abrir PharmaMobile. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-02-instalacion-confirmada.png) |
| T2-03 | 2 | La app muestra el mensaje de credenciales inválidas. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-03-credenciales-invalidas.jpeg) |
| T2-04 | 2 | Con modo avión activo, el inicio de sesión muestra el error de conexión al servidor. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-04-acceso-modo-avion.jpeg) |
| T2-05 | 2, 3 | La sesión permite abrir el formulario de edición de Doltoprofeno. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-05-edicion-producto.jpeg) |
| T2-06 | 3 | La app confirma la actualización y muestra el producto como Luiguiprofeno. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-06-producto-actualizado.jpeg) |
| T2-07 | 2, 4 | Se seleccionó la cuenta vendedor en la pantalla de acceso. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-07-acceso-vendedor.jpeg) |
| T2-08 | 2, 4 | El vendedor consulta el catálogo en modo solo lectura. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-08-vendedor-solo-lectura.jpeg) |
| T2-09 | 3, 5 | En modo avión, el catálogo sigue visible y jogeprofeno queda pendiente con ID local −1. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-09-alta-sin-red.jpeg) |
| T2-10 | 6 | Tras recuperar la red, PharmaSoft respondió y la app mostró cero pendientes y cero fallidas. | [Ver captura](evidencias/sesion-14-paso-7/telefono-2/T2-10-reconexion-sincronizacion.jpeg) |

## Registro de defectos

Las 24 capturas revisadas no confirman defectos. El mensaje al usar otra red o el modo avión es coherente con un backend disponible en la red local. Los casos parciales o sin verificar conservan sus comprobaciones pendientes. Si se confirma un defecto, registra los campos siguientes.

| Campo | Dato por registrar |
| --- | --- |
| Identificador | `D-01`, `D-02` y siguientes |
| Dispositivo y versión | Marca, modelo, versión de Android y versión del APK |
| Pasos para reproducir | Secuencia numerada desde que se abre PharmaMobile |
| Resultado esperado | Comportamiento definido para el caso |
| Resultado obtenido | Comportamiento observado y enlace a la captura o texto del error |
| Severidad | Alta si impide el recorrido, media si lo dificulta, baja si es visual |
| Estado | Corregido, pendiente o descartado con motivo |
