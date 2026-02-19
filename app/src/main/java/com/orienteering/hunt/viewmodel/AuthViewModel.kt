package com.orienteering.hunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orienteering.hunt.data.api.ApiException
import com.orienteering.hunt.data.api.ApiService
import com.orienteering.hunt.data.auth.AuthManager
import com.orienteering.hunt.data.api.toPlayer
import com.orienteering.hunt.data.models.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val currentPlayer: Player? = null,
    val isCheckingAuth: Boolean = true,
    val profileUpdateSuccess: Boolean = false,
    val passwordChangeSuccess: Boolean = false
)

class AuthViewModel(
    private val apiService: ApiService,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            val token = authManager.getToken()
            val user = authManager.getUser()
            
            if (token != null && user != null) {
                apiService.setAuthToken(token)
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        currentPlayer = user.toPlayer(),
                        isCheckingAuth = false
                    )
                }
            } else {
                _uiState.update { it.copy(isCheckingAuth = false) }
            }
        }
    }
    
    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = apiService.login(username, password)
            
            result.fold(
                onSuccess = { authResponse ->
                    apiService.setAuthToken(authResponse.token)
                    authManager.saveSession(authResponse.token, authResponse.user)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            currentPlayer = authResponse.user.toPlayer()
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = (error as? ApiException)?.message ?: "Login failed"
                        )
                    }
                }
            )
        }
    }
    
    fun register(username: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = apiService.register(username, email, password)
            
            result.fold(
                onSuccess = { authResponse ->
                    apiService.setAuthToken(authResponse.token)
                    authManager.saveSession(authResponse.token, authResponse.user)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            currentPlayer = authResponse.user.toPlayer()
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = (error as? ApiException)?.message ?: "Registration failed"
                        )
                    }
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            apiService.setAuthToken(null)
            authManager.clearSession()
            _uiState.update {
                AuthUiState(isCheckingAuth = false)
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun updateProfile(username: String?, email: String?, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, profileUpdateSuccess = false) }
            
            apiService.updateProfile(username, email).fold(
                onSuccess = { updatedUser ->
                    authManager.updateUser(updatedUser)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentPlayer = updatedUser.toPlayer(),
                            profileUpdateSuccess = true
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = (error as? ApiException)?.message ?: "Failed to update profile"
                        )
                    }
                }
            )
        }
    }
    
    fun changePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, passwordChangeSuccess = false) }
            
            apiService.changePassword(oldPassword, newPassword).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            passwordChangeSuccess = true
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = (error as? ApiException)?.message ?: "Failed to change password"
                        )
                    }
                }
            )
        }
    }
    
    fun clearProfileUpdateSuccess() {
        _uiState.update { it.copy(profileUpdateSuccess = false) }
    }
    
    fun clearPasswordChangeSuccess() {
        _uiState.update { it.copy(passwordChangeSuccess = false) }
    }
    
    suspend fun getCurrentPlayer(): Player? {
        return _uiState.value.currentPlayer
    }
}
