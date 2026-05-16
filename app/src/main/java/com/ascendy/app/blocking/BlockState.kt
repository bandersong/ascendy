package com.ascendy.app.blocking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BlockState {
    private val _active = MutableStateFlow(false)
    val active: StateFlow<Boolean> = _active

    private val _blocked = MutableStateFlow<Set<String>>(emptySet())
    val blocked: StateFlow<Set<String>> = _blocked

    private val _blockedDomains = MutableStateFlow<Set<String>>(emptySet())
    val blockedDomains: StateFlow<Set<String>> = _blockedDomains

    private val _startedAt = MutableStateFlow<Long?>(null)
    val startedAt: StateFlow<Long?> = _startedAt

    /** True iff the active session is non-strict and still has its single emergency override unused. */
    private val _emergencyAvailable = MutableStateFlow(false)
    val emergencyAvailable: StateFlow<Boolean> = _emergencyAvailable

    /** True iff the active session uses a strict list — no manual end is allowed. */
    private val _strict = MutableStateFlow(false)
    val strict: StateFlow<Boolean> = _strict

    /** True iff the active list is allow-mode: blocked = NOT in [blocked]. */
    private val _inverted = MutableStateFlow(false)
    val inverted: StateFlow<Boolean> = _inverted

    fun snapshot(): Set<String> = _blocked.value
    fun isActive(): Boolean = _active.value
    fun isBlocked(pkg: String): Boolean {
        if (!_active.value) return false
        val inSet = pkg in _blocked.value
        return if (_inverted.value) !inSet else inSet
    }
    fun blockedCount(): Int = _blocked.value.size
    fun isDomainBlocked(host: String): Boolean {
        if (!_active.value) return false
        val h = host.lowercase().removePrefix("www.")
        val set = _blockedDomains.value
        val matched = h in set || set.any { d -> h == d || h.endsWith(".$d") }
        return if (_inverted.value) !matched else matched
    }

    fun set(
        active: Boolean,
        blocked: Set<String>,
        blockedDomains: Set<String> = emptySet(),
        startedAt: Long? = null,
        emergencyAvailable: Boolean = false,
        strict: Boolean = false,
        inverted: Boolean = false,
    ) {
        _active.value = active
        _blocked.value = if (active) blocked else emptySet()
        _blockedDomains.value = if (active) blockedDomains else emptySet()
        _startedAt.value = if (active) startedAt else null
        _emergencyAvailable.value = active && emergencyAvailable
        _strict.value = active && strict
        _inverted.value = active && inverted
    }

    fun clear() {
        _active.value = false
        _blocked.value = emptySet()
        _blockedDomains.value = emptySet()
        _startedAt.value = null
        _emergencyAvailable.value = false
        _strict.value = false
        _inverted.value = false
    }
}
