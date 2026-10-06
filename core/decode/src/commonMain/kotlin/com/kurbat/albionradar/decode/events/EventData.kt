package com.kurbat.albionradar.decode.events

data class EventData(
    val code: Int,
    val parameters: Map<Int, Any?>,
    val wireCode: Int = code
) {
    inline fun <reified T> getParameter(key: Int): T? {
        return parameters[key] as? T
    }
    
    fun getInt(key: Int): Int? {
        return when (val value = parameters[key]) {
            is Byte -> value.toInt()
            is Short -> value.toInt()
            is Long -> value.toInt()
            is UInt -> value.toInt()
            is Int -> value
            is UByte -> value.toInt()
            is UShort -> value.toInt()
            is ULong -> value.toInt()
            else -> null
        }
    }
    
    fun getFloat(key: Int): Float? {
        return when (val value = parameters[key]) {
            is Float -> value
            is Double -> value.toFloat()
            is Byte -> value.toFloat()
            is Short -> value.toFloat()
            is Int -> value.toFloat()
            is Long -> value.toFloat()
            is UByte -> value.toFloat()
            is UShort -> value.toFloat()
            is UInt -> value.toFloat()
            is ULong -> value.toFloat()
            else -> null
        }
    }
    
    fun getString(key: Int): String? {
        return parameters[key] as? String
    }
    
    fun getByte(key: Int): Byte? {
        return when (val value = parameters[key]) {
            is UByte -> value.toByte()
            is Byte -> value
            is Short -> value.toByte()
            is Int -> value.toByte()
            else -> null
        }
    }
    
    fun getByteArray(key: Int): ByteArray? {
        return parameters[key] as? ByteArray
    }
    
    @Suppress("UNCHECKED_CAST")
    fun getList(key: Int): List<Any?>? {
        return parameters[key] as? List<Any?>
    }

    fun getIntList(key: Int): List<Int>? = getList(key)?.mapNotNull { value ->
        when (value) {
            is Byte -> value.toInt()
            is UByte -> value.toInt()
            is Short -> value.toInt()
            is UShort -> value.toInt()
            is Int -> value
            is UInt -> value.toInt()
            is Long -> value.toInt()
            is ULong -> value.toInt()
            else -> null
        }
    }

    fun getFloatList(key: Int): List<Float>? = getList(key)?.mapNotNull { value ->
        when (value) {
            is Float -> value
            is Double -> value.toFloat()
            is Number -> value.toFloat()
            else -> null
        }
    }
}
