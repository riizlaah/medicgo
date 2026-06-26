package com.example.medicgo

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DiscoveryScreen(modifier: Modifier) {

    var search by rememberSaveable { mutableStateOf("") }
    val specialties = listOf("all", "general", "specialist")
    var specialty by rememberSaveable { mutableStateOf(specialties[0]) }
    val doctors = remember { mutableStateListOf<Doctor>() }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (doctors.isEmpty()) doctors.addAll(HttpClient.getDoctors(specialty, search))
    }

    LaunchedEffect(search) {
        delay(500.milliseconds)
        val arr = HttpClient.getDoctors(specialty, search)
        doctors.clear()
        doctors.addAll(arr)
    }

    LaunchedEffect(specialty) {
        val arr = HttpClient.getDoctors(specialty, search)
        doctors.clear()
        doctors.addAll(arr)
    }

    Text(
        "Discovery",
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
        OutlinedTextField(
            search,
            { search = it },
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            singleLine = true,
            label = { Text("Search") })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            specialties.forEach { name ->
                if (specialty == name) {
                    GradBtn({ specialty = name }, Modifier.clip(corner(50))) {
                        Text(name.uppercase())
                    }
                } else {
                    OutlinedButton({ specialty = name }, shape = corner(50)) {
                        Text(name.uppercase())
                    }
                }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(doctors) { item ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, corner())
                        .clip(corner())
                        .background(Color.White)
                        .padding(12.dp)
                        .clickable(onClick = {
                            val int = Intent(ctx, DoctorDetailActivity::class.java).apply {
                                putExtra("id", item.id)
                            }
                            ctx.startActivity(int)
                        })
                ) {
                    var loading by remember { mutableStateOf(false) }

                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = typ().titleLarge.fontSize
                    )
                    Text(item.specialty, fontSize = typ().titleLarge.fontSize)
                    Spacer(Modifier.height(12.dp))
                    Text("${item.experience} of experience")
                    Text(item.location)
                    Row(Modifier.fillMaxWidth()) {
                        GradBtn({
                            scope.launch {
                                loading = true
                                HttpClient.bookDoctor(item.id)
                                loading = false
                            }
                        }, loading, Modifier
                            .clip(corner(50))
                            .weight(1f)) {
                            Text("BOOK NOW")
                        }
                        BookmarkToggle(item.id)
                    }
                }
            }
        }
    }

}