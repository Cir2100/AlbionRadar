package com.kurbat.albionradar.decode.photon

object TypeCodes {
    const val UNKNOWN: UByte = 0u
    const val BOOLEAN: UByte = 2u
    const val BYTE: UByte = 3u
    const val SHORT: UByte = 4u
    const val FLOAT: UByte = 5u
    const val DOUBLE: UByte = 6u
    const val STRING: UByte = 7u
    const val NULL: UByte = 8u
    const val COMPRESSED_INT: UByte = 9u
    const val COMPRESSED_LONG: UByte = 10u
    const val INT_1: UByte = 11u
    const val INT_1_NEG: UByte = 12u
    const val INT_2: UByte = 13u
    const val INT_2_NEG: UByte = 14u
    const val LONG_1: UByte = 15u
    const val LONG_1_NEG: UByte = 16u
    const val LONG_2: UByte = 17u
    const val LONG_2_NEG: UByte = 18u
    const val CUSTOM: UByte = 19u
    const val DICTIONARY: UByte = 20u
    const val HASHTABLE: UByte = 21u
    const val OBJECT_ARRAY: UByte = 23u
    const val OPERATION_REQUEST: UByte = 24u
    const val OPERATION_RESPONSE: UByte = 25u
    const val EVENT_DATA: UByte = 26u
    const val BOOL_FALSE: UByte = 27u
    const val BOOL_TRUE: UByte = 28u
    const val SHORT_ZERO: UByte = 29u
    const val INT_ZERO: UByte = 30u
    const val LONG_ZERO: UByte = 31u
    const val FLOAT_ZERO: UByte = 32u
    const val DOUBLE_ZERO: UByte = 33u
    const val BYTE_ZERO: UByte = 34u
    const val ARRAY: UByte = 0x40u
    const val CUSTOM_SLIM_BASE: UByte = 0x80u
}

object CommandTypes {
    const val DISCONNECT: UByte = 4u
    const val ACKNOWLEDGE: UByte = 5u
    const val RELIABLE: UByte = 6u
    const val UNRELIABLE: UByte = 7u
    const val FRAGMENT: UByte = 8u
}

object MessageTypes {
    const val REQUEST: UByte = 2u
    const val RESPONSE: UByte = 3u
    const val EVENT: UByte = 4u
    const val RESPONSE_ALT: UByte = 7u
    const val ENCRYPTED: UByte = 131u
}
