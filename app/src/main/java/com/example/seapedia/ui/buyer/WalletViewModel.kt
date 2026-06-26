package com.example.seapedia.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.Transaction
import com.example.seapedia.data.model.Wallet
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.WalletRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

sealed class WalletState {
    object Idle : WalletState()
    object Loading : WalletState()
    data class TopUpSuccess(val newBalance: Double) : WalletState()
    data class Error(val message: String) : WalletState()
}

class WalletViewModel(private val repository: WalletRepository) : ViewModel() {

    private val _wallet = MutableLiveData<Wallet>()
    val wallet: LiveData<Wallet> = _wallet

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _state = MutableLiveData<WalletState>(WalletState.Idle)
    val state: LiveData<WalletState> = _state

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            val walletDeferred = async { repository.getWallet() }
            val transactionsDeferred = async { repository.getTransactions() }

            when (val result = walletDeferred.await()) {
                is ApiResult.Success -> _wallet.value = result.data.data
                is ApiResult.Error -> _state.value = WalletState.Error(result.message)
            }

            when (val result = transactionsDeferred.await()) {
                is ApiResult.Success -> _transactions.value = result.data.data
                is ApiResult.Error -> _state.value = WalletState.Error(result.message)
            }

            _isRefreshing.value = false
        }
    }

    fun topUp(amount: String) {
        val amountDouble = amount.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0) {
            _state.value = WalletState.Error("Enter a valid amount")
            return
        }

        viewModelScope.launch {
            _state.value = WalletState.Loading
            when (val result = repository.topUp(amountDouble)) {
                is ApiResult.Success -> {
                    _state.value = WalletState.TopUpSuccess(result.data.data.newBalance)
                    refresh() // refresh balance and transactions after top up
                }
                is ApiResult.Error -> _state.value = WalletState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _state.value = WalletState.Idle
    }
}

class WalletViewModelFactory(
    private val repository: WalletRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            return WalletViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}