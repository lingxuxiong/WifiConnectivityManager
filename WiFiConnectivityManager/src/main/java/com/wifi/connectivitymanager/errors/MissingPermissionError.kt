package com.wifi.connectivitymanager.errors

class MissingPermissionError : WifiConnectError {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}