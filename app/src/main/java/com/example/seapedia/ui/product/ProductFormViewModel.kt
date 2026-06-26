package com.example.seapedia.ui.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.StoreRepository
import kotlinx.coroutines.launch
import java.io.File


sealed class ProductFormState {
    object Idle : ProductFormState()
    object Loading : ProductFormState()
    data class Success(val productId: Int) : ProductFormState()
    data class Error(val message: String) : ProductFormState()
}
class ProductFormViewModel(private val repository: StoreRepository) : ViewModel() {
    private val _state = MutableLiveData<ProductFormState>(ProductFormState.Idle)
    val state: LiveData<ProductFormState> = _state

    fun saveProduct(
        storeId: Int,
        productId: Int?,
        name: String,
        description: String,
        price: String,
        stock: String,
        imageFile: File?
    ) {
        if (name.isBlank()) {
            _state.value = ProductFormState.Error("Product name is required")
            return
        }
        if (description.isBlank()) {
            _state.value = ProductFormState.Error("Description is required")
            return
        }
        if (price.isBlank() || price.toDoubleOrNull() == null) {
            _state.value = ProductFormState.Error("Enter a valid price")
            return
        }
        if (stock.isBlank() || stock.toIntOrNull() == null) {
            _state.value = ProductFormState.Error("Enter a valid stock amount")
            return
        }

        viewModelScope.launch {
            _state.value = ProductFormState.Loading

            val result = if (productId == null) {
                repository.createProduct(storeId, name, description, price, stock.toInt())
            } else {
                repository.updateProduct(productId, storeId, name, description, price, stock.toInt())
            }

            when (result) {
                is ApiResult.Success -> {
                    val savedProductId = result.data.product.id

                    if (imageFile != null) {
                        val imageResult = repository.uploadProductImage(savedProductId, imageFile)
                        if (imageResult is ApiResult.Error) {
                            _state.value = ProductFormState.Success(savedProductId)
                            return@launch
                        }
                    }

                    _state.value = ProductFormState.Success(savedProductId)
                }
                is ApiResult.Error -> {
                    _state.value = ProductFormState.Error(result.message)
                }
            }
        }
    }
}

class ProductFormViewModelFactory(
    private val repository: StoreRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductFormViewModel::class.java)) {
            return ProductFormViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}