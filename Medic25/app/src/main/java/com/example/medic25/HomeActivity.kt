package com.example.medic25

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.medic25.ui.theme.Cyan
import com.example.medic25.ui.theme.DarkGreen
import com.example.medic25.ui.theme.Medic25Theme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Medic25Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                        var selectedTab by remember { mutableIntStateOf(intent.getIntExtra("selectedTab", 0)) }
                        val tabs = listOf(
                            Pair("Explore", R.drawable.search),
                            Pair("My Job", R.drawable.service_toolbox),
                            Pair("Profile", R.drawable.person),
                        )
                        val stacks = remember { mutableStateListOf(0) }

                        LaunchedEffect(Unit) {
                            HttpClient.getSavedDoctors()
                        }

                        BackHandler(enabled = stacks.isNotEmpty()) {
                            selectedTab = stacks.removeAt(stacks.lastIndex)
                        }


                        val modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(Color.White)
                            .padding(24.dp)
                        when(selectedTab) {
                            0 -> Discovery(modifier)
                            1 -> MyJob(modifier)
                            2 -> Profile(modifier, {selectedTab = if(stacks.isEmpty()) 0 else stacks.removeAt(stacks.lastIndex)})
                        }
                        if(selectedTab == 2) return@Column
                        PrimaryTabRow(selectedTab, Modifier.fillMaxWidth()) {
                            tabs.forEachIndexed { index, (text, iconR) ->
                                Tab(selectedTab == index, {
                                    stacks.add(selectedTab)
                                    selectedTab = index
                                }, Modifier.padding(vertical = 12.dp)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(painterResource(iconR), contentDescription = text)
                                        Text(text)
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