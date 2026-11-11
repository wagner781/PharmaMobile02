# Sesión 14 · Paso 5. Validación del APK minificado

**Fecha:** 11 de noviembre de 2026

**APK evaluado:** `Qquerar_ProyectoFinal_Android_Revision.apk`, versión `1.0.0` y `versionCode=1`. [Ficha de artefactos](sesion-14-paso-4-artefactos-android.md)

**Commit compilado:** [e403415](https://github.com/wagner781/PharmaMobile02/commit/e40341515ce8fe02d30d446c3c487266a971dc18)

**Entorno:** emulador Android temporal con la configuración de Pixel_10a (API 37) y PharmaSoft local. Se usó una instalación limpia de `release` separada de la instalación de depuración. El APK instalado se recuperó con ADB y se comprobó su SHA-256.

```text
88209F139DBAE164153CDDFED8398BD4F6FF8438EA9965F03AD575ECCFAA0882
```

El build del paso 4 ejecutó R8 `9.0.32` y generó `mapping.txt`. La
validación se realizó sobre ese binario con la minificación activa.

| Caso | Resultado observado |
| --- | --- |
| Instalación y arranque | Android instaló el APK firmado y abrió la pantalla de autenticación. [Captura](evidencias/sesion-14-paso-5/01-acceso-release.png). |
| Credenciales inválidas | La app mostró el rechazo de acceso. [Captura](evidencias/sesion-14-paso-5/02-credenciales-invalidas.png). |
| Acceso correcto y lectura | La cuenta de prueba abrió Productos y descargó el catálogo desde PharmaSoft. Esto ejercitó la inyección de dependencias y la deserialización de las respuestas de acceso y catálogo. [Captura](evidencias/sesion-14-paso-5/03-catalogo-release.png). |
| Validación de edición | Un nombre corto se rechazó y la app mostró el mensaje del servidor junto al campo **Nombre**. [Captura](evidencias/sesion-14-paso-5/04-validacion-servidor.png). |
| Alta, edición y baja con red | PharmaSoft asignó el ID 88. La edición se guardó con el mismo ID y la misma categoría. La baja retiró el producto del catálogo activo. [Edición confirmada](evidencias/sesion-14-paso-5/05-edicion-confirmada.png). |
| Modo avión y reconexión | El alta quedó con ID local −1 y un envío pendiente. Al reconectar y pulsar **Sincronizar**, la cola quedó vacía y PharmaSoft devolvió una sola fila activa con ID 89. La fila de prueba se retiró después. [Modo avión](evidencias/sesion-14-paso-5/06-alta-offline.png) · [Reconexión](evidencias/sesion-14-paso-5/07-reconexion.png). |
| Permiso y notificación | Se concedió el permiso desde el diálogo del sistema y Android registró la notificación local de prueba. [Captura](evidencias/sesion-14-paso-5/08-permiso-concedido.png). |
| Cierre de sesión | Al cerrar y reabrir la aplicación se mostró la pantalla de acceso. [Captura](evidencias/sesion-14-paso-5/09-cierre-release.png). |
| Rol de vendedor | En una instalación de prueba con datos reiniciados, la cuenta vendedor abrió Productos en solo lectura. [Captura](evidencias/sesion-14-paso-5/10-vendedor-solo-lectura.png). |

El recorrido ejercitó la inyección de dependencias, la lectura y escritura
de JSON, SQLDelight y el flujo de notificaciones con R8 activo. Los
avisos de metadatos de Kotlin del paso 4 no impidieron los casos verificados.
Logcat registró cero fallos fatales de PharmaMobile durante el recorrido.
La validación en los dos teléfonos físicos corresponde al paso 7 y requiere
una dirección de PharmaSoft accesible desde ellos.
