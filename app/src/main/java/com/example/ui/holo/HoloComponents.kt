package com.example.ui.holo

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.HoloBlueDark
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.HoloDarkSurface
import com.example.ui.theme.HoloDivider
import com.example.ui.theme.HoloGlow
import com.example.ui.theme.HoloTextHint
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary

/**
 * Authentic Android 4.1 (Jelly Bean) Holo Dark Button.
 * Characterized by a subtle gray/dark rectangular background, 2dp rounded corners,
 * and a vibrant Holo Blue (#33B5E5) outline and background glow when pressed.
 */
@Composable
fun HoloButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = when {
        !enabled -> Color(0xFF1C1C1C)
        isPressed -> HoloBlueDark.copy(alpha = 0.45f)
        isPrimary -> Color(0xFF1E2835)
        else -> Color(0xFF222222)
    }

    val borderColor = when {
        !enabled -> Color(0xFF333333)
        isPressed -> HoloBlueLight
        isPrimary -> HoloBlueLight.copy(alpha = 0.6f)
        else -> Color(0xFF444444)
    }

    val textColor = when {
        !enabled -> HoloTextHint
        isPressed || isPrimary -> HoloBlueLight
        else -> HoloTextPrimary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Authentic Android 4.1 Holo Switch component [ ON | OFF ].
 * Features the signature Holo Blue slider head with white ON / OFF indicator.
 */
@Composable
fun HoloSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val offsetProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        label = "holo_switch"
    )

    Box(
        modifier = modifier
            .width(84.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF141414))
            .border(1.dp, Color(0xFF404040), RoundedCornerShape(2.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Background labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ON",
                    color = if (checked) HoloBlueLight else HoloTextHint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OFF",
                    color = if (!checked) HoloTextSecondary else HoloTextHint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sliding Holo thumb
        val thumbOffset = (42.dp * offsetProgress)
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .width(42.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (checked) {
                        Brush.verticalGradient(listOf(HoloBlueLight, HoloBlueDark))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFF555555), Color(0xFF333333)))
                    }
                )
                .border(
                    1.dp,
                    if (checked) HoloBlueLight else Color(0xFF666666),
                    RoundedCornerShape(2.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (checked) "ON" else "OFF",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Android 4.1 Holo Blue Horizontal Progress Bar.
 * Glowing cyan blue bar across the action bar.
 */
@Composable
fun HoloProgressBar(
    progress: Int, // 0 to 100
    modifier: Modifier = Modifier
) {
    if (progress in 1..99) {
        val fraction = (progress / 100f).coerceIn(0f, 1f)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFF1A1A1A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(HoloBlueDark, HoloBlueLight, Color(0xFFE0F7FA))
                        )
                    )
            )
        }
    }
}

/**
 * Authentic Android 4.1 Holo Dialog.
 * Styled with classic top Holo Blue divider, dark surface, and bottom action bar.
 */
@Composable
fun HoloDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = "Отмена",
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(HoloDarkSurface)
                .border(1.dp, Color(0xFF383838), RoundedCornerShape(2.dp))
        ) {
            // Dialog Title
            Text(
                text = title,
                color = HoloBlueLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // Signature Holo Blue Divider under Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(HoloBlueLight)
            )

            // Dialog Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                content()
            }

            // Bottom Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(HoloDivider)
            )

            // Bottom Holo Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dismissText != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable(onClick = onDismissRequest),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dismissText.uppercase(),
                            color = HoloTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (confirmText != null && onConfirm != null) {
                    if (dismissText != null) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(HoloDivider)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable(onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmText.uppercase(),
                            color = HoloBlueLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
