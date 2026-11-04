# Actividad Autónoma 13 · Producto B. Evidencias por plataforma

**Estudiante:** Wagner Qquerar Mayta · **Código:** 202210261

**Fecha:** 4 de noviembre de 2026 · **Versión evaluada:** [8c0a3c6](https://github.com/wagner781/PharmaMobile02/commit/8c0a3c6e781f9848d7bf613d56d8367e0ae7b08b) · **Rama:** [feature/integrador-13](https://github.com/wagner781/PharmaMobile02/tree/feature/integrador-13)

## Android · Pixel_10a (API 37)

El [Producto A](sesion-13-autonoma-producto-a.md) contiene las capturas de los diez momentos del recorrido Android. Esta ficha reutiliza esas evidencias y las del paso de permisos para comparar los aspectos que pide la guía.

| Aspecto | Evidencia Android | Estado de iOS según el repositorio |
| --- | --- | --- |
| Almacenamiento del token | `EncryptedSharedPreferences` con una llave maestra AES256-GCM. En el recorrido se observó una entrada cifrada tras iniciar sesión y cero tras cerrar sesión. En una prueba aparte, cerrar y reabrir conservó la sesión. Desinstalar y reinstalar en el perfil temporal devolvió la app a «Autenticación JWT» con cero entradas cifradas. El APK declara `allowBackup=false`. [Acceso tras reinstalar](evidencias/sesion-13-autonoma-b/03-acceso-tras-reinstalar.png). | `AlmacenSeguroIos` usa `NSUserDefaults` con la clave `jwt_token`. Quedan por verificar la reinstalación y la persistencia con la implementación de iOS. |
| Permisos | El diálogo del emulador estaba en inglés: «Allow PharmaMobile to send you notifications?», con **Allow** y **Don’t allow**. En una prueba anterior del mismo Pixel_10a, el diálogo apareció dos veces al denegarlo en ambas ocasiones. Luego quedó disponible **Inicio → Notificaciones → Abrir Ajustes**. Al regresar de Ajustes, la app actualizó el estado. [Diálogo nativo](evidencias/sesion-13-autonoma-b/01-dialogo-nativo.png) · [Denegación permanente](evidencias/sesion-13-paso-5/02-denegado-permanentemente.png). | El módulo iOS carece de una implementación de `GestorPermisos` y el arranque de iOS usa la pantalla compartida sin el panel Android de notificaciones. |
| Sin red y recuperación | En modo avión con Wi-Fi apagado, el alta quedó con ID **−1** y un pendiente. Tras volver a conectar y pulsar **Sincronizar**, obtuvo el ID **86** y el contador bajó a cero. [Modo avión](evidencias/sesion-13-autonoma-a/05-alta-modo-avion.png) · [Reconexión](evidencias/sesion-13-autonoma-a/06-reconexion-sincronizada.png). | El módulo iOS declara `NativeSqliteDriver` para `pharma.db` y usa el motor Darwin de Ktor. El recorrido sin red queda por comprobar en un simulador o dispositivo Apple. |
| Notificaciones | La app solicita `POST_NOTIFICATIONS` en Android 13 o superior. Mientras el permiso está denegado, el botón de envío no aparece. Tras concederlo y pulsar **Enviar notificación de prueba**, el aviso apareció en la bandeja del sistema. [Notificación recibida](evidencias/sesion-13-autonoma-b/02-notificacion-recibida.png). | El emisor de la notificación de prueba reside en el módulo Android. La entrega de notificaciones en iOS queda por implementar y verificar. |
| Diferencias visuales | Las capturas del catálogo muestran la barra superior de la app, los estilos de texto de Compose y el indicador de gestos de Android. [Catálogo Android](evidencias/sesion-13-autonoma-a/03-primera-sincronizacion.png). | La entrada de iOS aloja Compose en SwiftUI mediante `ComposeView().ignoresSafeArea()`. El ajuste visual de tipografías, barras del sistema y áreas seguras se comprobará en un simulador o dispositivo Apple. |

## iOS · Estado y plan de verificación

El repositorio incluye el proyecto `iosApp/iosApp.xcodeproj` y los objetivos
`iosArm64` e `iosSimulatorArm64`. El proyecto Xcode configura iOS 18.2 como
versión mínima y enlaza el framework compartido `Shared`. El 4 de noviembre
se comprobó que el equipo Windows carece de `xcodebuild`, por lo que aquí
queda pendiente generar y ejecutar la app de iOS.

La comprobación en Gradle fue `.\gradlew.bat --offline :shared:linkDebugFrameworkIosSimulatorArm64 --console=plain`. La tarea `compileKotlinIosSimulatorArm64` compiló el código Kotlin. La tarea `linkDebugFrameworkIosSimulatorArm64` quedó marcada como `SKIPPED`. El resultado `BUILD SUCCESSFUL` corresponde a las tareas ejecutadas. El enlace, la compilación en Xcode y el recorrido en iOS quedan por verificar en macOS.

1. Abrir `iosApp/iosApp.xcodeproj` en una Mac con Xcode y seleccionar un
   simulador compatible. Ejecutar la compilación y registrar el resultado.
2. Configurar para iOS una URL de PharmaSoft alcanzable desde el simulador
   o dispositivo. El valor común actual es `http://10.0.2.2:8080`, usado
   en el emulador Android. Comprobar el transporte con la configuración
   de iOS antes de iniciar sesión.
3. Repetir los diez pasos del Producto A y capturar el almacenamiento
   tras reinstalar, el recorrido sin conexión y las diferencias visuales.
   Completar el soporte de notificaciones de iOS y repetir las pruebas de
   autorización y entrega en el entorno Apple.
