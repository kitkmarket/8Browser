package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.ui.holo.HoloProgressBar
import com.example.ui.theme.HoloBlueDark
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloCard
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.HoloDarkSurface
import com.example.ui.theme.HoloDivider
import com.example.ui.theme.HoloGreen
import com.example.ui.theme.HoloPurple
import com.example.ui.theme.HoloTextHint
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary

@Composable
fun HoloActionBar(
    tab: BrowserTab,
    tabCount: Int,
    isBookmarked: Boolean,
    onNavigateUrl: (String) -> Unit,
    onReloadOrStop: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenOverflowMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember(tab.url) { mutableStateOf(tab.url) }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HoloDarkSurface)
    ) {
        // Main action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App / Tab Brand or Incognito Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (tab.isIncognito) HoloPurple.copy(alpha = 0.25f) else Color(0xFF1E2835))
                    .border(
                        1.dp,
                        if (tab.isIncognito) HoloPurple else HoloBlueLight.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tab.isIncognito) "8 INC" else "8B",
                    color = if (tab.isIncognito) HoloPurple else HoloBlueLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // URL input container with Android 4.1 Holo styling
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isFocused) Color(0xFF0F1A24) else Color(0xFF121212))
                    .border(
                        1.dp,
                        if (isFocused) HoloBlueLight else Color(0xFF333333),
                        RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Security Lock indicator
                    Icon(
                        imageVector = if (tab.isSecure) Icons.Default.Lock else Icons.Outlined.LockOpen,
                        contentDescription = if (tab.isSecure) "Защищено" else "Не защищено",
                        tint = if (tab.isSecure) HoloGreen else HoloTextHint,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    BasicTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused },
                        textStyle = TextStyle(
                            color = HoloTextPrimary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(HoloBlueLight),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                onNavigateUrl(urlInput)
                            }
                        ),
                        decorationBox = { innerTextField ->
                            if (urlInput.isEmpty()) {
                                Text(
                                    text = "Поиск или URL...",
                                    color = HoloTextHint,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (isFocused && urlInput.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Очистить",
                            tint = HoloTextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { urlInput = "" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Reload or Stop
            IconButton(
                onClick = onReloadOrStop,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (tab.isLoading) Icons.Default.Close else Icons.Default.Refresh,
                    contentDescription = if (tab.isLoading) "Остановить" else "Обновить",
                    tint = if (tab.isLoading) HoloBlueLight else HoloTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Bookmark Star
            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Закладка",
                    tint = if (isBookmarked) Color(0xFFFFBB33) else HoloTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Holo Tab Count Button: square badge with tab count
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF222222))
                    .border(1.dp, HoloBlueLight.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                    .clickable(onClick = onOpenTabs),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabCount.toString(),
                    color = HoloBlueLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Overflow Menu (classic 3 vertical squares)
            IconButton(
                onClick = onOpenOverflowMenu,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню",
                    tint = HoloTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Holo Blue Loading Progress Bar
        HoloProgressBar(progress = tab.progress)

        // Signature Android 4.1 Holo Blue 2dp accent divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (tab.isIncognito) HoloPurple else HoloBlueLight)
        )
    }
}
