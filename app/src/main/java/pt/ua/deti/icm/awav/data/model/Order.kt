package pt.ua.deti.icm.awav.data.model

import java.util.Date
import java.util.UUID

enum class OrderStatus {
    PENDING,    // Order has been placed but not yet started
    PREPARING,  // Order is being prepared
    READY,      // Order is ready for pickup
    COMPLETED,  // Order has been picked up/delivered
    CANCELLED   // Order has been cancelled
}

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val standId: String,
    val userId: String,
    val userName: String,
    val items: List<OrderItem>,
    val status: OrderStatus = OrderStatus.PENDING,
    val totalPrice: Double,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val notes: String = ""
)

data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val notes: String = ""
) 