package com.example.seapedia.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.AuthRepository
import com.example.seapedia.data.utils.SessionManager
import kotlinx.coroutines.launch

sealed class SwitchRoleState {
    object Idle : SwitchRoleState()
    object Loading : SwitchRoleState()
    data class Success(val role: String) : SwitchRoleState()
    data class Error(val message: String) : SwitchRoleState()
}
class SwitchRoleViewModel (
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableLiveData<SwitchRoleState>(SwitchRoleState.Idle)
    val state: LiveData<SwitchRoleState> = _state

    fun handleRoleAction(role: String, hasRole: Boolean) {
        viewModelScope.launch {
            _state.value = SwitchRoleState.Loading

            if (hasRole) {
                when (val result = authRepository.switchRole(role)) {
                    is ApiResult.Success -> {
                        val user = result.data.user
                        sessionManager.saveActiveRole(user.activeRole.name, user.activeRoleId)
                        _state.value = SwitchRoleState.Success(role)
                    }
                    is ApiResult.Error -> _state.value = SwitchRoleState.Error(result.message)
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
                                _state.value = SwitchRoleState.Success(role)
                            }
                            is ApiResult.Error -> _state.value = SwitchRoleState.Error(switchResult.message)
                        }
                    }
                    is ApiResult.Error -> _state.value = SwitchRoleState.Error(addResult.message)
                }
            }
        }
    }

    fun resetState() {
        _state.value = SwitchRoleState.Idle
    }
}

class SwitchRoleViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SwitchRoleViewModel::class.java)) {
            return SwitchRoleViewModel(authRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}