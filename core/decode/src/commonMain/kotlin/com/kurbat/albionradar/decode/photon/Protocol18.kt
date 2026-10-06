package com.kurbat.albionradar.decode.photon

import com.kurbat.albionradar.decode.events.EventData

class Protocol18 {
    fun deserializeEventData(reader: BinaryReader): EventData {
        require(reader.hasRemaining) { "Event payload is empty" }
        val wireCode = reader.readUInt8().toInt()
        val parameters = deserializeParameterTable(reader)
        postProcessEvent(wireCode, parameters)
        val eventCode = parameters[EVENT_CODE_PARAMETER].asInt() ?: wireCode
        return EventData(code = eventCode, parameters = parameters, wireCode = wireCode)
    }

    fun deserializeOperationRequest(reader: BinaryReader): OperationRequestData {
        require(reader.hasRemaining) { "Operation request payload is empty" }
        val wireCode = reader.readUInt8().toInt()
        val parameters = deserializeParameterTable(reader)
        parameters.putIfAbsent(OPERATION_CODE_PARAMETER, wireCode.toUByte())
        val operationCode = parameters[OPERATION_CODE_PARAMETER].asInt() ?: wireCode
        return OperationRequestData(operationCode, parameters, wireCode)
    }

    fun deserializeOperationResponse(reader: BinaryReader): OperationResponseData {
        require(reader.remaining >= 3) { "Operation response payload is truncated" }
        val wireCode = reader.readUInt8().toInt()
        val returnCode = reader.readInt16LE()

        var debugMessage = ""
        var marketOrders: List<String>? = null
        if (reader.hasRemaining) {
            when (val debugValue = deserialize(reader, reader.readUInt8())) {
                is String -> debugMessage = debugValue
                is List<*> -> marketOrders = debugValue.filterIsInstance<String>()
            }
        }

        val parameters = deserializeParameterTable(reader)
        if (marketOrders != null) parameters[0] = marketOrders
        parameters.putIfAbsent(OPERATION_CODE_PARAMETER, wireCode.toUByte())
        val operationCode = parameters[OPERATION_CODE_PARAMETER].asInt() ?: wireCode

        return OperationResponseData(
            code = operationCode,
            returnCode = returnCode,
            debugMessage = debugMessage,
            parameters = parameters,
            wireCode = wireCode
        )
    }

    internal fun deserialize(reader: BinaryReader): Any? {
        require(reader.hasRemaining) { "Missing Protocol18 type code" }
        return deserialize(reader, reader.readUInt8())
    }

    internal fun deserialize(reader: BinaryReader, typeCode: UByte): Any? {
        if (typeCode >= TypeCodes.CUSTOM_SLIM_BASE) {
            return deserializeCustom(reader, typeCode)
        }

        return when (typeCode) {
            TypeCodes.UNKNOWN, TypeCodes.NULL -> null
            TypeCodes.BOOLEAN -> reader.readUInt8().toInt() != 0
            TypeCodes.BYTE -> reader.readUInt8()
            TypeCodes.SHORT -> reader.readInt16LE()
            TypeCodes.FLOAT -> reader.readFloat32LE()
            TypeCodes.DOUBLE -> reader.readFloat64LE()
            TypeCodes.STRING -> deserializeString(reader)
            TypeCodes.COMPRESSED_INT -> readCompressedInt(reader)
            TypeCodes.COMPRESSED_LONG -> readCompressedLong(reader)
            TypeCodes.INT_1 -> reader.readUInt8().toInt()
            TypeCodes.INT_1_NEG -> -reader.readUInt8().toInt()
            TypeCodes.INT_2 -> reader.readUInt16LE().toInt()
            TypeCodes.INT_2_NEG -> -reader.readUInt16LE().toInt()
            TypeCodes.LONG_1 -> reader.readUInt8().toLong()
            TypeCodes.LONG_1_NEG -> -reader.readUInt8().toLong()
            TypeCodes.LONG_2 -> reader.readUInt16LE().toLong()
            TypeCodes.LONG_2_NEG -> -reader.readUInt16LE().toLong()
            TypeCodes.CUSTOM -> deserializeCustom(reader, typeCode)
            TypeCodes.DICTIONARY, TypeCodes.HASHTABLE -> deserializeDictionary(reader)
            TypeCodes.OBJECT_ARRAY -> deserializeObjectArray(reader)
            TypeCodes.OPERATION_REQUEST -> deserializeOperationRequest(reader)
            TypeCodes.OPERATION_RESPONSE -> deserializeOperationResponse(reader)
            TypeCodes.EVENT_DATA -> deserializeEventData(reader)
            TypeCodes.BOOL_FALSE -> false
            TypeCodes.BOOL_TRUE -> true
            TypeCodes.SHORT_ZERO -> 0.toShort()
            TypeCodes.INT_ZERO -> 0
            TypeCodes.LONG_ZERO -> 0L
            TypeCodes.FLOAT_ZERO -> 0f
            TypeCodes.DOUBLE_ZERO -> 0.0
            TypeCodes.BYTE_ZERO -> 0.toUByte()
            TypeCodes.ARRAY -> deserializeNestedArray(reader)
            else -> {
                if ((typeCode.toInt() and TypeCodes.ARRAY.toInt()) != 0) {
                    deserializeTypedArray(reader, (typeCode.toInt() and TypeCodes.ARRAY.toInt().inv()).toUByte())
                } else {
                    error("Unsupported Protocol18 type code: $typeCode")
                }
            }
        }
    }

    private fun deserializeString(reader: BinaryReader): String {
        val length = readCount(reader)
        require(length <= reader.remaining) { "Protocol18 string is truncated" }
        return reader.readBytes(length).decodeToString()
    }

    private fun deserializeCustom(reader: BinaryReader, gpType: UByte): ByteArray {
        if (gpType < TypeCodes.CUSTOM_SLIM_BASE) {
            reader.readUInt8() // custom type id
        }
        val size = readCount(reader)
        require(size <= reader.remaining) { "Protocol18 custom value is truncated" }
        return reader.readBytes(size)
    }

    private fun deserializeDictionary(reader: BinaryReader): Map<Any?, Any?> {
        val keyType = reader.readUInt8()
        val valueType = reader.readUInt8()
        val size = readCount(reader)
        val result = mutableMapOf<Any?, Any?>()

        repeat(size) { index ->
            val actualKeyType = if (keyType == TypeCodes.UNKNOWN) reader.readUInt8() else keyType
            val actualValueType = if (valueType == TypeCodes.UNKNOWN) reader.readUInt8() else valueType
            val key = deserialize(reader, actualKeyType)
            val value = deserialize(reader, actualValueType)
            val safeKey = when (key) {
                is ByteArray, is List<*>, is Map<*, *> -> "UNHASHABLE_${index}_${key::class.simpleName}"
                else -> key
            }
            result[safeKey] = value
        }
        return result
    }

    private fun deserializeObjectArray(reader: BinaryReader): List<Any?> {
        val size = readCount(reader)
        return List(size) { deserialize(reader) }
    }

    private fun deserializeNestedArray(reader: BinaryReader): List<Any?> {
        val size = readCount(reader)
        val elementType = reader.readUInt8()
        return List(size) { deserialize(reader, elementType) }
    }

    private fun deserializeTypedArray(reader: BinaryReader, elementType: UByte): Any {
        val size = readCount(reader)
        return when (elementType) {
            TypeCodes.BOOLEAN -> {
                val packed = reader.readBytes((size + 7) / 8)
                List(size) { index ->
                    (packed[index / 8].toInt() and (1 shl (index % 8))) != 0
                }
            }
            TypeCodes.BYTE -> reader.readBytes(size)
            TypeCodes.SHORT -> List(size) { reader.readInt16LE() }
            TypeCodes.FLOAT -> List(size) { reader.readFloat32LE() }
            TypeCodes.DOUBLE -> List(size) { reader.readFloat64LE() }
            TypeCodes.STRING -> List(size) { deserializeString(reader) }
            TypeCodes.COMPRESSED_INT -> List(size) { readCompressedInt(reader) }
            TypeCodes.COMPRESSED_LONG -> List(size) { readCompressedLong(reader) }
            TypeCodes.DICTIONARY, TypeCodes.HASHTABLE -> List(size) { deserializeDictionary(reader) }
            TypeCodes.CUSTOM -> {
                reader.readUInt8() // shared custom type id
                List(size) {
                    val elementSize = readCount(reader)
                    require(elementSize <= reader.remaining) { "Protocol18 custom array item is truncated" }
                    reader.readBytes(elementSize)
                }
            }
            else -> List(size) { deserialize(reader, elementType) }
        }
    }

    private fun deserializeParameterTable(reader: BinaryReader): MutableMap<Int, Any?> {
        val size = readCount(reader)
        val result = mutableMapOf<Int, Any?>()
        repeat(size) {
            val key = reader.readUInt8().toInt()
            result[key] = deserialize(reader)
        }
        return result
    }

    private fun postProcessEvent(wireCode: Int, parameters: MutableMap<Int, Any?>) {
        parameters.putIfAbsent(EVENT_CODE_PARAMETER, wireCode.toUByte())
        if (wireCode != MOVE_EVENT_CODE) return

        val bytes = parameters[1] as? ByteArray ?: return
        if (bytes.size < MOVE_POSITION_END_OFFSET) return

        val positionReader = BinaryReader(bytes, position = MOVE_POSITION_OFFSET)
        val x = positionReader.readFloat32LE()
        val y = positionReader.readFloat32LE()
        if (!x.isFinite() || !y.isFinite()) return

        parameters[4] = x
        parameters[5] = y
    }

    private fun readCount(reader: BinaryReader): Int {
        val value = readCompressedUInt(reader)
        require(value <= MAX_ARRAY_SIZE.toUInt()) {
            "Protocol18 collection size $value exceeds $MAX_ARRAY_SIZE"
        }
        return value.toInt()
    }

    private fun readCompressedInt(reader: BinaryReader): Int {
        val value = readCompressedUInt(reader)
        val magnitude = (value shr 1).toInt()
        return if ((value and 1u) == 0u) magnitude else -magnitude - 1
    }

    private fun readCompressedLong(reader: BinaryReader): Long {
        val value = readCompressedULong(reader)
        val magnitude = (value shr 1).toLong()
        return if ((value and 1uL) == 0uL) magnitude else -magnitude - 1L
    }

    private fun readCompressedUInt(reader: BinaryReader): UInt {
        var value = 0u
        var shift = 0
        repeat(5) {
            val byte = reader.readUInt8().toInt()
            value = value or ((byte and 0x7F).toUInt() shl shift)
            if ((byte and 0x80) == 0) return value
            shift += 7
        }
        error("Invalid compressed UInt32")
    }

    private fun readCompressedULong(reader: BinaryReader): ULong {
        var value = 0uL
        var shift = 0
        repeat(10) {
            val byte = reader.readUInt8().toInt()
            value = value or ((byte and 0x7F).toULong() shl shift)
            if ((byte and 0x80) == 0) return value
            shift += 7
        }
        error("Invalid compressed UInt64")
    }

    private fun Any?.asInt(): Int? = when (this) {
        is Byte -> toInt()
        is UByte -> toInt()
        is Short -> toInt()
        is UShort -> toInt()
        is Int -> this
        is UInt -> toInt()
        is Long -> toInt()
        is ULong -> toInt()
        else -> null
    }

    private companion object {
        const val MAX_ARRAY_SIZE = 65_536
        const val EVENT_CODE_PARAMETER = 252
        const val OPERATION_CODE_PARAMETER = 253
        const val MOVE_EVENT_CODE = 3
        const val MOVE_POSITION_OFFSET = 9
        const val MOVE_POSITION_END_OFFSET = 17
    }
}

data class OperationRequestData(
    val code: Int,
    val parameters: Map<Int, Any?>,
    val wireCode: Int
)

data class OperationResponseData(
    val code: Int,
    val returnCode: Short,
    val debugMessage: String,
    val parameters: Map<Int, Any?>,
    val wireCode: Int
)
