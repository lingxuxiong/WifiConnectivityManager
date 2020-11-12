# Introduction
This repository aims to provide an unified interface for apps to connect to a specific Wi-Fi network, without having to care about what the OS version the app is running on or is targeting to. Internally, it handles the details when it comes to connecting to or disconnecting from a Wi-Fi network, and of course, it behaves differently on different OS platform.

# How to Use
## Add dependency to the library
```java
implementation 'com.nling.tools:wificonnectivitymanager:0.0.1'
```
### Connects to a SSID
- Define and initialize the WiFi connectivity manager
```Java
    val wifiConnManager = ConnectivityManagerBuilder(context!!)
            .addConnectivityCallback(this)
            .setInteractive(false)
            .build()
```
- Connect to the target network

```Java
wifiConnManager.connect(ssid, password)
```