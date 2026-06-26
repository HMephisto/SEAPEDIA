package com.example.seapedia.ui.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.Product
import com.example.seapedia.data.model.ProductDetail
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.CartRepository
import com.example.seapedia.data.repositrory.GuestRepository
import com.example.seapedia.ui.guest.adapter.GuestExploreViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

sealed class ProductDetailState {
    object Loading : ProductDetailState()
    data class Success(val product: ProductDetail) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}

sealed class AddToCartUiState {
    object Loading : AddToCartUiState()
    object Success : AddToCartUiState()
    data class Error(val message: String) : AddToCartUiState()
}

class ProductDetailViewModel(
    private val guestRepository: GuestRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableLiveData<ProductDetailState>()
    val state: LiveData<ProductDetailState> = _state

    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    private val _addToCartState = MutableLiveData<AddToCartUiState>()
    val addToCartState: LiveData<AddToCartUiState> = _addToCartState

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _state.value = ProductDetailState.Loading
            when (val result = guestRepository.getProductDetail(productId)) {
                is ApiResult.Success -> _state.value = ProductDetailState.Success(result.data)
                is ApiResult.Error -> _state.value = ProductDetailState.Error(result.message)
            }
        }
    }

    fun addToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            _addToCartState.value = AddToCartUiState.Loading
            when (val result = cartRepository.addToCart(productId, quantity)) {
                is ApiResult.Success -> _addToCartState.value = AddToCartUiState.Success
                is ApiResult.Error -> _addToCartState.value = AddToCartUiState.Error(result.message)
            }
        }
    }
}

class ProductDetailViewModelFactory(
    private val guestRepository: GuestRepository,
    private val cartRepository: CartRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailViewModel::class.java)) {
            return ProductDetailViewModel(guestRepository, cartRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}