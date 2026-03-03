package com.example.goedvoorgoed.network

import com.example.goedvoorgoed.data.NewsItem
import com.example.goedvoorgoed.data.NewsRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoedvoorgoedScraper {
    companion object {
        const val NEWS_URL = "https://www.goedvoorgoed.nl/nieuws/"
    }

    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 20000
            connectTimeoutMillis = 10000
        }
    }

    suspend fun fetchNewsList(): Result<List<NewsItem>> = withContext(Dispatchers.Default) {
        try {
            val html = client.get(NEWS_URL) {
                header("User-Agent", "Mozilla/5.0")
                header("Accept", "text/html")
            }.bodyAsText()

            val items = parseNewsList(html)
            NewsRepository.updateNews(items)
            Result.success(items)
        } catch (e: Exception) {
            val cached = NewsRepository.getCachedNews()
            if (cached.isNotEmpty()) Result.success(cached) else Result.failure(e)
        }
    }

    private fun parseNewsList(html: String): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        var id = 0
        var pos = 0

        while (true) {
            val start = html.indexOf("<article", pos)
            if (start == -1) break
            val end = html.indexOf("</article>", start)
            if (end == -1) break

            val block = html.substring(start, end + 10)
            if (!block.contains("et_pb_post")) {
                pos = end + 10
                continue
            }

            val url = extract(block, "href=\"", "\"")
            val title = clean(extract(block, "entry-title", "</a>"))
            val date = clean(extract(block, "published\">", "</span>"))
            val excerpt = clean(extract(block, "post-content-inner", "</p>")).take(200)
            val image = extractImage(block)

            if (title.isNotBlank() && url.isNotBlank()) {
                items.add(NewsItem(id++, title, date, "", excerpt, url,
                    if (image.isNotEmpty()) listOf(image) else emptyList(), false))
            }
            pos = end + 10
        }
        return items
    }

    private fun extract(html: String, start: String, end: String): String {
        val i = html.indexOf(start)
        if (i == -1) return ""
        val s = if (start.contains("\"")) i + start.length else html.indexOf(">", i) + 1
        val e = html.indexOf(end, s)
        return if (e != -1) html.substring(s, e) else ""
    }

    private fun clean(s: String): String {
        return s.replace(Regex("<[^>]+>"), "").replace("&nbsp;", " ").trim()
    }

    private fun extractImage(html: String): String {
        val i = html.indexOf("et_pb_image_container")
        if (i == -1) return ""
        val block = html.substring(i, html.indexOf("</div>", i))
        listOf("data-src=\"", "src=\"").forEach { a ->
            val u = extract(block, a, "\"")
            if (u.isNotEmpty() && !u.contains("data:")) return u
        }
        return ""
    }

    suspend fun fetchArticleDetail(articleUrl: String): Result<Pair<String, List<String>>> =
        withContext(Dispatchers.Default) {
            try {
                val html = client.get(articleUrl) {
                    header("User-Agent", "Mozilla/5.0")
                    header("Accept", "text/html")
                }.bodyAsText()

                val content = extractArticleContent(html)
                val image = extract(html, "og:image\" content=\"", "\"")

                NewsRepository.updateArticleContent(articleUrl, content,
                    if (image.isNotEmpty()) listOf(image) else emptyList())

                Result.success(Pair(content, if (image.isNotEmpty()) listOf(image) else emptyList()))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun extractArticleContent(html: String): String {
        val contentHtml = extractContentDiv(html)
        if (contentHtml != null) {
            val trimmed = trimRelatedContent(contentHtml)
            return htmlToText(trimmed).trim()
        }

        val articleHtml = extractArticleTag(html)
        if (articleHtml != null) {
            val trimmed = trimRelatedContent(articleHtml)
            return htmlToText(trimmed).trim()
        }

        return ""
    }

    private fun trimRelatedContent(contentHtml: String): String {
        val relatedMarkers = listOf("et_pb_blog_grid", "et_pb_related_posts", "class=\"share\"")
        var trimmedContent = contentHtml
        for (marker in relatedMarkers) {
            val idx = trimmedContent.indexOf(marker)
            if (idx != -1) {
                trimmedContent = trimmedContent.substring(0, idx)
            }
        }
        return trimmedContent
    }

    private fun extractArticleTag(html: String): String? {
        val lower = html.lowercase()
        val start = lower.indexOf("<article")
        if (start == -1) return null
        val openEnd = lower.indexOf(">", start)
        if (openEnd == -1) return null
        val end = lower.indexOf("</article>", openEnd)
        if (end == -1) return null
        return html.substring(openEnd + 1, end)
    }

    private fun extractContentDiv(html: String): String? {
        val lower = html.lowercase()
        val markers = listOf(
            "et_pb_post_content",
            "entry-content",
            "post-content",
            "et_pb_text_inner"
        )

        val h1Start = lower.indexOf("<h1")
        val h1End = if (h1Start != -1) lower.indexOf("</h1>", h1Start) else -1
        val searchFrom = if (h1End != -1) h1End + 5 else 0

        var classIdx = -1
        for (marker in markers) {
            classIdx = lower.indexOf(marker, searchFrom)
            if (classIdx != -1) {
                break
            }
        }

        if (classIdx == -1) {
            for (marker in markers) {
                classIdx = lower.indexOf(marker)
                if (classIdx != -1) {
                    break
                }
            }
        }

        if (classIdx == -1) {
            return null
        }

        val divOpenIdx = lower.lastIndexOf("<div", classIdx)
        if (divOpenIdx == -1) return null

        val divOpenEnd = lower.indexOf(">", divOpenIdx)
        if (divOpenEnd == -1) return null

        var depth = 1
        var pos = divOpenEnd + 1
        while (pos < lower.length - 5) {
            val nextOpen = lower.indexOf("<div", pos)
            val nextClose = lower.indexOf("</div>", pos)
            if (nextClose == -1) break

            if (nextOpen != -1 && nextOpen < nextClose) {
                depth++
                pos = nextOpen + 4
            } else {
                depth--
                pos = nextClose + 6
                if (depth == 0) {
                    return html.substring(divOpenEnd + 1, nextClose)
                }
            }
        }
        return null
    }

    private fun htmlToText(html: String): String {
        var text = html
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</p>"), "\n\n")
            .replace(Regex("(?i)</li>"), "\n")
            .replace(Regex("(?i)</h2>|</h3>|</h4>"), "\n\n")

        text = text.replace(Regex("<[^>]+>"), "")

        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}
