package com.example.blog
import com.example.user.CustomListSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Blog(
    val id:Int,
    val post:String,
    val author:Long,
    @Serializable(CustomListSerializer::class)
    val likes:List<Long>
)
