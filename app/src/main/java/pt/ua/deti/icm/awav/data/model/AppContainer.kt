package pt.ua.deti.icm.awav.data.model

override val standsRepository: StandsRepository by lazy {
    OfflineStandsRepository(AWAVDatabase.getDatabase(context).standDao())
}