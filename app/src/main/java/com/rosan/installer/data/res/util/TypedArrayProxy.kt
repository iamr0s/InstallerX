package android.content.res

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import androidx.annotation.RequiresApi

class TypedArrayProxy : TypedArray() {
    companion object {
        private val declaredFields = TypedArray::class.java.declaredFields.map {
            it.isAccessible = true
            it
        }

        fun TypedArray.proxy(): TypedArray {
            val value = TypedArrayProxy()
            declaredFields.map {
                it.set(value, it.get(this))
            }
            return value
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    fun getDrawableForDensity(index: Int, density: Int): Drawable? {
        val value = TypedValue()
        getValue(index, value)
        return when (value.type) {
            TypedValue.TYPE_REFERENCE -> resources.getDrawableForDensity(value.data, density, null)
            in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT -> ColorDrawable(
                value.data
            )
            else -> null
        }
    }
}