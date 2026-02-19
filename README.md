# 🔥 SmokeSignal

**Bitchat-style offline P2P app for smoke break signals.**

No internet. No servers. No accounts. Just tap and your friends' phones buzz.

## How It Works

1. **Open the app** → auto-generates a nickname like `smoker4291`
2. **Phones discover each other** via Bluetooth + WiFi Direct (Google Nearby Connections API)
3. **Hold the aura button** → all connected friends get a vibration + notification
4. **That's it.** No chat, no social features, no bloat.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material3 |
| Networking | Google Nearby Connections API (P2P_CLUSTER) |
| Identity | Bitchat-style auto-nicknames + DataStore persistence |
| Protocol | JSON over byte payloads (org.json) |
| Service | Foreground service for background mesh |
| Animations | Canvas-based Siri-like aura visualizer |

## Architecture

```
com.smokesignal.app/
├── identity/          # Bitchat-style nickname + device ID
├── model/             # NearbyUser, SignalPayload
├── service/           # P2P manager, foreground service, notifications
└── ui/
    ├── components/    # AuraVisualizer, NicknameCard, dialogs
    ├── screens/       # MainScreen
    └── theme/         # Dark theme colors
```

## Building

### Option 1: Android Studio
1. Clone/open this project in Android Studio
2. Let Gradle sync (it will download the wrapper automatically)
3. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
4. Find APK in `app/build/outputs/apk/debug/`

### Option 2: GitHub Actions (Recommended)
1. Push this repo to GitHub
2. Go to **Actions** tab → **Build APK** workflow
3. Click **Run workflow** (or it runs automatically on push)
4. Download the APK from the workflow artifacts

### Option 3: Command Line
```bash
# Generate gradle wrapper first
gradle wrapper --gradle-version 8.4

# Build debug APK
./gradlew assembleDebug
```

## Permissions Required

- **Bluetooth** — peer discovery
- **WiFi** — high-bandwidth P2P data transfer
- **Location** — required by Android for BT/WiFi scanning
- **Notifications** — smoke signal alerts
- **Vibrate** — haptic feedback

## Inspired By

- [Bitchat](https://bitchat.world) by Jack Dorsey — decentralized, serverless chat
- The universal office ritual of the group smoke break 🚬

## License

MIT — do whatever you want with it.
