package pt.ua.deti.icm.awav.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import pt.ua.deti.icm.awav.data.room.dao.EventDao
import pt.ua.deti.icm.awav.data.room.entity.Event
import pt.ua.deti.icm.awav.data.room.entity.Presenters
import pt.ua.deti.icm.awav.data.room.entity.ScheduleItem
import pt.ua.deti.icm.awav.data.room.entity.Ticket
import pt.ua.deti.icm.awav.data.room.entity.UserTicket

class OfflineEventsRepository(private val eventDao: EventDao) : EventsRepository {
    override fun getActiveEvents(): Flow<List<Event>> = eventDao.getActiveEvents()
    
    override fun getEventById(id: Int): Flow<Event> = eventDao.getEventById(id)
        .catch { e -> 
            Log.e("EventsRepository", "Error getting event by ID $id: ${e.message}", e)
            throw e
        }
    
    override fun getScheduleItemsForEvent(eventId: Int): Flow<List<ScheduleItem>> = 
        eventDao.getScheduleItemsForEvent(eventId)
    
    override fun getPresentersForScheduleItem(scheduleItemId: Int): Flow<List<Presenters>> = 
        eventDao.getPresentersForScheduleItem(scheduleItemId)
    
    override fun getTicketsForEvent(eventId: Int): Flow<List<Ticket>> = 
        eventDao.getTicketsForEvent(eventId)

    override fun getUserTickets(userId: String): Flow<List<UserTicket>> =
        eventDao.getUserTickets(userId)
        
    override fun getUserTicketByTicketId(userId: String, ticketId: Int): Flow<UserTicket?> =
        eventDao.getUserTicketByTicketId(userId, ticketId)
        
    override fun getActiveTicketCount(userId: String): Flow<Int> =
        eventDao.getActiveTicketCount(userId)
        
    override fun getEventsForUserTickets(userId: String): Flow<List<Event>> =
        eventDao.getEventsForUserTickets(userId)

    override suspend fun insertEvent(event: Event): Long {
        try {
            Log.d("EventsRepository", "Inserting event: $event")
            val id = eventDao.insertEvent(event)
            Log.d("EventsRepository", "Event inserted successfully with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error inserting event: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun insertScheduleItem(scheduleItem: ScheduleItem): Long = 
        eventDao.insertScheduleItem(scheduleItem)
    
    override suspend fun insertPresenter(presenter: Presenters) = 
        eventDao.insertPresenter(presenter)
    
    override suspend fun insertTicket(ticket: Ticket) = 
        eventDao.insertTicket(ticket)
        
    override suspend fun insertUserTicket(userTicket: UserTicket): Long {
        try {
            // First insert into local database
            val id = eventDao.insertUserTicket(userTicket)
            Log.d("EventsRepository", "User ticket inserted in local DB with ID: $id")
            
            try {
                // Store the ticket directly in the user's document in Firestore, like roles
                val db = FirebaseFirestore.getInstance()
                val usersCollection = db.collection("users")
                
                // Get the user document by userId (which is the Firebase UID)
                val userEmail = findUserEmailByUid(userTicket.userId)
                if (userEmail != null) {
                    // Use email as document ID like the roles system does
                    val userDoc = usersCollection.document(userEmail)
                    
                    // Get current tickets array or create new one
                    userDoc.get().await().let { document ->
                        // Create ticket data
                        val ticketData = mapOf(
                            "id" to userTicket.ticketId,
                            "purchaseDate" to userTicket.purchaseDate,
                            "isActive" to userTicket.isActive
                        )
                        
                        val tickets = document.get("tickets") as? List<Map<String, Any>> ?: listOf()
                        val updatedTickets = tickets + ticketData
                        
                        // Update the user document with the new tickets array
                        userDoc.update("tickets", updatedTickets).await()
                        Log.d("EventsRepository", "Ticket added to user document in Firestore")
                    }
                } else {
                    Log.e("EventsRepository", "Could not find user email for UID: ${userTicket.userId}")
                    // Fallback to old method - separate collection
                    val ticketsCollection = db.collection("user_tickets")
                    val ticketData = mapOf(
                        "userId" to userTicket.userId,
                        "ticketId" to userTicket.ticketId,
                        "purchaseDate" to userTicket.purchaseDate,
                        "isActive" to userTicket.isActive,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    )
                    ticketsCollection.add(ticketData).await()
                }
            } catch (e: Exception) {
                Log.e("EventsRepository", "Error saving ticket to Firebase (continuing anyway): ${e.message}", e)
            }
            
            return id
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error inserting user ticket: ${e.message}", e)
            throw e
        }
    }

    // Helper method to find email by UID
    private suspend fun findUserEmailByUid(uid: String): String? {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            // If this is the current user, we can get email directly
            if (currentUser != null && currentUser.uid == uid) {
                return currentUser.email
            }
            
            // Otherwise, we need to query Firestore to find the email
            val db = FirebaseFirestore.getInstance()
            val users = db.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .await()
            
            if (!users.isEmpty) {
                // Get the first matching user document
                val userDoc = users.documents.first()
                return userDoc.id // The document ID is the email
            }
            
            return null
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error finding user email by UID: ${e.message}", e)
            return null
        }
    }

    override suspend fun updateEvent(event: Event) {
        try {
            eventDao.updateEvent(event)
            Log.d("EventsRepository", "Event updated successfully: ${event.id}")
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error updating event: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteEvent(event: Event) {
        try {
            eventDao.deleteEvent(event)
            Log.d("EventsRepository", "Event deleted successfully: ${event.id}")
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error deleting event: ${e.message}", e)
            throw e
        }
    }

    override suspend fun updateScheduleItem(scheduleItem: ScheduleItem) {
        try {
            // Use ScheduleItemDao through the EventDao
            eventDao.updateScheduleItem(scheduleItem)
            Log.d("EventsRepository", "Schedule item updated successfully: ${scheduleItem.id}")
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error updating schedule item: ${e.message}", e)
            throw e
        }
    }

    override suspend fun deleteScheduleItem(scheduleItem: ScheduleItem) {
        try {
            // Use ScheduleItemDao through the EventDao
            eventDao.deleteScheduleItem(scheduleItem)
            Log.d("EventsRepository", "Schedule item deleted successfully: ${scheduleItem.id}")
        } catch (e: Exception) {
            Log.e("EventsRepository", "Error deleting schedule item: ${e.message}", e)
            throw e
        }
    }
}

