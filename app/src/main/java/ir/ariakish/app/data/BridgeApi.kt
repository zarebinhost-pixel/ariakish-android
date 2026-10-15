package ir.ariakish.app.data

import ir.ariakish.app.BuildConfig
import org.json.JSONArray
import org.json.JSONObject

object BridgeApi {
    suspend fun health(): Boolean {
        val r = ApiHttp.get(BuildConfig.BRIDGE_API + "health")
        return r.code in 200..299
    }

    suspend fun googleClientId(): String {
        val r = ApiHttp.get(BuildConfig.BRIDGE_API + "config")
        if (r.code !in 200..299) return ""
        return runCatching { JSONObject(r.body).optString("google_client_id") }.getOrDefault("")
    }

    suspend fun banners(): List<AppBanner> {
        val r = ApiHttp.get(BuildConfig.BRIDGE_API + "banners")
        if (r.code !in 200..299) return emptyList()
        val arr = JSONObject(r.body).optJSONArray("banners") ?: JSONArray()
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                add(
                    AppBanner(
                        id = o.optInt("id"),
                        title = o.optString("title"),
                        subtitle = o.optString("subtitle"),
                        buttonText = o.optString("button_text", "مشاهده"),
                        imageUrl = o.optString("image_url").takeIf { it.isNotBlank() },
                        targetUrl = o.optString("target_url").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    suspend fun variations(productId: Int): List<ProductVariation> {
        val r = ApiHttp.get(BuildConfig.BRIDGE_API + "products/$productId/variations")
        if (r.code !in 200..299) throw IllegalStateException(ApiHttp.message(r, "خطا در دریافت رنگ و سایز"))
        val arr = JSONObject(r.body).optJSONArray("variations") ?: JSONArray()
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val attrs = o.optJSONArray("attributes") ?: JSONArray()
                val list = buildList {
                    for (j in 0 until attrs.length()) {
                        val a = attrs.optJSONObject(j) ?: continue
                        add(VariationAttribute(a.optString("key"), a.optString("label"), a.optString("value")))
                    }
                }
                add(
                    ProductVariation(
                        id = o.optInt("id"),
                        price = o.optLong("price"),
                        regularPrice = o.optLong("regular_price"),
                        onSale = o.optBoolean("on_sale"),
                        inStock = o.optBoolean("in_stock"),
                        purchasable = o.optBoolean("purchasable"),
                        imageUrl = o.optString("image_url").takeIf { it.isNotBlank() },
                        attributes = list
                    )
                )
            }
        }
    }

    suspend fun register(name: String, email: String, password: String): AuthSession {
        val body = JSONObject().put("name", name).put("email", email).put("password", password)
        return authResult(ApiHttp.post(BuildConfig.BRIDGE_API + "register", body))
    }

    suspend fun login(email: String, password: String): AuthSession {
        val body = JSONObject().put("email", email).put("password", password)
        return authResult(ApiHttp.post(BuildConfig.BRIDGE_API + "login", body))
    }

    suspend fun googleLogin(idToken: String): AuthSession {
        val body = JSONObject().put("id_token", idToken)
        return authResult(ApiHttp.post(BuildConfig.BRIDGE_API + "google-login", body))
    }

    suspend fun forgotPassword(email: String) {
        val r = ApiHttp.post(BuildConfig.BRIDGE_API + "forgot-password", JSONObject().put("email", email))
        if (r.code !in 200..299) throw IllegalStateException(ApiHttp.message(r, "ارسال لینک بازیابی انجام نشد"))
    }

    suspend fun me(token: String): AppUser {
        val r = ApiHttp.get(BuildConfig.BRIDGE_API + "me", token)
        if (r.code !in 200..299) throw IllegalStateException(ApiHttp.message(r, "نشست کاربری منقضی شده است"))
        return parseUser(JSONObject(r.body).getJSONObject("user"))
    }

    suspend fun updateMe(token: String, user: AppUser): AppUser {
        val body = JSONObject()
            .put("first_name", user.firstName)
            .put("last_name", user.lastName)
            .put("name", user.name)
            .put("billing", addressJson(user.billing))
            .put("shipping", addressJson(user.shipping))
        val r = ApiHttp.put(BuildConfig.BRIDGE_API + "me", body, token)
        if (r.code !in 200..299) throw IllegalStateException(ApiHttp.message(r, "ذخیره اطلاعات انجام نشد"))
        return parseUser(JSONObject(r.body).getJSONObject("user"))
    }

    suspend fun orders(token: String): List<OrderSummary> {
        val r = ApiHttp.get(BuildConfig.BRIDGE_API + "orders", token)
        if (r.code !in 200..299) throw IllegalStateException(ApiHttp.message(r, "خطا در دریافت سفارش‌ها"))
        val arr = JSONObject(r.body).optJSONArray("orders") ?: JSONArray()
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                add(
                    OrderSummary(
                        id = o.optInt("id"),
                        number = o.optString("number"),
                        status = o.optString("status"),
                        statusLabel = o.optString("status_label"),
                        date = o.optString("date").takeIf { it.isNotBlank() && it != "null" },
                        total = o.optString("total"),
                        currency = o.optString("currency"),
                        itemCount = o.optInt("item_count")
                    )
                )
            }
        }
    }

    suspend fun order(token: String, orderId: Int): OrderDetail {
        val r = ApiHttp.get(BuildConfig.BRIDGE_API + "orders/$orderId", token)
        if (r.code !in 200..299) throw IllegalStateException(ApiHttp.message(r, "سفارش پیدا نشد"))
        val o = JSONObject(r.body).getJSONObject("order")
        val items = o.optJSONArray("items") ?: JSONArray()
        return OrderDetail(
            id = o.optInt("id"),
            number = o.optString("number"),
            status = o.optString("status"),
            statusLabel = o.optString("status_label"),
            date = o.optString("date").takeIf { it.isNotBlank() && it != "null" },
            total = o.optString("total"),
            currency = o.optString("currency"),
            paymentMethod = o.optString("payment_method"),
            billing = parseAddress(o.optJSONObject("billing") ?: JSONObject()),
            shipping = parseAddress(o.optJSONObject("shipping") ?: JSONObject()),
            items = buildList {
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    add(OrderLine(item.optString("name"), item.optInt("quantity"), item.optString("total"), item.optString("image_url").takeIf { it.isNotBlank() }))
                }
            }
        )
    }

    suspend fun checkoutHandoff(token: String, cart: List<CartLine>): String {
        val items = JSONArray()
        cart.forEach { line ->
            items.put(
                JSONObject()
                    .put("product_id", line.product.id)
                    .put("variation_id", line.variation?.id ?: 0)
                    .put("quantity", line.quantity)
            )
        }
        val r = ApiHttp.post(BuildConfig.BRIDGE_API + "checkout-handoff", JSONObject().put("items", items), token)
        if (r.code !in 200..299) throw IllegalStateException(ApiHttp.message(r, "انتقال به پرداخت انجام نشد"))
        return JSONObject(r.body).optString("checkout_url").ifBlank { throw IllegalStateException("آدرس پرداخت دریافت نشد") }
    }

    suspend fun logout(token: String) {
        ApiHttp.post(BuildConfig.BRIDGE_API + "logout", JSONObject(), token)
    }

    private fun authResult(r: HttpResult): AuthSession {
        if (r.code !in 200..299) throw IllegalStateException(ApiHttp.message(r, "ورود انجام نشد"))
        val o = JSONObject(r.body)
        return AuthSession(o.getString("access_token"), parseUser(o.getJSONObject("user")))
    }

    private fun parseUser(o: JSONObject): AppUser = AppUser(
        id = o.optInt("id"),
        email = o.optString("email"),
        name = o.optString("name"),
        firstName = o.optString("first_name"),
        lastName = o.optString("last_name"),
        billing = parseAddress(o.optJSONObject("billing") ?: JSONObject()),
        shipping = parseAddress(o.optJSONObject("shipping") ?: JSONObject())
    )

    private fun parseAddress(o: JSONObject) = Address(
        firstName = o.optString("first_name"),
        lastName = o.optString("last_name"),
        phone = o.optString("phone"),
        country = o.optString("country", "IR"),
        state = o.optString("state"),
        city = o.optString("city"),
        address1 = o.optString("address_1"),
        address2 = o.optString("address_2"),
        postcode = o.optString("postcode")
    )

    private fun addressJson(a: Address) = JSONObject()
        .put("first_name", a.firstName)
        .put("last_name", a.lastName)
        .put("phone", a.phone)
        .put("country", a.country)
        .put("state", a.state)
        .put("city", a.city)
        .put("address_1", a.address1)
        .put("address_2", a.address2)
        .put("postcode", a.postcode)
}
