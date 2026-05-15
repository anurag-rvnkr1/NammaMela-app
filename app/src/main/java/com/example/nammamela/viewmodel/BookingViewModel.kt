package com.example.nammamela.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.nammamela.data.*
import com.example.nammamela.util.ReminderWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BookingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())
    }

    fun confirmBooking(
        userId: Int,
        dramaId: Int,
        dramaName: String,
        showTimingId: Int,
        userName: String,
        phone: String,
        selectedSeats: List<SeatEntity>,
        onComplete: () -> Unit
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Get Show and Timing info for the booking record
                val drama = repository.getDramaById(dramaId)
                val timing = repository.getShowTimingById(showTimingId)

                // Check if any seat is already booked (Extra safety)
                val currentSeats = selectedSeats.map { repository.getSeatById(it.id) }
                if (currentSeats.any { it?.isBooked == true }) {
                    _error.value = "Some selected seats were already booked. Please try again."
                    _isLoading.value = false
                    return@launch
                }

                // Update seats as booked
                val updatedSeats = selectedSeats.map {
                    it.copy(isBooked = true, userId = userId, bookedByName = userName, bookedByPhone = phone)
                }
                repository.updateSeats(updatedSeats)

                // Remove locks for these seats
                repository.deleteLocksForSeats(selectedSeats.map { it.id })

                // Create booking record
                val seatNumbers = selectedSeats.map { "${('A'.code + it.row).toChar()}${it.column + 1}" }.joinToString(", ")
                val booking = BookingEntity(
                    userId = userId,
                    dramaId = dramaId,
                    dramaName = dramaName,
                    venueName = drama?.venueName ?: "",
                    showDate = timing?.date ?: "",
                    showTime = timing?.time ?: "",
                    userName = userName,
                    phone = phone,
                    seatNumbers = seatNumbers,
                    totalSeats = selectedSeats.size
                )
                repository.insertBooking(booking)
                
                // Track BOOK activity for recommendations
                repository.insertUserActivity(UserActivityEntity(userId = userId, dramaId = dramaId, actionType = "BOOK"))
                
                scheduleReminder(booking)
                onComplete()
            } catch (e: Exception) {
                _error.value = "Booking failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun scheduleReminder(booking: BookingEntity) {
        // Schedule notification (10 seconds delay for demo)
        val data = workDataOf(
            "dramaName" to booking.dramaName,
            "showTime" to "${booking.showDate} | ${booking.showTime}",
            "venueName" to booking.venueName
        )
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(data)
            .build()
        WorkManager.getInstance(getApplication()).enqueue(request)
    }
    
    suspend fun getShowTimingById(id: Int) = repository.getShowTimingById(id)
    suspend fun getDramaById(id: Int) = repository.getDramaById(id)
}
