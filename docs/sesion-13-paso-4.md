# Sesión 13 · Paso 4. Estado de la sincronización

El catálogo muestra la antigüedad de los datos, los envíos pendientes y los
fallidos. La cola y la hora de la última descarga se observan en SQLDelight.
El indicador de conexión refleja el resultado del último intento contra
PharmaSoft. Al abrir **Revisar fallos** se muestra el último motivo de cada
fila que requiere atención.

## Verificación en Android

Se usó el emulador Pixel_10a con un perfil temporal y PharmaSoft en el
puerto 8080. La compilación de Android y las 17 pruebas del módulo
compartido terminaron correctamente.

| Prueba | Resultado observado |
| --- | --- |
| Abrir el catálogo con conexión. | Apareció «PharmaSoft respondió», «Actualizado hace unos segundos» y «Pendientes: 0 · Fallidas: 0». |
| Activar modo avión, apagar Wi-Fi y registrar `ZQ`. | Apareció «Sin conexión» y «Pendientes: 1 · Fallidas: 0». [Captura del pendiente](evidencias/sesion-13-paso-4/01-pendiente-sin-conexion.png). |
| Restablecer Wi-Fi, pulsar **Sincronizar** y abrir **Revisar fallos**. | La pantalla pasó a «Pendientes: 0 · Fallidas: 1». El detalle de `ZQ` mostró el motivo de PharmaSoft: «El nombre debe tener entre 3 y 150 caracteres». [Captura del fallo](evidencias/sesion-13-paso-4/02-detalle-envio-fallido.png). |
