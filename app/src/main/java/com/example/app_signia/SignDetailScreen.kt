package com.example.app_signia

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.app_signia.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * RF09 - ANIMACIÓN EXPLICATIVA DE CADA SEÑA Y MANEJO DE ERRORES DE CARGA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignDetailScreen(
    navController: NavController,
    wordKey: String
) {
    val context = LocalContext.current
    val currentUser = remember { Firebase.auth.currentUser }

    val signDef = remember(wordKey) {
        SignDictionary.dictionary[wordKey.lowercase()]
    }

    var isPlaying by remember { mutableStateOf(true) }
    var hasMediaError by remember { mutableStateOf(false) }

    val rawResourceId = remember(signDef?.videoResName) {
        val resName = signDef?.videoResName ?: "hola"
        if (resName.isNotBlank()) {
            val id = context.resources.getIdentifier(resName, "raw", context.packageName)
            if (id != 0) id else R.raw.hola
        } else {
            R.raw.hola
        }
    }

    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = signDef?.displayWord ?: "Detalle de Seña",
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
        if (signDef == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontró información para esta seña.",
                    color = SigniaError,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ThemeManager.inputBackgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasMediaError) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = SigniaError,
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No se pudo cargar el video explicativo",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeManager.textColor
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { hasMediaError = false },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Reintentar", fontSize = 12.sp, color = ThemeManager.textColor)
                                    }
                                }
                            } else if (rawResourceId != 0) {
                                AndroidView(
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            val videoUri = Uri.parse("android.resource://${ctx.packageName}/$rawResourceId")
                                            setVideoURI(videoUri)
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                            }
                                            setOnCompletionListener {
                                                ProgressManager.markSignCompleted(currentUser?.uid, signDef.word)
                                                StreakManager.recordLessonCompleted(currentUser?.uid)
                                            }
                                            setOnErrorListener { _, _, _ ->
                                                hasMediaError = true
                                                true
                                            }
                                        }
                                    },
                                    update = { videoView ->
                                        if (isPlaying) {
                                            if (!videoView.isPlaying) videoView.start()
                                        } else {
                                            if (videoView.isPlaying) videoView.pause()
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = signDef.displayWord,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemeManager.textColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { isPlaying = !isPlaying },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeManager.heroCardColor),
                                modifier = Modifier.size(50.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Button(
                                onClick = {
                                    isPlaying = false
                                    hasMediaError = false
                                    isPlaying = true
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SigniaBluePrimary),
                                modifier = Modifier.size(50.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Repetir",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ThemeManager.cardBackgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Descripción del movimiento:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeManager.textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = signDef.description,
                            fontSize = 13.sp,
                            color = ThemeManager.subtextColor,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Pasos para realizar la seña:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeManager.textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        signDef.gestureSteps.forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SigniaBluePrimary,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step,
                                    fontSize = 13.sp,
                                    color = ThemeManager.textColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val isAlreadyCompleted = ProgressManager.completedSignsList.contains(signDef.word.lowercase())

                Button(
                    onClick = {
                        ProgressManager.markSignCompleted(currentUser?.uid, signDef.word)
                        StreakManager.recordLessonCompleted(currentUser?.uid) { newStreak ->
                            android.widget.Toast.makeText(
                                context,
                                "¡Seña aprendida! Racha actual: $newStreak días 🔥",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAlreadyCompleted) SigniaBluePrimary else ThemeManager.heroCardColor
                    )
                ) {
                    Icon(
                        imageVector = if (isAlreadyCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAlreadyCompleted) "Seña Aprendida ✓" else "Marcar Seña como Aprendida",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
