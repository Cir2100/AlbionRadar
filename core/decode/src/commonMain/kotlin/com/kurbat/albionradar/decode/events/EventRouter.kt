package com.kurbat.albionradar.decode.events

class EventRouter {
    private val handlers = mutableMapOf<Int, MutableList<EventHandler>>()
    
    fun registerHandler(eventCode: Int, handler: EventHandler) {
        handlers.getOrPut(eventCode) { mutableListOf() }.add(handler)
    }
    
    fun unregisterHandler(eventCode: Int, handler: EventHandler) {
        handlers[eventCode]?.remove(handler)
    }
    
    fun route(event: EventData) {
        handlers[event.code]?.forEach { handler ->
            try {
                handler.handle(event)
            } catch (e: Exception) {
            }
        }
    }
    
    fun clearHandlers() {
        handlers.clear()
    }
}
