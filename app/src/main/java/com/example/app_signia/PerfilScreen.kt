package com.example.app_signia

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.io.File
import java.io.FileOutputStream

/**
 * PANTALLA DE PERFIL (PerfilScreen.kt)
 * Maneja la información del usuario, guardado persistente de foto de perfil,
 * visualización de racha (RF17) y cierre de sesión con la paleta oficial SIGNIA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val currentUser = auth.currentUser

    // Cargar racha del usuario
    LaunchedEffect(currentUser?.uid) {
        StreakManager.loadStreakForUser(currentUser?.uid)
    }

    val nombreUsuario = currentUser?.displayName
        ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Usuario SIGNIA"
    val correoUsuario = currentUser?.email ?: "correo@ejemplo.com"

    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        val savedBitmap = loadSavedProfilePhoto(context)
        if (savedBitmap != null) {
            profileBitmap = savedBitmap.asImageBitmap()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
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
            } catch (_: Exception) {
                Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = SigniaLightLavender,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        fontWeight = FontWeight.Bold,
                        color = SigniaDarkPurple,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = SigniaDarkPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SigniaLightLavender)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

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
                                tint = SigniaDarkPurple
                            )
                        }
                    }
                }

                // Botón flotante para cambiar la foto
                SmallFloatingActionButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    containerColor = SigniaBluePrimary,
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
                color = SigniaDarkPurple,
                textAlign = TextAlign.Center
            )

            // Correo del Usuario
            Text(
                text = correoUsuario,
                fontSize = 14.sp,
                color = SigniaDarkPurple.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de Resumen de Cuenta
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de Cuenta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SigniaDarkPurple
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = SigniaBluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Señas Aprendidas", fontSize = 14.sp, color = SigniaDarkPurple)
                        }
                        Text(
                            text = "${MockProgressData.completedSigns} / ${MockProgressData.totalSigns}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SigniaDarkPurple
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = SigniaLightLavender
                    )

                    // Fila de Racha interactiva
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("racha") },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = SigniaBluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Racha Actual", fontSize = 14.sp, color = SigniaDarkPurple)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${StreakManager.currentStreak} Días",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SigniaDarkPurple
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ver racha",
                                tint = SigniaDarkPurple.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = SigniaLightLavender
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = SigniaBluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nivel", fontSize = 14.sp, color = SigniaDarkPurple)
                        }
                        Text(
                            text = "Principiante",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SigniaDarkPurple
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = SigniaDarkPurple
                )
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SigniaDarkPurple)
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SigniaError)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
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