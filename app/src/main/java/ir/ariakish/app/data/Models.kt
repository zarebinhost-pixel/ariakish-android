package ir.ariakish.app.data

data class Product(
    val id: Int,
    val name: String,
    val type: String,
    val imageUrl: String?,
    val gallery: List<String>,
    val price: Long,
    val regularPrice: Long,
    val onSale: Boolean,
    val shortDescription: String,
    val description: String,
    val permalink: String,
    val averageRating: Double,
    val ratingCount: Int,
    val inStock: Boolean,
    val purchasable: Boolean,
    val hasOptions: Boolean,
    val categoryIds: List<Int>
)

data class Category(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val count: Int
)

data class AppBanner(
    val id: Int,
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val imageUrl: String?,
    val targetUrl: String?
)

data class VariationAttribute(
    val key: String,
    val label: String,
    val value: String
)

data class ProductVariation(
    val id: Int,
    val price: Long,
    val regularPrice: Long,
    val onSale: Boolean,
    val inStock: Boolean,
    val purchasable: Boolean,
    val imageUrl: String?,
    val attributes: List<VariationAttribute>
)

data class CartLine(
    val product: Product,
    val quantity: Int,
    val variation: ProductVariation? = null
) {
    val key: String get() = "${product.id}:${variation?.id ?: 0}"
    val unitPrice: Long get() = variation?.price ?: product.price
}

data class Address(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val country: String = "IR",
    val state: String = "",
    val city: String = "",
    val address1: String = "",
    val address2: String = "",
    val postcode: String = ""
)

data class AppUser(
    val id: Int,
    val email: String,
    val name: String,
    val firstName: String,
    val lastName: String,
    val billing: Address = Address(),
    val shipping: Address = Address()
)

data class AuthSession(
    val token: String,
    val user: AppUser
)

data class OrderSummary(
    val id: Int,
    val number: String,
    val status: String,
    val statusLabel: String,
    val date: String?,
    val total: String,
    val currency: String,
    val itemCount: Int
)

data class OrderLine(
    val name: String,
    val quantity: Int,
    val total: String,
    val imageUrl: String?
)

data class OrderDetail(
    val id: Int,
    val number: String,
    val status: String,
    val statusLabel: String,
    val date: String?,
    val total: String,
    val currency: String,
    val paymentMethod: String,
    val billing: Address,
    val shipping: Address,
    val items: List<OrderLine>
)
