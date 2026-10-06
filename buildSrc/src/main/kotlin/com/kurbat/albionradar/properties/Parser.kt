package com.kurbat.albionradar.properties

import java.util.Locale

internal fun Any?.asBoolean(): Boolean? = when (this) {
    is Boolean -> this
    is String -> parseToBoolean()
    is Number -> toInt() != 0
    else -> null
}

internal fun Any?.asString(): String? = when (this) {
    is String -> this
    is Any -> toString()
    else -> null
}

internal fun Any?.asInt(): Int? = when (this) {
    is Int -> this
    is String -> trim().toIntOrNull()
    else -> null
}

internal fun String.parseToBoolean(): Boolean? = when (trim().lowercase(Locale.ENGLISH)) {
    "0", "false" -> false
    "1", "true" -> true
    else -> null
}
