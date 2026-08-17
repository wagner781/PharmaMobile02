package pe.edu.upeu.pharmamobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform