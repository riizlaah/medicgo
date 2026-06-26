package com.example.medicgo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

class DoctorDetailActivity : ComponentActivity() {
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
                        var doctor by remember { mutableStateOf<Doctor?>(null) }
                        var loading by remember { mutableStateOf(false) }
                        val tabs =  listOf("About Doctor", "Expertise",)
                        var selectedIdx by remember { mutableIntStateOf(0) }
                        val ctx = LocalContext.current
                        val scope = rememberCoroutineScope()

                        LaunchedEffect(Unit) {
                            doctor = HttpClient.getDoctor(intent.getIntExtra("id", 0))
                        }

                        OutlinedButton({ finish() }, border = BorderStroke(1.dp, Color.White)) {
                            Text("Back")
                        }
                        Spacer(Modifier.height(24.dp))

                        if(doctor == null) return@Column
                        val doc = doctor!!

                        Text(
                            doc.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = typ().displaySmall.fontSize,
                            color = Color.White
                        )


                        Column(
                            Modifier
                                .padding(top = 24.dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .background(Color.White)
                                .padding(24.dp)
                        ) {
                            Text(
                                doc.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = typ().headlineMedium.fontSize
                            )
                            Text(doc.specialty, fontSize = typ().headlineMedium.fontSize)
                            SecondaryTabRow(selectedIdx) {
                                tabs.forEachIndexed { i, name ->
                                    Tab(selectedIdx == i, {selectedIdx = i}, Modifier.padding(8.dp)) {
                                        Text(name)
                                    }
                                }
                            }
                            LazyColumn(Modifier.weight(1f)) {
                                if(selectedIdx == 0) {
                                    item {
                                        Text(
                                            "Doctor Overview",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = typ().headlineSmall.fontSize
                                        )
                                        Text(doc.description)
                                    }
                                } else {
                                    items(doc.expertises) { item ->
                                        Text(
                                            item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = typ().headlineSmall.fontSize
                                        )
                                        Text(item.content)
                                        Spacer(Modifier.height(24.dp))
                                    }
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GradBtn({
                                    scope.launch {
                                        loading = true
                                        HttpClient.bookDoctor(doc.id)
                                        loading = false
                                    }
                                }, loading, Modifier.clip(corner(50)).weight(1f)) {
                                    Text("BOOK NOW")
                                }
                                GradBtn({}, Modifier.clip(corner(50))) {
                                    Text("SHARE")
                                }
                                BookmarkToggle(doc.id)
                            }
                        }
                    }
                }
            }
        }
    }
}
