package com.kurbat.albionradar.decode.events

fun interface EventHandler {
    fun handle(event: EventData)
}
