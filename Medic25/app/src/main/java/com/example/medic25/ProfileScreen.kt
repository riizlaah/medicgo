package com.example.medic25

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Profile(modifier: Modifier, onBack: () -> Unit) {
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        user = HttpClient.me()
    }

    Button(
        onBack,
        colors = ButtonDefaults.buttonColors(Color.Transparent, Color.White),
        border = BorderStroke(
            1.dp, Color.White,
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(top = 12.dp, start = 12.dp)
    ) {
        Text("< Back")
    }
    Text(
        "Profile",
        fontSize = MaterialTheme.typography.displayMedium.fontSize,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(start = 12.dp)
    )
    Spacer(Modifier.height(24.dp))
    Column(modifier) {
        if(user == null) return@Column
        Text("Full Name : ${user!!.fullName}")
        Text("Email : ${user!!.email}")
        Text("Phone : ${user!!.phone}")
        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))
        Text("Educations", fontWeight = FontWeight.Bold)
        Text("Add education details...")
        Text("To be added in future phases")
        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))
        Text("Experiences", fontWeight = FontWeight.Bold)
        Text("Work history...")
        Text("To be added in future phases")
    }
}