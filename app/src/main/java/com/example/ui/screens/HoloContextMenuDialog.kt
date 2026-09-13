package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloDarkSurface
import com.example.ui.theme.HoloDivider
import com.example.ui.theme.HoloTextHint
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary
import com.example.viewmodel.ContextMenuTarget
import com.example.viewmodel.ContextMenuType

@Composable
fun HoloContextMenuDialog(
    target: ContextMenuTarget,
    onDownloadImage: (String) -> Unit,
    onOpenInNewTab: (String, Boolean) -> Unit,
    onDownloadLink: (String) -> Unit,
    onShowToast: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    val title = when (target.type) {
        ContextMenuType.IMAGE -> "Изображение"
        ContextMenuType.IMAGE_LINK -> "Изображение и ссылка"
        ContextMenuType.LINK -> "Ссылка"
    }

    val displayUrl = target.imageUrl ?: target.linkUrl ?: ""

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(HoloDarkSurface)
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(2.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Classic Android 4.1 Holo Dialog Title)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = title,
                        color = HoloBlueLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    if (displayUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (displayUrl.startsWith("data:")) "Встроенное изображение (data:image)" else displayUrl,
                            color = HoloTextHint,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Holo cyan divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(HoloBlueLight)
                )

                // IMAGE ACTIONS
                if (target.imageUrl != null) {
                    val imgUrl = target.imageUrl

                    ContextMenuItem(
                        icon = Icons.Default.Download,
                        title = "Скачать изображение",
                        accent = true,
                        onClick = {
                            onDismissRequest()
                            onDownloadImage(imgUrl)
                        }
                    )

                    ContextMenuItem(
                        icon = Icons.Default.OpenInBrowser,
                        title = "Открыть изображение в новой вкладке",
                        onClick = {
                            onDismissRequest()
                            onOpenInNewTab(imgUrl, false)
                        }
                    )

                    if (!imgUrl.startsWith("data:")) {
                        ContextMenuItem(
                            icon = Icons.Default.ContentCopy,
                            title = "Копировать ссылку на изображение",
                            onClick = {
                                copyToClipboard(context, imgUrl)
                                onShowToast("Ссылка скопирована")
                                onDismissRequest()
                            }
                        )

                        ContextMenuItem(
                            icon = Icons.Default.Share,
                            title = "Поделиться изображением",
                            onClick = {
                                shareText(context, imgUrl, "Изображение")
                                onDismissRequest()
                            }
                        )
                    }
                }

                // LINK ACTIONS
                if (target.linkUrl != null) {
                    val linkUrl = target.linkUrl

                    if (target.imageUrl != null) {
                        // Divider between image actions and link actions
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(HoloDivider)
                                .padding(vertical = 4.dp)
                        )
                        Text(
                            text = "ДЕЙСТВИЯ СО ССЫЛКОЙ",
                            color = HoloBlueLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                        )
                    }

                    ContextMenuItem(
                        icon = Icons.Default.Add,
                        title = "Открыть в новой вкладке",
                        onClick = {
                            onDismissRequest()
                            onOpenInNewTab(linkUrl, false)
                        }
                    )

                    ContextMenuItem(
                        icon = Icons.Default.Lock,
                        title = "Открыть в режиме инкогнито",
                        onClick = {
                            onDismissRequest()
                            onOpenInNewTab(linkUrl, true)
                        }
                    )

                    ContextMenuItem(
                        icon = Icons.Default.ContentCopy,
                        title = "Копировать адрес ссылки",
                        onClick = {
                            copyToClipboard(context, linkUrl)
                            onShowToast("Адрес ссылки скопирован")
                            onDismissRequest()
                        }
                    )

                    ContextMenuItem(
                        icon = Icons.Default.Download,
                        title = "Скачать по ссылке",
                        onClick = {
                            onDismissRequest()
                            onDownloadLink(linkUrl)
                        }
                    )

                    ContextMenuItem(
                        icon = Icons.Default.Share,
                        title = "Поделиться ссылкой",
                        onClick = {
                            shareText(context, linkUrl, "Ссылка")
                            onDismissRequest()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    title: String,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (accent) HoloBlueLight else HoloTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = if (accent) HoloBlueLight else HoloTextPrimary,
            fontSize = 13.sp,
            fontWeight = if (accent) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("URL", text))
}

private fun shareText(context: Context, text: String, title: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(shareIntent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (_: Exception) {
    }
}
