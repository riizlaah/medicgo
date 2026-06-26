package com.example.medicgo

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                        Text(
                            "Medic-Go",
                            color = Color.White,
                            fontSize = typ().displaySmall.fontSize,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                        LazyColumn(
                            Modifier
                                .weight(1f)
                                .clip(corner())
                                .background(Color.White)
                                .padding(24.dp)
                        ) {
                            item {

                                var username by remember { mutableStateOf("") }
                                var fullName by remember { mutableStateOf("") }
                                var email by remember { mutableStateOf("") }
                                var phone by remember { mutableStateOf("") }
                                var password by remember { mutableStateOf("") }
                                var password2 by remember { mutableStateOf("") }
                                var errMsg by remember { mutableStateOf("") }
                                var loading by remember { mutableStateOf(false) }
                                val scope = rememberCoroutineScope()


                                OutlinedTextField(username, {username = it}, Modifier.fillMaxWidth(), label = {Text("Username")}, singleLine = true)
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(fullName, {fullName = it}, Modifier.fillMaxWidth(), label = {Text("Full Name")}, singleLine = true)
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(email, {email = it}, Modifier.fillMaxWidth(), label = {Text("Email")}, singleLine = true)
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(phone, {phone = it}, Modifier.fillMaxWidth(), label = {Text("Phone Number")}, singleLine = true)
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(password, {password = it}, Modifier.fillMaxWidth(), label = {Text("Password")}, singleLine = true, visualTransformation = PasswordVisualTransformation())
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(password2, {password2 = it}, Modifier.fillMaxWidth(), label = {Text("Password Confirmatiopn")}, singleLine = true, visualTransformation = PasswordVisualTransformation())
                                Spacer(Modifier.height(12.dp))
                                ErrText(errMsg)
                                GradBtn({
                                    if(username.isBlank()) {
                                        errMsg = "Username required"
                                        return@GradBtn
                                    }
                                    if(fullName.isBlank()) {
                                        errMsg = "Full Name required"
                                        return@GradBtn
                                    }
                                    if(email.isBlank()) {
                                        errMsg = "Email required"
                                        return@GradBtn
                                    }
                                    if(phone.isBlank()) {
                                        errMsg = "Phone required"
                                        return@GradBtn
                                    }
                                    if(password.isBlank()) {
                                        errMsg = "Password required"
                                        return@GradBtn
                                    }
                                    if(password.length < 8) {
                                        errMsg = "Password must have 8 characters or more"
                                        return@GradBtn
                                    }
                                    if(password != password2) {
                                        errMsg = "Password confirmation not same"
                                        return@GradBtn
                                    }
                                    errMsg = ""
                                    scope.launch {
                                        loading = true
                                        when(val msg = HttpClient.register(username, fullName, email, phone, password)) {
                                            "ok" -> {finish()}
                                            else -> errMsg = msg
                                        }
                                        loading = false
                                    }
                                }, loading, Modifier.fillMaxWidth().clip(corner(50))) {
                                    Text("Register")
                                }
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Already have an account?")
                                    Spacer(Modifier.width(8.dp))
                                    TextButton({finish()}) { Text("Login") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
