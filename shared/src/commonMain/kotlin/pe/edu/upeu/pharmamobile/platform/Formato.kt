package pe.edu.upeu.pharmamobile.platform

expect fun formatearSoles(valor: Double): String

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
