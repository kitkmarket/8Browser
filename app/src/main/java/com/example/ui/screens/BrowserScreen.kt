package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.HoloBlueLight
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.HoloDarkSurface
import com.example.ui.theme.HoloTextPrimary
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.delay

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val currentTabIndex by viewModel.currentTabIndex.collectAsStateWithLifecycle()
    val currentTab = tabs.getOrNull(currentTabIndex) ?: tabs.first()

    val history by viewModel.history.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    val isBookmarked = remember(bookmarks, currentTab.url) {
        bookmarks.any { it.url == currentTab.url }
    }

    val cacheMode by viewModel.cacheMode.collectAsStateWithLifecycle()
    val searchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
    val javascriptEnabled by viewModel.javascriptEnabled.collectAsStateWithLifecycle()

    val showTabsSheet by viewModel.showTabsSheet.collectAsStateWithLifecycle()
    val showHistoryDialog by viewModel.showHistoryDialog.collectAsStateWithLifecycle()
    val showBookmarksDialog by viewModel.showBookmarksDialog.collectAsStateWithLifecycle()
    val showDownloadsDialog by viewModel.showDownloadsDialog.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showAboutDialog by viewModel.showAboutDialog.collectAsStateWithLifecycle()

    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var showOverflowMenu by remember { mutableStateOf(false) }

    // System back button handling: go back in web history if possible, else close tabs
    BackHandler(enabled = true) {
        if (showTabsSheet) {
            viewModel.toggleTabsSheet(false)
        } else if (showHistoryDialog) {
            viewModel.setShowHistory(false)
        } else if (showBookmarksDialog) {
            viewModel.setShowBookmarks(false)
        } else if (showDownloadsDialog) {
            viewModel.setShowDownloads(false)
        } else if (showSettingsDialog) {
            viewModel.setShowSettings(false)
        } else if (showAboutDialog) {
            viewModel.setShowAbout(false)
        } else if (currentTab.canGoBack) {
            viewModel.goBack()
        } else if (tabs.size > 1) {
            viewModel.closeTab(currentTabIndex)
        }
    }

    // Auto-dismiss status toast
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(2500)
            viewModel.clearStatusMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HoloDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Holo Action Bar (with status bar padding)
            HoloActionBar(
                tab = currentTab,
                tabCount = tabs.size,
                isBookmarked = isBookmarked,
                onNavigateUrl = { viewModel.loadUrl(it) },
                onReloadOrStop = {
                    if (currentTab.isLoading) viewModel.stopLoading() else viewModel.reload()
                },
                onToggleBookmark = { viewModel.toggleBookmarkCurrentPage() },
                onOpenTabs = { viewModel.toggleTabsSheet(true) },
                onOpenOverflowMenu = { showOverflowMenu = true },
                modifier = Modifier.statusBarsPadding()
            )

            // System WebView Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BrowserWebView(
                    tab = currentTab,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )

                // Overflow menu positioned right under action bar
                HoloActionOverflowMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                    isDesktopMode = currentTab.isDesktopMode,
                    onNewTab = { viewModel.openNewTab() },
                    onNewIncognitoTab = { viewModel.openNewTab(isIncognito = true) },
                    onBookmarks = { viewModel.setShowBookmarks(true) },
                    onHistory = { viewModel.setShowHistory(true) },
                    onDownloads = { viewModel.setShowDownloads(true) },
                    onToggleDesktop = { viewModel.toggleDesktopMode() },
                    onSettings = { viewModel.setShowSettings(true) },
                    onAbout = { viewModel.setShowAbout(true) },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            // Bottom Navigation Bar
            HoloBottomBar(
                canGoBack = currentTab.canGoBack,
                canGoForward = currentTab.canGoForward,
                onBack = { viewModel.goBack() },
                onForward = { viewModel.goForward() },
                onHome = { viewModel.loadUrl("https://ya.ru") },
                onNewTab = { viewModel.openNewTab() },
                onBookmarks = { viewModel.setShowBookmarks(true) }
            )
        }

        // Holo Retro Notification Banner
        AnimatedVisibility(
            visible = statusMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp, start = 16.dp, end = 16.dp)
        ) {
            statusMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1B2838))
                        .border(1.dp, HoloBlueLight, RoundedCornerShape(2.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = msg,
                        color = HoloBlueLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Dialogs & Sheets
        if (showTabsSheet) {
            HoloTabsSheet(
                tabs = tabs,
                currentIndex = currentTabIndex,
                onSelectTab = { viewModel.selectTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { viewModel.openNewTab() },
                onNewIncognitoTab = { viewModel.openNewTab(isIncognito = true) },
                onDismissRequest = { viewModel.toggleTabsSheet(false) }
            )
        }

        if (showHistoryDialog) {
            HoloHistoryDialog(
                historyList = history,
                onSelectUrl = { viewModel.loadUrl(it) },
                onDeleteItem = { viewModel.deleteHistoryItem(it) },
                onClearAll = { viewModel.clearAllHistory() },
                onDismissRequest = { viewModel.setShowHistory(false) }
            )
        }

        if (showBookmarksDialog) {
            HoloBookmarksDialog(
                bookmarks = bookmarks,
                onSelectUrl = { viewModel.loadUrl(it) },
                onDeleteBookmark = { viewModel.removeBookmark(it) },
                onDismissRequest = { viewModel.setShowBookmarks(false) }
            )
        }

        if (showDownloadsDialog) {
            HoloDownloadsDialog(
                downloads = downloads,
                onDeleteDownload = { viewModel.deleteDownload(it) },
                onClearAll = { viewModel.clearAllDownloads() },
                onDismissRequest = { viewModel.setShowDownloads(false) }
            )
        }

        if (showSettingsDialog) {
            HoloSettingsDialog(
                cacheMode = cacheMode,
                onSelectCacheMode = { viewModel.setCacheMode(it) },
                onClearCache = { viewModel.clearBrowserCache() },
                searchEngine = searchEngine,
                onSelectSearchEngine = { viewModel.setSearchEngine(it) },
                javascriptEnabled = javascriptEnabled,
                onToggleJavascript = { viewModel.toggleJavascript() },
                onDismissRequest = { viewModel.setShowSettings(false) }
            )
        }

        if (showAboutDialog) {
            HoloAboutDialog(
                onDismissRequest = { viewModel.setShowAbout(false) }
            )
        }
    }
}
