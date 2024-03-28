package com.example.messaging

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id:String,
    val message:String,
    val sender:Long,
    val time:Long,
    val receiver:Long,
    val isGroup:Boolean
)
