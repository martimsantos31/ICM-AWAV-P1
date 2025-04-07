package pt.ua.deti.icm.awav

import android.app.Application
import pt.ua.deti.icm.awav.data.AppContainer
import pt.ua.deti.icm.awav.data.AppDataContainer

class awavApplication : Application() {
    // The container provides access to repositories
    lateinit var appContainer: AppContainer
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AppContainer
        appContainer = AppDataContainer(this)
        
        // Save the instance to companion object for singleton access
        instance = this
    }
    
    companion object {
        // Singleton instance for global access
        private lateinit var instance: awavApplication
        
        // Static access to the appContainer
        val appContainer: AppContainer
            get() = instance.appContainer
    }
}