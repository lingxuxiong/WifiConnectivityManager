package com.wifi.connectivitymanager.errors

import java.lang.Exception

open class WifiConnectError : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}