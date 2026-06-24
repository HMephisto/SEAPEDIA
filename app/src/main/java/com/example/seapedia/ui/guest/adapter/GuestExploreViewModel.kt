package com.example.seapedia.ui.guest.adapter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.Product
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.GuestRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GuestExploreViewModel (private val repository: GuestRepository) : ViewModel() {
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshing = MutableLiveData<Boolean>(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var searchJob: Job? = null

    init {
        loadProducts()
    }

    fun loadProducts(search: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getProducts(search)) {
                is ApiResult.Success -> _products.value = result.data.data
                is ApiResult.Error -> _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun refresh(search: String? = null) {
        viewModelScope.launch {
            _isRefreshing.value = true
            when (val result = repository.getProducts(search)) {
                is ApiResult.Success -> _products.value = result.data.data
                is ApiResult.Error -> _errorMessage.value = result.message
            }
            _isRefreshing.value = false
        }
    }

    // debounce search so we don't fire an API call on every keystroke
    fun onSearchQuery(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            loadProducts(query.ifBlank { null })
        }
    }
}

class GuestExploreViewModelFactory(
    private val repository: GuestRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GuestExploreViewModel::class.java)) {
            return GuestExploreViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}