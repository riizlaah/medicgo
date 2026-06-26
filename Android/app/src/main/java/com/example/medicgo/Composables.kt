package com.example.medicgo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.medicgo.ui.theme.Blue1
import com.example.medicgo.ui.theme.Green1
import kotlinx.coroutines.launch

fun Modifier.grad(): Modifier {
    return background(Brush.horizontalGradient(listOf(Blue1, Green1)))
}

@Composable
fun typ(): Typography {
    return MaterialTheme.typography
}

fun corner(size: Dp = 12.dp): RoundedCornerShape {
    return RoundedCornerShape(size)
}

fun corner(size: Int): RoundedCornerShape {
    return RoundedCornerShape(size)
}

@Composable
fun GradBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick,
        modifier.grad(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        content = content
    )
}

@Composable
fun GradBtn(
    onClick: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Button(
        onClick,
        modifier.grad(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        LoadingOrC(loading, content)
    }
}

@Composable
fun LoadingOrC(loading: Boolean, content: @Composable () -> Unit) {
    if (loading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
    else content()
}

@Composable
fun ErrText(errMsg: String, modifier: Modifier = Modifier) {
    if (errMsg.isNotEmpty()) Text(
        errMsg,
        modifier.padding(vertical = 8.dp),
        textAlign = TextAlign.Center,
        color = Color.Red
    )
}

@Composable
fun BookmarkToggle(doctorId: Int) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val bookmarked = HttpClient.savedDoctors.any { it.doctorId == doctorId }

    IconButton(
        {
            scope.launch {
                if(bookmarked) {
                    val id = HttpClient.savedDoctors.first { it.doctorId == doctorId }.savedId
                    HttpClient.rmDoctor(id)
                } else {
                    HttpClient.addDoctor(doctorId)
                }
                HttpClient.getSavedDoctors()
            }
        },
        shape = CircleShape,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if(bookmarked) Green1 else Color.Transparent,
            contentColor = if(bookmarked) Color.White else Green1
        )
    ) {
        Icon(painterResource(if(bookmarked) R.drawable.bookmark_filled else R.drawable.bookmark), "Bookmark")
    }
}