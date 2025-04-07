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
data class MenuItem(
    @PrimaryKey val id: String,
    val standId: Int,
    val name: String,
    val price: Double,
    val description: String = "",
    val isAvailable: Boolean = true
) 