package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
import java.net.URLEncoder

enum class CacheModeSetting(val title: String, val webSettingsMode: Int) {
    DEFAULT("Стандартный (LOAD_DEFAULT)", WebSettings.LOAD_DEFAULT),
    CACHE_FIRST("Экономия трафика (LOAD_CACHE_ELSE_NETWORK)", WebSettings.LOAD_CACHE_ELSE_NETWORK),
    NO_CACHE("Без кэша (LOAD_NO_CACHE)", WebSettings.LOAD_NO_CACHE)
}

enum class SearchEngine(val title: String, val searchUrlPrefix: String, val homeUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q=", "https://www.google.com"),
    YANDEX("Яндекс", "https://ya.ru/search/?text=", "https://ya.ru"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com")
}

enum class ContextMenuType {
    IMAGE,
    IMAGE_LINK,
    LINK
}

data class ContextMenuTarget(
    val type: ContextMenuType,
    val imageUrl: String? = null,
    val linkUrl: String? = null
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("eight_browser_prefs", Context.MODE_PRIVATE)

    private val repository: BrowserRepository
    init {
        val db = BrowserDatabase.getDatabase(application)
        repository = BrowserRepository(application, db.browserDao())
    }

    private val initialSearchEngine: SearchEngine = run {
        val saved = prefs.getString("selected_search_engine", SearchEngine.GOOGLE.name)
        try {
            SearchEngine.valueOf(saved ?: SearchEngine.GOOGLE.name)
        } catch (_: Exception) {
            SearchEngine.GOOGLE
        }
    }

    private val initialCacheMode: CacheModeSetting = run {
        val saved = prefs.getString("selected_cache_mode", CacheModeSetting.DEFAULT.name)
        try {
            CacheModeSetting.valueOf(saved ?: CacheModeSetting.DEFAULT.name)
        } catch (_: Exception) {
            CacheModeSetting.DEFAULT
        }
    }

    private val initialJsEnabled: Boolean = prefs.getBoolean("javascript_enabled", true)

    // --- Settings State ---
    private val _searchEngine = MutableStateFlow(initialSearchEngine)
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    private val _cacheMode = MutableStateFlow(initialCacheMode)
    val cacheMode: StateFlow<CacheModeSetting> = _cacheMode.asStateFlow()

    private val _javascriptEnabled = MutableStateFlow(initialJsEnabled)
    val javascriptEnabled: StateFlow<Boolean> = _javascriptEnabled.asStateFlow()

    // --- Tabs State ---
    private val _tabs = MutableStateFlow<List<BrowserTab>>(listOf(
        BrowserTab(
            title = initialSearchEngine.title,
            url = initialSearchEngine.homeUrl,
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

    private val _showTabsSheet = MutableStateFlow(false)
    val showTabsSheet: StateFlow<Boolean> = _showTabsSheet.asStateFlow()

    private val _showHistoryDialog = MutableStateFlow(false)
    val showHistoryDialog: StateFlow<Boolean> = _showHistoryDialog.asStateFlow()

    private val _showBookmarksDialog = MutableStateFlow(false)
    val showBookmarksDialog: StateFlow<Boolean> = _showBookmarksDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showAboutDialog = MutableStateFlow(false)
    val showAboutDialog: StateFlow<Boolean> = _showAboutDialog.asStateFlow()

    private val _contextMenuTarget = MutableStateFlow<ContextMenuTarget?>(null)
    val contextMenuTarget: StateFlow<ContextMenuTarget?> = _contextMenuTarget.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Reference to active webview for back/forward/reload/clear
    private var activeWebView: WebView? = null

    fun registerActiveWebView(webView: WebView?) {
        activeWebView = webView
    }

    // --- Tab Management ---
    fun openNewTab(url: String? = null, isIncognito: Boolean = false) {
        val targetUrl = url ?: _searchEngine.value.homeUrl
        val newTab = BrowserTab(
            url = targetUrl,
            title = if (isIncognito) "Инкогнито" else _searchEngine.value.title,
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
                url = _searchEngine.value.homeUrl,
                title = _searchEngine.value.title,
                isIncognito = false
            )
            _tabs.value = list
            _currentTabIndex.value = 0
            _showTabsSheet.value = false
            activeWebView?.loadUrl(_searchEngine.value.homeUrl)
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
                formatted = _searchEngine.value.searchUrlPrefix + URLEncoder.encode(formatted, "UTF-8")
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

    // --- Context Menu (Long-press) ---
    fun showContextMenu(target: ContextMenuTarget) {
        _contextMenuTarget.value = target
    }

    fun dismissContextMenu() {
        _contextMenuTarget.value = null
    }

    fun downloadImage(imageUrl: String) {
        viewModelScope.launch {
            showToast("Загрузка изображения...")
            val result = repository.downloadImage(
                url = imageUrl,
                userAgent = activeWebView?.settings?.userAgentString,
                referer = currentTab.url
            )
            if (result > 0) {
                showToast("Изображение сохранено в папку Загрузки")
            } else {
                showToast("Не удалось загрузить изображение")
            }
        }
    }

    fun downloadLink(linkUrl: String) {
        viewModelScope.launch {
            showToast("Загрузка файла по ссылке...")
            val result = repository.startDownload(
                url = linkUrl,
                userAgent = activeWebView?.settings?.userAgentString,
                contentDisposition = null,
                mimeType = null,
                contentLength = 0L,
                referer = currentTab.url
            )
            if (result > 0) {
                showToast("Загрузка началась в системном менеджере")
            }
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
            repository.startDownload(
                url = url,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType,
                contentLength = contentLength,
                referer = currentTab.url
            )
            showToast("Загрузка началась через системный менеджер...")
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
        prefs.edit().putString("selected_cache_mode", mode.name).apply()
        activeWebView?.settings?.cacheMode = mode.webSettingsMode
        showToast("Режим кэша: ${mode.title.substringBefore('(')}")
    }

    fun clearBrowserCache() {
        activeWebView?.clearCache(true)
        showToast("Кэш браузера очищен")
    }

    fun setSearchEngine(engine: SearchEngine) {
        _searchEngine.value = engine
        prefs.edit().putString("selected_search_engine", engine.name).apply()
        showToast("Поисковая система: ${engine.title}")

        // If current tab is on a search engine home page or empty, navigate to the new search engine
        val currentUrl = currentTab.url
        val isSearchHome = SearchEngine.values().any {
            currentUrl.startsWith(it.homeUrl) || currentUrl.startsWith(it.searchUrlPrefix.substringBefore('?'))
        }
        if (isSearchHome || currentUrl.isBlank() || currentUrl == "about:blank") {
            loadUrl(engine.homeUrl)
        }
    }

    fun toggleJavascript() {
        val newValue = !_javascriptEnabled.value
        _javascriptEnabled.value = newValue
        prefs.edit().putBoolean("javascript_enabled", newValue).apply()
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
    fun setShowSettings(show: Boolean) { _showSettingsDialog.value = show }
    fun setShowAbout(show: Boolean) { _showAboutDialog.value = show }
}
