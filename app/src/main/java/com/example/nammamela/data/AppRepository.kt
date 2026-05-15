package com.example.nammamela.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(private val appDao: AppDao) {
    val allDramas: Flow<List<DramaEntity>> = appDao.getAllDramas()
    val allComments: Flow<List<CommentEntity>> = appDao.getAllComments()

    suspend fun insertUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        appDao.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        appDao.getUserByEmail(email)
    }

    suspend fun loginUser(email: String, password: String): UserEntity? = withContext(Dispatchers.IO) {
        appDao.loginUser(email, password)
    }

    suspend fun getUserById(userId: Int): UserEntity? = withContext(Dispatchers.IO) {
        appDao.getUserById(userId)
    }

    fun getBookingsByUser(userId: Int): Flow<List<BookingEntity>> = appDao.getBookingsByUser(userId)

    suspend fun getBookingById(id: Int): BookingEntity? = withContext(Dispatchers.IO) {
        appDao.getBookingById(id)
    }

    fun getSeatsByShowTiming(showTimingId: Int): Flow<List<SeatEntity>> = appDao.getSeatsByShowTiming(showTimingId)

    suspend fun insertDramas(dramas: List<DramaEntity>) = withContext(Dispatchers.IO) {
        appDao.insertDramas(dramas)
    }

    suspend fun getDramaById(id: Int): DramaEntity? = withContext(Dispatchers.IO) {
        appDao.getDramaById(id)
    }

    suspend fun getDramasByGenre(genre: String): List<DramaEntity> = withContext(Dispatchers.IO) {
        appDao.getDramasByGenre(genre)
    }

    suspend fun insertUserActivity(activity: UserActivityEntity) = withContext(Dispatchers.IO) {
        appDao.insertUserActivity(activity)
    }

    suspend fun getUserActivities(userId: Int): List<UserActivityEntity> = withContext(Dispatchers.IO) {
        appDao.getUserActivities(userId)
    }

    fun getShowTimingsByDramaId(dramaId: Int): Flow<List<ShowTimingEntity>> = appDao.getShowTimingsByDramaId(dramaId)

    suspend fun insertShowTimings(showTimings: List<ShowTimingEntity>) = withContext(Dispatchers.IO) {
        appDao.insertShowTimings(showTimings)
    }

    suspend fun getShowTimingById(id: Int): ShowTimingEntity? = withContext(Dispatchers.IO) {
        appDao.getShowTimingById(id)
    }

    fun getCastByDramaId(dramaId: Int): Flow<List<CastEntity>> = appDao.getCastByDramaId(dramaId)

    suspend fun insertCast(cast: List<CastEntity>) = withContext(Dispatchers.IO) {
        appDao.insertCast(cast)
    }

    suspend fun insertSeats(seats: List<SeatEntity>) = withContext(Dispatchers.IO) {
        appDao.insertSeats(seats)
    }

    suspend fun updateSeats(seats: List<SeatEntity>) = withContext(Dispatchers.IO) {
        appDao.updateSeats(seats)
    }

    suspend fun insertBooking(booking: BookingEntity) = withContext(Dispatchers.IO) {
        appDao.insertBooking(booking)
    }

    suspend fun insertComment(comment: CommentEntity) = withContext(Dispatchers.IO) {
        appDao.insertComment(comment)
    }

    fun getCommentsByDramaId(dramaId: Int): Flow<List<CommentEntity>> = appDao.getCommentsByDramaId(dramaId)

    suspend fun getSeat(showTimingId: Int, row: Int, column: Int): SeatEntity? = withContext(Dispatchers.IO) {
        return@withContext appDao.getSeat(showTimingId, row, column)
    }

    suspend fun getSeatById(id: Int): SeatEntity? = withContext(Dispatchers.IO) {
        appDao.getSeatById(id)
    }

    suspend fun insertSeatLock(lock: SeatLockEntity) = withContext(Dispatchers.IO) {
        appDao.insertSeatLock(lock)
    }

    suspend fun releaseLocksByUser(userId: Int, showTimingId: Int) = withContext(Dispatchers.IO) {
        appDao.releaseLocksByUser(userId, showTimingId)
    }

    suspend fun clearExpiredLocks(currentTime: Long) = withContext(Dispatchers.IO) {
        appDao.clearExpiredLocks(currentTime)
    }

    fun getLocksByShowTiming(showTimingId: Int): Flow<List<SeatLockEntity>> = appDao.getLocksByShowTiming(showTimingId)

    suspend fun getLockForSeat(seatId: Int): SeatLockEntity? = withContext(Dispatchers.IO) {
        appDao.getLockForSeat(seatId)
    }

    suspend fun deleteLocksForSeats(seatIds: List<Int>) = withContext(Dispatchers.IO) {
        appDao.deleteLocksForSeats(seatIds)
    }
}
