package ir.ariakish.app.ui

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ir.ariakish.app.data.Product
import ir.ariakish.app.data.ProductVariation
import ir.ariakish.app.data.VariationAttribute
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(state: AppState, product: Product, onBack: () -> Unit, onCart: () -> Unit) {
    var variations by remember(product.id) { mutableStateOf<List<ProductVariation>>(emptyList()) }
    var loadingVariations by remember(product.id) { mutableStateOf(product.hasOptions) }
    var variationError by remember(product.id) { mutableStateOf<String?>(null) }
    val selected = remember(product.id) { mutableStateMapOf<String, String>() }

    LaunchedEffect(product.id) {
        if (product.hasOptions) {
            loadingVariations = true
            runCatching { state.variations(product.id) }
                .onSuccess { variations = it }
                .onFailure { variationError = it.message ?: "دریافت رنگ و سایز انجام نشد" }
            loadingVariations = false
        }
    }

    val groups: Map<String, List<VariationAttribute>> = remember(variations) {
        variations.flatMap { it.attributes }.groupBy { it.key }.mapValues { (_, values) -> values.distinctBy { it.value } }
    }
    val selectedVariation = remember(variations, selected.toMap()) {
        if (groups.isEmpty()) null else variations.firstOrNull { v ->
            groups.keys.all { key ->
                val chosen = selected[key] ?: return@firstOrNull false
                v.attributes.firstOrNull { it.key == key }?.value.let { it.isNullOrBlank() || it == chosen }
            }
        }
    }
    val displayPrice = selectedVariation?.price ?: product.price
    val images = buildList {
        selectedVariation?.imageUrl?.takeIf { it.isNotBlank() }?.let { add(it) }
        addAll(product.gallery.filter { it.isNotBlank() })
        product.imageUrl?.takeIf { it.isNotBlank() && it !in this }?.let { add(it) }
    }.distinct()
    val pager = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            AppHeader(
                title = "جزئیات محصول",
                subtitle = "انتخاب قشنگ برای کوچولوها",
                canBack = true,
                onBack = onBack,
                trailing = {
                    IconButton(onClick = { state.toggleFavorite(product.id) }) {
                        Icon(if (product.id in state.favorites) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null, tint = Pink)
                    }
                }
            )
        }
        item {
            Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFFF7F2F5))) {
                HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth().height(390.dp)) { page ->
                    if (images.isNotEmpty()) AsyncImage(images[page], product.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Image, null, tint = Color.LightGray, modifier = Modifier.size(80.dp)) }
                }
                if (product.onSale) Surface(Modifier.align(Alignment.TopStart).padding(14.dp), shape = RoundedCornerShape(12.dp), color = Pink) {
                    Text("تخفیف ویژه", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name, Modifier.weight(1f), fontWeight = FontWeight.Black, fontSize = 22.sp, lineHeight = 31.sp)
                    if (product.averageRating > 0) InfoPill(Icons.Outlined.Star, String.format("%.1f", product.averageRating), Color(0xFFE4A223))
                }
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(money(displayPrice), color = Pink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    InfoPill(if (product.inStock) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel, if (product.inStock) "موجود" else "ناموجود", if (product.inStock) Color(0xFF2D9B67) else Color(0xFFB44E59))
                }
                if (product.onSale && product.regularPrice > product.price) {
                    Text(money(product.regularPrice), color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        if (product.hasOptions) {
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    when {
                        loadingVariations -> LoadingBlock("رنگ و سایزها در حال دریافت است")
                        variationError != null -> Text(variationError ?: "خطا", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        else -> groups.forEach { (key, options) ->
                            Text(options.firstOrNull()?.label ?: key, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(options, key = { it.value }) { a ->
                                    FilterChip(selected = selected[key] == a.value, onClick = { selected[key] = a.value }, label = { Text(a.value.ifBlank { "پیش‌فرض" }) })
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                if (product.shortDescription.isNotBlank()) {
                    Text(product.shortDescription, color = Color(0xFF5E585B), fontSize = 13.sp, lineHeight = 22.sp)
                    Spacer(Modifier.height(12.dp))
                }
                if (product.description.isNotBlank()) {
                    Text("توضیحات", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Spacer(Modifier.height(7.dp))
                    Text(product.description, color = Color(0xFF615B5E), fontSize = 13.sp, lineHeight = 23.sp)
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        state.addToCart(product, selectedVariation)
                        onCart()
                    },
                    enabled = product.inStock && product.purchasable && (!product.hasOptions || selectedVariation != null),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    Icon(Icons.Outlined.ShoppingBag, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (product.hasOptions && selectedVariation == null) "رنگ و سایز را انتخاب کنید" else "افزودن به سبد خرید", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CartScreen(state: AppState, onShop: () -> Unit, onAccount: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checkoutBusy by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
        item { AppHeader(title = "سبد خرید", subtitle = "انتخاب‌های دوست‌داشتنی شما") }
        if (state.cart.isEmpty()) {
            item { EmptyBlock(Icons.Outlined.ShoppingBag, "سبد خریدت هنوز خالیه", "چند لباس خوشگل انتخاب کنیم؟", "مشاهده فروشگاه", onShop) }
        } else {
            items(state.cart, key = { it.key }) { line ->
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(21.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(line.variation?.imageUrl ?: line.product.imageUrl, line.product.name, Modifier.size(88.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF8F3F5)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(line.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            if (line.variation != null) {
                                Text(line.variation.attributes.joinToString(" • ") { it.value }, color = Color.Gray, fontSize = 11.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(money(line.unitPrice), color = Pink, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Spacer(Modifier.height(7.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SmallQtyButton(Icons.Outlined.Add) { state.changeQuantity(line.key, 1) }
                                Text(line.quantity.toString(), Modifier.width(34.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Black)
                                SmallQtyButton(Icons.Outlined.Remove) { state.changeQuantity(line.key, -1) }
                            }
                        }
                        IconButton(onClick = { state.removeFromCart(line.key) }) { Icon(Icons.Outlined.DeleteOutline, null, tint = Color(0xFFB45D66)) }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth().padding(18.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(18.dp)) {
                        Row { Text("جمع سبد", Modifier.weight(1f), fontWeight = FontWeight.Bold); Text(money(state.cartTotal), color = Pink, fontWeight = FontWeight.Black) }
                        Spacer(Modifier.height(10.dp))
                        Text("هزینه ارسال و تخفیف‌ها در مرحله پرداخت سایت محاسبه می‌شود.", color = Color.Gray, fontSize = 11.sp, lineHeight = 18.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (!state.isLoggedIn) onAccount()
                                else scope.launch {
                                    checkoutBusy = true
                                    runCatching { state.checkoutUrl() }
                                        .onSuccess { url ->
                                            runCatching { CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, Uri.parse(url)) }
                                                .onFailure { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                                        }
                                        .onFailure { state.message = it.message ?: "انتقال به پرداخت انجام نشد" }
                                    checkoutBusy = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(17.dp),
                            enabled = !checkoutBusy
                        ) {
                            if (checkoutBusy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                            else {
                                Icon(Icons.Outlined.Lock, null); Spacer(Modifier.width(7.dp)); Text(if (state.isLoggedIn) "ادامه و پرداخت امن" else "ورود و ادامه خرید", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallQtyButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(shape = CircleShape, color = PinkSoft, modifier = Modifier.size(32.dp).clickable(onClick = onClick)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Pink, modifier = Modifier.size(16.dp)) }
    }
}

@Composable
fun FavoritesScreen(state: AppState, onBack: () -> Unit, onProduct: (Product) -> Unit) {
    val products = state.products.filter { it.id in state.favorites }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AppHeader(title = "علاقه‌مندی‌ها", subtitle = "انتخاب‌هایی که دوست داشتی", canBack = true, onBack = onBack) }
        if (products.isEmpty()) item { EmptyBlock(Icons.Outlined.FavoriteBorder, "هنوز چیزی ذخیره نکردی", "روی قلب محصولات بزن تا اینجا نگه‌شون داریم") }
        else items(products, key = { it.id }) { p ->
            Card(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp).clickable { onProduct(p) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(p.imageUrl, p.name, Modifier.size(86.dp).clip(RoundedCornerShape(15.dp)), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(p.name, fontWeight = FontWeight.Bold, maxLines = 2); Spacer(Modifier.height(5.dp)); Text(money(p.price), color = Pink, fontWeight = FontWeight.Black, fontSize = 12.sp) }
                    IconButton(onClick = { state.toggleFavorite(p.id) }) { Icon(Icons.Outlined.Favorite, null, tint = Pink) }
                }
            }
        }
    }
}
