package pt.ua.deti.icm.awav.data.model

@Entity(tableName = "stands")
data class Stand(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val menu: Menu,
    val workers: Worker,
)
