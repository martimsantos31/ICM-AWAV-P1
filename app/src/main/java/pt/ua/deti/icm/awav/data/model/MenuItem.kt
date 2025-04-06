package pt.ua.deti.icm.awav.data.model

data class MenuItem(
    val id: String,
    val name: String,
    val description: String = "",
    val price: Double,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val category: String = ""
)

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int = 1
)

data class Cart(
    val standId: String,
    val items: MutableList<CartItem> = mutableListOf(),
) {
    fun getTotalPrice(): Double {
        return items.sumOf { it.menuItem.price * it.quantity }
    }
    
    fun getTotalItems(): Int {
        return items.sumOf { it.quantity }
    }
    
    fun addItem(menuItem: MenuItem) {
        val existingItem = items.find { it.menuItem.id == menuItem.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            items.add(CartItem(menuItem))
        }
    }
    
    fun removeItem(menuItemId: String) {
        val existingItem = items.find { it.menuItem.id == menuItemId }
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity--
            } else {
                items.remove(existingItem)
            }
        }
    }
    
    fun clear() {
        items.clear()
    }
} 