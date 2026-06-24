package com.example.seapedia.ui.guest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.Product
import com.example.seapedia.data.model.Review
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.GuestRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class GuestHomeViewModel (private val repository: GuestRepository) : ViewModel(){
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private val _reviewSubmitState = MutableLiveData<ReviewSubmitState>(ReviewSubmitState.Idle)
    val reviewSubmitState: LiveData<ReviewSubmitState> = _reviewSubmitState

    private val _isLoadingProducts = MutableLiveData<Boolean>()
    val isLoadingProducts: LiveData<Boolean> = _isLoadingProducts

    private val _isLoadingReviews = MutableLiveData<Boolean>()
    val isLoadingReviews: LiveData<Boolean> = _isLoadingReviews

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isRefreshing = MutableLiveData<Boolean>(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    init {
        refresh()
    }



    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            // run both calls in parallel
            val productsDeferred = async { repository.getProducts() }
            val reviewsDeferred = async { repository.getReviews() }

            when (val result = productsDeferred.await()) {
                is ApiResult.Success -> _products.value = result.data.data
                is ApiResult.Error -> _errorMessage.value = result.message
            }

            when (val result = reviewsDeferred.await()) {
                is ApiResult.Success -> _reviews.value = result.data.data
                is ApiResult.Error -> _errorMessage.value = result.message
            }

            _isRefreshing.value = false
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoadingProducts.value = true
            when (val result = repository.getProducts()) {
                is ApiResult.Success -> _products.value = result.data.data
                is ApiResult.Error -> _errorMessage.value = result.message
            }
            _isLoadingProducts.value = false
        }
    }

    private fun loadReviews() {
        viewModelScope.launch {
            _isLoadingReviews.value = true
            when (val result = repository.getReviews()) {
                is ApiResult.Success -> _reviews.value = result.data.data
                is ApiResult.Error -> _errorMessage.value = result.message
            }
            _isLoadingReviews.value = false
        }
    }

    fun submitReview(rating: Int, comment: String) {
        if (rating == 0) {
            _reviewSubmitState.value = ReviewSubmitState.Error("Please select a rating")
            return
        }
        if (comment.isBlank()) {
            _reviewSubmitState.value = ReviewSubmitState.Error("Please write a comment")
            return
        }

        viewModelScope.launch {
            _reviewSubmitState.value = ReviewSubmitState.Loading
            when (val result = repository.addReview(rating, comment)) {
                is ApiResult.Success -> {
                    _reviewSubmitState.value = ReviewSubmitState.Success
                    loadReviews() // refresh the list after submitting
                }
                is ApiResult.Error -> {
                    _reviewSubmitState.value = ReviewSubmitState.Error(result.message)
                }
            }
        }
    }

    fun resetReviewState() {
        _reviewSubmitState.value = ReviewSubmitState.Idle
    }
}

sealed class ReviewSubmitState {
    object Idle : ReviewSubmitState()
    object Loading : ReviewSubmitState()
    object Success : ReviewSubmitState()
    data class Error(val message: String) : ReviewSubmitState()
}

class GuestHomeViewModelFactory(
    private val repository: GuestRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GuestHomeViewModel::class.java)) {
            return GuestHomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}