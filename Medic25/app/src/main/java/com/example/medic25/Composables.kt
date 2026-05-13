package com.example.medic25

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.medic25.ui.theme.Cyan
import com.example.medic25.ui.theme.DarkGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp

@Composable
fun LoadingOrContent(
    loading: Boolean,
    content: @Composable () -> Unit,
    color: Color = Color.White,
    size: Dp = 24.dp
) {
    if (loading) {
        CircularProgressIndicator(color = color, modifier = Modifier.size(size))
    } else {
        content()
    }
}

@Composable
fun ErrText(errMsg: String, modifier: Modifier = Modifier) {
    if (errMsg.isNotEmpty()) Text(
        errMsg,
        modifier = modifier,
        color = Color.Red,
        textAlign = TextAlign.Center
    )
}

@Composable
fun GradientBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Button(
        onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(Cyan, DarkGreen)))
    ) { content() }
}

@Composable
fun BookAppointmentDialog(opened: Boolean, onDismiss: () -> Unit, doctorId: Int, doctorPrice: Double, modifier: Modifier = Modifier) {
    val methods = listOf(Pair("debit_card", "Debit Card"), Pair("credit_card", "Credit Card"), Pair("paypal", "PayPal"))
    var selectedPaymentIdx by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var couponCode by remember { mutableStateOf("") }
    var errMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var couponDetail by remember { mutableStateOf<Coupon?>(null) }
    var checkBtnText by remember { mutableStateOf("Check") }
    var confirmBtnText by remember { mutableStateOf("Confirm") }

    LaunchedEffect(couponCode) {
        if(couponDetail != null && couponDetail!!.code != couponCode) couponDetail = null
    }

    if (opened) Dialog(onDismiss) {
        Column(modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(24.dp)) {
            Text("Payment Method")
            Box(Modifier.fillMaxWidth()) {
                OutlinedButton({expanded = !expanded}, Modifier.fillMaxWidth()) {
                    Text(methods[selectedPaymentIdx].second)
                }
                DropdownMenu(expanded, {expanded = false}) {
                    methods.forEachIndexed { index, pair ->
                        DropdownMenuItem({Text(pair.second)}, {
                            selectedPaymentIdx = index
                            expanded = false
                        })
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Coupon Code")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(couponCode, {couponCode = it}, singleLine = true, modifier = Modifier.weight(1f))
                GradientBtn({
                    if(couponCode.isBlank()) {
                        errMsg = "Coupon can't be blank"
                        return@GradientBtn
                    }
                    scope.launch {
                        checkBtnText = "..."
                        val (msg, coupon) = HttpClient.checkCoupon(couponCode)
                        when {
                            msg == "ok" && coupon != null -> {
                                couponDetail = coupon
                                checkBtnText = "Check"
                            }
                            else -> {
                                errMsg = msg
                                checkBtnText = "Error"
                                delay(500)
                                checkBtnText = "Check"
                            }
                        }
                    }
                }) {
                    Text(checkBtnText)
                }
            }
            Spacer(Modifier.height(12.dp))
            val actualPrice = if(couponDetail == null) doctorPrice else doctorPrice * (couponDetail!!.discount / 100.0)
            Text("Price : $actualPrice $")
            ErrText(errMsg, Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ElevatedButton(onDismiss, Modifier.weight(1f)) {
                    Text("Cancel")
                }
                GradientBtn({
                    if(methods.getOrNull(selectedPaymentIdx) == null) {
                        errMsg = "Please select payment method"
                        return@GradientBtn
                    }
                    errMsg = ""
                    scope.launch {
                        confirmBtnText = "..."
                        if(couponCode.isNotBlank() && couponDetail == null) {
                            errMsg = "Check coupon code first"
                            confirmBtnText = "Confirm"
                            return@launch
                        }
                        when(val msg = HttpClient.bookAppointment(doctorId, methods[selectedPaymentIdx].first, couponCode)) {
                            "ok" -> {
                                confirmBtnText = "Booked"
                                delay(250)
                                val intent = Intent(ctx, HomeActivity::class.java).apply { putExtra("selectedTab", 1) }
                                ctx.startActivity(intent)
                            }
                            else -> {
                                errMsg = msg
                                confirmBtnText = "Error"
                                delay(500)
                                confirmBtnText = "Confirm"
                            }
                        }
                    }
                },  Modifier.weight(1f)) {
                    Text(confirmBtnText)
                }
            }
        }
    }
}


fun Modifier.gradientMask(brush: Brush): Modifier {
    return graphicsLayer(alpha = 0.99f).drawWithCache {
        onDrawWithContent {
            drawContent()
            drawRect(brush, blendMode = BlendMode.SrcAtop)
        }
    }
}

