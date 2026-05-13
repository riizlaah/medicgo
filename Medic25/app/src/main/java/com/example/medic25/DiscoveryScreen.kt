package com.example.medic25

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.util.fastAny
import com.example.medic25.ui.theme.Cyan
import com.example.medic25.ui.theme.DarkGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Discovery(modifier: Modifier = Modifier) {
    var search by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val filters = listOf("all", "general", "specialist")
    var selectedFilter by remember { mutableStateOf(filters[0]) }
    val doctors = remember { mutableStateListOf<Doctor>() }


    LaunchedEffect(Unit) {
        doctors.clear()
        doctors.addAll(HttpClient.getDoctors(search, selectedFilter))
    }

    LaunchedEffect(search) {
        delay(500)
        doctors.clear()
        doctors.addAll(HttpClient.getDoctors(search, selectedFilter))
    }

    LaunchedEffect(selectedFilter) {
        doctors.clear()
        doctors.addAll(HttpClient.getDoctors(search, selectedFilter))
    }

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
    Spacer(Modifier.height(12.dp))
    Column(modifier) {
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
                .padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filters) { name ->
                val mod = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(Cyan, DarkGreen)))
                Button(
                    { selectedFilter = name },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = if(selectedFilter == name) mod else Modifier.border(2.dp, Color.Gray,
                        RoundedCornerShape(50))
                ) {
                    Text(
                        name.uppercase(),
                        color = if (selectedFilter == name) Color.White else Color.Gray
                    )
                }
            }
        }
        LazyColumn(
            Modifier
                .weight(1f)
                .padding(top = 12.dp)
        ) {
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
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GradientBtn(
                            {

                            },
                            Modifier.weight(1f)
                        ) {
                            Text("BOOK NOW")
                        }
                        Spacer(Modifier.width(12.dp))
                        IconButton({
                            scope.launch {
                                if(HttpClient.savedDoctors.fastAny { it.doctorId == item.id }) {
                                    HttpClient.removeSavedDoctor(item.id)
                                } else {
                                    HttpClient.saveDoctor(item.id)
                                }
                            }
                        }) {
                            Icon(
                                painterResource(if(HttpClient.savedDoctors.fastAny { it.doctorId == item.id }) R.drawable.bookmark_remove else R.drawable.bookmark_outline),
                                contentDescription = "Save"
                            )
                        }
                    }
                }
            }
        }
    }
}