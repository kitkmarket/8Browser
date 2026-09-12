package com.example.viewmodel

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookmarkEntity
import com.example.data.BrowserDatabase
import com.example.data.BrowserRepository
import com.example.data.DownloadItem
import com.example.data.HistoryEntity
import com.example.model.BrowserTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CacheModeSetting(val title: String, val webSettingsMode: Int) {
    DEFAULT("Стандартный (LOAD_DEFAULT)", WebSettings.LOAD_DEFAULT),
    CACHE_FIRST("Экономия трафика (LOAD_CACHE_ELSE_NETWORK)", WebSettings.LOAD_CACHE_ELSE_NETWORK),
    NO_CACHE("Без кэша (LOAD_NO_CACHE)", WebSettings.LOAD_NO_CACHE)
}

enum class SearchEngine(val title: String, val searchUrlPrefix: String) {
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q="),
    GOOGLE("Google", "https://www.google.com/search?q="),
    YANDEX("Яндекс", "https://ya.ru/search/?text=")
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrowserRepository
    init {
        val db = BrowserDatabase.getDatabase(application)
        repository = BrowserRepository(application, db.browserDao())
    }

    // --- Tabs State ---
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(
        BrowserTab(
            title = "Яндекс",
            url = "https://ya.ru",
            isIncognito = false
        )
    ))
    val tabs: StateFlow<List<BrowserTab>> = _tabs.asStateFlow()

    private val _currentTabIndex = MutableStateFlow(0)
    val currentTabIndex: StateFlow<Int> = _currentTabIndex.asStateFlow()

    val currentTab: BrowserTab
        get() = _tabs.value.getOrNull(_currentTabIndex.value) ?: _tabs.value.first()

    // --- History, Bookmarks, Downloads ---
    val history: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadItem>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Settings State ---
    private val _cacheMode = MutableStateFlow(CacheModeSetting.DEFAULT)
    val cacheMode: StateFlow<CacheModeSetting> = _cacheMode.asStateFlow()

    private val _searchEngine = MutableStateFlow(SearchEngine.YANDEX)
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    private val _javascriptEnabled = MutableStateFlow(true)
    val javascriptEnabled: StateFlow<Boolean> = _javascriptEnabled.asStateFlow()

    private val _showTabsSheet = MutableStateFlow(false)
    val showTabsSheet: StateFlow<Boolean> = _showTabsSheet.asStateFlow()

    private val _showHistoryDialog = MutableStateFlow(false)
    val showHistoryDialog: StateFlow<Boolean> = _showHistoryDialog.asStateFlow()

    private val _showBookmarksDialog = MutableStateFlow(false)
    val showBookmarksDialog: StateFlow<Boolean> = _showBookmarksDialog.asStateFlow()

    private val _showDownloadsDialog = MutableStateFlow(false)
    val showDownloadsDialog: StateFlow<Boolean> = _showDownloadsDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showAboutDialog = MutableStateFlow(false)
    val showAboutDialog: StateFlow<Boolean> = _showAboutDialog.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Reference to active webview for back/forward/reload/clear
    private var activeWebView: WebView? = null

    fun registerActiveWebView(webView: WebView?) {
        activeWebView = webView
    }

    // --- Tab Management ---
    fun openNewTab(url: String = "https://ya.ru", isIncognito: Boolean = false) {
        val newTab = BrowserTab(
            url = url,
            title = if (isIncognito) "Инкогнито" else "Новая вкладка",
            isIncognito = isIncognito
        )
        val updated = _tabs.value + newTab
        _tabs.value = updated
        _currentTabIndex.value = updated.size - 1
        _showTabsSheet.value = false
    }

    fun selectTab(index: Int) {
        if (index in _tabs.value.indices) {
            _currentTabIndex.value = index
            _showTabsSheet.value = false
        }
    }

    fun closeTab(index: Int) {
        val list = _tabs.value.toMutableList()
        if (list.size <= 1) {
            // Keep at least one tab
            list[0] = BrowserTab(
                url = "https://ya.ru",
                title = "Новая вкладка",
                isIncognito = false
            )
            _tabs.value = list
            _currentTabIndex.value = 0
            _showTabsSheet.value = false
            return
        }

        list.removeAt(index)
        _tabs.value = list
        if (_currentTabIndex.value >= list.size) {
            _currentTabIndex.value = list.size - 1
        }
    }

    fun toggleTabsSheet(show: Boolean) {
        _showTabsSheet.value = show
    }

    // --- Web Navigation ---
    fun loadUrl(input: String) {
        var formatted = input.trim()
        if (formatted.isBlank()) return

        if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
            if (formatted.contains(".") && !formatted.contains(" ")) {
                formatted = "https://$formatted"
            } else {
                formatted = _searchEngine.value.searchUrlPrefix + java.net.URLEncoder.encode(formatted, "UTF-8")
            }
        }

        updateCurrentTab { it.copy(url = formatted, isLoading = true, progress = 10) }
        activeWebView?.loadUrl(formatted)
    }

    fun goBack() {
        if (activeWebView?.canGoBack() == true) {
            activeWebView?.goBack()
        }
    }

    fun goForward() {
        if (activeWebView?.canGoForward() == true) {
            activeWebView?.goForward()
        }
    }

    fun reload() {
        activeWebView?.reload()
    }

    fun stopLoading() {
        activeWebView?.stopLoading()
        updateCurrentTab { it.copy(isLoading = false, progress = 0) }
    }

    fun toggleDesktopMode() {
        val current = currentTab
        val newDesktop = !current.isDesktopMode
        updateCurrentTab { it.copy(isDesktopMode = newDesktop) }
        activeWebView?.settings?.let { s ->
            if (newDesktop) {
                s.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                s.useWideViewPort = true
                s.loadWithOverviewMode = true
            } else {
                s.userAgentString = null
                s.useWideViewPort = true
                s.loadWithOverviewMode = true
            }
        }
        activeWebView?.reload()
        showToast(if (newDesktop) "ПК-версия включена" else "Мобильная версия")
    }

    // --- Callbacks from WebView ---
    fun onPageStarted(url: String) {
        val isSecure = url.startsWith("https://")
        updateCurrentTab {
            it.copy(
                url = url,
                isLoading = true,
                progress = 15,
                isSecure = isSecure
            )
        }
    }

    fun onPageFinished(url: String, title: String?) {
        val cleanTitle = title ?: url
        updateCurrentTab {
            it.copy(
                url = url,
                title = cleanTitle,
                isLoading = false,
                progress = 100,
                canGoBack = activeWebView?.canGoBack() == true,
                canGoForward = activeWebView?.canGoForward() == true
            )
        }

        // Only save to history if NOT incognito!
        if (!currentTab.isIncognito) {
            viewModelScope.launch {
                repository.addHistory(cleanTitle, url)
            }
        }
    }

    fun onProgressChanged(newProgress: Int) {
        updateCurrentTab {
            it.copy(
                progress = newProgress,
                isLoading = newProgress < 100
            )
        }
    }

    private fun updateCurrentTab(transform: (BrowserTab) -> BrowserTab) {
        val list = _tabs.value.toMutableList()
        val index = _currentTabIndex.value
        if (index in list.indices) {
            list[index] = transform(list[index])
            _tabs.value = list
        }
    }

    // --- Bookmarks ---
    fun toggleBookmarkCurrentPage() {
        val tab = currentTab
        viewModelScope.launch {
            repository.addBookmark(tab.title, tab.url)
            showToast("Добавлено в закладки")
        }
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
            showToast("Закладка удалена")
        }
    }

    // --- History ---
    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showToast("История очищена")
        }
    }

    // --- Download Handling ---
    fun handleDownload(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ) {
        viewModelScope.launch {
            repository.startDownload(url, userAgent, contentDisposition, mimeType, contentLength)
            showToast("Начало загрузки файла...")
            _showDownloadsDialog.value = true
        }
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            repository.clearDownloads()
            showToast("Список загрузок очищен")
        }
    }

    // --- Cache and Storage ---
    fun setCacheMode(mode: CacheModeSetting) {
        _cacheMode.value = mode
        activeWebView?.settings?.cacheMode = mode.webSettingsMode
        showToast("Режим кэша: ${mode.title.substringBefore('(')}")
    }

    fun clearBrowserCache() {
        activeWebView?.clearCache(true)
        showToast("Кэш браузера очищен")
    }

    fun setSearchEngine(engine: SearchEngine) {
        _searchEngine.value = engine
        showToast("Поисковая система: ${engine.title}")
    }

    fun toggleJavascript() {
        val newValue = !_javascriptEnabled.value
        _javascriptEnabled.value = newValue
        activeWebView?.settings?.javaScriptEnabled = newValue
        showToast(if (newValue) "JavaScript включен" else "JavaScript отключен")
    }

    fun showToast(msg: String) {
        _statusMessage.value = msg
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Dialog toggles
    fun setShowHistory(show: Boolean) { _showHistoryDialog.value = show }
    fun setShowBookmarks(show: Boolean) { _showBookmarksDialog.value = show }
    fun setShowDownloads(show: Boolean) { _showDownloadsDialog.value = show }
    fun setShowSettings(show: Boolean) { _showSettingsDialog.value = show }
    fun setShowAbout(show: Boolean) { _showAboutDialog.value = show }
}
