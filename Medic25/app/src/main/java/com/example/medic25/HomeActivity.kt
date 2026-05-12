package com.example.medic25

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.medic25.ui.theme.Cyan
import com.example.medic25.ui.theme.DarkGreen
import com.example.medic25.ui.theme.Medic25Theme
import kotlinx.coroutines.delay

class HomeActivity : ComponentActivity() {
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
                    val filters = listOf("all", "general", "specialist")
                    var selectedFilter by remember { mutableStateOf(filters[0]) }
                    val doctors = remember { mutableStateListOf<Doctor>() }

                    LaunchedEffect(Unit) {
                        doctors.addAll(HttpClient.getDoctors(""))
                    }

                    LaunchedEffect(search) {
                        delay(500)
                        doctors.clear()
                        doctors.addAll(HttpClient.getDoctors(search))
                    }

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Cyan, DarkGreen)
                                )
                            )
                    ) {
                        Text(
                            "Medic-Go",
                            fontSize = MaterialTheme.typography.displaySmall.fontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Discovery",
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            OutlinedTextField(
                                search,
                                { search = it },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                trailingIcon = {
                                    Icon(
                                        painterResource(R.drawable.search),
                                        contentDescription = "Search"
                                    )
                                })
                            LazyRow(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                items(filters) { name ->
                                    Button(
                                        {},
                                        colors = ButtonDefaults.buttonColors(
                                            if (selectedFilter == name) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            if (selectedFilter == name) Color.White else MaterialTheme.colorScheme.primary
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(name.uppercase())
                                    }
                                }
                            }
                            LazyColumn(Modifier
                                .weight(1f)
                                .padding(12.dp)) {
                                items(doctors) { item ->
                                    Column(
                                        Modifier
                                            .padding(vertical = 12.dp)
                                            .fillMaxWidth()
                                            .shadow(3.dp, RoundedCornerShape(12.dp))
                                            .clip(
                                                RoundedCornerShape(12.dp)
                                            )
                                            .background(Color.White)
                                            .padding(16.dp)
                                            .clickable(onClick = {
                                                val intent =
                                                    Intent(ctx, DoctorDetailActivity::class.java)
                                                intent.putExtra("id", item.id)
                                                ctx.startActivity(intent)
                                            })
                                    ) {
                                        Text(
                                            item.name,
                                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(item.specialty)
                                        Spacer(Modifier.height(24.dp))
                                        Text("${item.experience} of experience")
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                painterResource(R.drawable.location),
                                                contentDescription = "Location"
                                            )
                                            Text(item.location)
                                        }
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            GradientBtn({}, Brush.horizontalGradient(listOf(Cyan, DarkGreen)), Modifier.weight(1f)) {
                                                Text("BOOK NOW")
                                            }
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
        }
    }
}