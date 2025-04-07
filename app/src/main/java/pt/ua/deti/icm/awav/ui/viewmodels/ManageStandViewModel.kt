package pt.ua.deti.icm.awav.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.data.model.Product
import pt.ua.deti.icm.awav.data.repository.StandRepository
import pt.ua.deti.icm.awav.data.room.entity.MenuItem

class ManageStandViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    
    // UI State
    private val _standName = MutableStateFlow<String>("Loading...")
    val standName: StateFlow<String> = _standName.asStateFlow()
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _assignedStands = MutableStateFlow<List<String>>(emptyList())
    val assignedStands: StateFlow<List<String>> = _assignedStands.asStateFlow()
    
    private val _hasAssignedStands = MutableStateFlow(false)
    val hasAssignedStands: StateFlow<Boolean> = _hasAssignedStands.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        fetchUserStands()
    }
    
    // Fetch stands assigned to the current user
    fun fetchUserStands() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val email = currentUser.email
                    if (email != null) {
                        val userDoc = firestore.collection("users").document(email).get().await()
                        
                        if (userDoc.exists()) {
                            val managedStandIds = userDoc.get("managedStandIds") as? List<String> ?: emptyList()
                            _assignedStands.value = managedStandIds
                            _hasAssignedStands.value = managedStandIds.isNotEmpty()
                            
                            // If user has assigned stands, load the first one by default
                            if (managedStandIds.isNotEmpty()) {
                                loadStand(managedStandIds.first())
                                loadProducts(managedStandIds.first())
                            }
                        } else {
                            _hasAssignedStands.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ManageStandViewModel", "Error fetching user stands", e)
                _hasAssignedStands.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
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
    
    // Update an existing product
    suspend fun updateProduct(product: Product, standId: Int) {
        try {
            StandRepository.updateProduct(product, standId)
            
            // Refresh the products list to show the updated product
            loadProducts(standId.toString())
        } catch (e: Exception) {
            Log.e("ManageStandViewModel", "Error updating product", e)
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