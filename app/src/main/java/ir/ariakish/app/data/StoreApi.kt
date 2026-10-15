package ir.ariakish.app.data

import ir.ariakish.app.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

object StoreApi {
    suspend fun products(
        perPage: Int = 24,
        page: Int = 1,
        search: String = "",
        categoryId: Int? = null,
        onSale: Boolean? = null,
        orderBy: String = "date"
    ): List<Product> {
        val params = mutableListOf("per_page=$perPage", "page=$page", "orderby=$orderBy")
        if (search.isNotBlank()) params += "search=${enc(search)}"
        if (categoryId != null) params += "category=$categoryId"
        if (onSale != null) params += "on_sale=$onSale"
        val result = ApiHttp.get(BuildConfig.STORE_API + "products?" + params.joinToString("&"))
        if (result.code !in 200..299) error(ApiHttp.message(result, "خطا در دریافت محصولات"))
        return parseProducts(result.body)
    }

    suspend fun product(id: Int): Product {
        val result = ApiHttp.get(BuildConfig.STORE_API + "products/$id")
        if (result.code !in 200..299) error(ApiHttp.message(result, "محصول پیدا نشد"))
        return parseProduct(JSONObject(result.body))
    }

    suspend fun categories(): List<Category> {
        val result = ApiHttp.get(BuildConfig.STORE_API + "products/categories?per_page=100&hide_empty=true")
        if (result.code !in 200..299) error(ApiHttp.message(result, "خطا در دریافت دسته‌بندی‌ها"))
        val arr = JSONArray(result.body)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val image = o.optJSONObject("image")?.optString("src")
                add(Category(o.optInt("id"), clean(o.optString("name")), image, o.optInt("count")))
            }
        }.sortedByDescending { it.count }
    }

    private fun parseProducts(json: String): List<Product> {
        val arr = JSONArray(json)
        return buildList {
            for (i in 0 until arr.length()) arr.optJSONObject(i)?.let { add(parseProduct(it)) }
        }
    }

    private fun parseProduct(o: JSONObject): Product {
        val prices = o.optJSONObject("prices") ?: JSONObject()
        val images = o.optJSONArray("images")
        val gallery = buildList {
            if (images != null) for (i in 0 until images.length()) {
                val src = images.optJSONObject(i)?.optString("src").orEmpty()
                if (src.isNotBlank()) add(src)
            }
        }
        val categories = o.optJSONArray("categories")
        val categoryIds = buildList {
            if (categories != null) for (i in 0 until categories.length()) {
                val id = categories.optJSONObject(i)?.optInt("id") ?: 0
                if (id > 0) add(id)
            }
        }
        val minor = prices.optInt("currency_minor_unit", 0).coerceIn(0, 6)
        fun money(key: String): Long {
            val raw = prices.optString(key, "0").toLongOrNull() ?: 0L
            var factor = 1L
            repeat(minor) { factor *= 10L }
            return if (factor <= 1) raw else raw / factor
        }
        return Product(
            id = o.optInt("id"),
            name = clean(o.optString("name")),
            type = o.optString("type", "simple"),
            imageUrl = gallery.firstOrNull(),
            gallery = gallery,
            price = money("price"),
            regularPrice = money("regular_price"),
            onSale = o.optBoolean("on_sale"),
            shortDescription = stripHtml(o.optString("short_description")),
            description = stripHtml(o.optString("description")),
            permalink = o.optString("permalink"),
            averageRating = o.optString("average_rating", "0").toDoubleOrNull() ?: 0.0,
            ratingCount = o.optInt("review_count"),
            inStock = o.optBoolean("is_in_stock", true),
            purchasable = o.optBoolean("is_purchasable", true),
            hasOptions = o.optBoolean("has_options", o.optString("type") == "variable"),
            categoryIds = categoryIds
        )
    }

    private fun stripHtml(value: String): String = clean(
        value.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()
    )

    private fun clean(value: String): String = value
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&#8211;", "–")
        .replace("&#8212;", "—")
        .replace("&#8230;", "…")
        .replace("&quot;", "\"")
        .trim()

    private fun enc(value: String) = URLEncoder.encode(value, "UTF-8")
}
