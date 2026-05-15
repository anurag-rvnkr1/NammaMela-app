package com.example.nammamela.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // User operations
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginUser(email: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?

    // Drama operations
    @Query("SELECT * FROM dramas ORDER BY rating DESC")
    fun getAllDramas(): Flow<List<DramaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDramas(dramas: List<DramaEntity>)

    @Query("SELECT * FROM dramas WHERE id = :id LIMIT 1")
    suspend fun getDramaById(id: Int): DramaEntity?

    @Query("SELECT * FROM dramas WHERE genre = :genre")
    suspend fun getDramasByGenre(genre: String): List<DramaEntity>

    // User Activity operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserActivity(activity: UserActivityEntity)

    @Query("SELECT * FROM user_activities WHERE userId = :userId")
    suspend fun getUserActivities(userId: Int): List<UserActivityEntity>

    // Show Timing operations
    @Query("SELECT * FROM show_timings WHERE dramaId = :dramaId")
    fun getShowTimingsByDramaId(dramaId: Int): Flow<List<ShowTimingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShowTimings(showTimings: List<ShowTimingEntity>)
    
    @Query("SELECT * FROM show_timings WHERE id = :id LIMIT 1")
    suspend fun getShowTimingById(id: Int): ShowTimingEntity?

    // Cast operations
    @Query("SELECT * FROM cast_members WHERE dramaId = :dramaId")
    fun getCastByDramaId(dramaId: Int): Flow<List<CastEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCast(cast: List<CastEntity>)

    // Seat operations
    @Query("SELECT * FROM seats WHERE showTimingId = :showTimingId ORDER BY `row` ASC, `column` ASC")
    fun getSeatsByShowTiming(showTimingId: Int): Flow<List<SeatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeats(seats: List<SeatEntity>)

    @Update
    suspend fun updateSeats(seats: List<SeatEntity>)

    @Query("SELECT * FROM seats WHERE showTimingId = :showTimingId AND `row` = :row AND `column` = :column LIMIT 1")
    suspend fun getSeat(showTimingId: Int, row: Int, column: Int): SeatEntity?

    @Query("SELECT * FROM seats WHERE id = :id LIMIT 1")
    suspend fun getSeatById(id: Int): SeatEntity?

    // Seat Lock operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeatLock(lock: SeatLockEntity)

    @Query("DELETE FROM seat_locks WHERE userId = :userId AND showTimingId = :showTimingId")
    suspend fun releaseLocksByUser(userId: Int, showTimingId: Int)

    @Query("DELETE FROM seat_locks WHERE expiryTimestamp < :currentTime")
    suspend fun clearExpiredLocks(currentTime: Long)

    @Query("SELECT * FROM seat_locks WHERE showTimingId = :showTimingId")
    fun getLocksByShowTiming(showTimingId: Int): Flow<List<SeatLockEntity>>
    
    @Query("SELECT * FROM seat_locks WHERE seatId = :seatId LIMIT 1")
    suspend fun getLockForSeat(seatId: Int): SeatLockEntity?

    @Query("DELETE FROM seat_locks WHERE seatId IN (:seatIds)")
    suspend fun deleteLocksForSeats(seatIds: List<Int>)

    // Booking operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY dateTime DESC")
    fun getBookingsByUser(userId: Int): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Int): BookingEntity?

    // Comment operations
    @Query("SELECT * FROM comments WHERE dramaId = :dramaId ORDER BY timestamp DESC")
    fun getCommentsByDramaId(dramaId: Int): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("SELECT * FROM comments ORDER BY timestamp DESC")
    fun getAllComments(): Flow<List<CommentEntity>>
}
