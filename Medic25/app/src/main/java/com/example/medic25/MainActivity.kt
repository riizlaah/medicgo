package com.example.medic25

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.medic25.ui.theme.Cyan
import com.example.medic25.ui.theme.DarkGreen
import com.example.medic25.ui.theme.Medic25Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Medic25Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var username by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    val scope = rememberCoroutineScope()
                    var loading by remember { mutableStateOf(false) }
                    var errMsg by remember { mutableStateOf("") }
                    val ctx = LocalContext.current

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Cyan, DarkGreen)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                        Spacer(Modifier.height(48.dp))
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.White)
                                .padding(24.dp)
                        ) {
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
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation()
                            )
                            ErrText(errMsg, Modifier.fillMaxWidth().padding(top = 12.dp))
                            Spacer(Modifier.height(24.dp))
                            GradientBtn({
                                if(username.isEmpty()) {
                                    errMsg = "Username is required"
                                    return@GradientBtn
                                }
                                if(password.isEmpty()) {
                                    errMsg = "Password is required"
                                    return@GradientBtn
                                }
                                errMsg = ""
                                scope.launch {
                                    loading = true
                                    when(val msg = HttpClient.login(username, password)) {
                                        "ok" -> {
                                            val intent = Intent(ctx, HomeActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                            ctx.startActivity(intent)
                                        }
                                        else -> {errMsg = msg}
                                    }
                                    loading = false
                                }
                            }, Modifier.fillMaxWidth()) {
                                LoadingOrContent(loading, {
                                    Text("Login", fontWeight = FontWeight.Bold)
                                })
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Don't have an account?")
                                TextButton({
                                    val intent = Intent(ctx, RegisterActivity::class.java)
                                    ctx.startActivity(intent)
                                }) { Text("Register Now") }
                            }
                        }
                    }
                }
            }
        }
    }
}
