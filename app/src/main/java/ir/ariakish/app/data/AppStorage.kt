package ir.ariakish.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AppStorage(context: Context) {
    private val prefs = context.getSharedPreferences("ariakish_app", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) { prefs.edit().putString("token", value).apply() }

    fun loadFavorites(): Set<Int> = prefs.getStringSet("favorites", emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
    fun saveFavorites(ids: Collection<Int>) = prefs.edit().putStringSet("favorites", ids.map { it.toString() }.toSet()).apply()

    fun saveCart(lines: List<CartLine>) {
        val arr = JSONArray()
        lines.forEach { line ->
            val p = line.product
            val o = JSONObject()
                .put("quantity", line.quantity)
                .put("product", JSONObject()
                    .put("id", p.id).put("name", p.name).put("type", p.type)
                    .put("image", p.imageUrl ?: "").put("price", p.price).put("regular", p.regularPrice)
                    .put("on_sale", p.onSale).put("permalink", p.permalink).put("in_stock", p.inStock)
                    .put("purchasable", p.purchasable).put("has_options", p.hasOptions))
            line.variation?.let { v ->
                val attrs = JSONArray()
                v.attributes.forEach { a -> attrs.put(JSONObject().put("key", a.key).put("label", a.label).put("value", a.value)) }
                o.put("variation", JSONObject().put("id", v.id).put("price", v.price).put("regular", v.regularPrice)
                    .put("on_sale", v.onSale).put("in_stock", v.inStock).put("purchasable", v.purchasable)
                    .put("image", v.imageUrl ?: "").put("attributes", attrs))
            }
            arr.put(o)
        }
        prefs.edit().putString("cart", arr.toString()).apply()
    }

    fun loadCart(): List<CartLine> = try {
        val arr = JSONArray(prefs.getString("cart", "[]"))
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val p = o.optJSONObject("product") ?: continue
                val product = Product(
                    id = p.optInt("id"), name = p.optString("name"), type = p.optString("type", "simple"),
                    imageUrl = p.optString("image").takeIf { it.isNotBlank() }, gallery = emptyList(),
                    price = p.optLong("price"), regularPrice = p.optLong("regular"), onSale = p.optBoolean("on_sale"),
                    shortDescription = "", description = "", permalink = p.optString("permalink"), averageRating = 0.0,
                    ratingCount = 0, inStock = p.optBoolean("in_stock", true), purchasable = p.optBoolean("purchasable", true),
                    hasOptions = p.optBoolean("has_options"), categoryIds = emptyList()
                )
                val variationObj = o.optJSONObject("variation")
                val variation = variationObj?.let { v ->
                    val attrs = v.optJSONArray("attributes") ?: JSONArray()
                    ProductVariation(
                        id = v.optInt("id"), price = v.optLong("price"), regularPrice = v.optLong("regular"), onSale = v.optBoolean("on_sale"),
                        inStock = v.optBoolean("in_stock", true), purchasable = v.optBoolean("purchasable", true),
                        imageUrl = v.optString("image").takeIf { it.isNotBlank() },
                        attributes = buildList {
                            for (j in 0 until attrs.length()) attrs.optJSONObject(j)?.let { a ->
                                add(VariationAttribute(a.optString("key"), a.optString("label"), a.optString("value")))
                            }
                        }
                    )
                }
                add(CartLine(product, o.optInt("quantity", 1).coerceAtLeast(1), variation))
            }
        }
    } catch (_: Exception) { emptyList() }

    fun clearSession() { prefs.edit().remove("token").apply() }
}
