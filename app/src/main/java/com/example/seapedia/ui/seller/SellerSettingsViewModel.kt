package com.example.seapedia.ui.seller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.RoleConstants
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.AuthRepository
import com.example.seapedia.data.repositrory.StoreRepository
import com.example.seapedia.data.utils.SessionManager
import kotlinx.coroutines.launch

sealed class SettingsState {
    object Idle : SettingsState()
    object Loading : SettingsState()
    object StoreUpdated : SettingsState()
    data class RoleSwitched(val role: String) : SettingsState()
    data class RoleAdded(val role: String) : SettingsState()
    data class Error(val message: String) : SettingsState()
}

// represents a switchable/addable role item in the UI
data class RoleItem(
    val role: String,
    val displayName: String,
    val hasRole: Boolean
)

class SellerSettingsViewModel(
    private val storeRepository: StoreRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableLiveData<SettingsState>(SettingsState.Idle)
    val state: LiveData<SettingsState> = _state

    private val _store = MutableLiveData<com.example.seapedia.data.model.SellerStore>()
    val store: LiveData<com.example.seapedia.data.model.SellerStore> = _store

    init {
        loadStore()
    }

    private fun loadStore() {
        viewModelScope.launch {
            when (val result = storeRepository.getSellerStore()) {
                is ApiResult.Success -> _store.value = result.data.store
                is ApiResult.Error -> _state.value = SettingsState.Error(result.message)
            }
        }
    }

    fun updateStore(storeName: String, description: String, addressDetail: String) {
        if (storeName.isBlank()) {
            _state.value = SettingsState.Error("Store name is required")
            return
        }
        if (description.isBlank()) {
            _state.value = SettingsState.Error("Description is required")
            return
        }
        if (addressDetail.isBlank()) {
            _state.value = SettingsState.Error("Address is required")
            return
        }

        viewModelScope.launch {
            _state.value = SettingsState.Loading
            when (val result = storeRepository.updateStore(storeName, description, addressDetail)) {
                is ApiResult.Success -> {
                    _store.value = result.data.store
                    _state.value = SettingsState.StoreUpdated
                }
                is ApiResult.Error -> _state.value = SettingsState.Error(result.message)
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            sessionManager.clearSession()
            onComplete()
        }
    }
}

class SellerSettingsViewModelFactory(
    private val storeRepository: StoreRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SellerSettingsViewModel::class.java)) {
            return SellerSettingsViewModel(storeRepository, authRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}