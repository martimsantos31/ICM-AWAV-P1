package pt.ua.deti.icm.awav.data.model

import pt.ua.deti.icm.awav.data.room.entity.MenuItem


class Cart(val standId: String) {
    private val _items = mutableListOf<CartItem>()
    
    fun getItems(): List<CartItem> = _items.toList()
    
    fun addItem(menuItem: MenuItem) {
        val existingItem = _items.find { it.menuItem.id == menuItem.id }
        if (existingItem != null) {
            // If item already exists, increase quantity
            val index = _items.indexOf(existingItem)
            _items[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            // Add new item with quantity 1
            _items.add(CartItem(menuItem))
        }
    }
    
    fun removeItem(menuItem: MenuItem) {
        val existingItem = _items.find { it.menuItem.id == menuItem.id } ?: return
        val index = _items.indexOf(existingItem)
        
        if (existingItem.quantity > 1) {
            // Decrease quantity if more than 1
            _items[index] = existingItem.copy(quantity = existingItem.quantity - 1)
        } else {
            // Remove item if quantity is 1
            _items.removeAt(index)
        }
    }
    
    fun getTotalPrice(): Double {
        return _items.sumOf { it.menuItem.price * it.quantity }
    }
    
    fun getTotalItems(): Int {
        return _items.sumOf { it.quantity }
    }
    
    fun isEmpty(): Boolean {
        return _items.isEmpty()
    }
    
    fun clear() {
        _items.clear()
    }
} 