package com.example.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class BrowserRepository(
    private val context: Context,
    private val browserDao: BrowserDao
) {
    // --- History ---
    val allHistory: Flow<List<HistoryEntity>> = browserDao.getAllHistory()

    fun searchHistory(query: String): Flow<List<HistoryEntity>> =
        browserDao.searchHistory(query)

    suspend fun addHistory(title: String, url: String) = withContext(Dispatchers.IO) {
        if (url.isBlank() || url.startsWith("about:") || url.startsWith("data:")) return@withContext
        val cleanTitle = if (title.isBlank()) url else title
        browserDao.insertHistory(
            HistoryEntity(
                title = cleanTitle,
                url = url,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        browserDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        browserDao.clearAllHistory()
    }

    // --- Bookmarks ---
    val allBookmarks: Flow<List<BookmarkEntity>> = browserDao.getAllBookmarks()

    fun isBookmarked(url: String): Flow<Boolean> = browserDao.isBookmarked(url)

    suspend fun addBookmark(title: String, url: String) = withContext(Dispatchers.IO) {
        val cleanTitle = if (title.isBlank()) url else title
        browserDao.insertBookmark(
            BookmarkEntity(
                title = cleanTitle,
                url = url,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeBookmark(url: String) = withContext(Dispatchers.IO) {
        browserDao.deleteBookmarkByUrl(url)
    }

    suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        browserDao.deleteBookmarkById(id)
    }

    // --- Downloads ---
    val allDownloads: Flow<List<DownloadItem>> = browserDao.getAllDownloads()

    suspend fun startDownload(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ): Long = withContext(Dispatchers.IO) {
        var guessedFileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        if (guessedFileName.isNullOrBlank() || guessedFileName == "downloadfile") {
            val cleanUrl = url.substringBefore('?').substringBefore('#')
            guessedFileName = cleanUrl.substringAfterLast('/', "download_${System.currentTimeMillis()}")
        }

        var systemDownloadId = -1L
        var targetPath: String? = null

        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (downloadManager != null) {
                val uri = Uri.parse(url)
                val request = DownloadManager.Request(uri).apply {
                    setTitle(guessedFileName)
                    setDescription("8Browser: $guessedFileName")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setMimeType(mimeType)
                    if (!userAgent.isNullOrBlank()) {
                        addRequestHeader("User-Agent", userAgent)
                    }
                    try {
                        setDestinationInExternalFilesDir(
                            context,
                            Environment.DIRECTORY_DOWNLOADS,
                            guessedFileName
                        )
                        targetPath = File(
                            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                            guessedFileName
                        ).absolutePath
                    } catch (_: Exception) {
                        // Fallback destination
                    }
                }
                systemDownloadId = downloadManager.enqueue(request)
            }
        } catch (_: Exception) {
            // Ignored if download manager request fails
        }

        val item = DownloadItem(
            systemDownloadId = systemDownloadId,
            fileName = guessedFileName,
            url = url,
            mimeType = mimeType ?: "application/octet-stream",
            totalBytes = if (contentLength > 0) contentLength else 0L,
            downloadedBytes = 0L,
            status = if (systemDownloadId != -1L) DownloadStatus.DOWNLOADING else DownloadStatus.FAILED,
            filePath = targetPath,
            timestamp = System.currentTimeMillis()
        )
        browserDao.insertDownload(item)
    }

    suspend fun deleteDownload(id: Long) = withContext(Dispatchers.IO) {
        browserDao.deleteDownloadById(id)
    }

    suspend fun clearDownloads() = withContext(Dispatchers.IO) {
        browserDao.clearAllDownloads()
    }
}
