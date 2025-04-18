package pt.ua.deti.icm.awav.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.model.Cart
import pt.ua.deti.icm.awav.data.model.CartItem
import pt.ua.deti.icm.awav.data.model.Product
import pt.ua.deti.icm.awav.data.room.entity.MenuItem
import pt.ua.deti.icm.awav.data.room.entity.Stand
import java.util.UUID

/**
 * A singleton repository for stand and menu operations
 */
object StandRepository {
    // Cache for carts
    private val carts = mutableMapOf<String, Cart>()
    
    // Hold a reference to the repository we're wrapping
    private val standsRepository: StandsRepository by lazy {
        AWAVApplication.appContainer.standsRepository
    }
    
    // Get stand by ID
    fun getStandById(standId: String): Stand? {
        // Convert from String to Int, assuming that's the type in the database
        val id = standId.toIntOrNull() ?: return null
        
        return try {
            runBlocking {
                standsRepository.getStandById(id).first()
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Get menu items for a stand
    fun getMenuItems(standId: String): List<MenuItem> {
        val id = standId.toIntOrNull() ?: return emptyList()
        
        return try {
            runBlocking {
                standsRepository.getMenuItemsForStand(id).first()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Get or create a cart for a stand
    fun getCart(standId: String): Cart {
        return carts.getOrPut(standId) { Cart(standId) }
    }
    
    // Convert Product to MenuItem and save it to the database
    suspend fun saveProduct(product: Product, standId: Int): String {
        val menuItem = MenuItem(
            id = product.id.ifEmpty { UUID.randomUUID().toString() },
            standId = standId,
            name = product.name,
            price = product.price,
            description = product.description,
            isAvailable = product.isAvailable
        )
        
        standsRepository.insertMenuItem(menuItem)
        return menuItem.id
    }
    
    // Delete a product/menu item
    suspend fun deleteProduct(productId: String, standId: Int) {
        // We'd need to implement a delete method in StandsRepository
        // For now we can't do this without modifying the interface
    }
    
    // Update a product/menu item
    suspend fun updateProduct(product: Product, standId: Int) {
        saveProduct(product, standId)
    }
} 