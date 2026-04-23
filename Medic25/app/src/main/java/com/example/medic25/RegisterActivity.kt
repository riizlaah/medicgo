package com.example.medic25

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.example.medic25.ui.theme.Medic25Theme
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Medic25Theme {
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = {
                    SnackbarHost(snackbarHostState)
                }) { innerPadding ->
                    var fullname by remember { mutableStateOf("") }
                    var email by remember { mutableStateOf("") }
                    var phone by remember { mutableStateOf("") }
                    var username by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    var password2 by remember { mutableStateOf("") }
                    val scope = rememberCoroutineScope()
                    val ctx = LocalContext.current

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF009688),
                                        Color(0xFF1E6220)
                                    )
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            "Medic-Go",
                            fontSize = MaterialTheme.typography.displayMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        )
                        Spacer(Modifier.height(48.dp))
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(24.dp)
                        ) {
                            OutlinedTextField(
                                fullname,
                                { fullname = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Username") })
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                email,
                                { email = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Username") })
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                phone,
                                { phone = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Username") })
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                username,
                                { username = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Username") })
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                password,
                                { password = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                password2,
                                { password2 = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
                            Spacer(Modifier.height(24.dp))
                            Button(
                                {
                                    if(fullname.isEmpty()) return@Button
                                    if(username.isEmpty()) return@Button
                                    if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@Button
                                    if(!phone.isDigitsOnly()) return@Button
                                    if(password.length < 8) return@Button
                                    if(password != password2) return@Button
                                    scope.launch {
                                        when(val msg = HttpClient.register(username, fullname, email, phone, password)) {
                                            "ok" -> {finish()}
                                            else -> snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                            ) {
                                Text("Register")
                            }
                            Row(Modifier.fillMaxWidth()) {
                                Text("Already have an account?")
                                TextButton({finish()}) { Text("Login") }
                            }
                        }
                    }
                }
            }
        }
    }
}