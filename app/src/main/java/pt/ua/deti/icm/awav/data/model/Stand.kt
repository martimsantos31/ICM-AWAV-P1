package pt.ua.deti.icm.awav.data.model

data class Stand(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val eventId: String = "",
    val location: String = "",
    val workersIds: List<String> = emptyList(),
    val products: List<Product> = emptyList(),
    val transactionHistory: List<Transaction> = emptyList()
)

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val category: String = "",
    val available: Boolean = true
)

data class Transaction(
    val id: String = "",
    val standId: String = "",
    val userId: String = "",
    val products: List<TransactionItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class TransactionItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 1,
    val pricePerUnit: Double = 0.0
) 