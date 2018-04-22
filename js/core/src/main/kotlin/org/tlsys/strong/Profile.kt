package org.tlsys.strong

actual val Strong.Profile.name: String
    get() = this::class.simpleName ?: "UNKNOWN"