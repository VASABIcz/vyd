import kotlin.reflect.KProperty

abstract class Converter<T> {
    abstract fun convert(value: kotlin.String, name: kotlin.String): T

    object String : Converter<kotlin.String>() {
        override fun convert(value: kotlin.String, name: kotlin.String): kotlin.String = value
    }

    object Boolean : Converter<kotlin.Boolean>() {
        override fun convert(value: kotlin.String, name: kotlin.String): kotlin.Boolean = value.toBoolean()
    }

    object Int : Converter<kotlin.Int>() {
        override fun convert(value: kotlin.String, name: kotlin.String): kotlin.Int = value.toInt()
    }

    object Float : Converter<kotlin.String>() {
        override fun convert(value: kotlin.String, name: kotlin.String): kotlin.String = value
    }

    class Custom<T>(val converter: (kotlin.String, kotlin.String) -> T): Converter<T>() {
        override fun convert(value: kotlin.String, name: kotlin.String): T = converter(value, name)
    }
}

class Parameter<T>(val value: T) {

    operator fun <R>getValue(t: R?, property: KProperty<*>): T? {
        return value
    }
}

class Parameters(private val parameters: io.ktor.http.Parameters) {
    private var issues: String = ""
    private var invalid: Boolean = false

    val isValid: Boolean
        get() = !invalid

    val getIssues: String
        get() = issues

    private fun missingParameter(name: String) {
        invalid = true
        issues += "missing parameter $name\n"
    }

    private fun invalidParameter(name: String) {
        invalid = true
        issues += "invalid parameter $name\n"
    }

    fun <T>parameter(name: String, type: Converter<T>, defaultValue: T? = null, optional: Boolean = false): Parameter<T?> {
        val x = parameters[name]
        val c = if (x != null) {
            try {
                type.convert(x, name)
                    ?: (defaultValue
                        ?: if (optional) {
                            null
                        } else {
                            throw Throwable("ff-15")
                        })
            } catch (_: Throwable) {
                invalidParameter(name)
                null
            }
        }
        else defaultValue
            ?: if (optional) {
                null
            }
            else {
                missingParameter(name)
                null
            }
        return Parameter(c)
    }
}