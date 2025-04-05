package pt.ua.deti.icm.awav.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ua.deti.icm.awav.data.model.Stand
import pt.ua.deti.icm.awav.data.model.StandsRepository

/**
 * UI state for the Stands screen
 */
data class StandsUiState(
    val standsList: List<Stand> = listOf(),
    val isLoading: Boolean = true
)

/**
 * ViewModel to manage the stands data in the UI
 */
class StandsViewModel(private val standsRepository: StandsRepository) : ViewModel() {
    
    val standsUiState: StateFlow<StandsUiState> =
        standsRepository.getAllStandsStream()
            .map { stands ->
                StandsUiState(
                    standsList = stands,
                    isLoading = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = StandsUiState()
            )

    fun addStand(stand: Stand) {
        viewModelScope.launch {
            standsRepository.insertStand(stand)
        }
    }

    fun updateStand(stand: Stand) {
        viewModelScope.launch {
            standsRepository.updateStand(stand)
        }
    }

    fun deleteStand(stand: Stand) {
        viewModelScope.launch {
            standsRepository.deleteStand(stand)
        }
    }
} 