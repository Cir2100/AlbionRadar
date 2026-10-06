package com.kurbat.albionradar.core.capture

data class Packet(val payload: ByteArrayContainer)

// https://www.jetbrains.com/help/inspectopedia/ArrayInDataClass.html
data class ByteArrayContainer(val array: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ByteArrayContainer

        return array.contentEquals(other.array)
    }

    override fun hashCode(): Int = array.contentHashCode()
}
