package com.example.app_signia

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.UserProfileChangeRequest
import java.io.File
import java.io.FileOutputStream

/**
 * PANTALLA DE EDITAR PERFIL (EditarPerfilScreen.kt)
 * Fiel al diseño de referencia con persistencia en ProfileManager y switches estilizados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val currentUser = auth.currentUser

    var nombreCompleto by remember {
        mutableStateOf(ProfileManager.getFullName(context, currentUser?.displayName ?: "Valeria Mendoza"))
    }
    var nombreUsuario by remember {
        mutableStateOf(ProfileManager.getUsername(context, "@" + (currentUser?.email?.substringBefore("@") ?: "valeria_kp")))
    }
    var correoElectronico by remember {
        mutableStateOf(currentUser?.email ?: "valeria.mendoza@gmail.com")
    }
    var ubicacion by remember {
        mutableStateOf(ProfileManager.getLocation(context, "Lima, Perú"))
    }

    var selectedRole by remember {
        mutableStateOf(ProfileManager.getRoleIndex(context, 0))
    }

    // Switches estilizados como modo oscuro/claro
    var cameraPrecision by remember {
        mutableStateOf(ProfileManager.getCameraPrecision(context, true))
    }
    var highContrast3d by remember {
        mutableStateOf(ProfileManager.getHighContrast(context, false))
    }

    // Auto-guardado en tiempo real para Experiencia y Accesibilidad
    LaunchedEffect(selectedRole) {
        ProfileManager.saveProfile(context, nombreCompleto, nombreUsuario, ubicacion, selectedRole)
    }

    LaunchedEffect(cameraPrecision, highContrast3d) {
        ProfileManager.saveAccessibility(context, cameraPrecision, highContrast3d)
    }

    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        val savedBitmap = loadSavedProfilePhotoForEdit(context)
        if (savedBitmap != null) {
            profileBitmap = savedBitmap.asImageBitmap()
        }
    }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Perfil",
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
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SigniaBluePrimary.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap!!,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.mascot_racha),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar principal con borde más grueso (sin botón de cámara)
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .border(4.dp, SigniaBluePrimary, CircleShape),
                shape = CircleShape,
                color = ThemeManager.cardBackgroundColor,
                shadowElevation = 6.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(SigniaBluePrimary.copy(alpha = 0.4f), ThemeManager.cardBackgroundColor)
                        )
                    )
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.mascot_racha),
                            contentDescription = "Avatar por defecto",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = nombreCompleto,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeManager.textColor
            )
            Text(
                text = "$nombreUsuario • 📍 $ubicacion",
                fontSize = 13.sp,
                color = ThemeManager.subtextColor
            )

            Spacer(modifier = Modifier.height(26.dp))

            // SECCIÓN: Información Personal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = SigniaBluePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Información Personal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeManager.textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Nombre Completo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ThemeManager.subtextColor)
                        OutlinedTextField(
                            value = nombreCompleto,
                            onValueChange = { nombreCompleto = it },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = SigniaBluePrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SigniaBluePrimary,
                                unfocusedBorderColor = ThemeManager.subtextColor.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Nombre de Usuario", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ThemeManager.subtextColor)
                        OutlinedTextField(
                            value = nombreUsuario,
                            onValueChange = { nombreUsuario = it },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = SigniaBluePrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SigniaBluePrimary,
                                unfocusedBorderColor = ThemeManager.subtextColor.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Correo Electrónico", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ThemeManager.subtextColor)
                        OutlinedTextField(
                            value = correoElectronico,
                            onValueChange = { correoElectronico = it },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SigniaBluePrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SigniaBluePrimary,
                                unfocusedBorderColor = ThemeManager.subtextColor.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Ubicación / Región", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ThemeManager.subtextColor)
                        OutlinedTextField(
                            value = ubicacion,
                            onValueChange = { ubicacion = it },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SigniaBluePrimary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SigniaBluePrimary,
                                unfocusedBorderColor = ThemeManager.subtextColor.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (nombreCompleto.isNotBlank()) {
                                ProfileManager.saveProfile(context, nombreCompleto, nombreUsuario, ubicacion, selectedRole)
                                if (currentUser != null) {
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(nombreCompleto)
                                        .build()
                                    currentUser.updateProfile(profileUpdates)
                                }
                                Toast.makeText(context, "¡Cambios guardados con éxito!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Por favor ingresa un nombre válido", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SigniaBluePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // SECCIÓN: Experiencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = SigniaBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Experiencia",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeManager.textColor
                    )
                }
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ThemeManager.subtextColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Rol en la Comunidad",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ThemeManager.subtextColor
                    )

                    RoleOptionItemEdit(
                        title = "Oyente Aprendiendo",
                        subtitle = "Estudiante o entusiasta de inclusión",
                        selected = selectedRole == 0,
                        onClick = { selectedRole = 0 }
                    )
                    RoleOptionItemEdit(
                        title = "Persona Sorda / Nativo LSP",
                        subtitle = "Parte del corazón de la comunidad",
                        selected = selectedRole == 1,
                        onClick = { selectedRole = 1 }
                    )
                    RoleOptionItemEdit(
                        title = "Educador / Intérprete",
                        subtitle = "Docencia y facilitación bilingüe",
                        selected = selectedRole == 2,
                        onClick = { selectedRole = 2 }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (ThemeManager.isDarkMode) ThemeManager.inputBackgroundColor else SigniaSoftLilac.copy(alpha = 0.3f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.mascot_racha),
                                contentDescription = "Mascota Racha",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = "Racha de ${StreakManager.currentStreak} días",
                                fontWeight = FontWeight.Bold,
                                color = ThemeManager.textColor,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = SigniaBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // SECCIÓN: Accesibilidad y Práctica (Estilizados como modo oscuro/claro)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = SigniaBluePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Accesibilidad y Práctica",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeManager.textColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Switch 1: Cámara de Alta Precisión
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { cameraPrecision = !cameraPrecision },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Cámara de Alta Precisión", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ThemeManager.textColor)
                            Text(text = "Detecta micro-gestos en manos y rostro", fontSize = 12.sp, color = ThemeManager.subtextColor)
                        }
                        Switch(
                            checked = cameraPrecision,
                            onCheckedChange = { cameraPrecision = it },
                            thumbContent = {
                                Icon(
                                    imageVector = if (cameraPrecision) Icons.Default.CameraAlt else Icons.Default.Camera,
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

                    // Switch 2: Alto Contraste para Avatar 3D
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { highContrast3d = !highContrast3d },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Alto Contraste para Avatar 3D", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ThemeManager.textColor)
                            Text(text = "Bordes definidos para baja visión", fontSize = 12.sp, color = ThemeManager.subtextColor)
                        }
                        Switch(
                            checked = highContrast3d,
                            onCheckedChange = { highContrast3d = it },
                            thumbContent = {
                                Icon(
                                    imageVector = if (highContrast3d) Icons.Default.Contrast else Icons.Default.Brightness6,
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

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Cancelar", fontWeight = FontWeight.Bold, color = ThemeManager.textColor, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RoleOptionItemEdit(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ThemeManager.textColor)
            Text(text = subtitle, fontSize = 11.sp, color = ThemeManager.subtextColor)
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = SigniaBluePrimary)
        )
    }
}

private fun loadSavedProfilePhotoForEdit(context: android.content.Context): Bitmap? {
    return try {
        val file = File(context.filesDir, "profile_photo.jpg")
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else null
    } catch (e: Exception) {
        null
    }
}
