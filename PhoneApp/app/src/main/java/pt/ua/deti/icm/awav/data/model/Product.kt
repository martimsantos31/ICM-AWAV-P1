package pt.ua.deti.icm.awav.data.model

/**
 * Represents a product that can be added to a stand's menu
 */
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val isAvailable: Boolean = true
) 