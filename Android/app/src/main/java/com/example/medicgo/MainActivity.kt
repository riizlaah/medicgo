package com.example.medicgo

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medicgo.ui.theme.MedicGoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HttpClient.prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        HttpClient.loadToken()
        enableEdgeToEdge()
        setContent {
            MedicGoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .grad(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val ctx = LocalContext.current

                        LaunchedEffect(Unit) {
                            if(HttpClient.profile()) {
                                val int = Intent(ctx, HomeActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                ctx.startActivity(int)
                            }
                        }

                        Text(
                            "Medic-Go",
                            color = Color.White,
                            fontSize = typ().displaySmall.fontSize,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(corner())
                                .background(Color.White)
                                .padding(24.dp)
                        ) {
                            var username by remember { mutableStateOf("") }
                            var password by remember { mutableStateOf("") }
                            var errMsg by remember { mutableStateOf("") }
                            var loading by remember { mutableStateOf(false) }
                            val scope = rememberCoroutineScope()


                            OutlinedTextField(username, {username = it}, Modifier.fillMaxWidth(), label = {Text("Username")}, singleLine = true)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(password, {password = it}, Modifier.fillMaxWidth(), label = {Text("Password")}, singleLine = true, visualTransformation = PasswordVisualTransformation())
                            Spacer(Modifier.height(12.dp))
                            ErrText(errMsg)
                            GradBtn({
                                if(username.isBlank()) {
                                    errMsg = "Username required"
                                    return@GradBtn
                                }
                                if(password.isBlank()) {
                                    errMsg = "Password required"
                                    return@GradBtn
                                }
                                errMsg = ""
                                scope.launch {
                                    loading = true
                                    when(val msg = HttpClient.login(username, password)) {
                                        "ok" -> {
                                            val int = Intent(ctx, HomeActivity::class.java).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            }
                                            ctx.startActivity(int)
                                        }
                                        else -> errMsg = msg
                                    }
                                    loading = false
                                }
                            }, loading, Modifier.fillMaxWidth().clip(corner(50))) {
                                Text("Login")
                            }
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Don't have an account?")
                                Spacer(Modifier.width(8.dp))
                                TextButton({
                                    val int = Intent(ctx, RegisterActivity::class.java)
                                    startActivity(int)
                                }) { Text("Register now") }
                            }
                        }
                    }
                }
            }
        }
    }
}
