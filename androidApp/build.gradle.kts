import java.net.URI
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

if (file("google-services.json").isFile) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

// La firma de entrega se carga fuera de la caché de configuración de Gradle.
val cacheDeConfiguracionActiva = gradle.startParameter.isConfigurationCacheRequested
val pharmaSoftBaseUrl = providers.gradleProperty("pharmaSoftBaseUrl").orNull
if (pharmaSoftBaseUrl != null) {
    val uri = URI.create(pharmaSoftBaseUrl)
    require(uri.scheme in setOf("http", "https") && uri.host != null &&
        uri.rawUserInfo == null && uri.rawQuery == null && uri.rawFragment == null &&
        uri.rawPath in setOf("", "/")) {
        "pharmaSoftBaseUrl debe ser un origen HTTP o HTTPS, sin credenciales ni ruta."
    }
}
val archivoFirma = rootProject.file("keystore.properties")
val propiedadesFirma = Properties()
if (!cacheDeConfiguracionActiva && archivoFirma.isFile) {
    archivoFirma.inputStream().use { propiedadesFirma.load(it) }
}
val camposFirma = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
val almacenFirma = propiedadesFirma.getProperty("storeFile")?.takeIf { it.isNotBlank() }
    ?.let { rootProject.file(it) }
val errorFirma = when {
    cacheDeConfiguracionActiva -> "Ejecuta el build release con --no-configuration-cache."
    !archivoFirma.isFile -> "Falta keystore.properties en la raíz del proyecto."
    camposFirma.any { propiedadesFirma.getProperty(it).isNullOrBlank() } -> "Completa los cuatro campos de keystore.properties."
    almacenFirma?.isFile != true -> "El almacén indicado en keystore.properties no existe."
    else -> null
}

val verificarConfiguracionFirmaRelease by tasks.registering {
    val mensaje = errorFirma
    doLast {
        mensaje?.let { throw GradleException(it) }
    }
}

tasks.configureEach {
    if (name in setOf("preReleaseBuild", "packageRelease", "signReleaseBundle", "assembleRelease", "bundleRelease")) {
        dependsOn(verificarConfiguracionFirmaRelease)
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "pe.edu.upeu.pharmamobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pe.edu.upeu.pharmamobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "PHARMASOFT_BASE_URL", "\"${pharmaSoftBaseUrl.orEmpty()}\"")
    }
    signingConfigs {
        if (errorFirma == null) {
            create("release") {
                storeFile = almacenFirma
                storePassword = propiedadesFirma.getProperty("storePassword")
                keyAlias = propiedadesFirma.getProperty("keyAlias")
                keyPassword = propiedadesFirma.getProperty("keyPassword")
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}
