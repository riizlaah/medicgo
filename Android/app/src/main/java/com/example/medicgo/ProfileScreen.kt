package com.example.medicgo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ProfileScreen(modifier: Modifier, onBack: () -> Unit, onLogout: () -> Unit) {
    LaunchedEffect(Unit) {
        HttpClient.profile()
    }
    OutlinedButton(onBack, border = BorderStroke(1.dp, Color.White)) {
        Text("Back")
    }
    Spacer(Modifier.height(24.dp))

    Text(
        "Profile",
        fontWeight = FontWeight.Bold,
        fontSize = typ().displaySmall.fontSize,
        color = Color.White
    )
    Column(
        modifier
            .padding(top = 24.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        if(HttpClient.profile == null) return@Column
        val profile = HttpClient.profile!!

        Text("Full Name: ${profile.fullName}")
        Text("Email: ${profile.email}")
        Text("Phone: ${profile.phone}")
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Educations", fontWeight = FontWeight.Bold)
        Text("Educations details")
        Text("Educations details...")
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Experiences", fontWeight = FontWeight.Bold)
        Text("Experiences details")
        Text("Experiences details...")
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        OutlinedButton(onLogout) {
            Text("Log out")
        }
    }

}