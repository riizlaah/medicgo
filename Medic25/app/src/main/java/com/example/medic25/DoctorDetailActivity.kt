package com.example.medic25

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medic25.ui.theme.Medic25Theme
import kotlin.math.exp

class DoctorDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Medic25Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var search by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    val scope = rememberCoroutineScope()
                    val ctx = LocalContext.current
                    var doctor by remember { mutableStateOf<Doctor?>(null) }
                    var selectedTab by remember { mutableIntStateOf(0) }
                    val tabs = listOf("About Doctor", "Expertise")
                    var selectedTabPrimary by remember { mutableIntStateOf(0) }




                    LaunchedEffect(Unit) {
                        doctor = HttpClient.getDoctorById(intent.getIntExtra("id", 0))
                    }

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
                    ) {
                        Button(
                            { finish() },
                            colors = ButtonDefaults.buttonColors(Color.Transparent, Color.White),
                            border = BorderStroke(
                                1.dp, Color.White,
                            )
                        ) {
                            Text("< Back")
                        }
                        if (doctor == null) return@Column
                        Spacer(Modifier.height(36.dp))
                        Text(
                            doctor!!.name,
                            fontSize = MaterialTheme.typography.displaySmall.fontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            Text(
                                doctor!!.name,
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                doctor!!.specialty,
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                            )
                            SecondaryTabRow(selectedTab) {
                                tabs.forEachIndexed { index, string ->
                                    Tab(
                                        index == selectedTab,
                                        { selectedTab = index },
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(string)
                                    }
                                }
                            }
                            LazyColumn(Modifier.weight(1f)) {
                                if (selectedTab == 0) {
                                    item {
                                        Text("Doctor Overview", fontWeight = FontWeight.Bold)
                                        Text(doctor!!.description)
                                    }
                                } else {
                                    item {
                                        Text("Doctor Expertise", fontWeight = FontWeight.Bold)
                                        doctor!!.expertise.forEach { exp ->
                                            Text(exp.title, fontWeight = FontWeight.Medium)
                                            Text(exp.content)
                                            Spacer(Modifier.height(12.dp))
                                        }
                                    }
                                }
                            }
                            HorizontalDivider()
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button({}, Modifier.weight(1f)) { Text("BOOK NOW") }
                                Spacer(Modifier.width(12.dp))
                                Button({}, Modifier.weight(1f)) { Text("SHARE") }
                                Spacer(Modifier.width(12.dp))
                                IconButton({}) {
                                    Icon(
                                        painterResource(R.drawable.bookmark_outline),
                                        contentDescription = "Save"
                                    )
                                }
                            }

                        }

                    }
                }
            }
        }
    }
}
