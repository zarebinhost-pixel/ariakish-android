package ir.ariakish.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ir.ariakish.app.auth.GoogleAuth
import ir.ariakish.app.data.Address
import ir.ariakish.app.data.AppUser
import ir.ariakish.app.data.OrderDetail
import ir.ariakish.app.data.OrderSummary
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    state: AppState,
    onOrders: () -> Unit,
    onProfile: () -> Unit,
    onFavorites: () -> Unit
) {
    if (state.isLoggedIn) AccountDashboard(state, onOrders, onProfile, onFavorites)
    else LoginRegisterScreen(state)
}

@Composable
private fun LoginRegisterScreen(state: AppState) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var registerMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = PinkSoft, modifier = Modifier.size(94.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Favorite, null, tint = Pink, modifier = Modifier.size(46.dp)) }
                }
                Spacer(Modifier.height(15.dp))
                Text(if (registerMode) "ساخت حساب آریا کیش" else "خوش اومدی", fontWeight = FontWeight.Black, fontSize = 23.sp)
                Text("خریدهای کوچولوها رو یکجا مدیریت کن", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(24.dp))
            }
        }
        if (registerMode) item {
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("نام و نام خانوادگی") }, leadingIcon = { Icon(Icons.Outlined.Person, null) }, singleLine = true, shape = RoundedCornerShape(16.dp))
            Spacer(Modifier.height(10.dp))
        }
        item {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("ایمیل") }, leadingIcon = { Icon(Icons.Outlined.Email, null) }, singleLine = true, shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), label = { Text("رمز عبور") }, leadingIcon = { Icon(Icons.Outlined.Lock, null) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, shape = RoundedCornerShape(16.dp))
                if (!registerMode) {
                    TextButton(onClick = { if (email.isBlank()) state.message = "اول ایمیلت رو وارد کن" else state.forgotPassword(email) }, modifier = Modifier.align(Alignment.Start)) { Text("رمز عبور را فراموش کرده‌ام") }
                }
                Button(
                    onClick = {
                        if (email.isBlank() || password.length < 6 || (registerMode && name.isBlank())) {
                            state.message = "اطلاعات را کامل وارد کن؛ رمز حداقل ۶ کاراکتر باشد"
                        } else if (registerMode) state.register(name, email, password) else state.login(email, password)
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                    enabled = !state.authBusy
                ) {
                    if (state.authBusy) CircularProgressIndicator(Modifier.size(21.dp), strokeWidth = 2.dp, color = Color.White)
                    else Text(if (registerMode) "ثبت‌نام" else "ورود", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(13.dp))
                OutlinedButton(
                    onClick = {
                        if (state.googleClientId.isBlank()) {
                            state.message = "برای فعال شدن ورود Google باید Client ID در نسخه نهایی تنظیم شود"
                        } else scope.launch {
                            runCatching { GoogleAuth.signIn(context, state.googleClientId) }
                                .onSuccess { state.googleLogin(it) }
                                .onFailure { state.message = it.message ?: "ورود Google انجام نشد" }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.width(9.dp)); Text("ادامه با حساب Google", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = { registerMode = !registerMode }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(if (registerMode) "حساب داری؟ وارد شو" else "حساب نداری؟ ثبت‌نام", fontWeight = FontWeight.Bold, color = Lavender)
                }
                Spacer(Modifier.height(18.dp))
                Text("ورود با شماره موبایل و Digits در معماری اپ پیش‌بینی شده و بعداً بدون تغییر حساب مشتری اضافه می‌شود.", color = Color.Gray, fontSize = 10.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun AccountDashboard(state: AppState, onOrders: () -> Unit, onProfile: () -> Unit, onFavorites: () -> Unit) {
    val user = state.user ?: return
    LaunchedEffect(Unit) { state.refreshOrders() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { AppHeader(title = "حساب من", subtitle = user.email) }
        item {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp).background(
                    Brush.linearGradient(listOf(Color(0xFFFFEAF1), Color(0xFFEAF0FF))), RoundedCornerShape(26.dp)
                ).padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = .8f), modifier = Modifier.size(62.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Person, null, tint = Pink, modifier = Modifier.size(32.dp)) }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(user.name.ifBlank { user.firstName.ifBlank { "مشتری آریا کیش" } }, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("همه خریدها و اطلاعاتت همین‌جاست", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardTile(Icons.Outlined.ReceiptLong, "سفارش‌ها", state.orders.size.toString(), Modifier.weight(1f), onOrders)
                DashboardTile(Icons.Outlined.FavoriteBorder, "علاقه‌مندی", state.favorites.size.toString(), Modifier.weight(1f), onFavorites)
                DashboardTile(Icons.Outlined.EditLocationAlt, "پروفایل", "ویرایش", Modifier.weight(1f), onProfile)
            }
        }
        item { SectionTitle("آخرین سفارش‌ها", "همه سفارش‌ها", onOrders) }
        if (state.ordersBusy) item { LoadingBlock("سفارش‌ها در حال دریافت است") }
        else if (state.orders.isEmpty()) item { EmptyBlock(Icons.Outlined.Inventory2, "هنوز سفارشی نداری", "بعد از اولین خرید، سفارش‌ها اینجا نمایش داده می‌شوند") }
        else items(state.orders.take(3), key = { it.id }) { order -> OrderSummaryCard(order, onOrders) }
        item {
            OutlinedButton(onClick = state::logout, modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Outlined.Logout, null); Spacer(Modifier.width(7.dp)); Text("خروج از حساب")
            }
        }
    }
}

@Composable
private fun DashboardTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0E7EB))) {
        Column(Modifier.padding(vertical = 15.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Lavender); Spacer(Modifier.height(5.dp)); Text(value, fontWeight = FontWeight.Black, color = Pink); Text(title, fontSize = 10.sp)
        }
    }
}

@Composable
private fun OrderSummaryCard(order: OrderSummary, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 18.dp).clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = Color(0xFFEFF1FF), modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Inventory2, null, tint = Lavender) } }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text("سفارش #${order.number}", fontWeight = FontWeight.Bold); Text("${order.itemCount} کالا • ${order.statusLabel}", color = Color.Gray, fontSize = 11.sp) }
            Text(order.total + " " + currencyLabel(order.currency), color = Pink, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun OrdersScreen(state: AppState, onBack: () -> Unit, onOrder: (Int) -> Unit) {
    LaunchedEffect(Unit) { state.refreshOrders() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AppHeader(title = "سفارش‌های من", subtitle = "پیگیری خریدهای آریا کیش", canBack = true, onBack = onBack) }
        if (state.ordersBusy) item { LoadingBlock() }
        else if (state.orders.isEmpty()) item { EmptyBlock(Icons.Outlined.Inventory2, "سفارشی وجود ندارد", "بعد از خرید، وضعیت سفارش‌ها اینجا قابل پیگیری است") }
        else items(state.orders, key = { it.id }) { o ->
            Card(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp).clickable { onOrder(o.id) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("سفارش #${o.number}", Modifier.weight(1f), fontWeight = FontWeight.Black); InfoPill(Icons.Outlined.Schedule, o.statusLabel, Lavender) }
                    Spacer(Modifier.height(8.dp)); Text("${o.itemCount} کالا", color = Color.Gray, fontSize = 11.sp); Spacer(Modifier.height(4.dp)); Text(o.total + " " + currencyLabel(o.currency), color = Pink, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun OrderDetailScreen(state: AppState, orderId: Int, onBack: () -> Unit) {
    var order by remember { mutableStateOf<OrderDetail?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(orderId) {
        runCatching { state.orderDetail(orderId) }.onSuccess { order = it }.onFailure { error = it.message }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { AppHeader(title = "جزئیات سفارش", subtitle = "اطلاعات کامل خرید", canBack = true, onBack = onBack) }
        when {
            error != null -> item { EmptyBlock(Icons.Outlined.ErrorOutline, "سفارش باز نشد", error ?: "خطا") }
            order == null -> item { LoadingBlock() }
            else -> {
                val o = order!!
                item {
                    Card(Modifier.fillMaxWidth().padding(18.dp), shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(18.dp)) {
                            Row { Text("سفارش #${o.number}", Modifier.weight(1f), fontWeight = FontWeight.Black, fontSize = 18.sp); InfoPill(Icons.Outlined.Verified, o.statusLabel, Lavender) }
                            Spacer(Modifier.height(12.dp)); Text("مبلغ کل", color = Color.Gray, fontSize = 11.sp); Text(o.total + " " + currencyLabel(o.currency), color = Pink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            if (o.paymentMethod.isNotBlank()) { Spacer(Modifier.height(7.dp)); Text("روش پرداخت: ${o.paymentMethod}", fontSize = 11.sp, color = Color.Gray) }
                        }
                    }
                }
                items(o.items) { line ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(line.imageUrl, line.name, Modifier.size(64.dp).background(Color(0xFFF6F1F4), RoundedCornerShape(14.dp)))
                        Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(line.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis); Text("تعداد: ${line.quantity}", fontSize = 11.sp, color = Color.Gray) }
                        Text(line.total + " " + currencyLabel(o.currency), color = Pink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(state: AppState, onBack: () -> Unit) {
    val original = state.user ?: return
    var firstName by remember(original.id) { mutableStateOf(original.firstName) }
    var lastName by remember(original.id) { mutableStateOf(original.lastName) }
    var phone by remember(original.id) { mutableStateOf(original.billing.phone) }
    var stateName by remember(original.id) { mutableStateOf(original.billing.state) }
    var city by remember(original.id) { mutableStateOf(original.billing.city) }
    var address1 by remember(original.id) { mutableStateOf(original.billing.address1) }
    var postcode by remember(original.id) { mutableStateOf(original.billing.postcode) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { AppHeader(title = "اطلاعات حساب", subtitle = original.email, canBack = true, onBack = onBack) }
        item { Text("مشخصات مشتری", fontWeight = FontWeight.Black, fontSize = 17.sp) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProfileField("نام", firstName, { firstName = it }, Modifier.weight(1f)); ProfileField("نام خانوادگی", lastName, { lastName = it }, Modifier.weight(1f)) } }
        item { ProfileField("شماره تماس", phone, { phone = it }, Modifier.fillMaxWidth()) }
        item { Text("آدرس صورتحساب", fontWeight = FontWeight.Black, fontSize = 17.sp) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { ProfileField("استان", stateName, { stateName = it }, Modifier.weight(1f)); ProfileField("شهر", city, { city = it }, Modifier.weight(1f)) } }
        item { ProfileField("آدرس", address1, { address1 = it }, Modifier.fillMaxWidth()) }
        item { ProfileField("کدپستی", postcode, { postcode = it }, Modifier.fillMaxWidth()) }
        item {
            Button(
                onClick = {
                    val billing = Address(firstName, lastName, phone, original.billing.country.ifBlank { "IR" }, stateName, city, address1, original.billing.address2, postcode)
                    state.updateProfile(original.copy(firstName = firstName, lastName = lastName, name = "$firstName $lastName".trim(), billing = billing, shipping = billing))
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(17.dp),
                enabled = !state.authBusy
            ) { Text("ذخیره اطلاعات", fontWeight = FontWeight.Bold) }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ProfileField(label: String, value: String, onValue: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(value = value, onValueChange = onValue, modifier = modifier, label = { Text(label) }, shape = RoundedCornerShape(16.dp), singleLine = true)
}

private fun currencyLabel(code: String): String = when (code.uppercase()) {
    "IRT", "TOMAN" -> "تومان"
    "IRR" -> "ریال"
    else -> code
}
