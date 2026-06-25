package com.example.seapedia.ui.seller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.StoreRepository
import kotlinx.coroutines.launch

sealed class StoreCheckState {
    object Loading : StoreCheckState()
    object HasStore : StoreCheckState()
    object NoStore : StoreCheckState()
    data class Error(val message: String) : StoreCheckState()
}

sealed class StoreSetupState {
    object Idle : StoreSetupState()
    object Loading : StoreSetupState()
    object Created : StoreSetupState()
    data class Error(val message: String) : StoreSetupState()
}

class StoreSetupViewModel(private val repository: StoreRepository) : ViewModel() {

    private val _checkState = MutableLiveData<StoreCheckState>()
    val checkState: LiveData<StoreCheckState> = _checkState

    private val _setupState = MutableLiveData<StoreSetupState>(StoreSetupState.Idle)
    val setupState: LiveData<StoreSetupState> = _setupState

    fun checkStore() {
        viewModelScope.launch {
            _checkState.value = StoreCheckState.Loading
            when (val result = repository.checkStore()) {
                is ApiResult.Success -> {
                    _checkState.value = if (result.data.hasStore) {
                        StoreCheckState.HasStore
                    } else {
                        StoreCheckState.NoStore
                    }
                }
                is ApiResult.Error -> {
                    _checkState.value = StoreCheckState.Error(result.message)
                }
            }
        }
    }

    fun createStore(storeName: String, description: String, addressDetail: String) {
        if (storeName.isBlank()) {
            _setupState.value = StoreSetupState.Error("Store name is required")
            return
        }
        if (description.isBlank()) {
            _setupState.value = StoreSetupState.Error("Description is required")
            return
        }
        if (addressDetail.isBlank()) {
            _setupState.value = StoreSetupState.Error("Address is required")
            return
        }

        viewModelScope.launch {
            _setupState.value = StoreSetupState.Loading
            when (val result = repository.createStore(storeName, description, addressDetail)) {
                is ApiResult.Success -> _setupState.value = StoreSetupState.Created
                is ApiResult.Error -> _setupState.value = StoreSetupState.Error(result.message)
            }
        }
    }
}

class StoreSetupViewModelFactory(
    private val repository: StoreRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreSetupViewModel::class.java)) {
            return StoreSetupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}