package pt.ua.deti.icm.awav.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Stand::class,
            parentColumns = ["id"],
            childColumns = ["standId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("standId")]
)

data class Worker(
    @PrimaryKey val id: Int,
    val standId: Int,
    val name: String
) 