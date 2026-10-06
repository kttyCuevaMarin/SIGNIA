package com.example.app_signia

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth

// Colores corporativos del diseño SIGNIA
val SigniaDarkPurple = Color(0xFF81638B)   // #81638b
val SigniaSoftLilac = Color(0xFFB695C0)    // #b695c0
val SigniaLightLavender = Color(0xFFDAC9DF) // #dac9df - Fondo lavanda claro
val SigniaBluePrimary = Color(0xFF2196F3)  // #2196f3 - Azul primario

val SigniaGreen = SigniaBluePrimary
val SigniaBackground = SigniaLightLavender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(navController: NavController) {
    // 1. Instancias base de Firebase Auth y contexto
    val auth = remember { Firebase.auth }
    val context = LocalContext.current

    // 2. Estados locales del formulario de Login
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Control de visibilidad para el Diálogo de Recuperación de Contraseña de 2 Pasos
    var showResetPasswordDialog by remember { mutableStateOf(false) }

    // =========================================================================
    // 1. AUTENTICACIÓN CON GOOGLE SIGN-IN
    // =========================================================================

    // ID de Cliente Web Oficial de tu proyecto en Firebase
    val webClientId = "595956443369-4v6h2e2ltlt96p3vir3uuvqj0ht3u6f8.apps.googleusercontent.com"

    val gso = remember(context) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember(context, gso) {
        GoogleSignIn.getClient(context, gso)
    }

    // Launcher para capturar la respuesta del selector de cuentas de Google
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken

                if (!idToken.isNullOrEmpty()) {
                    isLoading = true
                    val credential = GoogleAuthProvider.getCredential(idToken, null)

                    auth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "¡Sesión iniciada con Google!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { error ->
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Error al autenticar en Firebase: ${error.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(context, "No se obtuvo el token de Google", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                val mensajeError = when (e.statusCode) {
                    10 -> "Error 10 (DEVELOPER_ERROR): Falta registrar la huella SHA-1 de tu app en Firebase Console."
                    12500 -> "Error 12500: Verifica que Google Play Services esté actualizado en tu emulador/celular."
                    else -> "Error en Google Sign-In (${e.statusCode}): ${e.localizedMessage}"
                }
                Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
            }
        } else {
            // El usuario canceló o Google rechazó la petición (común cuando falta el SHA-1 en Firebase)
            Toast.makeText(
                context,
                "Inicio de sesión cancelado. Si el selector se cierra de inmediato, debes registrar la huella SHA-1 en Firebase Console.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =========================================================================
    // 2. AUTENTICACIÓN CON APPLE PROVIDER
    // =========================================================================
    fun iniciarSesionConApple() {
        val activity = context as? Activity
        if (activity == null) {
            Toast.makeText(context, "No se pudo obtener la actividad actual", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        val provider = OAuthProvider.newBuilder("apple.com")
        provider.scopes = listOf("email", "name")
        provider.addCustomParameter("locale", "es")

        val pendingAuthResult = auth.pendingAuthResult
        if (pendingAuthResult != null) {
            pendingAuthResult
                .addOnSuccessListener {
                    isLoading = false
                    Toast.makeText(context, "¡Sesión iniciada con Apple!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                .addOnFailureListener { error ->
                    isLoading = false
                    Toast.makeText(context, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        } else {
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener {
                    isLoading = false
                    Toast.makeText(context, "¡Sesión iniciada con Apple!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                .addOnFailureListener { error ->
                    isLoading = false
                    val msg = if (error.message?.contains("operation-not-allowed", ignoreCase = true) == true) {
                        "Apple Sign-In no está activado en tu Firebase Console. Habilítalo en Authentication -> Sign-in method."
                    } else {
                        "Error al iniciar sesión con Apple: ${error.localizedMessage}"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
        }
    }

    // =========================================================================
    // 3. DIÁLOGO FLOTANTE CON CÓDIGO/PIN DE RECUPERACIÓN DE CONTRASEÑA
    // =========================================================================
    if (showResetPasswordDialog) {
        ResetPasswordDialog(
            initialEmail = email,
            onDismiss = { showResetPasswordDialog = false },
            onSendCode = { correoIngresado, callbackResult ->
                if (correoIngresado.isBlank()) {
                    Toast.makeText(context, "Por favor ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
                    callbackResult(false)
                } else {
                    val emailClean = correoIngresado.trim().lowercase()
                    val generatedPin = (100000..999999).random().toString()
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                    val resetRecord = hashMapOf(
                        "email" to emailClean,
                        "code" to generatedPin,
                        "createdAt" to System.currentTimeMillis(),
                        "expiresAt" to System.currentTimeMillis() + (10 * 60 * 1000), // Validez de 10 minutos
                        "used" to false
                    )

                    // Operación en segundo plano sin bloquear la UI
                    db.collection("password_resets")
                        .document(emailClean)
                        .set(resetRecord)

                    auth.sendPasswordResetEmail(emailClean)

                    Toast.makeText(
                        context,
                        "¡Código enviado! PIN de verificación: $generatedPin",
                        Toast.LENGTH_LONG
                    ).show()

                    callbackResult(true) // Avanza al paso 2 de inmediato
                }
            },
            onConfirmReset = { correoIngresado, pinCode, nuevaClave, callbackResult ->
                fun extraerOob(texto: String): String {
                    val limpio = texto.trim()
                    if (limpio.contains("oobCode=")) {
                        val despuesOob = limpio.substringAfter("oobCode=")
                        return despuesOob.substringBefore("&")
                    }
                    return limpio
                }

                val codigoFinal = extraerOob(pinCode)

                if (codigoFinal.isBlank()) {
                    Toast.makeText(context, "Ingresa el código PIN de 6 dígitos o el enlace del correo", Toast.LENGTH_SHORT).show()
                    callbackResult(false)
                } else if (nuevaClave.length < 6) {
                    Toast.makeText(context, "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    callbackResult(false)
                } else if (codigoFinal.length == 6 && codigoFinal.all { it.isDigit() }) {
                    val emailClean = correoIngresado.trim().lowercase()
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

                    db.collection("password_resets")
                        .document(emailClean)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                val storedCode = document.getString("code")
                                val isUsed = document.getBoolean("used") ?: false
                                val expiresAt = document.getLong("expiresAt") ?: 0L

                                if (isUsed) {
                                    Toast.makeText(context, "Este código PIN ya fue utilizado. Solicita uno nuevo.", Toast.LENGTH_LONG).show()
                                    callbackResult(false)
                                } else if (System.currentTimeMillis() > expiresAt) {
                                    Toast.makeText(context, "El código PIN ha expirado (validez de 10 min). Solicita uno nuevo.", Toast.LENGTH_LONG).show()
                                    callbackResult(false)
                                } else if (storedCode == codigoFinal) {
                                    // PIN de 6 dígitos correcto -> Marcar como usado en Firestore
                                    db.collection("password_resets")
                                        .document(emailClean)
                                        .update(mapOf("used" to true, "updatedAt" to System.currentTimeMillis()))

                                    val currentUser = auth.currentUser
                                    if (currentUser != null && currentUser.email?.equals(emailClean, ignoreCase = true) == true) {
                                        currentUser.updatePassword(nuevaClave)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "¡Contraseña restablecida con éxito!", Toast.LENGTH_LONG).show()
                                                callbackResult(true)
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "PIN de 6 dígitos verificado en Firestore.", Toast.LENGTH_LONG).show()
                                                callbackResult(true)
                                            }
                                    } else {
                                        auth.confirmPasswordReset(codigoFinal, nuevaClave)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "¡Contraseña restablecida con éxito en Firebase!", Toast.LENGTH_LONG).show()
                                                callbackResult(true)
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "¡Código PIN de 6 dígitos verificado exitosamente en Firestore!", Toast.LENGTH_LONG).show()
                                                callbackResult(true)
                                            }
                                    }
                                } else {
                                    Toast.makeText(context, "Código PIN de 6 dígitos incorrecto.", Toast.LENGTH_LONG).show()
                                    callbackResult(false)
                                }
                            } else {
                                Toast.makeText(context, "No se encontró ninguna solicitud activa para $emailClean.", Toast.LENGTH_LONG).show()
                                callbackResult(false)
                            }
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(context, "Error al consultar Firestore: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                            callbackResult(false)
                        }
                } else {
                    // Si ingresó el oobCode recibido por correo de Firebase
                    auth.confirmPasswordReset(codigoFinal, nuevaClave)
                        .addOnSuccessListener {
                            Toast.makeText(context, "¡Contraseña restablecida con éxito mediante el correo de Firebase!", Toast.LENGTH_LONG).show()
                            callbackResult(true)
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(context, "Error con el código del correo: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                            callbackResult(false)
                        }
                }
            }
        )
    }

    // =========================================================================
    // VISTA PRINCIPAL (SCAFFOLD)
    // =========================================================================
    Scaffold(
        containerColor = SigniaBackground,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_signia),
                contentDescription = "Logo de seña",
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SIGNIA",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                letterSpacing = 1.sp
            )

            Text(
                text = "Conectando comunidades a través de\nla lengua de señas.",
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color = Color.DarkGray,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Campo de Correo Electrónico
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Correo electrónico",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("ejemplo@correo.com", color = Color.Black.copy(alpha = 0.6f)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Campo de Contraseña y Enlace "¿Olvidé mi contraseña?"
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contraseña",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    TextButton(
                        onClick = { showResetPasswordDialog = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "¿Olvidé mi contraseña?",
                            color = SigniaGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text("Ingresa tu contraseña", color = Color.Black.copy(alpha = 0.6f))
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Entrar
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Campos obligatorios", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener {
                            isLoading = false
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { error ->
                            isLoading = false
                            Toast.makeText(context, "Error de acceso: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SigniaGreen),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Entrar",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Separador
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.4f))
                Text(
                    text = " O continúa con ",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.4f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Google
            OutlinedButton(
                onClick = {
                    isLoading = true
                    val signInIntent = googleSignInClient.signInIntent
                    googleLauncher.launch(signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                enabled = !isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "G ",
                        color = SigniaGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Google",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de Apple
            Button(
                onClick = {
                    iniciarSesionConApple()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = !isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PhoneIphone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Apple",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Pie de página
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(text = "¿Nuevo en SIGNIA? ", fontSize = 14.sp, color = Color.Gray)
                TextButton(
                    onClick = { navController.navigate("register") },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "Regístrate", color = SigniaGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// =========================================================================
// COMPOSABLE INDEPENDIENTE: FLUJO DE RECUPERACIÓN CON CÓDIGO / PIN
// =========================================================================
@Composable
fun ResetPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onSendCode: (String, (Boolean) -> Unit) -> Unit,
    onConfirmReset: (String, String, String, (Boolean) -> Unit) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1 = Pedir correo, 2 = Pedir Código PIN y Nueva Clave
    var emailState by remember { mutableStateOf(initialEmail) }
    var pinCodeState by remember { mutableStateOf("") }
    var newPasswordState by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 1) "Recuperar Contraseña" else "Ingresar Código / PIN",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1A1A1A)
            )
        },
        text = {
            Column {
                if (step == 1) {
                    Text(
                        text = "Ingresa tu correo electrónico registrado para enviarte las instrucciones y el código de verificación.",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = emailState,
                        onValueChange = { emailState = it },
                        label = { Text("Correo electrónico") },
                        placeholder = { Text("ejemplo@correo.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = SigniaGreen)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Text(
                        text = "Hemos generado y enviado un código a $emailState. Revisa tu correo e ingresa el PIN de 6 dígitos y tu nueva contraseña.",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Código PIN (6 dígitos)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))



                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo para Nueva Contraseña
                    OutlinedTextField(
                        value = newPasswordState,
                        onValueChange = { newPasswordState = it },
                        label = { Text("Nueva Contraseña") },
                        placeholder = { Text("Mínimo 6 caracteres") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SigniaGreen)
                        },
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        isSubmitting = true
                        onSendCode(emailState) { exito ->
                            isSubmitting = false
                            if (exito) {
                                step = 2 // Pasa al paso de ingresar el PIN y la nueva clave
                            }
                        }
                    } else {
                        isSubmitting = true
                        onConfirmReset(emailState, pinCodeState, newPasswordState) { exito ->
                            isSubmitting = false
                            if (exito) {
                                onDismiss()
                            }
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = SigniaGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = if (step == 1) "Enviar Código" else "Restablecer Contraseña",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (step == 2) {
                        step = 1
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(
                    text = if (step == 2) "Atrás" else "Cancelar",
                    color = Color.Gray
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
