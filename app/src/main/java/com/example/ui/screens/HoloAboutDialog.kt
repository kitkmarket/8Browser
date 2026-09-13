package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.holo.HoloButton
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.HoloDarkSurface
import com.example.ui.theme.HoloDivider
import com.example.ui.theme.HoloGreen
import com.example.ui.theme.HoloTextHint
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary

@Composable
fun HoloAboutDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(HoloDarkSurface)
                    .border(1.dp, Color(0xFF383838), RoundedCornerShape(2.dp))
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = HoloBlueLight,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "О программе 8Browser",
                        color = HoloBlueLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Holo Blue Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(HoloBlueLight)
                )

                // Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E2835))
                                .border(1.dp, HoloBlueLight, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "8B",
                                color = HoloBlueLight,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column {
                            Text(
                                text = "8Browser",
                                color = HoloTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Версия 1.0",
                                color = HoloBlueLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "8Browser — быстрый и удобный браузер.\n\n" +
                                "• Системный движок WebView с аппаратным ускорением\n" +
                                "• Режим инкогнито с защитой приватности\n" +
                                "• Встроенный менеджер загрузок\n" +
                                "• Полная поддержка истории и закладок\n" +
                                "• Выбор поисковых систем (Google, Яндекс, DuckDuckGo)\n" +
                                "• Тонкая настройка кэша и режима сохранения трафика\n" +
                                "• Поддержка микрофона, камеры и геолокации",
                        color = HoloTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                // Bottom bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(HoloDivider)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    HoloButton(
                        text = "Понятно",
                        onClick = onDismissRequest,
                        isPrimary = true
                    )
                }
            }
        }
    }
}
