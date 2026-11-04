# Actividad Autónoma 13 · Producto A. Mapa del flujo completo

**Estudiante:** Wagner Qquerar Mayta · **Código:** 202210261

**Fecha de prueba:** 4 de noviembre de 2026 · **Plataforma:** Android, Pixel_10a

**Versión evaluada:** [8c0a3c6](https://github.com/wagner781/PharmaMobile02/commit/8c0a3c6e781f9848d7bf613d56d8367e0ae7b08b) en [feature/integrador-13](https://github.com/wagner781/PharmaMobile02/tree/feature/integrador-13).

El recorrido usó un perfil temporal, PharmaSoft local y cuentas de prueba. Las capturas corresponden a la ventana del emulador. Los estados se complementaron con consultas de solo lectura al catálogo, inspección de SQLite y verificación de la presencia de una entrada cifrada de sesión, sin exponer su valor.

| N.º | Momento y captura | Estado del sistema |
| --- | --- | --- |
| 1 | [Primera apertura](evidencias/sesion-13-autonoma-a/01-primera-apertura.png) | La instalación del perfil limpio mostró «Autenticación JWT». La base de Productos todavía no se había inicializado y no había un token de sesión guardado. |
| 2 | [Inicio de sesión correcto](evidencias/sesion-13-autonoma-a/02-sesion-administrador.png) | La cuenta de prueba `admin` ingresó con rol `ADMIN`, permisos de lectura y escritura del catálogo y ocho privilegios en total. Se verificó una entrada en el almacenamiento cifrado de sesión de Android. |
| 3 | [Primera sincronización](evidencias/sesion-13-autonoma-a/03-primera-sincronizacion.png) | La app mostró «PharmaSoft respondió». SQLite registró cinco filas activas descargadas, cero pendientes y una última descarga a las 16:19:39 (UTC−05:00). |
| 4 | [Alta con conexión](evidencias/sesion-13-autonoma-a/04-alta-con-conexion.png) | El producto `ZZ-AA13-1-ONLINE` apareció en el catálogo. PharmaSoft confirmó el ID **85**. |
| 5 | [Alta en modo avión](evidencias/sesion-13-autonoma-a/05-alta-modo-avion.png) | Con Wi-Fi apagado, `ZZ-AA13-2-OFFLINE` se guardó con ID provisional **−1**. La interfaz mostró «Sin conexión» y **1 pendiente, 0 fallidas**. |
| 6 | [Reconexión y sincronización](evidencias/sesion-13-autonoma-a/06-reconexion-sincronizada.png) | Tras recuperar Wi-Fi y pulsar **Sincronizar**, la cola pasó de **1 a 0 pendientes**. PharmaSoft y SQLite confirmaron el ID **86** para el alta offline. |
| 7 | [Error de validación](evidencias/sesion-13-autonoma-a/07-validacion-nombre.png) | El alta `ZQ` devolvió **HTTP 400**. El mensaje corresponde al campo **Nombre** y aparece en **Revisar fallos** como «El nombre debe tener entre 3 y 150 caracteres». La app lo muestra en el detalle del envío, separado del campo del formulario. |
| 8 | [Operación sin privilegio](evidencias/sesion-13-autonoma-a/08-operacion-sin-privilegio.png) | La cuenta `vendedor` tenía `CATALOGO_LEER` y carecía de `CATALOGO_ESCRIBIR`. La app mostró «Modo solo lectura» y ocultó el registro. Un POST de prueba a PharmaSoft con esa cuenta devolvió **HTTP 403**. |
| 9 | [Token vencido](evidencias/sesion-13-autonoma-a/09-token-vencido.png) · [Acceso al reabrir](evidencias/sesion-13-autonoma-a/09b-acceso-tras-vencimiento.png) | Con un JWT de 60 segundos, sincronizar después del vencimiento devolvió **HTTP 401**. La app mostró «Sesión vencida. Inicia sesión nuevamente» y mantuvo el catálogo visible. Al reabrir volvió a «Autenticación JWT». |
| 10 | [Cierre de sesión](evidencias/sesion-13-autonoma-a/10-cierre-de-sesion.png) | La app volvió al acceso. La entrada de sesión cifrada pasó de **1 a 0**. SQLite conservó **8 filas**, incluida **1 alta fallida local**, y la marca de última descarga. |

## Alcance de la prueba de vencimiento

El caso 9 usó una compilación de prueba del mismo commit con el identificador
`pe.edu.upeu.pharmamobile.qa` y una instancia separada de PharmaSoft en el
puerto `18081`. El token de 60 segundos permitió reproducir el vencimiento.
La prueba de cierre del caso 10 se realizó en la instalación principal del
perfil temporal, con la cuenta `vendedor`.

Tras capturar el recorrido, se retiraron las dos altas de prueba de PharmaSoft.
El catálogo volvió a mostrar las cinco filas activas con las que comenzó la prueba.
