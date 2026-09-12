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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BrowserTab
import com.example.ui.holo.HoloButton
import com.example.ui.theme.HoloBlueDark
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloCard
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.HoloDarkSurface
import com.example.ui.theme.HoloDivider
import com.example.ui.theme.HoloPurple
import com.example.ui.theme.HoloTextHint
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary

@Composable
fun HoloTabsSheet(
    tabs: List<BrowserTab>,
    currentIndex: Int,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
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
                        Text(
                            text = "Вкладки",
                            color = HoloBlueLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${tabs.size})",
                            color = HoloTextSecondary,
                            fontSize = 14.sp
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

                // Tabs List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height((tabs.size * 72).coerceAtMost(360).dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    itemsIndexed(tabs) { index, tab ->
                        val isSelected = index == currentIndex
                        val borderColor = when {
                            isSelected && tab.isIncognito -> HoloPurple
                            isSelected -> HoloBlueLight
                            tab.isIncognito -> HoloPurple.copy(alpha = 0.4f)
                            else -> Color(0xFF333333)
                        }

                        val cardBg = when {
                            isSelected -> Color(0xFF1E2838)
                            tab.isIncognito -> Color(0xFF1B1424)
                            else -> HoloCard
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(cardBg)
                                .border(1.dp, borderColor, RoundedCornerShape(2.dp))
                                .clickable { onSelectTab(index) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon
                            Icon(
                                imageVector = if (tab.isIncognito) Icons.Default.Lock else Icons.Default.Public,
                                contentDescription = null,
                                tint = if (tab.isIncognito) HoloPurple else if (isSelected) HoloBlueLight else HoloTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Tab Info
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (tab.isIncognito) {
                                        Text(
                                            text = "[ИНКОГНИТО] ",
                                            color = HoloPurple,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = tab.title.ifBlank { "Без названия" },
                                        color = if (isSelected) HoloBlueLight else HoloTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = tab.url,
                                    color = HoloTextHint,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Close Tab Button
                            IconButton(
                                onClick = { onCloseTab(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Закрыть вкладку",
                                    tint = HoloTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Holo Bottom Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(HoloDivider)
                )

                // Actions: New Tab / New Incognito
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HoloButton(
                        text = "+ Вкладка",
                        onClick = onNewTab,
                        modifier = Modifier.weight(1f),
                        isPrimary = true
                    )

                    HoloButton(
                        text = "+ Инкогнито",
                        onClick = onNewIncognitoTab,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
