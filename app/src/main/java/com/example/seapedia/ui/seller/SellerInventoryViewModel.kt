package com.example.seapedia.ui.seller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.Product
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.StoreRepository
import kotlinx.coroutines.launch

sealed class DeleteState {
    object Idle : DeleteState()
    object Loading : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}

class SellerInventoryViewModel(private val repository: StoreRepository) : ViewModel() {
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _deleteState = MutableLiveData<DeleteState>(DeleteState.Idle)
    val deleteState: LiveData<DeleteState> = _deleteState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var storeId: Int = -1

    fun init(storeId: Int) {
        this.storeId = storeId
        refresh()
    }

    fun refresh() {
        if (storeId == -1) return
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = repository.getProductsByStore(storeId)) {
                is ApiResult.Success -> _products.value = result.data.data
                is ApiResult.Error -> _errorMessage.value = result.message
            }
            _isRefreshing.value = false
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Loading
            when (val result = repository.deleteProduct(productId)) {
                is ApiResult.Success -> {
                    _deleteState.value = DeleteState.Success
                    refresh()
                }
                is ApiResult.Error -> {
                    _deleteState.value = DeleteState.Error(result.message)
                }
            }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteState.Idle
    }
}

class SellerInventoryViewModelFactory(
    private val repository: StoreRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SellerInventoryViewModel::class.java)) {
            return SellerInventoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}