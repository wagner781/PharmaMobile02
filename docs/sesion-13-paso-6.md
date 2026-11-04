# Sesión 13 · Paso 6. Firebase detrás de una interfaz

El repositorio de Productos usa `Telemetria`, un contrato del código común.
En Android, Koin entrega un adaptador de Crashlytics de GitLive. iOS usa la
implementación sin envío de informes. [SDK de GitLive](https://github.com/GitLiveApp/firebase-kotlin-sdk/tree/v2.7.0)

Una sincronización completada deja una entrada de diagnóstico que puede
aparecer en un informe posterior de Crashlytics. Un fallo técnico de
sincronización registra un informe no fatal con la operación y la categoría
del fallo. El adaptador genera una excepción nueva con valores fijos y omite
la excepción original, el token, los datos de usuario y los datos del catálogo.

## Activar Crashlytics en Android

1. En la [consola de Firebase](https://console.firebase.google.com/), crea o
   selecciona un proyecto y registra una app Android con el paquete
   `pe.edu.upeu.pharmamobile`. [Guía de registro](https://firebase.google.com/docs/android/setup)
2. Descarga `google-services.json` y colócalo en `androidApp/`, junto a
   `build.gradle.kts`. Git ignora ese archivo. Al compilar, su presencia
   activa los complementos de Google Services y Crashlytics.
3. Abre Crashlytics en la consola y completa su configuración. Vuelve a
   ejecutar la app. Con un backend de prueba que devuelva HTTP 503 al
   descargar el catálogo, pulsa **Sincronizar**. Restablece el servicio y
   reinicia la app para permitir el envío del informe. En la consola, busca el
   fallo no fatal con `operacion=SINCRONIZAR_PRODUCTOS` y
   `tipo_fallo=SERVIDOR`. [Guía de verificación](https://firebase.google.com/docs/crashlytics/android/test-implementation)

## Verificación

- La compilación de Android sin `google-services.json` y las 18 pruebas
  del módulo compartido pasaron. La prueba del contrato verificó un evento
  tras una sincronización correcta, un informe técnico para HTTP 503 y la
  ausencia de informes para HTTP 401.
- En una copia aislada con un JSON ficticio pasaron las tareas
  `processDebugGoogleServices` e `injectCrashlyticsMappingFileIdDebug`.
- En el Pixel_10a, la app compilada sin el JSON abrió la sesión y sincronizó
  el catálogo con PharmaSoft.
- La recepción de informes en la consola se verificará con el proyecto de
  Firebase registrado.
