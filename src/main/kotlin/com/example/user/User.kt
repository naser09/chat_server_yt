package com.example.user

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val id:Long,
    val username:String,
    val password:String,
    @Serializable(CustomListSerializer::class)
    val inbox:List<Long> = emptyList(),
    @Serializable(CustomListSerializer::class)
    val friends:List<Long> = emptyList(),
)
