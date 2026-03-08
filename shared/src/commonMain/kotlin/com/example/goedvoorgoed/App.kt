@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.goedvoorgoed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.goedvoorgoed.data.AppData
import com.example.goedvoorgoed.data.NewsItem
import com.example.goedvoorgoed.network.GoedvoorgoedScraper
import com.example.goedvoorgoed.ui.theme.GoedvoorgoedTheme

// Enum for navigation screens
enum class Screen {
    HOME, NIEUWS, HALEN_BRENGEN, CHERITY, OPENINGSTIJDEN, CONTACT, ARTICLE_DETAIL
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun App() {
    GoedvoorgoedTheme {
        GoedVoorGoedApp()
    }
}

@Composable
fun GoedVoorGoedApp() {
    var currentScreenIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedArticle by rememberSaveable { mutableStateOf<NewsItem?>(null) }

    val screens = listOf(
        Screen.HOME, Screen.NIEUWS, Screen.HALEN_BRENGEN,
        Screen.CHERITY, Screen.OPENINGSTIJDEN, Screen.CONTACT
    )

    val currentScreen = screens[currentScreenIndex]

    val navItems = listOf(
        BottomNavItem(Screen.HOME, "Home", Icons.Default.Home),
        BottomNavItem(Screen.NIEUWS, "GOED Nieuws!", Icons.Default.Info),
        BottomNavItem(Screen.HALEN_BRENGEN, "Halen & Brengen", Icons.Default.Email),
        BottomNavItem(Screen.CHERITY, "Cherity Re-Use", Icons.Default.ThumbUp),
        BottomNavItem(Screen.OPENINGSTIJDEN, "Openingstijden", Icons.Default.DateRange),
        BottomNavItem(Screen.CONTACT, "Contact", Icons.Default.Email)
    )

    // Show Article Detail Screen if article is selected
    if (selectedArticle != null) {
        ArticleDetailScreen(
            newsItem = selectedArticle!!,
            onBackClick = { selectedArticle = null }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = currentScreenIndex == index
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = isSelected,
                        onClick = { currentScreenIndex = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    onNavigateToNieuws = { currentScreenIndex = 1 },
                    onNavigateToHalenBrengen = { currentScreenIndex = 2 },
                    onNavigateToCherity = { currentScreenIndex = 3 },
                    onNavigateToOpeningstijden = { currentScreenIndex = 4 },
                    onNavigateToContact = { currentScreenIndex = 5 }
                )
                Screen.NIEUWS -> NieuwsScreen(
                    onBackClick = { currentScreenIndex = 0 },
                    onOpenArticle = { newsItem -> selectedArticle = newsItem }
                )
                Screen.HALEN_BRENGEN -> HalenBrengenScreen(
                    onBackClick = { currentScreenIndex = 0 }
                )
                Screen.CHERITY -> CherityReUseScreen(onBackClick = { currentScreenIndex = 0 })
                Screen.OPENINGSTIJDEN -> OpeningstijdenScreen(onBackClick = { currentScreenIndex = 0 })
                Screen.CONTACT -> ContactScreen(
                    onBackClick = { currentScreenIndex = 0 }
                )
                else -> {}
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToNieuws: () -> Unit,
    onNavigateToHalenBrengen: () -> Unit,
    onNavigateToCherity: () -> Unit,
    onNavigateToOpeningstijden: () -> Unit,
    onNavigateToContact: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo placeholder - using icon instead of image resource
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Goed Voor Goed Logo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GOED voor GOED",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Welkom bij GOED voor GOED",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bij ons draait alles om hergebruik en duurzaamheid. We geven spullen een tweede leven en helpen mensen die het nodig hebben.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ontdek onze pagina's",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickNavButton(icon = Icons.Default.Info, title = "GOED Nieuws!", subtitle = "Laatste nieuws", onClick = onNavigateToNieuws, modifier = Modifier.weight(1f))
                    QuickNavButton(icon = Icons.Default.Email, title = "Halen & Brengen", subtitle = "Afspraak maken", onClick = onNavigateToHalenBrengen, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickNavButton(icon = Icons.Default.ThumbUp, title = "Cherity Re-Use", subtitle = "Geven aan = Geven om!", onClick = onNavigateToCherity, modifier = Modifier.weight(1f))
                    QuickNavButton(icon = Icons.Default.DateRange, title = "Openingstijden", subtitle = "Wanneer open?", onClick = onNavigateToOpeningstijden, modifier = Modifier.weight(1f))
                }
                Button(
                    onClick = onNavigateToContact,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Contact", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun QuickNavButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun NieuwsScreen(onBackClick: () -> Unit = {}, onOpenArticle: (NewsItem) -> Unit = {}) {
    val scraper = remember { GoedvoorgoedScraper() }
    var newsItems by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        isLoading = true
        error = null
        val result = scraper.fetchNewsList()
        result.fold(
            onSuccess = { items ->
                newsItems = items
                isLoading = false
            },
            onFailure = { e ->
                error = "Kon nieuws niet laden: ${e.message}"
                isLoading = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Nieuws laden...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                        }
                    }
                }
                error != null && newsItems.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = error ?: "Nieuws kon niet geladen worden", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                        }
                    }
                }
                newsItems.isNotEmpty() -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(newsItems.size) { index ->
                            val newsItem = newsItems[index]
                            NewsCard(newsItem = newsItem, onClick = { onOpenArticle(newsItem) })
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Geen nieuws gevonden", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(newsItem: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image section
            if (newsItem.imageUrls.isNotEmpty()) {
                val firstImage = newsItem.imageUrls.first()
                AsyncImage(
                    model = firstImage,
                    contentDescription = newsItem.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Content section
            Column(modifier = Modifier.padding(16.dp)) {
                // Date badge
                if (newsItem.date.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = newsItem.date,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Title
                Text(
                    text = newsItem.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Excerpt
                Text(
                    text = newsItem.excerpt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Show image count indicator if there are more images
                if (newsItem.imageUrls.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+${newsItem.imageUrls.size - 1} afbeeldingen",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ContactScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(AppData.contactLocations.size) { index ->
                val location = AppData.contactLocations[index]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(location.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(location.address, color = MaterialTheme.colorScheme.onSurface)
                        Text(location.postalCodeCity, color = MaterialTheme.colorScheme.onSurface)
                        if (location.email.isNotBlank()) {
                            Text(location.email, color = MaterialTheme.colorScheme.primary)
                        }
                        location.phone?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                    }
                }
            }
        }
    }
}

@Composable
fun HalenBrengenScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            // New header text
            item {
                Text(
                    text = "Mail ons gerust voor een afspraak. LET OP: het kan even duren voordat we terug kunnen reageren. U kunt ook de spullen bij ons komen brengen. zie hiervoor de innametijden per locatie. houd ook onze socials in de gaten.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Location cards
            items(AppData.halenBrengenLocations.size) { index ->
                val location = AppData.halenBrengenLocations[index]
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(location.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(location.address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${location.postalCode} ${location.city}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(location.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun OpeningstijdenScreen(onBackClick: () -> Unit = {}) {
    val locations = listOf(
        Triple("Sommelsdijk", "Gerard Walschapstraat 9, 3245 MD Sommelsdijk", AppData.openingHoursSommelsdijk),
        Triple("Oude-Tonge", "Energiebaan 2a, 3255 SB Oude-Tonge", AppData.openingHoursOudeTonge),
        Triple("Stellendam", "Delta-Industrieweg 38, 3251 LX Stellendam", AppData.openingHoursStellendam),
        Triple("Halsteren", "Tholenseweg 4, 4661 PB Halsteren", AppData.openingHoursHalsteren)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(locations.size) { index ->
                val (name, address, hours) = locations[index]
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        hours.forEach { (day, time) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(day, color = MaterialTheme.colorScheme.onSurface)
                                Text(time, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CherityReUseScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Wat is Cherity Re-Use?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Via Cherity Re-Use kunnen mensen met een smalle beurs gratis meubels en kleding krijgen.", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Locatie: Gerard Walschapstraat 9, 3245 MD Sommelsdijk", color = MaterialTheme.colorScheme.onSurface)
                        Text("Email: cherityre-use@goedvoorgoed.nl", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleDetailScreen(
    newsItem: NewsItem,
    onBackClick: () -> Unit = {}
) {
    val scraper = remember { GoedvoorgoedScraper() }
    var articleContent by remember { mutableStateOf(newsItem.content) }
    var articleImages by remember { mutableStateOf(newsItem.imageUrls) }
    var isLoading by remember { mutableStateOf(!newsItem.isFullyLoaded) }

    // Fetch full article content if not loaded
    LaunchedEffect(newsItem.articleUrl) {
        if (!newsItem.isFullyLoaded && articleContent.isBlank()) {
            isLoading = true
            val result = scraper.fetchArticleDetail(newsItem.articleUrl)
            result.fold(
                onSuccess = { (content, images) ->
                    articleContent = content
                    articleImages = images
                    isLoading = false
                },
                onFailure = {
                    isLoading = false
                }
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Hero image
            if (articleImages.isNotEmpty()) {
                item {
                    AsyncImage(
                        model = articleImages.first(),
                        contentDescription = newsItem.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(bottom = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Date
            if (newsItem.date.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = newsItem.date,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Title
            item {
                Text(
                    text = newsItem.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Loading indicator
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Article content - split into chunks for better scrolling
            if (articleContent.isNotBlank()) {
                // Split content by paragraphs and make each a separate item
                val paragraphs = articleContent.split("\n\n")
                paragraphs.forEach { paragraph ->
                    if (paragraph.isNotBlank()) {
                        item {
                            Text(
                                text = paragraph.trim(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            } else if (!isLoading) {
                item {
                    Text(
                        text = newsItem.excerpt,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            // Spacer at bottom
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
