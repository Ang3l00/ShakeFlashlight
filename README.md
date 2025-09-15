# ShakeFlashlight
An Android APP to activate the LED by shaking the smartphone

## ✨ Features

- 🔦 **Shake to Light**: Shake your phone to turn the LED on/off

- 📱 **Screen Off**: Always-on background service

- 🎛️ **Simple Toggle**: Enable/disable the service with a tap

- ⚡ **Ultra-light**: Minimal battery consumption

- 🔒 **Privacy first**: No network permissions, only local sensors


## 🔧 How It Works

1. **Install** the app

2. **Activate** the service with the main toggle

3. **Shake** the phone to turn the flashlight on/off

## Shake Sensitivity

The app detects motion via an accelerometer with an optimized threshold to prevent accidental activation.

## Prerequisites

- Android 12+

## 🚀 Download

**APK** [Releases](https://github.com/Ang3l00/ShakeFlashlight/releases) 

## 🔑 Permissions Required

<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

Why these permissions:

    CAMERA/FLASHLIGHT: Controls LED flashlight
    FOREGROUND_SERVICE: Service active when screen is off
    WAKE_LOCK: Detects shake when screen is off

🤝 Contribute

Contributions are welcome! Please:

  Fork the repository
  Create a feature branch (git checkout -b feature/AmazingFeature)
  Commit your changes (git commit -m 'Add some AmazingFeature')
  Push the branch (git push origin feature/AmazingFeature)
  Open a Pull Request

🐛 Bug Report & Feature Request

Use Issues to:

    🐛 Report bugs
    💡 Propose new features
    ❓ Ask questions about the app

⭐ Support

If you like the project, please leave a star ⭐ and share it!

Made with ❤️ for the Android community
Copyright (C) 2025 Ang3l00

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
