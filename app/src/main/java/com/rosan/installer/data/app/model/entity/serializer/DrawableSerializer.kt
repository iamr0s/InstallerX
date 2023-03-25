package com.rosan.installer.data.app.model.entity.serializer

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.core.graphics.drawable.toBitmapOrNull
import com.rosan.installer.data.common.util.base64String
import com.rosan.installer.data.common.util.unbase64
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.ByteArrayOutputStream

class DrawableSerializer : KSerializer<Drawable> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()

    override fun deserialize(decoder: Decoder): Drawable {
        val base64 = decoder.decodeString()
        val bytes = base64.unbase64(Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        return BitmapDrawable(Resources.getSystem(), bitmap)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Drawable) {
        val bitmap = if (value.intrinsicWidth > 0 && value.intrinsicHeight > 0)
            value.toBitmapOrNull()
        else value.toBitmapOrNull(width = 512, height = 512)
        if (bitmap == null) {
            encoder.encodeNull()
            return
        }
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.close()
        val bytes = out.toByteArray()
        val base64 = bytes.base64String(Base64.NO_WRAP)
        encoder.encodeString(base64)
    }
}