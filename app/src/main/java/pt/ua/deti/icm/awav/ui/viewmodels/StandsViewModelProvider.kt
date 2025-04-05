package pt.ua.deti.icm.awav.ui.viewmodels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pt.ua.deti.icm.awav.AWAVApplication

/**
 * Factory for creating StandsViewModel instances
 */
object StandsViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            StandsViewModel(
                standsRepository = awavApplication().container.standsRepository
            )
        }
    }
}

/**
 * Extension function to get AWAVApplication instance from CreationExtras
 */
fun CreationExtras.awavApplication(): AWAVApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AWAVApplication) 