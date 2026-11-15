package com.example.app_signia

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
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

    var displayNameState by remember {
        mutableStateOf(ProfileManager.getFullName(context, currentUser?.displayName ?: "Usuario SIGNIA"))
    }
    var locationState by remember {
        mutableStateOf(ProfileManager.getLocation(context, "Lima, Perú"))
    }
    var roleIndexState by remember {
        mutableStateOf(ProfileManager.getRoleIndex(context, 0))
    }
    val correoUsuario = currentUser?.email ?: "correo@ejemplo.com"

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                displayNameState = ProfileManager.getFullName(context, currentUser?.displayName ?: "Usuario SIGNIA")
                locationState = ProfileManager.getLocation(context, "Lima, Perú")
                roleIndexState = ProfileManager.getRoleIndex(context, 0)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var pendingEditingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showZoomDialog by remember { mutableStateOf(false) }

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
                    pendingEditingBitmap = bitmap
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (pendingEditingBitmap != null) {
        ProfilePhotoEditorDialog(
            initialBitmap = pendingEditingBitmap!!,
            onDismiss = { pendingEditingBitmap = null },
            onSave = { editedBitmap ->
                saveProfilePhotoToDisk(context, editedBitmap)
                profileBitmap = editedBitmap.asImageBitmap()
                pendingEditingBitmap = null
                Toast.makeText(context, "¡Foto de perfil guardada con éxito!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showZoomDialog && profileBitmap != null) {
        ProfileZoomDialog(
            bitmap = profileBitmap!!,
            onDismiss = { showZoomDialog = false }
        )
    }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
                        fontWeight = FontWeight.Bold,
                        color = ThemeManager.textColor,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ThemeManager.textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeManager.topBarColor)
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
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            if (profileBitmap != null) {
                                showZoomDialog = true
                            }
                        },
                    shape = CircleShape,
                    color = ThemeManager.cardBackgroundColor,
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
                                tint = ThemeManager.textColor
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

            // Nombre del Usuario con icono de lápiz para editar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { navController.navigate("editar_perfil") }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = displayNameState,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeManager.textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar nombre",
                    tint = SigniaBluePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "📍 $locationState",
                fontSize = 13.sp,
                color = ThemeManager.subtextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))
            val roleTitle = when (roleIndexState) {
                1 -> "Persona Sorda / Nativo LSP"
                2 -> "Educador / Intérprete"
                else -> "Oyente Aprendiendo"
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SigniaBluePrimary.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = when (roleIndexState) {
                            1 -> Icons.Default.SignLanguage
                            2 -> Icons.Default.School
                            else -> Icons.Default.Verified
                        },
                        contentDescription = null,
                        tint = SigniaBluePrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = roleTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SigniaBluePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Correo del Usuario
            Text(
                text = correoUsuario,
                fontSize = 14.sp,
                color = ThemeManager.subtextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de Resumen de Cuenta
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de Cuenta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ThemeManager.textColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val totalSignsInDict = remember(SignDictionary.dictionary.size) {
                        SignDictionary.dictionary.values.distinctBy { it.word.lowercase() }.size.coerceAtLeast(1)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = SigniaBluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Señas Aprendidas", fontSize = 14.sp, color = ThemeManager.textColor)
                        }
                        Text(
                            text = "${ProgressManager.completedSignsCount} / $totalSignsInDict",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ThemeManager.textColor
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = if (ThemeManager.isDarkMode) ThemeManager.inputBackgroundColor else SigniaLightLavender
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
                            Text("Racha Actual", fontSize = 14.sp, color = ThemeManager.textColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${StreakManager.currentStreak} Días",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ThemeManager.textColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ver racha",
                                tint = ThemeManager.subtextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = if (ThemeManager.isDarkMode) ThemeManager.inputBackgroundColor else SigniaLightLavender
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = SigniaBluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nivel", fontSize = 14.sp, color = ThemeManager.textColor)
                        }
                        Text(
                            text = "Principiante",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ThemeManager.textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =================================================================
            // TARJETA DE APARIENCIA (MODO OSCURO / MODO CLARO)
            // =================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Apariencia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ThemeManager.textColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { ThemeManager.isDarkMode = !ThemeManager.isDarkMode },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (ThemeManager.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Modo de apariencia",
                                tint = SigniaBluePrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (ThemeManager.isDarkMode) "Modo Oscuro" else "Modo Claro",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ThemeManager.textColor
                                )
                                Text(
                                    text = if (ThemeManager.isDarkMode) "Activo (Toca para cambiar a claro)" else "Activo (Toca para cambiar a oscuro)",
                                    fontSize = 12.sp,
                                    color = ThemeManager.subtextColor
                                )
                            }
                        }

                        Switch(
                            checked = ThemeManager.isDarkMode,
                            onCheckedChange = { ThemeManager.isDarkMode = it },
                            thumbContent = {
                                Icon(
                                    imageVector = if (ThemeManager.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SigniaBluePrimary,
                                uncheckedThumbColor = SigniaDarkPurple,
                                uncheckedTrackColor = SigniaLightLavender
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Cargar Foto de Perfil
            OutlinedButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ThemeManager.cardBackgroundColor,
                    contentColor = ThemeManager.textColor
                )
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = ThemeManager.textColor)
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

/**
 * Diálogo de edición de foto de perfil (Rotar 90°).
 */
@Composable
fun ProfilePhotoEditorDialog(
    initialBitmap: Bitmap,
    onDismiss: () -> Unit,
    onSave: (Bitmap) -> Unit
) {
    var workingBitmap by remember { mutableStateOf(initialBitmap) }

    fun rotateImage() {
        val matrix = android.graphics.Matrix().apply { postRotate(90f) }
        workingBitmap = Bitmap.createBitmap(
            workingBitmap, 0, 0,
            workingBitmap.width, workingBitmap.height,
            matrix, true
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Foto de Perfil",
                fontWeight = FontWeight.Bold,
                color = ThemeManager.textColor
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ThemeManager.cardBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = workingBitmap.asImageBitmap(),
                        contentDescription = "Vista previa",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { rotateImage() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SigniaBluePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rotar 90°")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(workingBitmap) },
                colors = ButtonDefaults.buttonColors(containerColor = SigniaBluePrimary)
            ) {
                Text("Guardar y Aplicar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ThemeManager.subtextColor)
            }
        },
        containerColor = ThemeManager.backgroundColor
    )
}

/**
 * Diálogo para ver la foto de perfil en grande con Zoom interactivo y arrastre.
 */
@Composable
fun ProfileZoomDialog(
    bitmap: ImageBitmap,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = SigniaBluePrimary, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text("Mi foto de perfil ", fontWeight = FontWeight.Bold, color = ThemeManager.textColor)
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Foto ampliada con zoom",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        },
        containerColor = ThemeManager.backgroundColor
    )
}