package com.example.group
import com.example.user.CustomListSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id:Long,
    val name:String,
    val owner:Long,
    @Serializable(CustomListSerializer::class)
    val members:List<Long>
)
