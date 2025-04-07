package pt.ua.deti.icm.awav.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.data.model.FirebaseTicket
import pt.ua.deti.icm.awav.data.room.entity.Event
import java.util.UUID

class FirebaseTicketRepository {
    private val TAG = "FirebaseTicketRepo"
    
    private val db = FirebaseFirestore.getInstance()
    private val ticketsCollection = db.collection("tickets")
    private val eventsCollection = db.collection("events")
    private val usersCollection = db.collection("users")
    private val auth = FirebaseAuth.getInstance()
    
    // User's active tickets
    private val _userTickets = MutableStateFlow<List<FirebaseTicket>>(emptyList())
    val userTickets: StateFlow<List<FirebaseTicket>> = _userTickets.asStateFlow()
    
    // Has active tickets flag
    private val _hasActiveTickets = MutableStateFlow(false)
    val hasActiveTickets: StateFlow<Boolean> = _hasActiveTickets.asStateFlow()
    
    /**
     * Generate tickets for an event based on its capacity
     */
    suspend fun generateTicketsForEvent(event: Event, price: Double): Boolean {
        return try {
            Log.d(TAG, "Generating tickets for event ${event.name} with capacity ${event.capacity}")
            
            // Save event to Firebase first
            val eventData = mapOf(
                "id" to event.id,
                "name" to event.name,
                "description" to event.description,
                "location" to event.location,
                "startDate" to event.startDate,
                "endDate" to event.endDate,
                "isActive" to event.isActive,
                "capacity" to event.capacity,
                "createdAt" to Timestamp.now()
            )
            
            eventsCollection.document(event.id.toString()).set(eventData).await()
            
            // Generate tickets
            val batch = db.batch()
            for (i in 1..event.capacity) {
                val ticketId = UUID.randomUUID().toString()
                val ticketRef = ticketsCollection.document(ticketId)
                
                val ticketData = hashMapOf(
                    "id" to ticketId,
                    "eventId" to event.id,
                    "price" to price,
                    "bought" to false,
                    "seat" to "Seat $i", // Optional: generate seat numbers
                    "createdAt" to Timestamp.now()
                )
                
                batch.set(ticketRef, ticketData)
            }
            
            // Commit the batch
            batch.commit().await()
            Log.d(TAG, "Successfully generated ${event.capacity} tickets for event ${event.name}")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error generating tickets: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get available (unsold) tickets for an event
     */
    suspend fun getAvailableTicketsForEvent(eventId: Int): List<FirebaseTicket> {
        return try {
            val query = ticketsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("bought", false)
                .get()
                .await()
            
            query.documents.mapNotNull { doc ->
                doc.toObject(FirebaseTicket::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting available tickets: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Purchase a ticket
     */
    suspend fun purchaseTicket(eventId: Int): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val userEmail = auth.currentUser?.email ?: return false
        
        return try {
            // Find an available ticket
            val availableTickets = getAvailableTicketsForEvent(eventId)
            if (availableTickets.isEmpty()) {
                Log.d(TAG, "No available tickets for event $eventId")
                return false
            }
            
            // Get the first available ticket
            val ticket = availableTickets.first()
            
            // Update the ticket as purchased
            val ticketRef = ticketsCollection.document(ticket.id)
            val now = Timestamp.now()
            
            ticketRef.update(
                mapOf(
                    "bought" to true,
                    "userId" to userId,
                    "purchaseDate" to now
                )
            ).await()
            
            // Also store the ticket ID in the user document
            val userDoc = usersCollection.document(userEmail)
            
            userDoc.get().await().let { document ->
                val ticketData = mapOf(
                    "id" to ticket.id,
                    "eventId" to eventId,
                    "purchaseDate" to now
                )
                
                val tickets = document.get("tickets") as? List<Map<String, Any>> ?: listOf()
                val updatedTickets = tickets + ticketData
                
                userDoc.update("tickets", updatedTickets).await()
            }
            
            // Refresh user tickets
            refreshUserTickets()
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error purchasing ticket: ${e.message}", e)
            false
        }
    }
    
    /**
     * Refresh user's tickets from Firebase
     */
    suspend fun refreshUserTickets() {
        val userId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email
        
        if (userId == null || userEmail == null) {
            _userTickets.value = emptyList()
            _hasActiveTickets.value = false
            return
        }
        
        try {
            // First check user document for tickets
            val userDoc = usersCollection.document(userEmail).get().await()
            
            if (userDoc.exists()) {
                val tickets = userDoc.get("tickets") as? List<Map<String, Any>> ?: emptyList()
                
                if (tickets.isNotEmpty()) {
                    // User has tickets in their user document
                    _hasActiveTickets.value = true
                    
                    // Fetch the full ticket details
                    val ticketIds = tickets.mapNotNull { it["id"] as? String }
                    val userTickets = mutableListOf<FirebaseTicket>()
                    
                    for (ticketId in ticketIds) {
                        val ticketDoc = ticketsCollection.document(ticketId).get().await()
                        ticketDoc.toObject(FirebaseTicket::class.java)?.let {
                            userTickets.add(it)
                        }
                    }
                    
                    _userTickets.value = userTickets
                    return
                }
            }
            
            // As a fallback, query the tickets collection directly
            val query = ticketsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("bought", true)
                .get()
                .await()
            
            val userTickets = query.documents.mapNotNull { doc ->
                doc.toObject(FirebaseTicket::class.java)
            }
            
            _userTickets.value = userTickets
            _hasActiveTickets.value = userTickets.isNotEmpty()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing user tickets: ${e.message}", e)
            _hasActiveTickets.value = false
        }
    }
    
    /**
     * Get count of remaining tickets for an event
     */
    suspend fun getRemainingTicketCount(eventId: Int): Int {
        return try {
            val query = ticketsCollection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("bought", false)
                .get()
                .await()
            
            query.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting remaining ticket count: ${e.message}", e)
            0
        }
    }
    
    /**
     * Get event details from Firebase
     */
    suspend fun getEvent(eventId: Int): Map<String, Any>? {
        return try {
            val doc = eventsCollection.document(eventId.toString()).get().await()
            if (doc.exists()) {
                doc.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting event: ${e.message}", e)
            null
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: FirebaseTicketRepository? = null
        
        fun getInstance(): FirebaseTicketRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseTicketRepository()
                INSTANCE = instance
                instance
            }
        }
    }
} 