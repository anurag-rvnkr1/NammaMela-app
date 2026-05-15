package com.example.nammamela.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String? = null,
    val loginType: String // "GOOGLE" or "MANUAL"
)

@Entity(tableName = "dramas")
data class DramaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val duration: String,
    val posterUrl: String,
    val description: String = "",
    val timing: String = "",
    val venueName: String = "",
    val venueAddress: String = "",
    val venueCity: String = "",
    val genre: String = "Drama",
    val rating: Float = 4.5f
)

@Entity(
    tableName = "user_activities",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DramaEntity::class,
            parentColumns = ["id"],
            childColumns = ["dramaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("dramaId")]
)
data class UserActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val dramaId: Int,
    val actionType: String, // "VIEW", "BOOK"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "show_timings",
    foreignKeys = [
        ForeignKey(
            entity = DramaEntity::class,
            parentColumns = ["id"],
            childColumns = ["dramaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dramaId")]
)
data class ShowTimingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dramaId: Int,
    val date: String, // e.g. "12 May"
    val time: String, // e.g. "6:30 PM"
    val availableSeats: Int = 48
)

@Entity(
    tableName = "seats",
    foreignKeys = [
        ForeignKey(
            entity = ShowTimingEntity::class,
            parentColumns = ["id"],
            childColumns = ["showTimingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("showTimingId")]
)
data class SeatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val showTimingId: Int,
    val row: Int,
    val column: Int,
    var isBooked: Boolean,
    var userId: Int? = null,
    var bookedByName: String? = null,
    var bookedByPhone: String? = null
)

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val dramaId: Int,
    val dramaName: String,
    val venueName: String = "",
    val showDate: String = "",
    val showTime: String = "",
    val userName: String,
    val phone: String,
    val seatNumbers: String,
    val totalSeats: Int = 1,
    val dateTime: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "seat_locks",
    foreignKeys = [
        ForeignKey(
            entity = ShowTimingEntity::class,
            parentColumns = ["id"],
            childColumns = ["showTimingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("showTimingId")]
)
data class SeatLockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val showTimingId: Int,
    val seatId: Int,
    val userId: Int,
    val expiryTimestamp: Long
)

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = DramaEntity::class,
            parentColumns = ["id"],
            childColumns = ["dramaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dramaId"), Index("userId")]
)
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dramaId: Int? = null,
    val userId: Int,
    val userName: String,
    val commentText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cast_members",
    foreignKeys = [
        ForeignKey(
            entity = DramaEntity::class,
            parentColumns = ["id"],
            childColumns = ["dramaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dramaId")]
)
data class CastEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dramaId: Int,
    val name: String,
    val role: String,
    val imageUrl: String
)
