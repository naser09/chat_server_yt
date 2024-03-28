package com.example.user

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CustomListSerializer:KSerializer<List<Long>> {
    override val descriptor: SerialDescriptor
        get() = ListSerializer(Long.serializer()).descriptor

    override fun deserialize(decoder: Decoder): List<Long> {
        return decoder.decodeSerializableValue(ListSerializer(Long.serializer()))
    }

    override fun serialize(encoder: Encoder, value: List<Long>) {
        encoder.encodeSerializableValue(ListSerializer(Long.serializer()),value)
    }
}