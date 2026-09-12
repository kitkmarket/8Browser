package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.holo.HoloSwitch
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.HoloDarkSurface
import com.example.ui.theme.HoloDivider
import com.example.ui.theme.HoloGreen
import com.example.ui.theme.HoloTextHint
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary
import com.example.viewmodel.CacheModeSetting
import com.example.viewmodel.SearchEngine

@Composable
fun HoloSettingsDialog(
    cacheMode: CacheModeSetting,
    onSelectCacheMode: (CacheModeSetting) -> Unit,
    onClearCache: () -> Unit,
    searchEngine: SearchEngine,
    onSelectSearchEngine: (SearchEngine) -> Unit,
    javascriptEnabled: Boolean,
    onToggleJavascript: () -> Unit,
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = HoloBlueLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Настройки 8Browser",
                            color = HoloBlueLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = HoloTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Holo Blue Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(HoloBlueLight)
                )

                // Settings Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(400.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // SECTION: CACHE
                    SectionHeader(title = "КЭШИРОВАНИЕ И ОФЛАЙН")

                    Text(
                        text = "Режим кэша:",
                        color = HoloTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                    )

                    CacheModeSetting.values().forEach { mode ->
                        val isSelected = mode == cacheMode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isSelected) Color(0xFF1B2838) else Color(0xFF141414))
                                .border(
                                    1.dp,
                                    if (isSelected) HoloBlueLight else Color(0xFF333333),
                                    RoundedCornerShape(2.dp)
                                )
                                .clickable { onSelectCacheMode(mode) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mode.title,
                                color = if (isSelected) HoloBlueLight else HoloTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Очистка кэша WebView",
                                color = HoloTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Удалить сохраненные файлы и кэш страниц",
                                color = HoloTextHint,
                                fontSize = 11.sp
                            )
                        }
                        HoloButton(
                            text = "Очистить",
                            onClick = onClearCache
                        )
                    }

                    HoloDividerLine()

                    // SECTION: WEB FEATURES
                    SectionHeader(title = "ВЕБ-ФУНКЦИИ")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Выполнение JavaScript",
                                color = HoloTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Включить поддержку скриптов на сайтах",
                                color = HoloTextHint,
                                fontSize = 11.sp
                            )
                        }
                        HoloSwitch(
                            checked = javascriptEnabled,
                            onCheckedChange = { onToggleJavascript() }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Поисковая система по умолчанию:",
                        color = HoloTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SearchEngine.values().forEach { engine ->
                            val isSelected = engine == searchEngine
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isSelected) Color(0xFF1E2D3D) else Color(0xFF181818))
                                    .border(
                                        1.dp,
                                        if (isSelected) HoloBlueLight else Color(0xFF333333),
                                        RoundedCornerShape(2.dp)
                                    )
                                    .clickable { onSelectSearchEngine(engine) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = engine.title,
                                    color = if (isSelected) HoloBlueLight else HoloTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    HoloDividerLine()

                    // SECTION: SYSTEM & DESIGN
                    SectionHeader(title = "СИСТЕМА И ДИЗАЙН")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Тема интерфейса:", color = HoloTextSecondary, fontSize = 12.sp)
                        Text(text = "Holo Dark (Android 4.1)", color = HoloBlueLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Минимальная версия:", color = HoloTextSecondary, fontSize = 12.sp)
                        Text(text = "Android 4.2+ (Jelly Bean MR1)", color = HoloGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Движок рендеринга:", color = HoloTextSecondary, fontSize = 12.sp)
                        Text(text = "Системный Android WebView", color = HoloTextPrimary, fontSize = 12.sp)
                    }
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
                        text = "Готово",
                        onClick = onDismissRequest,
                        isPrimary = true
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = HoloBlueLight,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun HoloDividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(1.dp)
            .background(HoloDivider)
    )
}
