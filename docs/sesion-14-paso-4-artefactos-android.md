# Sesión 14 · Paso 4. Artefactos Android firmados

**Fecha:** 11 de noviembre de 2026

**Rama:** [feature/builds-14](https://github.com/wagner781/PharmaMobile02/tree/feature/builds-14)

**Commit compilado:** [e403415](https://github.com/wagner781/PharmaMobile02/commit/e40341515ce8fe02d30d446c3c487266a971dc18)

Gradle ejecutó `bundleRelease` y `assembleRelease` con la caché de
configuración desactivada. Los dos archivos se guardaron localmente en
`build/entrega/sesion-14/`. Los nombres usan el apellido del responsable
porque la entrega es individual.

| Archivo | Uso | Tamaño en bytes |
| --- | --- | ---: |
| `Qquerar_ProyectoFinal_Android.aab` | Paquete de entrega | 5 880 964 |
| `Qquerar_ProyectoFinal_Android_Revision.apk` | Instalación y revisión | 2 967 327 |

Ambos archivos declaran `pe.edu.upeu.pharmamobile`, `versionCode=1` y
`versionName=1.0.0`. Se comprobaron los metadatos con `aapt2` para el
APK y con `bundletool validate` y `dump manifest` para el AAB.
[bundletool 1.18.3](https://github.com/google/bundletool/releases/tag/1.18.3)

## Verificación de la firma

- **APK.** `apksigner verify --verbose --print-certs` devolvió
  `Verifies` y confirmó el esquema v2. [Salida de apksigner](evidencias/sesion-14-paso-4/verificacion-firma-apk.png)
- **AAB.** `jarsigner -verify` devolvió `jar verified.` y
  `keytool -printcert -jarfile` confirmó el mismo titular y la misma
  huella que el APK.
- **Titular.** Wagner Qquerar Mayta, unidad Proyecto PharmaMobile, país PE.
- **Huella SHA-256 del certificado.**
  `91:B3:90:27:63:FE:88:6C:DA:BC:CE:A5:1A:49:1F:7E:29:9D:B1:71:56:28:93:8F:34:D1:97:77:64:53:E3:32`

## SHA-256 de los archivos

```text
9B96E7C0A3D191DF605E9DF3FC08CDFF3827B80800F685A98CEC318A27E1C1D8  Qquerar_ProyectoFinal_Android.aab
88209F139DBAE164153CDDFED8398BD4F6FF8438EA9965F03AD575ECCFAA0882  Qquerar_ProyectoFinal_Android_Revision.apk
```

R8 completó la minificación y generó el mapa de símbolos. Gradle emitió
avisos al leer metadatos de Kotlin. El recorrido del APK minificado
queda para el paso 5. Para la validación en teléfonos físicos se debe
usar una URL de PharmaSoft alcanzable desde el teléfono. El commit
compilado apunta a `10.0.2.2:8080`, la dirección del emulador.
