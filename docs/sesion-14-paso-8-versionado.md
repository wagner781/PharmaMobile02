# Sesión 14 · Paso 8. Versionado y nomenclatura de la entrega

**Modalidad:** individual · **Rama de entrega:** [`feature/builds-14`](https://github.com/wagner781/PharmaMobile02/tree/feature/builds-14)

| Dato | Valor verificado |
| --- | --- |
| Nombre de la app | PharmaMobile |
| `applicationId` | `pe.edu.upeu.pharmamobile` |
| `versionCode` | `1` |
| `versionName` | `1.0.0` |
| Etiqueta de Git | `v1.0-final` |
| Commit compilado | `bef0ee63b16814a5829f1169f628d9486da9aef9` |
| URL de PharmaSoft | `http://10.40.29.72:8080`, accesible desde la red de la prueba |

Los nombres usan el apellido del responsable según la modalidad individual.
Los dos binarios están en `build/entrega/sesion-14/final/`.

| Archivo | Tamaño en bytes | SHA-256 |
| --- | ---: | --- |
| `Qquerar_ProyectoFinal_Android.aab` | 5 882 089 | `4C163892705C54A53FF30674297FEA83011DBF1C0AB81AF14716BD08246ACF65` |
| `Qquerar_ProyectoFinal_Android_Revision.apk` | 2 967 327 | `486D5EA91674DEE7054702F9DC7825F8B6E18B518083699F1F3377892EB2F44C` |

## Reproducción y verificación

```powershell
git switch --detach v1.0-final
.\gradlew.bat --no-configuration-cache :androidApp:bundleRelease :androidApp:assembleRelease '-PpharmaSoftBaseUrl=http://10.40.29.72:8080'
```

La firma requiere el almacén y el archivo local documentados en el [README](../README.md). `apksigner` verificó el esquema v2 del APK. `jarsigner` verificó la firma del AAB y `bundletool` validó su estructura. `aapt2` y `bundletool` confirmaron el identificador y las versiones. `keytool` confirmó que ambos artefactos tienen el mismo certificado de Wagner Qquerar Mayta.

**SHA-256 del certificado:** `91:B3:90:27:63:FE:88:6C:DA:BC:CE:A5:1A:49:1F:7E:29:9D:B1:71:56:28:93:8F:34:D1:97:77:64:53:E3:32`.

El APK probado en los teléfonos tiene el SHA-256 indicado en la [matriz del paso 7](sesion-14-paso-7-dispositivos-reales.md). Al comparar las 487 entradas internas de ese APK con las del APK de entrega, la única que cambió fue `META-INF/version-control-info.textproto`, que registra el commit compilado. Los recursos y el DEX coinciden. El DEX también coincide con el del AAB. El recorrido físico corresponde al APK del paso 7 y conserva las comprobaciones pendientes de la matriz.

La rama `develop` no existe en el repositorio local ni en `origin`. La compilación y la etiqueta se hicieron desde `feature/builds-14`, la rama de entrega. Este commit de documentación es posterior al commit etiquetado.
