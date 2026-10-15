package ir.ariakish.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ir.ariakish.app.data.Category
import ir.ariakish.app.data.Product
import java.text.DecimalFormat
import kotlin.math.abs

fun money(value: Long): String = DecimalFormat("#,###").format(value) + " تومان"

@Composable
fun AppHeader(
    title: String = "آریا کیش",
    subtitle: String = "پوشاک شاد کوچولوها",
    canBack: Boolean = false,
    onBack: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (canBack) Color(0xFFF3EFF1) else PinkSoft,
            modifier = Modifier.size(44.dp).clickable { if (canBack) onBack() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(if (canBack) Icons.Outlined.ArrowForward else Icons.Outlined.Favorite, null, tint = Pink)
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 21.sp, color = Ink)
            if (subtitle.isNotBlank()) Text(subtitle, fontSize = 11.sp, color = Color(0xFF8C8589), maxLines = 1)
        }
        if (trailing != null) trailing() else {
            Surface(shape = CircleShape, color = Color(0xFFE9EEFF), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.AutoAwesome, null, tint = Lavender) }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, Modifier.weight(1f), fontWeight = FontWeight.Black, fontSize = 19.sp, color = Ink)
        if (!action.isNullOrBlank()) {
            Text(action, color = Pink, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}

@Composable
fun CategoryStrip(categories: List<Category>, selected: Int? = null, onClick: (Category) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories, key = { it.id }) { c ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(82.dp).clickable { onClick(c) }
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selected == c.id) PinkSoft else pastel(c.id),
                    border = if (selected == c.id) androidx.compose.foundation.BorderStroke(2.dp, Pink) else null,
                    modifier = Modifier.size(72.dp)
                ) {
                    if (!c.imageUrl.isNullOrBlank()) {
                        AsyncImage(c.imageUrl, c.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Checkroom, null, tint = if (selected == c.id) Pink else Lavender, modifier = Modifier.size(32.dp))
                        }
                    }
                }
                Spacer(Modifier.height(7.dp))
                Text(c.name, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (selected == c.id) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onOpen: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onOpen),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxWidth().aspectRatio(.92f).background(Color(0xFFF8F3F5)),
                    contentScale = ContentScale.Crop
                )
                if (product.onSale) {
                    Surface(
                        Modifier.padding(9.dp).align(Alignment.TopStart),
                        shape = RoundedCornerShape(10.dp),
                        color = Pink
                    ) {
                        Text("تخفیف", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onFavorite, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(if (favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (favorite) Pink else Color.DarkGray)
                }
                if (!product.inStock) {
                    Surface(Modifier.align(Alignment.BottomStart).padding(8.dp), shape = RoundedCornerShape(10.dp), color = Color(0xCC3A3638)) {
                        Text("ناموجود", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(7.dp))
                Text(money(product.price), color = Pink, fontWeight = FontWeight.Black, fontSize = 13.sp)
                if (product.onSale && product.regularPrice > product.price) {
                    Text(money(product.regularPrice), color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(Modifier.height(9.dp))
                FilledTonalButton(
                    onClick = onQuickAdd,
                    enabled = product.inStock && product.purchasable,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = PaddingValues(vertical = 7.dp)
                ) {
                    Icon(if (product.hasOptions) Icons.Outlined.Tune else Icons.Outlined.AddShoppingCart, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(if (product.hasOptions) "انتخاب" else "افزودن", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun BottomBar(current: String, cartCount: Int, onHome: () -> Unit, onShop: () -> Unit, onCart: () -> Unit, onAccount: () -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 10.dp) {
        NavigationBarItem(selected = current == "home", onClick = onHome, icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("خانه") })
        NavigationBarItem(selected = current == "shop", onClick = onShop, icon = { Icon(Icons.Outlined.Storefront, null) }, label = { Text("فروشگاه") })
        NavigationBarItem(
            selected = current == "cart",
            onClick = onCart,
            icon = {
                BadgedBox(badge = { if (cartCount > 0) Badge { Text(cartCount.coerceAtMost(99).toString()) } }) {
                    Icon(Icons.Outlined.ShoppingBag, null)
                }
            },
            label = { Text("سبد") }
        )
        NavigationBarItem(selected = current == "account", onClick = onAccount, icon = { Icon(Icons.Outlined.Person, null) }, label = { Text("حساب من") })
    }
}

@Composable
fun LoadingBlock(text: String = "در حال دریافت اطلاعات...") {
    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Pink, strokeWidth = 3.dp)
            Spacer(Modifier.height(12.dp))
            Text(text, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun EmptyBlock(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, text: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = PinkSoft, modifier = Modifier.size(88.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Pink, modifier = Modifier.size(42.dp)) }
        }
        Spacer(Modifier.height(18.dp))
        Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Spacer(Modifier.height(7.dp))
        Text(text, color = Color.Gray, fontSize = 13.sp, lineHeight = 21.sp)
        if (!action.isNullOrBlank()) {
            Spacer(Modifier.height(18.dp))
            Button(onClick = onAction, shape = RoundedCornerShape(15.dp)) { Text(action, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun InfoPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = .13f)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun pastel(seed: Int): Color = listOf(
    Color(0xFFFFE0E9), Color(0xFFDDEEFF), Color(0xFFFFECC9), Color(0xFFECE2FF), Color(0xFFE3F4E8)
)[abs(seed) % 5]
