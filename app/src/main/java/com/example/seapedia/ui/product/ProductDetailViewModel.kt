package com.example.seapedia.ui.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.ProductDetail
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.GuestRepository
import kotlinx.coroutines.launch

sealed class ProductDetailState {
    object Loading : ProductDetailState()
    data class Success(val product: ProductDetail) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}

class ProductDetailViewModel(
    private val repository: GuestRepository
) : ViewModel() {

    private val _state = MutableLiveData<ProductDetailState>()
    val state: LiveData<ProductDetailState> = _state

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _state.value = ProductDetailState.Loading
            when (val result = repository.getProductDetail(productId)) {
                is ApiResult.Success -> _state.value = ProductDetailState.Success(result.data)
                is ApiResult.Error -> _state.value = ProductDetailState.Error(result.message)
            }
        }
    }
}

class ProductDetailViewModelFactory(
    private val repository: GuestRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
            return ProductDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}