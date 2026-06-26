package com.example.seapedia.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.CartItem
import com.example.seapedia.data.model.CartStore
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.CartRepository
import kotlinx.coroutines.launch

data class CartUiState(
    val store: CartStore? = null,
    val items: List<CartItem> = emptyList(),
    val total: Long = 0L,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null
)

data class CartActionState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class BuyerCartViewModel (private val repo: CartRepository) : ViewModel(){
    private val _uiState = MutableLiveData(CartUiState())
    val uiState: LiveData<CartUiState> = _uiState

    private val _actionState = MutableLiveData(CartActionState())
    val actionState: LiveData<CartActionState> = _actionState

    init {
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, error = null)
            when (val result = repo.getCart()) {
                is ApiResult.Success -> {
                    val data = result.data
                    _uiState.value = CartUiState(
                        store = data.store,
                        items = data.items,
                        total = data.total,
                        isLoading = false,
                        isEmpty = data.items.isEmpty()
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = CartUiState(isLoading = false, error = result.message, isEmpty = true)
                }
            }
        }
    }

    private val _updatingItemId = MutableLiveData<Int?>()
    val updatingItemId: LiveData<Int?> = _updatingItemId

    fun updateQuantity(itemId: Int, quantity: Int) {
        if (quantity < 1) return
        viewModelScope.launch {
            _updatingItemId.value = itemId
            when (val result = repo.updateCartItem(itemId, quantity)) {
                is ApiResult.Success -> loadCart()
                is ApiResult.Error -> _uiState.value = _uiState.value?.copy(error = result.message)
            }
            _updatingItemId.value = null
        }
    }

    fun removeItem(itemId: Int) {
        viewModelScope.launch {
            _actionState.value = CartActionState(isLoading = true)
            when (val result = repo.removeCartItem(itemId)) {
                is ApiResult.Success -> {
                    _actionState.value = CartActionState(isSuccess = true)
                    loadCart()
                }
                is ApiResult.Error -> {
                    _actionState.value = CartActionState(error = result.message)
                }
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _actionState.value = CartActionState(isLoading = true)
            when (val result = repo.clearCart()) {
                is ApiResult.Success -> {
                    _actionState.value = CartActionState(isSuccess = true)
                    loadCart()
                }
                is ApiResult.Error -> {
                    _actionState.value = CartActionState(error = result.message)
                }
            }
        }
    }

    fun resetActionState() {
        _actionState.value = CartActionState()
    }
}

class CartViewModelFactory(private val repo: CartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BuyerCartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BuyerCartViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}