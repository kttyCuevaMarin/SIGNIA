package com.example.app_signia

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.File
import java.io.FileOutputStream

/**
 * PANTALLA DE PERFIL (PerfilScreen.kt)
 * Maneja la información del usuario, guardado persistente de foto de perfil
 * y cierre de sesión.
 */
@Composable
fun PerfilScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val currentUser = auth.currentUser

    // Datos del usuario registrado
    val nombreUsuario = currentUser?.displayName
        ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Usuario SIGNIA"
    val correoUsuario = currentUser?.email ?: "correo@ejemplo.com"

    // Estado para guardar la foto seleccionada en memoria
    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Cargar la foto previamente guardada en el almacenamiento local al iniciar la pantalla
    LaunchedEffect(Unit) {
        val savedBitmap = loadSavedProfilePhoto(context)
        if (savedBitmap != null) {
            profileBitmap = savedBitmap.asImageBitmap()
        }
    }

    // Launcher para abrir la galería y guardar la imagen localmente de forma permanente
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    saveProfilePhotoToDisk(context, bitmap)
                    profileBitmap = bitmap.asImageBitmap()
                    Toast.makeText(context, "¡Foto de perfil guardada con éxito!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Mi Perfil",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = HomeDarkPurple
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Contenedor Circular para la Foto de Perfil
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.size(120.dp)
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Icono por defecto",
                            modifier = Modifier.size(70.dp),
                            tint = HomeDarkPurple
                        )
                    }
                }
            }

            // Botón flotante para cambiar la foto
            SmallFloatingActionButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                containerColor = HomeBluePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Cargar foto",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre del Usuario
        Text(
            text = nombreUsuario,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HomeDarkPurple,
            textAlign = TextAlign.Center
        )

        // Correo del Usuario
        Text(
            text = correoUsuario,
            fontSize = 14.sp,
            color = HomeDarkPurple.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de Resumen de Cuenta
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Resumen de Cuenta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = HomeDarkPurple
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = HomeBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Señas Aprendidas", fontSize = 14.sp, color = HomeDarkPurple)
                    }
                    Text(
                        text = "${MockProgressData.completedSigns} / ${MockProgressData.totalSigns}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HomeDarkPurple
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = HomeLightLavender
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = HomeBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Racha Actual", fontSize = 14.sp, color = HomeDarkPurple)
                    }
                    Text(
                        text = "7 Días",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HomeDarkPurple
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = HomeLightLavender
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = HomeBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nivel", fontSize = 14.sp, color = HomeDarkPurple)
                    }
                    Text(
                        text = "Principiante",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HomeDarkPurple
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Botón Cargar Foto de Perfil
        OutlinedButton(
            onClick = { photoPickerLauncher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = HomeDarkPurple)
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = HomeDarkPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cargar Foto de Perfil", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón Cerrar Sesión
        Button(
            onClick = {
                auth.signOut()
                Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Guarda la imagen seleccionada permanentemente en el almacenamiento interno de la app
 */
private fun saveProfilePhotoToDisk(context: Context, bitmap: Bitmap) {
    try {
        val file = File(context.filesDir, "profile_photo.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Carga la foto de perfil almacenada en disco si existe
 */
private fun loadSavedProfilePhoto(context: Context): Bitmap? {
    return try {
        val file = File(context.filesDir, "profile_photo.jpg")
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else null
    } catch (e: Exception) {
        null
    }
}
