package com.example.seapedia.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.MeResponse
import com.example.seapedia.data.model.Wallet
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.AuthRepository
import com.example.seapedia.data.repositrory.UserRepository
import com.example.seapedia.data.repositrory.WalletRepository
import com.example.seapedia.data.utils.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class RoleSwitched(val role: String) : ProfileState()
    data class RoleAdded(val role: String) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class BuyerProfileViewModel(
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _me = MutableLiveData<MeResponse>()
    val me: LiveData<MeResponse> = _me

    private val _wallet = MutableLiveData<Wallet>()
    val wallet: LiveData<Wallet> = _wallet

    private val _state = MutableLiveData<ProfileState>(ProfileState.Idle)
    val state: LiveData<ProfileState> = _state

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true

            val meDeferred = async { userRepository.getMe() }
            val walletDeferred = async { walletRepository.getWallet() }

            when (val result = meDeferred.await()) {
                is ApiResult.Success -> _me.value = result.data
                is ApiResult.Error -> _state.value = ProfileState.Error(result.message)
            }

            when (val result = walletDeferred.await()) {
                is ApiResult.Success -> _wallet.value = result.data.data
                is ApiResult.Error -> _state.value = ProfileState.Error(result.message)
            }

            _isRefreshing.value = false
        }
    }

    fun handleRoleAction(role: String, hasRole: Boolean) {
        viewModelScope.launch {
            _state.value = ProfileState.Loading
            if (hasRole) {
                when (val result = authRepository.switchRole(role)) {
                    is ApiResult.Success -> {
                        val user = result.data.user
                        sessionManager.saveActiveRole(user.activeRole.name, user.activeRoleId)
                        _state.value = ProfileState.RoleSwitched(role)
                    }
                    is ApiResult.Error -> _state.value = ProfileState.Error(result.message)
                }
            } else {
                when (val addResult = authRepository.addRole(role)) {
                    is ApiResult.Success -> {
                        val updatedRoles = addResult.data.user.roles.map { it.name }
                        sessionManager.saveUserRoles(updatedRoles)
                        when (val switchResult = authRepository.switchRole(role)) {
                            is ApiResult.Success -> {
                                val user = switchResult.data.user
                                sessionManager.saveActiveRole(user.activeRole.name, user.activeRoleId)
                                _state.value = ProfileState.RoleAdded(role)
                            }
                            is ApiResult.Error -> _state.value = ProfileState.Error(switchResult.message)
                        }
                    }
                    is ApiResult.Error -> _state.value = ProfileState.Error(addResult.message)
                }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            sessionManager.clearSession()
            onComplete()
        }
    }
}

class BuyerProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BuyerProfileViewModel::class.java)) {
            return BuyerProfileViewModel(userRepository, walletRepository, authRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}