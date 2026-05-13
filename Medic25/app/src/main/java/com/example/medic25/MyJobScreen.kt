package com.example.medic25

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.launch

@Composable
fun MyJob(modifier: Modifier) {
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
        "My Appointments",
        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = Modifier.padding(start = 12.dp)
    )
    Spacer(Modifier.height(24.dp))
    Column(modifier) {
        var selectedTab by remember { mutableIntStateOf(1) }
        val tabs = listOf("Saved Doctors", "Application List")
        val ctx = LocalContext.current
        var refreshing by remember { mutableStateOf(false) }
        val appointments = remember { mutableStateListOf<Appointment>() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(refreshing) {
            if(refreshing) {
                appointments.clear()
                appointments.addAll(HttpClient.getAppointments())
                refreshing = false
            }
        }

        LaunchedEffect(Unit) {
            appointments.clear()
            appointments.addAll(HttpClient.getAppointments())
        }

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
        if(selectedTab == 0) {
            LazyColumn(Modifier.weight(1f).padding(top = 12.dp)) {
                items(HttpClient.savedDoctors) {item ->
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
                            item.doctorName,
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
                                {},
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
                                    painterResource(R.drawable.bookmark_remove),
                                    contentDescription = "Save"
                                )
                            }
                        }
                    }
                }
            }
        } else {
            PullToRefreshBox(refreshing, {refreshing = true}, Modifier.weight(1f)) {
                LazyColumn(Modifier.fillMaxSize(1f).padding(top = 12.dp)) {
                    items(appointments) {item ->
                        Column(
                            Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    val intent =
                                        Intent(ctx, DoctorDetailActivity::class.java)
                                    intent.putExtra("id", item.id)
                                    ctx.startActivity(intent)
                                })
                        ) {
                            Text("Status : ${item.status}", Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(Color(
                                0xFFB8D1FF
                            )
                            ).padding(16.dp))
                            Column(Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .background(Color.White)
                                .padding(16.dp)
                            ) {
                                Text(
                                    item.doctorName,
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(item.specialty)
                                Spacer(Modifier.height(24.dp))
//                            Text("Payment Method : ${item.paymentMethod}")
                                Text(item.status, Modifier.clip(CircleShape).background(Color.LightGray).padding(16.dp))
                            }
                        }
                    }
                }
            }

        }
    }
}