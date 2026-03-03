package com.example.goedvoorgoed.data

data class NewsItem(
    val id: Int,
    val title: String,
    val date: String,
    val content: String,
    val excerpt: String,
    val articleUrl: String,
    val imageUrls: List<String> = emptyList(),
    val isFullyLoaded: Boolean = false
)

data class Article(
    val title: String,
    val date: String,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val author: String? = null
)

data class Location(
    val name: String,
    val address: String,
    val postalCode: String,
    val city: String,
    val email: String,
    val phone: String? = null,
    val additionalInfo: String? = null,
    val openingHours: Map<String, String>? = null
)

data class ContactLocation(
    val name: String,
    val address: String,
    val postalCodeCity: String,
    val email: String,
    val phone: String? = null,
    val website: String? = null,
    val hours: String? = null,
    val additionalInfo: String? = null
)

// Dynamic news repository
object NewsRepository {
    private var newsItems: List<NewsItem> = emptyList()
    private var lastFetchTime: Long = 0
    private const val CACHE_DURATION_MS = 5 * 60 * 1000 // 5 minutes cache

    fun getCachedNews(): List<NewsItem> {
        return newsItems
    }

    fun updateNews(newNews: List<NewsItem>) {
        newsItems = newNews
        lastFetchTime = getCurrentTimeMillis()
    }

    fun shouldRefresh(): Boolean {
        return newsItems.isEmpty() ||
               (getCurrentTimeMillis() - lastFetchTime) > CACHE_DURATION_MS
    }

    fun updateArticleContent(articleUrl: String, content: String, images: List<String>) {
        newsItems = newsItems.map { item ->
            if (item.articleUrl == articleUrl) {
                item.copy(content = content, imageUrls = images, isFullyLoaded = true)
            } else {
                item
            }
        }
    }
}

// Platform-agnostic time function
internal expect fun getCurrentTimeMillis(): Long

// Static app data
object AppData {
    // These will be populated from the scraper
    var newsItems: List<NewsItem> = emptyList()
    
    val openingHoursSommelsdijk = mapOf(
        "Maandag" to "09:00 - 17:00",
        "Dinsdag" to "09:00 - 17:00",
        "Woensdag" to "09:00 - 17:00",
        "Donderdag" to "09:00 - 17:00",
        "Vrijdag" to "09:00 - 17:00",
        "Zaterdag" to "09:00 - 17:00",
        "Zondag" to "Gesloten"
    )

    val openingHoursOudeTonge = mapOf(
        "Maandag" to "09:00 - 17:00",
        "Dinsdag" to "09:00 - 17:00",
        "Woensdag" to "09:00 - 17:00",
        "Donderdag" to "09:00 - 17:00",
        "Vrijdag" to "09:00 - 17:00",
        "Zaterdag" to "09:00 - 17:00",
        "Zondag" to "Gesloten"
    )

    val openingHoursStellendam = mapOf(
        "Maandag" to "09:00 - 17:00",
        "Dinsdag" to "09:00 - 17:00",
        "Woensdag" to "09:00 - 17:00",
        "Donderdag" to "09:00 - 17:00",
        "Vrijdag" to "09:00 - 17:00",
        "Zaterdag" to "09:00 - 17:00",
        "Zondag" to "Gesloten"
    )

    val openingHoursHalsteren = mapOf(
        "Maandag" to "09:00 - 17:00",
        "Dinsdag" to "09:00 - 17:00",
        "Woensdag" to "09:00 - 17:00",
        "Donderdag" to "09:00 - 17:00",
        "Vrijdag" to "09:00 - 17:00",
        "Zaterdag" to "09:00 - 17:00",
        "Zondag" to "Gesloten"
    )

    val halenBrengenLocations = listOf(
        Location(
            name = "Sommelsdijk",
            address = "Gerard Walschapstraat 9",
            postalCode = "3245 MD",
            city = "Sommelsdijk",
            email = "sommelsdijk@goedvoorgoed.nl"
        ),
        Location(
            name = "Oude-Tonge",
            address = "Energiebaan 2a",
            postalCode = "3255 SB",
            city = "Oude-Tonge",
            email = "oudetonge@goedvoorgoed.nl"
        ),
        Location(
            name = "Stellendam",
            address = "Delta-Industrieweg 38",
            postalCode = "3251 LX",
            city = "Stellendam",
            email = "stellendam@goedvoorgoed.nl"
        ),
        Location(
            name = "Halsteren",
            address = "Tholenseweg 4",
            postalCode = "4661 PB",
            city = "Halsteren",
            email = "halsteren@goedvoorgoed.nl"
        )
    )

    val contactLocations = listOf(
        ContactLocation(
            name = "Sommelsdijk",
            address = "Gerard Walschapstraat 9",
            postalCodeCity = "3245 MD Sommelsdijk",
            email = "sommelsdijk@goedvoorgoed.nl",
            phone = "0187-785180"
        ),
        ContactLocation(
            name = "Oude-Tonge",
            address = "Energiebaan 2a",
            postalCodeCity = "3255 SB Oude-Tonge",
            email = "oudetonge@goedvoorgoed.nl",
            phone = "0187-785180"
        ),
        ContactLocation(
            name = "Stellendam",
            address = "Delta-Industrieweg 38",
            postalCodeCity = "3251 LX Stellendam",
            email = "stellendam@goedvoorgoed.nl",
            phone = "0187-785180"
        ),
        ContactLocation(
            name = "Halsteren",
            address = "Tholenseweg 4",
            postalCodeCity = "4661 PB Halsteren",
            email = "halsteren@goedvoorgoed.nl",
            phone = "0187-785180"
        ),
        ContactLocation(
            name = "Cherity Re-Use",
            address = "Gerard Walschapstraat 9",
            postalCodeCity = "3245 MD Sommelsdijk",
            email = "cherityre-use@goedvoorgoed.nl",
            phone = "0638594941",
            additionalInfo = "Maandag t/m Donderdag telefonisch bereikbaar"
        ),
        ContactLocation(
            name = "Kunstenmaker",
            address = "Gerard Walschapstraat 9",
            postalCodeCity = "3245 MD Sommelsdijk",
            email = "kunstenmaker@goedvoorgoed.nl",
            hours = "Maandag t/m Donderdag 10.00 – 16.00 uur"
        ),
        ContactLocation(
            name = "Bouwmarkt Oude-Tonge",
            address = "Energiebaan 2a",
            postalCodeCity = "3255 SB Oude-Tonge",
            email = "oudetonge@goedvoorgoed.nl",
            phone = "0187-785180"
        ),
        ContactLocation(
            name = "Kantoor Sommelsdijk",
            address = "Gerard Walschapstraat 9",
            postalCodeCity = "3245 MD Sommelsdijk",
            email = "",
            phone = "0187-785180",
            hours = "Maandag t/m Donderdag"
        ),
        ContactLocation(
            name = "Dierenvoedselbank",
            address = "Gerard Walschapstraat 9",
            postalCodeCity = "3245 MD Sommelsdijk",
            email = "",
            phone = "0643148323",
            additionalInfo = "Bereikbaar via WhatsApp"
        )
    )
}
