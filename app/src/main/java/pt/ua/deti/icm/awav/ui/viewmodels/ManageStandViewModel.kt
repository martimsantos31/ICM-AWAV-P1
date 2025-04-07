package pt.ua.deti.icm.awav.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.data.model.Product
import pt.ua.deti.icm.awav.data.repository.StandRepository
import pt.ua.deti.icm.awav.data.room.entity.MenuItem

class ManageStandViewModel : ViewModel() {
    // UI State
    private val _standName = MutableStateFlow<String>("Loading...")
    val standName: StateFlow<String> = _standName.asStateFlow()
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    // Load stand information
    fun loadStand(standId: String) {
        viewModelScope.launch {
            val stand = StandRepository.getStandById(standId)
            _standName.value = stand?.name ?: "Unknown Stand"
        }
    }
    
    // Load products for the stand
    fun loadProducts(standId: String) {
        viewModelScope.launch {
            val menuItems = StandRepository.getMenuItems(standId)
            _products.value = convertMenuItemsToProducts(menuItems)
        }
    }
    
    // Save a new product
    suspend fun saveProduct(product: Product, standId: Int) {
        val productId = StandRepository.saveProduct(product, standId)
        // Refresh the list
        loadProducts(standId.toString())
    }
    
    // Delete a product
    suspend fun deleteProduct(product: Product, standId: Int) {
        try {
            // This implementation is a workaround since we don't have a direct
            // deletion method in the repository
            StandRepository.deleteProduct(product.id, standId)
            
            // Refresh the products list
            loadProducts(standId.toString())
        } catch (e: Exception) {
            // Handle error, maybe show a message
        }
    }
    
    // Convert MenuItem entities to Product models
    private fun convertMenuItemsToProducts(menuItems: List<MenuItem>): List<Product> {
        return menuItems.map { menuItem ->
            Product(
                id = menuItem.id,
                name = menuItem.name,
                description = menuItem.description,
                price = menuItem.price,
                isAvailable = menuItem.isAvailable
            )
        }
    }
} 