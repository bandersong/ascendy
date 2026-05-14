package com.ascendy.app.blocking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BlockState {
    private val _active = MutableStateFlow(false)
    val active: StateFlow<Boolean> = _active

    private val _blocked = MutableStateFlow<Set<String>>(emptySet())
    val blocked: StateFlow<Set<String>> = _blocked

    fun snapshot(): Set<String> = _blocked.value
    fun isActive(): Boolean = _active.value
    fun isBlocked(pkg: String): Boolean = _active.value && pkg in _blocked.value

    fun set(active: Boolean, blocked: Set<String>) {
        _active.value = active
        _blocked.value = if (active) blocked else emptySet()
    }

    fun clear() {
        _active.value = false
        _blocked.value = emptySet()
    }
}
