package com.rosan.installer.data.app.model.entity.serializer

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmapOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.ByteArrayOutputStream

class DrawableSerializer : KSerializer<Drawable> {
    private val delegateSerializer = ByteArraySerializer()

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("Drawable", delegateSerializer.descriptor)

    override fun deserialize(decoder: Decoder): Drawable {
        val bytes = decoder.decodeSerializableValue(delegateSerializer)
        return BitmapDrawable(
            Resources.getSystem(), BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Drawable) {
        val bitmap =
            if (value.intrinsicWidth > 0 && value.intrinsicHeight > 0) value.toBitmapOrNull()
            else value.toBitmapOrNull(width = 512, height = 512)
        if (bitmap == null) {
            encoder.encodeNullableSerializableValue(delegateSerializer, null)
            return
        }
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.close()
        val bytes = out.toByteArray()
        encoder.encodeSerializableValue(delegateSerializer, bytes)
    }
}