package ir.ariakish.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.ariakish.app.data.AppBanner
import ir.ariakish.app.data.Product

private sealed interface Screen {
    data object Home : Screen
    data class Shop(val categoryId: Int? = null, val categoryName: String? = null) : Screen
    data class ProductDetail(val product: Product) : Screen
    data object Cart : Screen
    data object Account : Screen
    data object Favorites : Screen
    data object Orders : Screen
    data class OrderDetail(val id: Int) : Screen
    data object Profile : Screen
}

@Composable
fun AriakishApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state = remember { AppState(context.applicationContext, scope) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    val history = remember { mutableStateListOf<Screen>() }
    val snackbarHostState = remember { SnackbarHostState() }

    fun go(next: Screen) {
        if (screen == next) return
        history.add(screen)
        screen = next
    }
    fun root(next: Screen) {
        history.clear()
        screen = next
    }
    fun back() {
        screen = if (history.isNotEmpty()) history.removeAt(history.lastIndex) else Screen.Home
    }

    LaunchedEffect(Unit) { state.initialize() }
    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        state.message = null
    }
    BackHandler(enabled = history.isNotEmpty()) { back() }

    val bottomVisible = screen is Screen.Home || screen is Screen.Shop || screen is Screen.Cart || screen is Screen.Account
    val bottomKey = when (screen) {
        Screen.Home -> "home"
        is Screen.Shop -> "shop"
        Screen.Cart -> "cart"
        Screen.Account -> "account"
        else -> ""
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (bottomVisible) {
                    BottomBar(
                        current = bottomKey,
                        cartCount = state.cartCount,
                        onHome = { root(Screen.Home) },
                        onShop = { root(Screen.Shop()) },
                        onCart = { root(Screen.Cart) },
                        onAccount = { root(Screen.Account) }
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (val current = screen) {
                    Screen.Home -> HomeScreen(
                        state = state,
                        onShop = { id, name -> go(Screen.Shop(id, name)) },
                        onProduct = { go(Screen.ProductDetail(it)) },
                        onFavorites = { go(Screen.Favorites) },
                        onBanner = { banner -> handleBanner(context, banner) { go(Screen.Shop()) } }
                    )
                    is Screen.Shop -> ShopScreen(
                        state = state,
                        initialCategoryId = current.categoryId,
                        initialCategoryName = current.categoryName,
                        onBack = { if (history.isEmpty()) root(Screen.Home) else back() },
                        onProduct = { go(Screen.ProductDetail(it)) }
                    )
                    is Screen.ProductDetail -> ProductDetailScreen(
                        state = state,
                        product = current.product,
                        onBack = ::back,
                        onCart = { go(Screen.Cart) }
                    )
                    Screen.Cart -> CartScreen(state, onShop = { root(Screen.Shop()) }, onAccount = { root(Screen.Account) })
                    Screen.Account -> AccountScreen(state, onOrders = { go(Screen.Orders) }, onProfile = { go(Screen.Profile) }, onFavorites = { go(Screen.Favorites) })
                    Screen.Favorites -> FavoritesScreen(state, onBack = ::back, onProduct = { go(Screen.ProductDetail(it)) })
                    Screen.Orders -> OrdersScreen(state, onBack = ::back, onOrder = { go(Screen.OrderDetail(it)) })
                    is Screen.OrderDetail -> OrderDetailScreen(state, current.id, onBack = ::back)
                    Screen.Profile -> ProfileScreen(state, onBack = ::back)
                }
            }
        }
    }
}

private fun handleBanner(context: android.content.Context, banner: AppBanner, fallback: () -> Unit) {
    val url = banner.targetUrl
    if (url.isNullOrBlank()) fallback()
    else runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }.onFailure { fallback() }
}
