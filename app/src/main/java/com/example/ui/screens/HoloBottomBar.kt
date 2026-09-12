package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloDarkSurface
import com.example.ui.theme.HoloDivider
import com.example.ui.theme.HoloTextHint
import com.example.ui.theme.HoloTextPrimary

@Composable
fun HoloBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onNewTab: () -> Unit,
    onBookmarks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HoloDarkSurface)
            .navigationBarsPadding()
    ) {
        // Holo Divider Top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(HoloDivider)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back
            IconButton(
                onClick = onBack,
                enabled = canGoBack,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = if (canGoBack) HoloBlueLight else HoloTextHint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Forward
            IconButton(
                onClick = onForward,
                enabled = canGoForward,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Вперед",
                    tint = if (canGoForward) HoloBlueLight else HoloTextHint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Home
            IconButton(
                onClick = onHome,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Домой",
                    tint = HoloTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // New Tab
            IconButton(
                onClick = onNewTab,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Новая вкладка",
                    tint = HoloBlueLight,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Bookmarks Quick Access
            IconButton(
                onClick = onBookmarks,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Закладки",
                    tint = HoloTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
