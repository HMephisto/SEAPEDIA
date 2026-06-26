package com.example.seapedia.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.Address
import com.example.seapedia.data.repositrory.AddressRepository
import com.example.seapedia.data.repositrory.ApiResult
import kotlinx.coroutines.launch

sealed class AddressState {
    object Idle : AddressState()
    object Loading : AddressState()
    object Saved : AddressState()
    object Deleted : AddressState()
    object DefaultSet : AddressState()
    data class Error(val message: String) : AddressState()
}

class AddressViewModel(private val repository: AddressRepository) : ViewModel() {

    private val _addresses = MutableLiveData<List<Address>>()
    val addresses: LiveData<List<Address>> = _addresses

    private val _state = MutableLiveData<AddressState>(AddressState.Idle)
    val state: LiveData<AddressState> = _state

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = repository.getAddresses()) {
                is ApiResult.Success -> _addresses.value = result.data.data
                is ApiResult.Error -> _state.value = AddressState.Error(result.message)
            }
            _isRefreshing.value = false
        }
    }

    fun saveAddress(
        id: Int?,
        recipientName: String,
        phone: String,
        addressDetail: String,
        isDefault: Boolean
    ) {
        if (recipientName.isBlank()) {
            _state.value = AddressState.Error("Recipient name is required")
            return
        }
        if (phone.isBlank()) {
            _state.value = AddressState.Error("Phone number is required")
            return
        }
        if (addressDetail.isBlank()) {
            _state.value = AddressState.Error("Address detail is required")
            return
        }

        viewModelScope.launch {
            _state.value = AddressState.Loading
            val result = if (id == null) {
                repository.createAddress(recipientName, phone, addressDetail, isDefault)
            } else {
                repository.updateAddress(id, recipientName, phone, addressDetail, isDefault)
            }
            when (result) {
                is ApiResult.Success -> {
                    _state.value = AddressState.Saved
                    refresh()
                }
                is ApiResult.Error -> _state.value = AddressState.Error(result.message)
            }
        }
    }

    fun deleteAddress(id: Int) {
        viewModelScope.launch {
            _state.value = AddressState.Loading
            when (val result = repository.deleteAddress(id)) {
                is ApiResult.Success -> {
                    _state.value = AddressState.Deleted
                    refresh()
                }
                is ApiResult.Error -> _state.value = AddressState.Error(result.message)
            }
        }
    }

    fun setDefaultAddress(id: Int) {
        viewModelScope.launch {
            _state.value = AddressState.Loading
            when (val result = repository.setDefaultAddress(id)) {
                is ApiResult.Success -> {
                    _state.value = AddressState.DefaultSet
                    refresh()
                }
                is ApiResult.Error -> _state.value = AddressState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _state.value = AddressState.Idle
    }
}

class AddressViewModelFactory(
    private val repository: AddressRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddressViewModel::class.java)) {
            return AddressViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}