package android.content.res

import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import androidx.annotation.RequiresApi
import com.rosan.installer.data.res.repo.AxmlPullRepo
import com.rosan.installer.data.res.util.ResourcesProxy

class TypedArrayProxy2(
    private val _resources: ResourcesProxy,
    private val set: AxmlPullRepo,
    private val attrs: IntArray
) : TypedArray() {
    private var values: List<TypedValue?>

    init {
        val data = mutableListOf<TypedValue?>()
        for (attr in attrs) {
            var index = -1
            for (i in 0 until set.attributeCount) {
                if (attr == set.getAttributeNameResource(i)) {
                    index = i
                    break
                }
            }
            if (index == -1) {
                data.add(null)
                continue
            }
            data.add(set.getAttributeTypedValue(index))
        }

        values = data
    }

    override fun getResources(): ResourcesProxy {
        return _resources
    }

    override fun length(): Int {
        return attrs.size
    }

    override fun getIndexCount(): Int {
        return values.count {
            it != null
        }
    }

    override fun getIndex(at: Int): Int {
        return super.getIndex(at)
    }

    override fun getValue(index: Int, outValue: TypedValue?): Boolean {
        val value = peekValue(index) ?: return false
        outValue?.type = value.type
        outValue?.data = value.data
        outValue?.resourceId = value.resourceId
        outValue?.changingConfigurations = value.changingConfigurations
        outValue?.string = value.string
        outValue?.density = value.density
        return true
    }

    override fun peekValue(index: Int): TypedValue? {
        return values.getOrNull(index)
    }

    override fun getText(index: Int): CharSequence? {
        val value = peekValue(index) ?: return null
        return value.coerceToString()
    }

    override fun getString(index: Int): String? {
        val value = peekValue(index) ?: return null
        return value.string.toString()
    }

    override fun getNonResourceString(index: Int): String? {
        val value = peekValue(index) ?: return null
        return value.string.toString()
    }

    override fun getBoolean(index: Int, defValue: Boolean): Boolean {
        val value = peekValue(index) ?: return defValue
        return value.data != 0
    }

    override fun getInt(index: Int, defValue: Int): Int {
        val value = peekValue(index) ?: return defValue
        return value.data
    }

    override fun getFloat(index: Int, defValue: Float): Float {
        val value = peekValue(index) ?: return defValue
        return java.lang.Float.intBitsToFloat(value.data)
    }

    override fun getColor(index: Int, defValue: Int): Int {
        val value = peekValue(index) ?: return defValue
        return when (value.type) {
            in TypedValue.TYPE_FIRST_INT..TypedValue.TYPE_LAST_INT -> value.data
            TypedValue.TYPE_STRING -> {
                resources.getColorStateList(value.resourceId, null).defaultColor
            }
            else -> defValue
        }
    }

    @Override
    fun getComplexColor(index: Int): ComplexColor? {
        val value = peekValue(index) ?: return null
        return when (value.type) {
            in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT -> ColorStateList.valueOf(
                value.data
            ) as ComplexColor
            else -> resources.loadComplexColor(TypedValue(), value.resourceId, null)
        }
    }

    override fun getColorStateList(index: Int): ColorStateList? {
        return getComplexColor(index) as ColorStateList?
    }

    override fun getInteger(index: Int, defValue: Int): Int {
        val value = peekValue(index) ?: return defValue
        return when (value.type) {
            in TypedValue.TYPE_FIRST_INT..TypedValue.TYPE_LAST_INT -> value.data
            else -> defValue
        }
    }

    override fun getDimension(index: Int, defValue: Float): Float {
        val value = peekValue(index) ?: return defValue
        return when (value.type) {
            TypedValue.TYPE_DIMENSION -> TypedValue.complexToDimension(
                value.data,
                resources.displayMetrics
            )
            else -> defValue
        }
    }

    override fun getDimensionPixelOffset(index: Int, defValue: Int): Int {
        val value = peekValue(index) ?: return defValue
        return when (value.type) {
            TypedValue.TYPE_DIMENSION -> TypedValue.complexToDimensionPixelOffset(
                value.data,
                resources.displayMetrics
            )
            else -> defValue
        }
    }

    override fun getDimensionPixelSize(index: Int, defValue: Int): Int {
        val value = peekValue(index) ?: return defValue
        return when (value.type) {
            TypedValue.TYPE_DIMENSION -> TypedValue.complexToDimensionPixelSize(
                value.data,
                resources.displayMetrics
            )
            else -> defValue
        }
    }

    override fun getLayoutDimension(index: Int, defValue: Int): Int {
        val value = peekValue(index) ?: return defValue
        return when (value.type) {
            in TypedValue.TYPE_FIRST_INT..TypedValue.TYPE_LAST_INT -> value.data
            TypedValue.TYPE_DIMENSION -> TypedValue.complexToDimensionPixelSize(
                value.data,
                resources.displayMetrics
            )
            else -> defValue
        }
    }

    override fun getLayoutDimension(index: Int, name: String?): Int {
        return getLayoutDimension(index, 0)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    fun getDrawableForDensity(index: Int, density: Int): Drawable? {
        val value = TypedValue()
        getValue(index, value)
        return when (value.type) {
            TypedValue.TYPE_REFERENCE -> resources.getDrawableForDensity(
                value.resourceId,
                density,
                null
            )
            in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT -> ColorDrawable(
                value.data
            )
            else -> null
        }
    }

    override fun getFont(index: Int): Typeface? {
        val value = peekValue(index) ?: return null
        return when (value.type) {
            else -> resources.getFont(value.resourceId)
        }
    }

    override fun getFraction(index: Int, base: Int, pbase: Int, defValue: Float): Float {
        val value = peekValue(index) ?: return defValue
        return when (value.type) {
            TypedValue.TYPE_FRACTION -> {
                TypedValue.complexToFraction(value.data, base.toFloat(), pbase.toFloat())
            }
            else -> defValue
        }
    }

    override fun getTextArray(index: Int): Array<CharSequence>? {
        val value = peekValue(index) ?: return null
        return when (value.type) {
            else -> resources.getTextArray(value.resourceId)
        }
    }

    override fun getType(index: Int): Int {
        val value = peekValue(index) ?: return TypedValue.TYPE_NULL
        return value.type
    }

    override fun getResourceId(index: Int, defValue: Int): Int {
        val value = peekValue(index) ?: return defValue
        return value.resourceId
    }

    override fun getSourceResourceId(index: Int, defaultValue: Int): Int {
        val value = peekValue(index) ?: return -1
        return value.sourceResourceId
    }

    override fun hasValue(index: Int): Boolean {
        val value = peekValue(index) ?: return false
        return value.type != TypedValue.TYPE_NULL
    }

    override fun hasValueOrEmpty(index: Int): Boolean {
        val value = peekValue(index) ?: return false
        return value.type != TypedValue.TYPE_NULL
                || value.type != TypedValue.DATA_NULL_EMPTY
    }

    override fun getPositionDescription(): String {
        return set.positionDescription
    }

    override fun getDrawable(index: Int): Drawable? {
        return getDrawableForDensity(index, 0)
    }

    override fun getChangingConfigurations(): Int {
        return 0
    }

    @Override
    fun extractThemeAttrs(): IntArray {
        return extractThemeAttrs(null)
    }

    @Override
    fun extractThemeAttrs(scrap: IntArray?): IntArray {
        return attrs
    }

    override fun recycle() {
    }
}