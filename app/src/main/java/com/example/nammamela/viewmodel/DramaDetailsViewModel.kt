package com.example.nammamela.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.nammamela.data.*
import kotlinx.coroutines.launch

class DramaDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository = AppRepository(AppDatabase.getDatabase(application).appDao())
    private val sessionManager = SessionManager(application)

    private val _dramaId = MutableLiveData<Int>()

    val drama: LiveData<DramaEntity?> = _dramaId.switchMap { id ->
        liveData { emit(repository.getDramaById(id)) }
    }

    val cast: LiveData<List<CastEntity>> = _dramaId.switchMap { id ->
        repository.getCastByDramaId(id).asLiveData()
    }

    val comments: LiveData<List<CommentEntity>> = _dramaId.switchMap { id ->
        repository.getCommentsByDramaId(id).asLiveData()
    }

    val showTimings: LiveData<List<ShowTimingEntity>> = _dramaId.switchMap { id ->
        repository.getShowTimingsByDramaId(id).asLiveData()
    }

    private val _selectedDate = MutableLiveData<String?>()
    val selectedDate: LiveData<String?> = _selectedDate

    private val _selectedTiming = MutableLiveData<ShowTimingEntity?>()
    val selectedTiming: LiveData<ShowTimingEntity?> = _selectedTiming

    fun setDramaId(id: Int) {
        _dramaId.value = id
        trackDramaView(id)
    }

    private fun trackDramaView(dramaId: Int) {
        val userId = sessionManager.getUserId()
        if (userId == -1) return
        viewModelScope.launch {
            repository.insertUserActivity(UserActivityEntity(userId = userId, dramaId = dramaId, actionType = "VIEW"))
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        // Reset timing if not available for this date
        if (_selectedTiming.value?.date != date) {
            _selectedTiming.value = null
        }
    }

    fun selectTiming(timing: ShowTimingEntity) {
        _selectedTiming.value = timing
    }

    fun postComment(text: String) {
        val dramaId = _dramaId.value ?: return
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            val user = repository.getUserById(userId)
            val userName = user?.name ?: "Unknown User"
            repository.insertComment(CommentEntity(dramaId = dramaId, userId = userId, userName = userName, commentText = text))
        }
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}
