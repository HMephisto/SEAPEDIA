package com.example.seapedia.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seapedia.data.model.UserRole
import com.example.seapedia.data.repositrory.ApiResult
import com.example.seapedia.data.repositrory.AuthRepository
import com.example.seapedia.data.utils.SessionManager
import kotlinx.coroutines.launch


sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val role: String) : LoginUiState()
    data class MultiRole(val roles: List<UserRole>) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class AuthViewModel (
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _loginState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val loginState: LiveData<LoginUiState> = _loginState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginUiState.Error("Email and password are required")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            val result = authRepository.login(email, password)

            when (result) {
                is ApiResult.Success -> {
                    val user = result.data.user
                    sessionManager.saveSession(
                        token = result.data.token,
                        userId = user.id,
                        fullName = user.fullName,
                        email = user.email,
                        activeRole = user.activeRole.name,
                        activeRoleId = user.activeRoleId
                    )

                    if (user.roles.size > 1) {
                        _loginState.value = LoginUiState.MultiRole(user.roles)
                    } else {
                        _loginState.value = LoginUiState.Success(user.activeRole.name)
                    }
                }
                is ApiResult.Error -> {
                    _loginState.value = LoginUiState.Error(result.message)
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

    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading

            val result = authRepository.switchRole(role.name)

            when (result) {
                is ApiResult.Success -> {
                    val user = result.data.user
                    sessionManager.saveActiveRole(user.activeRole.name, user.activeRoleId)
                    _loginState.value = LoginUiState.Success(user.activeRole.name)
                }
                is ApiResult.Error -> {
                    _loginState.value = LoginUiState.Error(result.message)
                }
            }
        }
    }

}