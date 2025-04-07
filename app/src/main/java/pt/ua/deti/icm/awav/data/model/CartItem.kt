package pt.ua.deti.icm.awav.data.model

import pt.ua.deti.icm.awav.data.room.entity.MenuItem

/**
 * Represents an item in a shopping cart with a menu item and quantity
 */
data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int = 1
) 