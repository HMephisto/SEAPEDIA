package com.example.seapedia.ui.seller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.Product
import com.example.seapedia.data.model.SellerStore
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.StoreRepository
import kotlinx.coroutines.launch

class SellerDashboardViewModel(private val repository: StoreRepository) : ViewModel() {
    private val _store = MutableLiveData<SellerStore>()
    val store: LiveData<SellerStore> = _store

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _lowStockProducts = MutableLiveData<List<Product>>()
    val lowStockProducts: LiveData<List<Product>> = _lowStockProducts

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    companion object {
        const val LOW_STOCK_THRESHOLD = 5
    }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            // load store first to get store ID
            when (val storeResult = repository.getSellerStore()) {
                is ApiResult.Success -> {
                    val store = storeResult.data.store
                    _store.value = store

                    // then load products using that store ID
                    when (val productsResult = repository.getProductsByStore(store.id)) {
                        is ApiResult.Success -> {
                            val products = productsResult.data.data
                            _products.value = products
                            _lowStockProducts.value = products.filter {
                                it.stock <= LOW_STOCK_THRESHOLD
                            }
                        }
                        is ApiResult.Error -> {
                            _errorMessage.value = productsResult.message
                        }
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = storeResult.message
                }
            }

            _isRefreshing.value = false
        }
    }
}

class SellerDashboardViewModelFactory(
    private val repository: StoreRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SellerDashboardViewModel::class.java)) {
            return SellerDashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}