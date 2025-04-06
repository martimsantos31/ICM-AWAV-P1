package pt.ua.deti.icm.awav.data.repository

import androidx.compose.runtime.mutableStateOf
import pt.ua.deti.icm.awav.data.model.Cart
import pt.ua.deti.icm.awav.data.model.MenuItem
import pt.ua.deti.icm.awav.data.model.Stand

object StandRepository {
    
    // In-memory cart for the current session
    private val currentCart = mutableStateOf<Cart?>(null)
    
    fun getCart(standId: String): Cart {
        if (currentCart.value == null || currentCart.value?.standId != standId) {
            currentCart.value = Cart(standId)
        }
        return currentCart.value!!
    }
    
    // Mock data for stands
    private val stands = listOf(
        Stand(
            id = "1",
            name = "Daniel's Chorizo",
            description = "Authentic Portuguese chorizo sandwiches and more",
            imageUrl = "chorizo",
            eventId = "1",
            location = "Food Court - North",
            workersIds = listOf("worker1", "worker2"),
            products = emptyList(),
            transactionHistory = emptyList()
        ),
        Stand(
            id = "2",
            name = "Mohamed's Kebab",
            description = "Traditional kebabs and Middle Eastern cuisine",
            imageUrl = "kebab",
            eventId = "1",
            location = "Food Court - South",
            workersIds = listOf("worker3"),
            products = emptyList(),
            transactionHistory = emptyList()
        )
    )
    
    // Mock data for menu items
    private val menuItems = mapOf(
        "1" to listOf(
            MenuItem(
                id = "1-1",
                name = "Pão com Chouriço",
                description = "Traditional Portuguese bread with chorizo",
                price = 3.5,
                imageUrl = "chorizo_bread",
                isAvailable = true,
                category = "Food"
            ),
            MenuItem(
                id = "1-2",
                name = "Pão com Chouriço c/Queijo",
                description = "Portuguese bread with chorizo and cheese",
                price = 4.0,
                imageUrl = "chorizo_cheese",
                isAvailable = false,
                category = "Food"
            ),
            MenuItem(
                id = "1-3",
                name = "Água",
                description = "Bottled water 500ml",
                price = 2.0,
                imageUrl = "water",
                isAvailable = true,
                category = "Drinks"
            )
        ),
        "2" to listOf(
            MenuItem(
                id = "2-1",
                name = "Kebab de Frango",
                description = "Chicken kebab with vegetables",
                price = 5.0,
                imageUrl = "chicken_kebab",
                isAvailable = true,
                category = "Food"
            ),
            MenuItem(
                id = "2-2",
                name = "Kebab Misto",
                description = "Mixed meat kebab with vegetables",
                price = 5.5,
                imageUrl = "mixed_kebab",
                isAvailable = true,
                category = "Food"
            ),
            MenuItem(
                id = "2-3",
                name = "Refrigerante",
                description = "Soft drink 330ml",
                price = 2.5,
                imageUrl = "soda",
                isAvailable = true,
                category = "Drinks"
            )
        )
    )
    
    fun getAllStands(): List<Stand> = stands
    
    fun getStandById(standId: String): Stand? = stands.find { it.id == standId }
    
    fun getMenuItems(standId: String): List<MenuItem> = menuItems[standId] ?: emptyList()
    
    fun getMenuItemById(standId: String, menuItemId: String): MenuItem? {
        return menuItems[standId]?.find { it.id == menuItemId }
    }
    
    fun getWaitTime(standId: String): Int {
        // Mock wait times - in a real app this would come from the backend
        return when (standId) {
            "1" -> 30
            "2" -> 15
            else -> 10
        }
    }
} 