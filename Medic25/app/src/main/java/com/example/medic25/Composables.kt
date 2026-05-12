package com.example.medic25

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.medic25.ui.theme.Cyan
import com.example.medic25.ui.theme.DarkGreen

@Composable
fun LoadingOrContent(loading: Boolean, content: @Composable () -> Unit, color: Color = Color.White, size: Dp = 24.dp) {
    if(loading) {
        CircularProgressIndicator(color = color, modifier = Modifier.size(size))
    } else {
        content()
    }
}

@Composable
fun GradientBtn(onClick: () -> Unit, gradient: Brush, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Button(
        onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(gradient)
    ) { content() }
}