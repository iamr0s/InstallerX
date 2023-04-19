package com.rosan.installer.data.common.model.entity.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ThrowableSerializer : KSerializer<Throwable> {
    private val delegateSerializer = ByteArraySerializer()

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("Throwable", delegateSerializer.descriptor)

    override fun deserialize(decoder: Decoder): Throwable {
        val bytes = decoder.decodeSerializableValue(delegateSerializer)
        val input = ByteArrayInputStream(bytes)
        val objectInput = ObjectInputStream(input)
        return objectInput.readObject() as Throwable
    }

    override fun serialize(encoder: Encoder, value: Throwable) {
        val output = ByteArrayOutputStream()
        val objectOutput = ObjectOutputStream(output)
        objectOutput.writeObject(value)
        objectOutput.flush()
        val bytes = output.toByteArray()
        encoder.encodeSerializableValue(delegateSerializer, bytes)
    }
}