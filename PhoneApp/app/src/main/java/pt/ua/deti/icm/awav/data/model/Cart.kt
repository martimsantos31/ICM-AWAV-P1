package pt.ua.deti.icm.awav.data.model

import pt.ua.deti.icm.awav.data.room.entity.MenuItem


class Cart(val standId: String) {
    private val items = mutableListOf<CartItem>()
    
    fun getItems(): List<CartItem> = items.toList()
    
    fun addItem(menuItem: MenuItem) {
        val existingItem = items.find { it.menuItem.id == menuItem.id }
        if (existingItem != null) {
            // If item already exists, increase quantity
            val index = items.indexOf(existingItem)
            items[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            // Add new item with quantity 1
            items.add(CartItem(menuItem))
        }
    }
    
    fun removeItem(menuItem: MenuItem) {
        val existingItem = items.find { it.menuItem.id == menuItem.id } ?: return
        val index = items.indexOf(existingItem)
        
        if (existingItem.quantity > 1) {
            // Decrease quantity if more than 1
            items[index] = existingItem.copy(quantity = existingItem.quantity - 1)
        } else {
            // Remove item if quantity is 1
            items.removeAt(index)
        }
    }
    
    fun getTotalPrice(): Double {
        return items.sumOf { it.menuItem.price * it.quantity }
    }
    
    fun getTotalItems(): Int {
        return items.sumOf { it.quantity }
    }
    
    fun isEmpty(): Boolean {
        return items.isEmpty()
    }
    
    fun clear() {
        items.clear()
    }
} 