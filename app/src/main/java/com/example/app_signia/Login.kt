package com.example.app_signia

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import android.R.attr.tint as tint
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image


// Colores del diseño
val SigniaGreen = Color(0xFF2A835F)
val SigniaBackground = Color(0xFFE8F5EF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(navController: NavController) {
    val auth = remember { Firebase.auth }
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

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

            // Campo de Correo
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
                    placeholder = { Text("ejemplo@correo.com",
                        color = Color.Black.copy(alpha = 0.6f)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Gray) },
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

            // Campo de Contraseña
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
                        onClick = { },
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
                        Text("Ingresa tu contraseña",
                            color = Color.Black.copy(alpha = 0.6f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Gray) },
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
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Error de acceso", Toast.LENGTH_SHORT).show()
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Entrar",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 16.sp)

                        Spacer(modifier = Modifier.width(8.dp))

                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // O continúa con
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

            // Google Button
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.8.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Placeholder simple para Google
                    Text("G ",
                        color = SigniaGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp)
                    Text("Google",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Apple Button
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apple", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Footer
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
