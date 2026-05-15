package com.example.nammamela.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nammamela.data.*
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val sessionManager: SessionManager

    private val _user = MutableLiveData<UserEntity?>()
    val user: LiveData<UserEntity?> = _user

    val bookings: LiveData<List<BookingEntity>>

    init {
        val dao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(dao)
        sessionManager = SessionManager(application)
        
        val userId = sessionManager.getUserId()
        bookings = repository.getBookingsByUser(userId).asLiveData()
        
        viewModelScope.launch {
            _user.value = repository.getUserById(userId)
        }
    }
}
