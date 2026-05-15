package com.example.nammamela.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.nammamela.data.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DramaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository
    private val sessionManager = SessionManager(application)
    val allDramas: LiveData<List<DramaEntity>>
    val allComments: LiveData<List<CommentEntity>>

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _recommendations = MutableLiveData<List<DramaEntity>>()
    val recommendations: LiveData<List<DramaEntity>> = _recommendations

    private val _castList = MutableLiveData<List<CastEntity>>()
    val castList: LiveData<List<CastEntity>> = _castList

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())
        allDramas = repository.allDramas.asLiveData()
        allComments = repository.allComments.asLiveData()
        
        prepopulateData()
        loadRecommendations()
    }

    fun loadRecommendations() {
        val userId = sessionManager.getUserId()
        viewModelScope.launch {
            val allDramasFlow = repository.allDramas.firstOrNull() ?: emptyList()
            if (userId == -1) {
                // Cold start: Show trending/popular dramas
                _recommendations.value = allDramasFlow.sortedByDescending { it.rating }.take(5)
                return@launch
            }

            val activities = repository.getUserActivities(userId)
            val bookingsFlow = repository.getBookingsByUser(userId).firstOrNull() ?: emptyList()

            // Scoring logic
            val scores = mutableMapOf<Int, Int>()
            allDramasFlow.forEach { drama ->
                var score = 0
                // +5 -> booked before
                if (bookingsFlow.any { it.dramaId == drama.id }) score += 5
                
                // +3 -> frequently viewed
                val viewCount = activities.count { it.dramaId == drama.id && it.actionType == "VIEW" }
                score += (viewCount * 3)

                // +2 -> same genre (based on most viewed/booked genres)
                val favoriteGenres = (activities.mapNotNull { act -> allDramasFlow.find { it.id == act.dramaId }?.genre } +
                                     bookingsFlow.mapNotNull { b -> allDramasFlow.find { it.id == b.dramaId }?.genre })
                                     .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.take(2).map { it.key }
                
                if (favoriteGenres.contains(drama.genre)) score += 2

                // +1 -> popular (rating)
                if (drama.rating >= 4.5f) score += 1

                scores[drama.id] = score
            }

            val recommended = allDramasFlow.sortedByDescending { scores[it.id] ?: 0 }.take(5)
            _recommendations.value = recommended
        }
    }

    fun trackDramaView(dramaId: Int) {
        val userId = sessionManager.getUserId()
        if (userId == -1) return
        viewModelScope.launch {
            repository.insertUserActivity(UserActivityEntity(userId = userId, dramaId = dramaId, actionType = "VIEW"))
            loadRecommendations() // Refresh recommendations
        }
    }

    private fun prepopulateData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Simulate network delay for shimmer effect
                kotlinx.coroutines.delay(1500)
                val dramas = repository.allDramas.firstOrNull()
                if (dramas.isNullOrEmpty()) {
                    val dramaList = listOf(
                        DramaEntity(
                            title = "The Legend of Namma Mela",
                            duration = "2h 30m",
                            posterUrl = "https://images.unsplash.com/photo-1514306191717-452ec28c7814?q=80&w=1000&auto=format&fit=crop",
                            description = "An epic tale of a local fair that brings together people from all walks of life, celebrating culture and tradition.",
                            timing = "Multiple Shows",
                            venueName = "Grand Theatre",
                            venueAddress = "123 Culture Street",
                            venueCity = "Bengaluru",
                            genre = "Mythology",
                            rating = 4.8f
                        ),
                        DramaEntity(
                            title = "Yakshagana Night",
                            duration = "3h 00m",
                            posterUrl = "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?q=80&w=1000&auto=format&fit=crop",
                            description = "Experience the traditional dance-drama of coastal Karnataka with vibrant costumes and powerful music.",
                            timing = "Multiple Shows",
                            venueName = "Heritage Hall",
                            venueAddress = "456 Tradition Road",
                            venueCity = "Mangaluru",
                            genre = "Traditional",
                            rating = 4.9f
                        ),
                        DramaEntity(
                            title = "Malnad Stories",
                            duration = "2h 15m",
                            posterUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?q=80&w=1000&auto=format&fit=crop",
                            description = "Heartwarming stories from the lush green hills of Malnad, depicting the simple yet profound lives of its people.",
                            timing = "Multiple Shows",
                            venueName = "Mountain View Arena",
                            venueAddress = "789 Nature Lane",
                            venueCity = "Shivamogga",
                            genre = "Drama",
                            rating = 4.7f
                        )
                    )
                    repository.insertDramas(dramaList)

                    val insertedDramas = repository.allDramas.firstOrNull()
                    insertedDramas?.forEach { drama ->
                        // Prepopulate show timings
                        val dates = listOf("12 May", "13 May", "14 May")
                        val times = listOf("10:00 AM", "2:00 PM", "6:30 PM")
                        
                        val showTimings = mutableListOf<ShowTimingEntity>()
                        dates.forEach { date ->
                            times.forEach { time ->
                                showTimings.add(ShowTimingEntity(dramaId = drama.id, date = date, time = time))
                            }
                        }
                        repository.insertShowTimings(showTimings)
                        
                        // Prepopulate seats for EACH show timing
                        val insertedTimings = repository.getShowTimingsByDramaId(drama.id).firstOrNull()
                        insertedTimings?.forEach { timing ->
                            val seatList = mutableListOf<SeatEntity>()
                            for (r in 0 until 6) {
                                for (c in 0 until 8) {
                                    seatList.add(SeatEntity(showTimingId = timing.id, row = r, column = c, isBooked = false))
                                }
                            }
                            repository.insertSeats(seatList)
                        }

                        // Prepopulate cast
                        val castList = listOf(
                            CastEntity(dramaId = drama.id, name = "Rajkumar", role = "Lead Actor", imageUrl = "https://i.pravatar.cc/150?u=1"),
                            CastEntity(dramaId = drama.id, name = "Priya", role = "Lead Actress", imageUrl = "https://i.pravatar.cc/150?u=2"),
                            CastEntity(dramaId = drama.id, name = "Suresh", role = "Antagonist", imageUrl = "https://i.pravatar.cc/150?u=3")
                        )
                        repository.insertCast(castList)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addComment(text: String) {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = repository.getUserById(userId)
                val userName = user?.name ?: "Anonymous"
                repository.insertComment(
                    CommentEntity(
                        dramaId = null,
                        userId = userId,
                        userName = userName,
                        commentText = text
                    )
                )
            } catch (e: Exception) {
                _error.value = "Failed to post comment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
