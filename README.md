# Introduction
This repository aims to provide an uniform interface for apps to connect to a specific Wi-Fi network, without having to care about what the OS version the app is running on or is targeting to. Internally, it handles the details when it comes to connecting to or disconnecting from a Wi-Fi network, and of course, it behaves differently on different OS platform.
# Features Highlight
1. WifiConnectivityManager
2. WifiConnectivityManagerPreAndroid10Imp
3. WifiConnectivityManagerAndroid10Imp
4. WifiConnectivityManagerInteractiveImp

# How to Use
### Connects to a SSID
- Define and initialize the WiFi connectivity manager
```Java
val wifiConnManager = WifiConnectivityManager(getContext())
```

- Register callback to be able to receive connect result
```Java
wifiConnManager.registerConnectResultCallback(this)
```

- Connect to the target network

```Java
wifiConnManager.connect(ssid, password)
```