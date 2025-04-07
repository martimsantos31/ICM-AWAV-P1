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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.AWAVApplication
import pt.ua.deti.icm.awav.data.model.Order
import pt.ua.deti.icm.awav.data.model.OrderItem
import pt.ua.deti.icm.awav.data.model.OrderStatus
import pt.ua.deti.icm.awav.data.model.Stand
import pt.ua.deti.icm.awav.data.repository.FirebaseRepository
import java.util.Date
import java.util.UUID

class OrdersViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val firebaseRepository = AWAVApplication.appContainer.firebaseRepository
    
    // UI State
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _assignedStands = MutableStateFlow<List<String>>(emptyList())
    val assignedStands: StateFlow<List<String>> = _assignedStands.asStateFlow()
    
    private val _hasAssignedStands = MutableStateFlow(false)
    val hasAssignedStands: StateFlow<Boolean> = _hasAssignedStands.asStateFlow()
    
    private val _waitTimeMinutes = MutableStateFlow(15) // Default 15 minutes
    val waitTimeMinutes: StateFlow<Int> = _waitTimeMinutes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        fetchUserStands()
    }
    
    // Fetch stands assigned to the current user
    private fun fetchUserStands() {
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
                            
                            // If user has assigned stands, fetch orders and stand data
                            if (managedStandIds.isNotEmpty()) {
                                val firstStandId = managedStandIds.first()
                                fetchWaitTime(firstStandId)
                                fetchOrders(firstStandId)
                            }
                        } else {
                            _hasAssignedStands.value = false
                            _isLoading.value = false
                        }
                    } else {
                        _isLoading.value = false
                    }
                } else {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "Error fetching user stands", e)
                _hasAssignedStands.value = false
                _isLoading.value = false
            }
        }
    }
    
    // Fetch the current wait time for a stand from Firestore
    private fun fetchWaitTime(standId: String) {
        viewModelScope.launch {
            try {
                // Get the stand using the FirebaseRepository
                val stand = firebaseRepository.getStandById(standId).first()
                
                if (stand != null) {
                    _waitTimeMinutes.value = stand.waitTimeMinutes
                }
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "Error fetching wait time", e)
            }
        }
    }
    
    // For now, generate dummy orders for demo purposes
    private fun fetchOrders(standId: String) {
        viewModelScope.launch {
            try {
                // In a real app, this would fetch orders from Firebase
                // For now, we'll generate mock data
                val dummyOrders = generateDummyOrders(standId)
                _orders.value = dummyOrders
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "Error fetching orders", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Update the wait time for the stand in Firestore
    fun updateWaitTime(minutes: Int) {
        viewModelScope.launch {
            if (_assignedStands.value.isNotEmpty()) {
                val standId = _assignedStands.value.first()
                
                // Log for debugging
                Log.d("OrdersViewModel", "Updating wait time to $minutes minutes for stand $standId")
                
                try {
                    // Update the wait time in Firestore using the FirebaseRepository
                    firebaseRepository.updateStandWaitTime(standId, minutes)
                    _waitTimeMinutes.value = minutes
                } catch (e: Exception) {
                    Log.e("OrdersViewModel", "Failed to update wait time", e)
                }
            }
        }
    }
    
    // Update an order's status
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            // Find and update the order in the local list
            val updatedOrders = _orders.value.map { order ->
                if (order.id == orderId) {
                    order.copy(
                        status = newStatus,
                        updatedAt = Date()
                    )
                } else {
                    order
                }
            }
            
            // Update the local state
            _orders.value = updatedOrders
            
            // In a real app, we would update this in Firebase
            Log.d("OrdersViewModel", "Updated order $orderId status to $newStatus")
        }
    }
    
    // Refresh orders
    fun refreshOrders() {
        if (_assignedStands.value.isNotEmpty()) {
            _isLoading.value = true
            fetchOrders(_assignedStands.value.first())
        }
    }
    
    // Generate dummy orders for demo purposes
    private fun generateDummyOrders(standId: String): List<Order> {
        val statuses = OrderStatus.values()
        val names = listOf("John Smith", "Maria Garcia", "Emma Brown", "James Johnson", "Olivia Rodriguez")
        val products = listOf(
            "Cheeseburger" to 8.99,
            "French Fries" to 3.99,
            "Pizza Slice" to 5.99,
            "Chicken Sandwich" to 7.99,
            "Soda" to 2.49,
            "Ice Cream" to 4.50,
            "Salad" to 6.99,
            "Hot Dog" to 4.99
        )
        
        val random = java.util.Random()
        
        // Generate between 0-5 orders
        val numberOfOrders = random.nextInt(6)
        val orders = mutableListOf<Order>()
        
        for (i in 0 until numberOfOrders) {
            // Generate between 1-5 items per order
            val numberOfItems = random.nextInt(5) + 1
            val items = mutableListOf<OrderItem>()
            var totalPrice = 0.0
            
            for (j in 0 until numberOfItems) {
                val product = products[random.nextInt(products.size)]
                val quantity = random.nextInt(3) + 1
                val price = product.second
                
                items.add(OrderItem(
                    productId = UUID.randomUUID().toString(),
                    productName = product.first,
                    quantity = quantity,
                    price = price,
                    notes = if (random.nextBoolean()) "Special request" else ""
                ))
                
                totalPrice += price * quantity
            }
            
            // Randomize the status with weighted distribution
            // More likely to have PENDING or PREPARING orders
            val statusIndex = when {
                random.nextDouble() < 0.4 -> 0 // 40% PENDING
                random.nextDouble() < 0.7 -> 1 // 30% PREPARING
                random.nextDouble() < 0.85 -> 2 // 15% READY
                random.nextDouble() < 0.95 -> 3 // 10% COMPLETED
                else -> 4 // 5% CANCELLED
            }
            
            // Generate a random time between now and 2 hours ago
            val createdAtOffset = random.nextInt(120) * 60 * 1000L * -1
            val createdAt = Date(System.currentTimeMillis() + createdAtOffset)
            
            // Updated time is between created time and now
            val updatedAtOffset = random.nextInt(Math.abs(createdAtOffset.toInt())) * -1
            val updatedAt = Date(System.currentTimeMillis() + updatedAtOffset)
            
            orders.add(Order(
                id = UUID.randomUUID().toString(),
                standId = standId,
                userId = UUID.randomUUID().toString(),
                userName = names[random.nextInt(names.size)],
                items = items,
                status = statuses[statusIndex],
                totalPrice = totalPrice,
                createdAt = createdAt,
                updatedAt = updatedAt,
                notes = if (random.nextBoolean()) "Please deliver to table 12" else ""
            ))
        }
        
        return orders
    }
} 