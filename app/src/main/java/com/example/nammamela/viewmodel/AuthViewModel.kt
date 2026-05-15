package com.example.nammamela.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nammamela.data.*
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val sessionManager: SessionManager
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableLiveData<UserEntity?>()
    val user: LiveData<UserEntity?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        val dao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(dao)
        sessionManager = SessionManager(application)
    }

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val existingUser = repository.getUserByEmail(email)
            if (existingUser != null) {
                _error.value = "Email already exists"
            } else {
                val newUser = UserEntity(name = name, email = email, password = password, loginType = "MANUAL")
                val id = repository.insertUser(newUser)
                val user = newUser.copy(id = id.toInt())
                sessionManager.saveAuthToken(user.id)
                _user.value = user
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val user = repository.loginUser(email, password)
            if (user != null) {
                sessionManager.saveAuthToken(user.id)
                _user.value = user
            } else {
                _error.value = "Invalid email or password"
            }
            _isLoading.value = false
        }
    }

    fun handleGoogleSignIn(credential: AuthCredential) {
        _isLoading.value = true
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        viewModelScope.launch {
                            var user = repository.getUserByEmail(firebaseUser.email ?: "")
                            if (user == null) {
                                val newUser = UserEntity(
                                    name = firebaseUser.displayName ?: "User",
                                    email = firebaseUser.email ?: "",
                                    loginType = "GOOGLE"
                                )
                                val id = repository.insertUser(newUser)
                                user = newUser.copy(id = id.toInt())
                            }
                            sessionManager.saveAuthToken(user.id)
                            _user.value = user
                            _isLoading.value = false
                        }
                    } else {
                        _isLoading.value = false
                        _error.value = "User info not found"
                    }
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message ?: "Google Sign-In failed"
                }
            }
    }

    fun logout() {
        sessionManager.logout()
        _user.value = null
    }

    fun checkSession() {
        if (sessionManager.isLoggedIn()) {
            val userId = sessionManager.getUserId()
            viewModelScope.launch {
                _user.value = repository.getUserById(userId)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
