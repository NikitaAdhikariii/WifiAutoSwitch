# WiFi Auto Switch Android App

An Android application that scans available WiFi networks, displays them in a dropdown, allows you to connect by entering passwords, and supports preferred networks management and auto-switching.

---

## Features

- Scan and list nearby WiFi networks
- Show WiFi signal strength (RSSI)
- Connect to selected WiFi network with password input
- Show/hide password toggle
- Add and view preferred networks saved locally
- Auto-refresh WiFi scan every 30 seconds
- Monitor last WiFi network switch status
- Supports Android 10+ (uses WifiNetworkSpecifier API) and older versions

---

## Permissions

The app requires the following permissions:

- `ACCESS_FINE_LOCATION` — needed to scan WiFi networks (mandatory since Android 6.0)
- `CHANGE_WIFI_STATE` — to connect/disconnect WiFi networks
- `ACCESS_WIFI_STATE` — to read WiFi info

---

## How to Build and Run

1. Clone the repository or download the source code.
2. Open the project in Android Studio.
3. Grant location permission when prompted on your device/emulator.
4. Run the app on your Android device or emulator.
5. Use the dropdown to select a WiFi network.
6. Enter the network password and connect.
7. Use buttons to refresh networks, add preferred networks, and monitor status.

---

## Notes

- Android 10+ uses the recommended `WifiNetworkSpecifier` API for connecting to WiFi.
- On older Android versions, the app uses `WifiConfiguration`.
- WiFi scanning requires location permission due to Android system restrictions.
- Preferred networks are saved in `SharedPreferences`.
- Auto-switch scans every 30 seconds to update available networks.

---

## Troubleshooting

- Make sure location services are enabled on your device.
- If WiFi scanning doesn’t work, check if location permission is granted.
- For Android 10+, the app cannot connect to networks programmatically if the device does not support the `WifiNetworkSpecifier` API.

---
<img src="wifi.jpeg" height=500 width=300>
<img src="fiwi.jpeg" height=500 width=500>
