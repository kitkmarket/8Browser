package com.example.data

import android.app.DownloadManager
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.URLUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

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

    // --- Downloads (System DownloadManager Integration) ---
    val allDownloads: Flow<List<DownloadItem>> = browserDao.getAllDownloads()

    private fun sanitizeFileName(name: String): String {
        val clean = name.substringBefore('?').substringBefore('#')
        val safe = clean.replace(Regex("[\\\\/:*?\"<>|%\\s]+"), "_")
        return if (safe.isBlank() || safe == "_") "download_${System.currentTimeMillis()}" else safe
    }

    suspend fun downloadImage(
        url: String,
        userAgent: String?,
        referer: String? = null
    ): Long = withContext(Dispatchers.IO) {
        if (url.startsWith("data:image/")) {
            return@withContext downloadDataUriImage(url)
        }

        val guessedMime = when {
            url.contains(".png", ignoreCase = true) -> "image/png"
            url.contains(".webp", ignoreCase = true) -> "image/webp"
            url.contains(".gif", ignoreCase = true) -> "image/gif"
            url.contains(".svg", ignoreCase = true) -> "image/svg+xml"
            else -> "image/jpeg"
        }

        val ext = when (guessedMime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            "image/svg+xml" -> "svg"
            else -> "jpg"
        }

        val rawName = URLUtil.guessFileName(url, null, guessedMime)
        var fileName = sanitizeFileName(rawName)
        if (!fileName.contains(".") || fileName.endsWith(".bin") || fileName.endsWith(".download")) {
            fileName = "image_${System.currentTimeMillis()}.$ext"
        }

        // Try direct HTTP stream first (guarantees session cookies & bypasses CDN blocks)
        val directSuccess = downloadHttpDirect(
            url = url,
            fileName = fileName,
            mimeType = guessedMime,
            userAgent = userAgent,
            referer = referer
        )

        if (directSuccess) {
            return@withContext 1L
        }

        // Fallback to DownloadManager.enqueue
        startDownload(
            url = url,
            userAgent = userAgent,
            contentDisposition = null,
            mimeType = guessedMime,
            contentLength = 0L,
            referer = referer
        )
    }

    private suspend fun downloadDataUriImage(dataUrl: String): Long = withContext(Dispatchers.IO) {
        try {
            val mimeType = dataUrl.substringAfter("data:").substringBefore(";", "image/jpeg")
            val base64Data = dataUrl.substringAfter("base64,")
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val ext = when {
                mimeType.contains("png", ignoreCase = true) -> "png"
                mimeType.contains("webp", ignoreCase = true) -> "webp"
                mimeType.contains("gif", ignoreCase = true) -> "gif"
                else -> "jpg"
            }
            val fileName = "image_${System.currentTimeMillis()}.$ext"

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val targetFile = File(downloadsDir, fileName)
            targetFile.writeBytes(bytes)

            // Register in system DownloadManager so it appears in system notifications and downloads
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            try {
                downloadManager?.addCompletedDownload(
                    fileName,
                    "8Browser: $fileName",
                    true,
                    mimeType,
                    targetFile.absolutePath,
                    targetFile.length(),
                    true
                )
            } catch (_: Exception) {}

            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                arrayOf(mimeType),
                null
            )

            val item = DownloadItem(
                systemDownloadId = -1L,
                fileName = fileName,
                url = "data:$mimeType;base64,...",
                mimeType = mimeType,
                totalBytes = bytes.size.toLong(),
                downloadedBytes = bytes.size.toLong(),
                status = DownloadStatus.COMPLETED,
                filePath = targetFile.absolutePath,
                timestamp = System.currentTimeMillis()
            )
            browserDao.insertDownload(item)
            1L
        } catch (e: Exception) {
            -1L
        }
    }

    private suspend fun downloadHttpDirect(
        url: String,
        fileName: String,
        mimeType: String,
        userAgent: String?,
        referer: String?
    ): Boolean = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val targetFile = File(downloadsDir, fileName)

            var currentUrl = url
            var redirects = 0
            while (redirects < 5) {
                val u = URL(currentUrl)
                connection = u.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 15000
                connection.readTimeout = 20000

                if (!userAgent.isNullOrBlank()) {
                    val cleanUa = userAgent.replace("\n", "").replace("\r", "").trim()
                    if (cleanUa.isNotBlank()) connection.setRequestProperty("User-Agent", cleanUa)
                }
                try {
                    val cookies = CookieManager.getInstance().getCookie(currentUrl)
                    if (!cookies.isNullOrBlank()) {
                        val cleanCookie = cookies.replace("\n", "").replace("\r", "").trim()
                        if (cleanCookie.isNotBlank()) connection.setRequestProperty("Cookie", cleanCookie)
                    }
                } catch (_: Exception) {}

                if (!referer.isNullOrBlank()) {
                    val cleanRef = referer.replace("\n", "").replace("\r", "").trim()
                    if (cleanRef.isNotBlank()) connection.setRequestProperty("Referer", cleanRef)
                }

                connection.setRequestProperty("Accept", "*/*")
                connection.connect()

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307 || code == 308) {
                    val loc = connection.getHeaderField("Location")
                    if (!loc.isNullOrBlank()) {
                        currentUrl = if (loc.startsWith("http://") || loc.startsWith("https://")) loc else URL(u, loc).toString()
                        connection.disconnect()
                        redirects++
                        continue
                    }
                }
                break
            }

            val responseCode = connection?.responseCode ?: -1
            if (responseCode in 200..299) {
                inputStream = connection!!.inputStream
                outputStream = FileOutputStream(targetFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes = 0L
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                }
                outputStream.flush()

                // Register directly into Android's system DownloadManager
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                try {
                    downloadManager?.addCompletedDownload(
                        fileName,
                        "8Browser: $fileName",
                        true,
                        mimeType,
                        targetFile.absolutePath,
                        targetFile.length(),
                        true
                    )
                } catch (_: Exception) {}

                // Index in Gallery / Photos via media scanner
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetFile.absolutePath),
                    arrayOf(mimeType),
                    null
                )

                val item = DownloadItem(
                    systemDownloadId = -1L,
                    fileName = fileName,
                    url = url,
                    mimeType = mimeType,
                    totalBytes = totalBytes,
                    downloadedBytes = totalBytes,
                    status = DownloadStatus.COMPLETED,
                    filePath = targetFile.absolutePath,
                    timestamp = System.currentTimeMillis()
                )
                browserDao.insertDownload(item)
                return@withContext true
            }
        } catch (_: Exception) {
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { outputStream?.close() } catch (_: Exception) {}
            try { connection?.disconnect() } catch (_: Exception) {}
        }
        return@withContext false
    }

    suspend fun startDownload(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long,
        referer: String? = null
    ): Long = withContext(Dispatchers.IO) {
        var rawName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        if (rawName.isNullOrBlank() || rawName == "downloadfile" || !rawName.contains(".")) {
            val cleanUrl = url.substringBefore('?').substringBefore('#')
            val ext = when {
                mimeType?.contains("png", ignoreCase = true) == true -> "png"
                mimeType?.contains("webp", ignoreCase = true) == true -> "webp"
                mimeType?.contains("gif", ignoreCase = true) == true -> "gif"
                mimeType?.contains("jpeg", ignoreCase = true) == true || mimeType?.contains("jpg", ignoreCase = true) == true -> "jpg"
                mimeType?.contains("pdf", ignoreCase = true) == true -> "pdf"
                mimeType?.contains("apk", ignoreCase = true) == true -> "apk"
                mimeType?.contains("zip", ignoreCase = true) == true -> "zip"
                else -> ""
            }
            val baseName = cleanUrl.substringAfterLast('/').takeIf { it.isNotBlank() } ?: "download_${System.currentTimeMillis()}"
            rawName = if (ext.isNotBlank() && !baseName.contains(".")) "$baseName.$ext" else baseName
        }

        var safeFileName = sanitizeFileName(rawName)
        var systemDownloadId = -1L
        var targetPath: String? = null

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            if (downloadManager != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                val uri = Uri.parse(url)
                val request = DownloadManager.Request(uri).apply {
                    setTitle(safeFileName)
                    setDescription("8Browser: $safeFileName")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                    if (!mimeType.isNullOrBlank()) {
                        try { setMimeType(mimeType) } catch (_: Exception) {}
                    }
                    if (!userAgent.isNullOrBlank()) {
                        val cleanUa = userAgent.replace("\n", "").replace("\r", "").trim()
                        if (cleanUa.isNotBlank()) {
                            try { addRequestHeader("User-Agent", cleanUa) } catch (_: Exception) {}
                        }
                    }
                    try {
                        val cookies = CookieManager.getInstance().getCookie(url)
                        if (!cookies.isNullOrBlank()) {
                            val cleanCookie = cookies.replace("\n", "").replace("\r", "").trim()
                            if (cleanCookie.isNotBlank()) {
                                addRequestHeader("Cookie", cleanCookie)
                            }
                        }
                    } catch (_: Exception) {}

                    if (!referer.isNullOrBlank()) {
                        val cleanReferer = referer.replace("\n", "").replace("\r", "").trim()
                        if (cleanReferer.isNotBlank()) {
                            try { addRequestHeader("Referer", cleanReferer) } catch (_: Exception) {}
                        }
                    }

                    try {
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, safeFileName)
                        targetPath = File(downloadsDir, safeFileName).absolutePath
                    } catch (_: Exception) {
                        try {
                            setDestinationInExternalFilesDir(
                                context,
                                Environment.DIRECTORY_DOWNLOADS,
                                safeFileName
                            )
                            targetPath = File(
                                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                safeFileName
                            ).absolutePath
                        } catch (_: Exception) {}
                    }
                }
                systemDownloadId = downloadManager.enqueue(request)
            }
        } catch (_: Exception) {
            // If DownloadManager.enqueue fails, fallback to direct download
        }

        // If system DownloadManager enqueue failed or returned -1, perform direct download and register
        if (systemDownloadId == -1L && (url.startsWith("http://") || url.startsWith("https://"))) {
            val success = downloadHttpDirect(
                url = url,
                fileName = safeFileName,
                mimeType = mimeType ?: "application/octet-stream",
                userAgent = userAgent,
                referer = referer
            )
            if (success) {
                return@withContext 1L
            }
        }

        val item = DownloadItem(
            systemDownloadId = systemDownloadId,
            fileName = safeFileName,
            url = url,
            mimeType = mimeType ?: "application/octet-stream",
            totalBytes = if (contentLength > 0) contentLength else 0L,
            downloadedBytes = 0L,
            status = if (systemDownloadId != -1L) DownloadStatus.DOWNLOADING else DownloadStatus.FAILED,
            filePath = targetPath,
            timestamp = System.currentTimeMillis()
        )
        browserDao.insertDownload(item)
        systemDownloadId
    }

    suspend fun deleteDownload(id: Long) = withContext(Dispatchers.IO) {
        browserDao.deleteDownloadById(id)
    }

    suspend fun clearDownloads() = withContext(Dispatchers.IO) {
        browserDao.clearAllDownloads()
    }
}
