package pt.ua.deti.icm.awav.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.AuthRepository
import pt.ua.deti.icm.awav.data.model.Product
import pt.ua.deti.icm.awav.data.repository.StandRepository
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.entity.MenuItem
import pt.ua.deti.icm.awav.data.room.entity.Stand

class ManageStandViewModel : ViewModel() {
    private val TAG = "ManageStandViewModel"
    
    // Repositories
    private val standsRepository: StandsRepository = AWAVApplication.appContainer.standsRepository
    private val authRepository = AuthRepository.getInstance()
    
    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _standName = MutableStateFlow<String>("Loading...")
    val standName: StateFlow<String> = _standName.asStateFlow()
    
    private val _stands = MutableStateFlow<List<Stand>>(emptyList())
    val stands: StateFlow<List<Stand>> = _stands.asStateFlow()
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _selectedStand = MutableStateFlow<Stand?>(null)
    val selectedStand: StateFlow<Stand?> = _selectedStand.asStateFlow()
    
    // Load stands assigned to current worker
    fun loadAssignedStands() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Get current user email (which is used as the identifier in Worker table)
                val currentUser = authRepository.currentUser.value
                if (currentUser != null) {
                    val email = currentUser.email
                    if (email.isNullOrBlank()) {
                        Log.e(TAG, "Cannot load stands: Current user has no email")
                        _error.value = "Cannot load stands: User email not available"
                        return@launch
                    }
                    
                    Log.d(TAG, "Loading stands for worker with email: $email")
                    
                    val stands = standsRepository.getStandsForWorker(email)
                    _stands.value = stands
                    
                    if (stands.isNotEmpty()) {
                        // Auto-select the first stand
                        selectStand(stands.first().id)
                    } else {
                        Log.w(TAG, "No stands assigned to worker: $email")
                        _error.value = "You don't have any stands assigned"
                    }
                    
                    Log.d(TAG, "Loaded ${stands.size} stands for worker")
                } else {
                    Log.e(TAG, "Cannot load stands: Current user is null")
                    _error.value = "Cannot load stands: Not logged in"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading stands: ${e.message}", e)
                _error.value = "Failed to load stands: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Select a stand to work with
    fun selectStand(standId: Int) {
        viewModelScope.launch {
            val stand = _stands.value.find { it.id == standId }
            if (stand != null) {
                _selectedStand.value = stand
                _standName.value = stand.name
                loadProducts(standId.toString())
            }
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
            Log.e(TAG, "Error deleting product: ${e.message}", e)
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