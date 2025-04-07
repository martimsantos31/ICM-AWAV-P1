package pt.ua.deti.icm.awav.data.repository

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.data.model.Event
import pt.ua.deti.icm.awav.data.model.MenuItem
import pt.ua.deti.icm.awav.data.model.Stand
import java.util.Date

/**
 * Repository class that handles Firebase Firestore operations for Event and Stand entities
 */
class FirebaseRepository {
    private val db = Firebase.firestore
    private val TAG = "FirebaseRepository"
    
    // Collection references
    private val eventsCollection = db.collection("events")
    private val standsCollection = db.collection("stands")
    private val menuItemsCollection = db.collection("menuItems")
    
    // Event Operations
    
    /**
     * Adds a new event to Firestore
     * @param event The event to add
     * @return The ID of the newly created event
     */
    suspend fun addEvent(event: Event): String {
        return try {
            val docRef = eventsCollection.add(event.toMap()).await()
            // Update the document with its ID
            eventsCollection.document(docRef.id).update("id", docRef.id).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding event", e)
            throw e
        }
    }
    
    /**
     * Gets all events from Firestore as a Flow
     * @return Flow of all events
     */
    fun getAllEvents(): Flow<List<Event>> = callbackFlow {
        val listenerRegistration = eventsCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed", e)
                return@addSnapshotListener
            }
            
            val events = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Event::class.java) ?: Event(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    date = doc.getDate("date") ?: Date(),
                    location = doc.getString("location") ?: ""
                )
            } ?: emptyList()
            
            trySend(events)
        }
        
        // Clean up listener when flow is cancelled
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Gets a specific event by ID
     * @param eventId The ID of the event to get
     * @return Flow of the event
     */
    fun getEventById(eventId: String): Flow<Event?> = callbackFlow {
        val listenerRegistration = eventsCollection.document(eventId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed", e)
                    return@addSnapshotListener
                }
                
                val event = snapshot?.let { doc ->
                    doc.toObject(Event::class.java) ?: Event(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        date = doc.getDate("date") ?: Date(),
                        location = doc.getString("location") ?: ""
                    )
                }
                
                trySend(event)
            }
        
        // Clean up listener when flow is cancelled
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Updates an existing event in Firestore
     * @param event The event to update
     */
    suspend fun updateEvent(event: Event) {
        try {
            eventsCollection.document(event.id).set(event.toMap()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event", e)
            throw e
        }
    }
    
    /**
     * Deletes an event from Firestore
     * @param eventId The ID of the event to delete
     */
    suspend fun deleteEvent(eventId: String) {
        try {
            // First, delete all stands for this event
            val stands = standsCollection.whereEqualTo("eventId", eventId).get().await()
            for (standDoc in stands.documents) {
                deleteStand(standDoc.id)
            }
            
            // Then delete the event
            eventsCollection.document(eventId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting event", e)
            throw e
        }
    }
    
    // Stand Operations
    
    /**
     * Adds a new stand to Firestore
     * @param stand The stand to add
     * @return The ID of the newly created stand
     */
    suspend fun addStand(stand: Stand): String {
        return try {
            val docRef = standsCollection.add(stand.toMap()).await()
            // Update the document with its ID
            standsCollection.document(docRef.id).update("id", docRef.id).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding stand", e)
            throw e
        }
    }
    
    /**
     * Gets all stands for a specific event
     * @param eventId The ID of the event
     * @return Flow of stands for the event
     */
    fun getStandsForEvent(eventId: String): Flow<List<Stand>> = callbackFlow {
        val listenerRegistration = standsCollection
            .whereEqualTo("eventId", eventId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed", e)
                    return@addSnapshotListener
                }
                
                val stands = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Stand::class.java) ?: Stand(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        eventId = doc.getString("eventId") ?: "",
                        waitTimeMinutes = doc.getLong("waitTimeMinutes")?.toInt() ?: 0
                    )
                } ?: emptyList()
                
                trySend(stands)
            }
        
        // Clean up listener when flow is cancelled
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Gets a specific stand by ID
     * @param standId The ID of the stand to get
     * @return Flow of the stand
     */
    fun getStandById(standId: String): Flow<Stand?> = callbackFlow {
        val listenerRegistration = standsCollection.document(standId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed", e)
                    return@addSnapshotListener
                }
                
                val stand = snapshot?.let { doc ->
                    doc.toObject(Stand::class.java) ?: Stand(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        eventId = doc.getString("eventId") ?: "",
                        waitTimeMinutes = doc.getLong("waitTimeMinutes")?.toInt() ?: 0
                    )
                }
                
                trySend(stand)
            }
        
        // Clean up listener when flow is cancelled
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Updates an existing stand in Firestore
     * @param stand The stand to update
     */
    suspend fun updateStand(stand: Stand) {
        try {
            standsCollection.document(stand.id).set(stand.toMap()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stand", e)
            throw e
        }
    }
    
    /**
     * Updates just the wait time for a stand
     * @param standId The ID of the stand to update
     * @param waitTimeMinutes The new wait time in minutes
     */
    suspend fun updateStandWaitTime(standId: String, waitTimeMinutes: Int) {
        try {
            standsCollection.document(standId)
                .update("waitTimeMinutes", waitTimeMinutes).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating stand wait time", e)
            throw e
        }
    }
    
    /**
     * Deletes a stand from Firestore
     * @param standId The ID of the stand to delete
     */
    suspend fun deleteStand(standId: String) {
        try {
            // First, delete all menu items for this stand
            val menuItems = menuItemsCollection.whereEqualTo("standId", standId).get().await()
            for (menuItemDoc in menuItems.documents) {
                menuItemsCollection.document(menuItemDoc.id).delete().await()
            }
            
            // Then delete the stand
            standsCollection.document(standId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting stand", e)
            throw e
        }
    }
    
    // Menu Item Operations
    
    /**
     * Adds a new menu item to Firestore
     * @param menuItem The menu item to add
     * @return The ID of the newly created menu item
     */
    suspend fun addMenuItem(menuItem: MenuItem): String {
        return try {
            val docRef = menuItemsCollection.add(menuItem.toMap()).await()
            // Update the document with its ID
            menuItemsCollection.document(docRef.id).update("id", docRef.id).await()
            docRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding menu item", e)
            throw e
        }
    }
    
    /**
     * Gets all menu items for a specific stand
     * @param standId The ID of the stand
     * @return Flow of menu items for the stand
     */
    fun getMenuItemsForStand(standId: String): Flow<List<MenuItem>> = callbackFlow {
        val listenerRegistration = menuItemsCollection
            .whereEqualTo("standId", standId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed", e)
                    return@addSnapshotListener
                }
                
                val menuItems = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MenuItem::class.java) ?: MenuItem(
                        id = doc.id,
                        standId = doc.getString("standId") ?: "",
                        name = doc.getString("name") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        description = doc.getString("description") ?: "",
                        isAvailable = doc.getBoolean("isAvailable") ?: true
                    )
                } ?: emptyList()
                
                trySend(menuItems)
            }
        
        // Clean up listener when flow is cancelled
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Updates an existing menu item in Firestore
     * @param menuItem The menu item to update
     */
    suspend fun updateMenuItem(menuItem: MenuItem) {
        try {
            menuItemsCollection.document(menuItem.id).set(menuItem.toMap()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating menu item", e)
            throw e
        }
    }
    
    /**
     * Deletes a menu item from Firestore
     * @param menuItemId The ID of the menu item to delete
     */
    suspend fun deleteMenuItem(menuItemId: String) {
        try {
            menuItemsCollection.document(menuItemId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting menu item", e)
            throw e
        }
    }
} 