package com.kurbat.albionradar.utils

object Logger {
    private var isDebugEnabled = false
    
    fun enableDebug(enabled: Boolean) {
        isDebugEnabled = enabled
    }
    
    fun debug(tag: String, message: () -> String) {
        if (isDebugEnabled) {
            println("[DEBUG][$tag] ${message()}")
        }
    }
    
    fun info(tag: String, message: String) {
        println("[INFO][$tag] $message")
    }
    
    fun warn(tag: String, message: String) {
        println("[WARN][$tag] $message")
    }
    
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        println("[ERROR][$tag] $message")
        throwable?.printStackTrace()
    }
}
