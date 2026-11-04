package pe.edu.upeu.pharmamobile.presentation.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import pharmamobile.shared.generated.resources.Res
import pharmamobile.shared.generated.resources.images

@Composable
fun InicioScreen(contenidoAdicional: @Composable (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar el logo
        Image(
            painter = painterResource(Res.drawable.images),
            contentDescription = "Logo PharmaMobil",
            modifier = Modifier.size(128.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bienvenido a PharmaMobil",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Gestión de inventario farmacéutico",
            style = MaterialTheme.typography.bodyLarge
        )
        if (contenidoAdicional != null) {
            Spacer(modifier = Modifier.height(24.dp))
            contenidoAdicional()
        }
    }
}
