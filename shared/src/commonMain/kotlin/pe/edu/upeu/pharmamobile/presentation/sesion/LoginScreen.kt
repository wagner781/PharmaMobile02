package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(viewModel: SesionViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarClave by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PharmaMobil",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Autenticación JWT",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Campo Usuario
                OutlinedTextField(
                    value = uiState.usuario,
                    onValueChange = { viewModel.onUsuarioChange(it) },
                    label = { Text("Usuario") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Usuario")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.cargando
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Contraseña
                OutlinedTextField(
                    value = uiState.clave,
                    onValueChange = { viewModel.onClaveChange(it) },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Contraseña")
                    },
                    trailingIcon = {
                        IconButton(onClick = { mostrarClave = !mostrarClave }) {
                            Icon(
                                imageVector = if (mostrarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (mostrarClave) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.cargando
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mensaje de Error
                if (uiState.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Botón Iniciar Sesión
                Button(
                    onClick = { viewModel.iniciarSesion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.cargando && uiState.usuario.isNotBlank() && uiState.clave.isNotBlank()
                ) {
                    if (uiState.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Iniciar Sesión", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Accesos Rápidos para Evaluación
                Text(
                    text = "Usuarios de prueba:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(
                        onClick = {
                            viewModel.onUsuarioChange("admin")
                            viewModel.onClaveChange("Admin123")
                        },
                        label = { Text("admin") }
                    )
                    AssistChip(
                        onClick = {
                            viewModel.onUsuarioChange("vendedor")
                            viewModel.onClaveChange("Vendedor123")
                        },
                        label = { Text("vendedor") }
                    )
                    AssistChip(
                        onClick = {
                            viewModel.onUsuarioChange("almacen")
                            viewModel.onClaveChange("Almacen123")
                        },
                        label = { Text("almacen") }
                    )
                }
            }
        }
    }
}
