package ir.ariakish.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ir.ariakish.app.data.AppBanner
import ir.ariakish.app.data.Category
import ir.ariakish.app.data.Product
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    state: AppState,
    onShop: (Int?, String?) -> Unit,
    onProduct: (Product) -> Unit,
    onFavorites: () -> Unit,
    onBanner: (AppBanner) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AppHeader(
                trailing = {
                    IconButton(onClick = onFavorites) {
                        BadgedBox(badge = { if (state.favorites.isNotEmpty()) Badge { Text(state.favorites.size.toString()) } }) {
                            Icon(Icons.Outlined.FavoriteBorder, null, tint = Lavender)
                        }
                    }
                }
            )
        }
        item { SearchLauncher { onShop(null, null) } }
        item { BannerCarousel(state.banners, onBanner) }
        item { SectionTitle("دسته‌بندی‌ها", "مشاهده همه") { onShop(null, null) } }
        item {
            if (state.categories.isEmpty() && state.loadingHome) LoadingBlock("دسته‌بندی‌ها در حال بارگذاری است")
            else CategoryStrip(state.categories.take(10)) { onShop(it.id, it.name) }
        }
        item { SectionTitle("پیشنهادهای ویژه", "همه محصولات") { onShop(null, null) } }
        item {
            when {
                state.loadingHome -> LoadingBlock("محصولات جدید در حال دریافت است")
                state.homeError != null && state.products.isEmpty() -> ErrorCard(state.homeError ?: "خطا")
                else -> HomeProductGrid(
                    products = state.products.take(8),
                    favorites = state.favorites,
                    onFavorite = state::toggleFavorite,
                    onOpen = onProduct,
                    onQuickAdd = { if (it.hasOptions) onProduct(it) else state.addToCart(it) }
                )
            }
        }
        item { PromoCard() }
        if (state.products.size > 8) {
            item { SectionTitle("تازه‌های آریا کیش", "مشاهده بیشتر") { onShop(null, null) } }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.products.drop(8).take(10), key = { it.id }) { p ->
                        Card(
                            Modifier.width(160.dp).clickable { onProduct(p) },
                            shape = RoundedCornerShape(21.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            AsyncImage(p.imageUrl, p.name, Modifier.fillMaxWidth().height(150.dp), contentScale = ContentScale.Crop)
                            Column(Modifier.padding(10.dp)) {
                                Text(p.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(money(p.price), color = Pink, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
        item { ServiceStrip() }
    }
}

@Composable
private fun SearchLauncher(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(19.dp),
        color = Color(0xFFF4F1F3)
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Search, null, tint = Color.Gray)
            Spacer(Modifier.width(10.dp))
            Text("جستجوی لباس، کفش، اکسسوری ...", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.Tune, null, tint = Pink)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BannerCarousel(banners: List<AppBanner>, onBanner: (AppBanner) -> Unit) {
    val data = if (banners.isNotEmpty()) banners else listOf(
        AppBanner(0, "استایل‌های دوست‌داشتنی برای کوچولوها", "کالکشن جدید آریا کیش", "شروع خرید", null, null)
    )
    val pager = rememberPagerState(pageCount = { data.size })
    Column {
        HorizontalPager(
            state = pager,
            contentPadding = PaddingValues(horizontal = 18.dp),
            pageSpacing = 12.dp
        ) { page ->
            HeroBanner(data[page]) { onBanner(data[page]) }
        }
        if (data.size > 1) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                data.indices.forEach { i ->
                    Box(
                        Modifier.padding(horizontal = 3.dp).size(if (i == pager.currentPage) 16.dp else 7.dp, 7.dp)
                            .clip(CircleShape).background(if (i == pager.currentPage) Pink else Color(0xFFE0D8DC))
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBanner(banner: AppBanner, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(30.dp)).clickable(onClick = onClick)
            .background(Brush.linearGradient(listOf(Color(0xFFFFDCE8), Color(0xFFE2F0FF), Color(0xFFFFEAC9))))
    ) {
        if (!banner.imageUrl.isNullOrBlank()) {
            AsyncImage(banner.imageUrl, banner.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = .93f), Color.White.copy(alpha = .46f), Color.Transparent))))
        }
        Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(.72f).padding(22.dp)) {
            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = .84f)) {
                Text(banner.subtitle.ifBlank { "کالکشن جدید" }, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Pink, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
            }
            Spacer(Modifier.height(9.dp))
            Text(banner.title.ifBlank { "استایل‌های شاد برای کوچولوها" }, fontWeight = FontWeight.Black, fontSize = 24.sp, lineHeight = 31.sp, maxLines = 2)
            Spacer(Modifier.height(13.dp))
            Button(onClick = onClick, shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 9.dp)) {
                Text(banner.buttonText.ifBlank { "مشاهده" }, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(5.dp)); Icon(Icons.Outlined.ArrowBack, null, Modifier.size(17.dp))
            }
        }
        if (banner.imageUrl.isNullOrBlank()) {
            Surface(Modifier.align(Alignment.CenterEnd).padding(end = 18.dp).size(94.dp), shape = CircleShape, color = Color.White.copy(alpha = .58f)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.ChildCare, null, tint = Pink, modifier = Modifier.size(56.dp)) }
            }
        }
    }
}

@Composable
private fun HomeProductGrid(
    products: List<Product>,
    favorites: List<Int>,
    onFavorite: (Int) -> Unit,
    onOpen: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
        val columns = when {
            maxWidth >= 840.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            products.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { p ->
                        ProductCard(p, p.id in favorites, { onFavorite(p.id) }, { onOpen(p) }, { onQuickAdd(p) }, Modifier.weight(1f))
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun PromoCard() {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp).clip(RoundedCornerShape(25.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFFEAF1), Color(0xFFF1ECFF)))).padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = .72f), modifier = Modifier.size(58.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.AutoAwesome, null, tint = Color(0xFFE9A82E), modifier = Modifier.size(28.dp)) }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("لبخند کودکی، الهام ماست", fontWeight = FontWeight.Black, fontSize = 17.sp)
                Text("خرید ساده، شاد و مطمئن برای خانواده", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ServiceStrip() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallService(Icons.Outlined.Verified, "خرید مطمئن", Modifier.weight(1f))
        SmallService(Icons.Outlined.LocalShipping, "ارسال سفارش", Modifier.weight(1f))
        SmallService(Icons.Outlined.SupportAgent, "پشتیبانی", Modifier.weight(1f))
    }
}

@Composable
private fun SmallService(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0E7EB))) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Lavender, modifier = Modifier.size(23.dp)); Spacer(Modifier.height(5.dp)); Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ErrorCard(text: String) {
    Surface(Modifier.fillMaxWidth().padding(horizontal = 18.dp), shape = RoundedCornerShape(20.dp), color = Color(0xFFFFECEF)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CloudOff, null, tint = Pink); Spacer(Modifier.width(10.dp)); Text(text, fontSize = 12.sp)
        }
    }
}

@Composable
fun ShopScreen(
    state: AppState,
    initialCategoryId: Int?,
    initialCategoryName: String?,
    onBack: () -> Unit,
    onProduct: (Product) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var categoryId by remember(initialCategoryId) { mutableStateOf(initialCategoryId) }
    var categoryName by remember(initialCategoryName) { mutableStateOf(initialCategoryName) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query, categoryId) {
        delay(if (query.isBlank()) 0 else 350)
        loading = true; error = null
        runCatching { state.shopProducts(query, categoryId) }
            .onSuccess { products = it }
            .onFailure { error = it.message ?: "دریافت محصولات انجام نشد" }
        loading = false
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(155.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp, start = 14.dp, end = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            AppHeader(title = categoryName ?: "فروشگاه", subtitle = "هر چی برای کوچولوت دوست داری", canBack = true, onBack = onBack)
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                placeholder = { Text("جستجو بین محصولات...") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, null) } },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                item {
                    FilterChip(selected = categoryId == null, onClick = { categoryId = null; categoryName = null }, label = { Text("همه") }, leadingIcon = { Icon(Icons.Outlined.Apps, null, Modifier.size(16.dp)) })
                }
                items(state.categories, key = { it.id }) { c ->
                    FilterChip(selected = categoryId == c.id, onClick = { categoryId = c.id; categoryName = c.name }, label = { Text(c.name, maxLines = 1) })
                }
            }
        }
        if (loading) {
            item(span = { GridItemSpan(maxLineSpan) }) { LoadingBlock() }
        } else if (error != null) {
            item(span = { GridItemSpan(maxLineSpan) }) { ErrorCard(error ?: "خطا") }
        } else if (products.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyBlock(Icons.Outlined.SearchOff, "چیزی پیدا نشد", "عبارت جستجو یا دسته‌بندی را تغییر بده") }
        } else {
            gridItems(products, key = { it.id }) { p ->
                ProductCard(
                    product = p,
                    favorite = p.id in state.favorites,
                    onFavorite = { state.toggleFavorite(p.id) },
                    onOpen = { onProduct(p) },
                    onQuickAdd = { if (p.hasOptions) onProduct(p) else state.addToCart(p) }
                )
            }
        }
    }
}
