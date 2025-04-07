package pt.ua.deti.icm.awav.data

import android.content.Context
import pt.ua.deti.icm.awav.data.repository.EventsRepository
import pt.ua.deti.icm.awav.data.repository.OfflineEventsRepository

import pt.ua.deti.icm.awav.data.repository.OfflineStandsRepository
import pt.ua.deti.icm.awav.data.repository.StandsRepository
import pt.ua.deti.icm.awav.data.room.AppDatabase

interface AppContainer{
    val standsRepository : StandsRepository
    val eventsRepository : EventsRepository
}

class AppDataContainer(private val context: Context): AppContainer {
    override val standsRepository: StandsRepository by lazy {
        OfflineStandsRepository(AppDatabase.getDatabase(context).standDao())
    }

    override val eventsRepository: EventsRepository by lazy {
        OfflineEventsRepository(AppDatabase.getDatabase(context).eventDao())
    }
}