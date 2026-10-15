package ir.ariakish.app.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ir.ariakish.app.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AppState(
    context: Context,
    private val scope: CoroutineScope
) {
    private val storage = AppStorage(context)

    var products by mutableStateOf<List<Product>>(emptyList())
        private set
    var categories by mutableStateOf<List<Category>>(emptyList())
        private set
    var banners by mutableStateOf<List<AppBanner>>(emptyList())
        private set
    var googleClientId by mutableStateOf("")
        private set
    var loadingHome by mutableStateOf(true)
        private set
    var homeError by mutableStateOf<String?>(null)
        private set

    val cart = mutableStateListOf<CartLine>()
    val favorites = mutableStateListOf<Int>()

    var token by mutableStateOf(storage.token)
        private set
    var user by mutableStateOf<AppUser?>(null)
        private set
    var authBusy by mutableStateOf(false)
        private set
    var orders by mutableStateOf<List<OrderSummary>>(emptyList())
        private set
    var ordersBusy by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)

    val cartCount: Int get() = cart.sumOf { it.quantity }
    val cartTotal: Long get() = cart.sumOf { it.unitPrice * it.quantity }
    val isLoggedIn: Boolean get() = !token.isNullOrBlank() && user != null

    suspend fun initialize() {
        cart.clear(); cart.addAll(storage.loadCart())
        favorites.clear(); favorites.addAll(storage.loadFavorites())
        loadingHome = true
        homeError = null
        try {
            coroutineScope {
                val p = async { StoreApi.products(perPage = 24) }
                val c = async { StoreApi.categories() }
                val b = async { runCatching { BridgeApi.banners() }.getOrDefault(emptyList()) }
                val g = async { runCatching { BridgeApi.googleClientId() }.getOrDefault("") }
                products = p.await()
                categories = c.await()
                banners = b.await()
                googleClientId = g.await()
            }
        } catch (e: Exception) {
            homeError = e.message ?: "اتصال به فروشگاه برقرار نشد"
        } finally {
            loadingHome = false
        }

        val saved = token
        if (!saved.isNullOrBlank()) {
            runCatching { BridgeApi.me(saved) }
                .onSuccess { user = it }
                .onFailure { clearSession() }
        }
    }

    suspend fun shopProducts(search: String = "", categoryId: Int? = null): List<Product> =
        StoreApi.products(perPage = 50, search = search, categoryId = categoryId)

    suspend fun variations(productId: Int): List<ProductVariation> = BridgeApi.variations(productId)

    fun toggleFavorite(id: Int) {
        if (id in favorites) favorites.remove(id) else favorites.add(id)
        storage.saveFavorites(favorites)
    }

    fun addToCart(product: Product, variation: ProductVariation? = null) {
        val key = "${product.id}:${variation?.id ?: 0}"
        val index = cart.indexOfFirst { it.key == key }
        if (index >= 0) cart[index] = cart[index].copy(quantity = cart[index].quantity + 1)
        else cart.add(CartLine(product, 1, variation))
        storage.saveCart(cart)
        message = "به سبد خرید اضافه شد"
    }

    fun changeQuantity(key: String, delta: Int) {
        val index = cart.indexOfFirst { it.key == key }
        if (index < 0) return
        val next = cart[index].quantity + delta
        if (next <= 0) cart.removeAt(index) else cart[index] = cart[index].copy(quantity = next)
        storage.saveCart(cart)
    }

    fun removeFromCart(key: String) {
        cart.removeAll { it.key == key }
        storage.saveCart(cart)
    }

    fun clearCart() {
        cart.clear()
        storage.saveCart(cart)
    }

    fun login(email: String, password: String, onDone: (Boolean) -> Unit = {}) {
        scope.launch {
            authBusy = true
            runCatching { BridgeApi.login(email.trim(), password) }
                .onSuccess { saveSession(it); message = "خوش آمدید"; onDone(true) }
                .onFailure { message = it.message ?: "ورود انجام نشد"; onDone(false) }
            authBusy = false
        }
    }

    fun register(name: String, email: String, password: String, onDone: (Boolean) -> Unit = {}) {
        scope.launch {
            authBusy = true
            runCatching { BridgeApi.register(name.trim(), email.trim(), password) }
                .onSuccess { saveSession(it); message = "حساب شما ساخته شد"; onDone(true) }
                .onFailure { message = it.message ?: "ثبت‌نام انجام نشد"; onDone(false) }
            authBusy = false
        }
    }

    fun googleLogin(idToken: String, onDone: (Boolean) -> Unit = {}) {
        scope.launch {
            authBusy = true
            runCatching { BridgeApi.googleLogin(idToken) }
                .onSuccess { saveSession(it); message = "با حساب Google وارد شدید"; onDone(true) }
                .onFailure { message = it.message ?: "ورود Google انجام نشد"; onDone(false) }
            authBusy = false
        }
    }

    fun forgotPassword(email: String) {
        scope.launch {
            authBusy = true
            runCatching { BridgeApi.forgotPassword(email.trim()) }
                .onSuccess { message = "اگر این ایمیل ثبت شده باشد، لینک بازیابی ارسال می‌شود" }
                .onFailure { message = it.message ?: "ارسال لینک بازیابی انجام نشد" }
            authBusy = false
        }
    }

    fun refreshMe() {
        val t = token ?: return
        scope.launch {
            runCatching { BridgeApi.me(t) }
                .onSuccess { user = it }
                .onFailure { clearSession() }
        }
    }

    fun updateProfile(newUser: AppUser, onDone: (Boolean) -> Unit = {}) {
        val t = token ?: return onDone(false)
        scope.launch {
            authBusy = true
            runCatching { BridgeApi.updateMe(t, newUser) }
                .onSuccess { user = it; message = "اطلاعات حساب ذخیره شد"; onDone(true) }
                .onFailure { message = it.message ?: "ذخیره اطلاعات انجام نشد"; onDone(false) }
            authBusy = false
        }
    }

    fun refreshOrders() {
        val t = token ?: return
        scope.launch {
            ordersBusy = true
            runCatching { BridgeApi.orders(t) }
                .onSuccess { orders = it }
                .onFailure { message = it.message ?: "دریافت سفارش‌ها انجام نشد" }
            ordersBusy = false
        }
    }

    suspend fun orderDetail(id: Int): OrderDetail {
        val t = token ?: error("برای مشاهده سفارش وارد حساب شوید")
        return BridgeApi.order(t, id)
    }

    suspend fun checkoutUrl(): String {
        val t = token ?: error("برای ادامه خرید وارد حساب شوید")
        if (cart.isEmpty()) error("سبد خرید خالی است")
        return BridgeApi.checkoutHandoff(t, cart)
    }

    fun logout() {
        val old = token
        clearSession()
        if (!old.isNullOrBlank()) scope.launch { runCatching { BridgeApi.logout(old) } }
        message = "از حساب خارج شدید"
    }

    private fun saveSession(session: AuthSession) {
        token = session.token
        user = session.user
        storage.token = session.token
        refreshOrders()
    }

    private fun clearSession() {
        token = null
        user = null
        orders = emptyList()
        storage.clearSession()
    }
}
