package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.HoloDarkSurface
import com.example.ui.theme.HoloDivider
import com.example.ui.theme.HoloPurple
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary

@Composable
fun HoloActionOverflowMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    isDesktopMode: Boolean,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onBookmarks: () -> Unit,
    onHistory: () -> Unit,
    onDownloads: () -> Unit,
    onToggleDesktop: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .width(240.dp)
            .background(HoloDarkSurface)
            .border(1.dp, Color(0xFF3C3C3C), RoundedCornerShape(2.dp))
    ) {
        HoloMenuItem(
            icon = Icons.Default.Add,
            label = "Новая вкладка",
            onClick = {
                onDismissRequest()
                onNewTab()
            }
        )

        HoloMenuItem(
            icon = Icons.Default.Lock,
            iconTint = HoloPurple,
            label = "Инкогнито вкладка",
            onClick = {
                onDismissRequest()
                onNewIncognitoTab()
            }
        )

        HoloMenuDivider()

        HoloMenuItem(
            icon = Icons.Default.Star,
            label = "Закладки",
            onClick = {
                onDismissRequest()
                onBookmarks()
            }
        )

        HoloMenuItem(
            icon = Icons.Default.History,
            label = "История",
            onClick = {
                onDismissRequest()
                onHistory()
            }
        )

        HoloMenuItem(
            icon = Icons.Default.Download,
            label = "Загрузки",
            onClick = {
                onDismissRequest()
                onDownloads()
            }
        )

        HoloMenuDivider()

        HoloMenuItem(
            icon = Icons.Default.DesktopWindows,
            label = "Версия для ПК",
            trailingContent = {
                if (isDesktopMode) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Включено",
                        tint = HoloBlueLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            onClick = {
                onDismissRequest()
                onToggleDesktop()
            }
        )

        HoloMenuItem(
            icon = Icons.Default.Settings,
            label = "Настройки и кэш",
            onClick = {
                onDismissRequest()
                onSettings()
            }
        )

        HoloMenuItem(
            icon = Icons.Default.Info,
            label = "О браузере",
            onClick = {
                onDismissRequest()
                onAbout()
            }
        )
    }
}

@Composable
private fun HoloMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color = HoloBlueLight,
    trailingContent: (@Composable () -> Unit)? = null
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    color = HoloTextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                trailingContent?.invoke()
            }
        },
        onClick = onClick,
        modifier = Modifier.height(44.dp)
    )
}

@Composable
private fun HoloMenuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(HoloDivider)
    )
}
