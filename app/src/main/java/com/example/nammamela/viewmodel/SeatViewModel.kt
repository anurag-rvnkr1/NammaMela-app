package com.example.nammamela.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.nammamela.data.*
import kotlinx.coroutines.launch

class SeatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val sessionManager = SessionManager(application)
    private val _showTimingId = MutableLiveData<Int>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    val seats: LiveData<List<SeatEntity>> = _showTimingId.switchMap { id ->
        repository.getSeatsByShowTiming(id).asLiveData()
    }

    val locks: LiveData<List<SeatLockEntity>> = _showTimingId.switchMap { id ->
        repository.getLocksByShowTiming(id).asLiveData()
    }

    val showTiming: LiveData<ShowTimingEntity?> = _showTimingId.switchMap { id ->
        liveData { emit(repository.getShowTimingById(id)) }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())
        viewModelScope.launch {
            repository.clearExpiredLocks(System.currentTimeMillis())
        }
    }

    fun setShowTimingId(id: Int) {
        _showTimingId.value = id
    }

    fun getUserId(): Int = sessionManager.getUserId()

    fun tryLockSeat(seatId: Int, onResult: (Boolean, String?) -> Unit) {
        val userId = getUserId()
        if (userId == -1) {
            _error.value = "Please login first"
            onResult(false, "Please login first")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.clearExpiredLocks(System.currentTimeMillis())
                val existingLock = repository.getLockForSeat(seatId)
                if (existingLock != null && existingLock.userId != userId) {
                    _error.value = "Seat temporarily unavailable"
                    onResult(false, "Seat temporarily unavailable")
                } else {
                    val showTimingId = _showTimingId.value ?: return@launch
                    val expiry = System.currentTimeMillis() + (2 * 60 * 1000)
                    repository.insertSeatLock(SeatLockEntity(showTimingId = showTimingId, seatId = seatId, userId = userId, expiryTimestamp = expiry))
                    onResult(true, null)
                }
            } catch (e: Exception) {
                _error.value = "Action failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun unlockSeat(seatId: Int) {
        val userId = getUserId()
        if (userId == -1) return
        viewModelScope.launch {
            repository.deleteLocksForSeats(listOf(seatId))
        }
    }

    fun releaseUserLocks() {
        val userId = getUserId()
        val showTimingId = _showTimingId.value ?: return
        if (userId != -1) {
            viewModelScope.launch {
                repository.releaseLocksByUser(userId, showTimingId)
            }
        }
    }
}
