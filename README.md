
# 🔦 ShakeFlashlight

**Turn on the flashlight with a simple shake - even when the screen is off!**

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![Release](https://img.shields.io/github/v/release/Ang3l00/ShakeFlashlight)](https://github.com/Ang3l00/ShakeFlashlight/releases)

## ✨ Features

- 🔦 **Shake to Light**: Shake your phone to turn the LED on/off
- 📱 **Screen Off**: Always-on background service
- 🎛️ **Simple Toggle**: Enable/disable the service with a tap
- 📊 **Notification**: Quickly check the service status
- 🔋 **Ultra-light**: Minimal battery consumption (<1%)
- 🔒 **Privacy first**: No network permissions, only local sensors
- 🎯 **Optimized sensitivity**: Prevents accidental power on

## 🚀 Download

### Download APK

[<img src="https://img.shields.io/badge/Download-APK-green?style=for-the-badge&logo=android" alt="Download APK" height="60">](https://github.com/Ang3l00/ShakeFlashlight/releases/latest)

*Note: The app is not available on the Google Play Store to maintain the open source philosophy*

### APK Installation

1. Enable **"Install unknown apps"** in Android settings
2. Download the **APK** file from the releases
3. Tap the APK file to install
4. Grant the **permissions** required on first launch

## 🔧 How It Works

### Initial Setup

1. **Open** ShakeFlashlight
2. **Activate** the service with the main toggle
3. **Test** by shaking your smartphone

### Daily Use

- **Quick Shake**: 2-3 movements to activate
- **Long Shake**: Hold the shake for 1 second
- **Tap Widget**: Tap for status/direct control
- **App Toggle**: Temporarily disable the service

## 📲 System Requirements

- **Android 12.0** (API 31) or higher
- **Hardware**: Accelerometer + LED flashlight
- **Battery optimization** disabled for the app

### Compatibility Tested

- ✅ **Google Pixel** (8 series) vanilla
- ✅ **Google Pixel** (8 series) GrapheneOS

## 🔑 Required Permissions

<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

### Permissions Explained

| Permission | Usage | Requirement |
|----------|----------|-----------|
| `CAMERA` | Flashlight Hardware Access | **Essential** |
| `NOTIFICATIONS` | Flashlight LED Control | **Essential** |
| `SENSORS` | Shake Detection | **Essential** |

### Privacy & Security

- 🔒 **No internet permissions**: No data transmission
- 🛡️ **No file access**: Hardware control only
- 👁️ **Open source**: Publicly inspectable code
- 🔐 **Local data**: All settings remain on the device

## 🤝 Contributing to the Project

Contributions of all kinds are **welcome**! Here's how to participate:

### How to Contribute

1. **🍴 Fork** the repository
2. **🌿 Create** feature branches
3. **💻 Develop** and test changes
4. **📝 Commit** with descriptive messages
5. **🚀 Push** the branch
6. **🔄 Open** Pull Request with detailed description

### Types of Contributions Requested

🐛 Bug Fixes
✨ New Features
🚀 Performance
📚 Translation
🎨 UI/UX Material Design 3

## 🐛 Bug Report & Feature Requests

### Bug Report

Use [**Bug Report Template**](https://github.com/Ang3l00/ShakeFlashlight/issues/new?template=bug_report.md) including:

#### Essential Information

- **📱 Device**: Brand, model, Android version
- **📊 App Version**: ShakeFlashlight version installed
- **🔋 Battery**: Battery level and optimization settings
- **⚙️ ROM**: Stock Android, MIUI, OneUI, etc.

#### Problem Description

- **🎯 Expected behavior**: What should happen
- **❌ Actual behavior**: What happens instead
- **📝 Steps to reproduce**: How to reproduce the bug
- **📹 Media**: Screenshots, videos, logs if available

# Crash Reports - To get logcat
adb logcat -s ShakeFlashlight

Or by device:
Settings > Developer Options > Take Bug Report

# Feature Requests

Use [**Feature Request Template**](https://github.com/Ang3l00/ShakeFlashlight/issues/new?template=feature_request.md) with:
- **💡 Feature description**: Clear description of the feature
- **🎯 Use case**: When/why it would be useful
- **🔄 Alternatives**: Alternative solutions considered
- **📊 Priority**: Low, medium, high, and rationale

### Issue Labels

| Label | Description | Priority |
|-------|-----------|----------|
| `bug` | Confirmed bugs | 🔥 High |
| `enhancement` | New features | 📈 Medium |
| `help-wanted` | External contributions welcome | 🤝 Variable |
| `documentation` | Docs improvements | 📚 Low |
| `question` | User support | ❓ Low |
| `duplicate` | Existing issue | ➡️ Closing |

### Support & Community
- **💬 Discussions**: [GitHub Discussions](https://github.com/Ang3l00/ShakeFlashlight/discussions)

## 🗺️ Roadmap & Future Developments
- [ ] 🎨 **UI refresh** with full Material Design 3
- [ ] ⚙️ **Settings page** for advanced configuration
- [ ] 🌐 **Multi-language support** (EN, IT, ES, FR, DE)
- [ ] 🔊 **Vibration feedback** configurable
- [ ] 📊 **Advanced shake sensitivity** with configuration slider
- [ ] ⏰ **Auto timer** Weekly schedule for automatic app activation
- [ ] 🔋 **Battery optimization** Code improvements to reduce battery impact

## 🏆 Contributors & Acknowledgements

### Core Team

- **👑 [@Ang3l00](https://github.com/Ang3l00)** - *Creator & Lead Developer*

### Community Contributors

*The list will grow with your contributions! Early contributors will receive special mention.*

### Special Thanks

- **🤖 Android Open Source Project** - For the core APIs
- **💎 Kotlin Team** - For the amazing language
- **📱 Material Design Team** - For the UI/UX guidelines
- **🔧 Android Studio Team** - For the development environment
- **🌍 Open Source Community** - For constant inspiration

### Common FAQ

#### The app doesn't detect shaking
1. ✅ Check active service (toggle ON)
2. ✅ Check app permissions in Android Settings
3. ✅ Disable battery optimization for ShakeFlashlight
4. ✅ Test with a firmer, but not excessive, shake
3. ✅ Restart ShakeFlashlight completely

#### Notification widget not working
1. ✅ Check active service (toggle ON)
2. ✅ Check App permissions in Android Settings
3. ✅ Disable battery optimization for ShakeFlashlight
3. ✅ Restart ShakeFlashlight completely

#### Flashlight won't turn on
1. ✅ Check flashlight hardware (camera app)
2. ✅ Close other apps that use the camera/flashlight
3. ✅ Restart ShakeFlashlight completely

### Performance Optimization
- ⚙️ **Disable** unnecessary background app refreshes
- 🔋 **Whitelist** ShakeFlashlight from battery optimization
- 📱 **Keep** apps in memory (don't swipe-close frequently)

### Known Issues & Workarounds

| Problem | Device | Workaround |
|----------|-------------|------------|


### Getting Help

1. **🔍 Search existing** [Issues](https://github.com/Ang3l00/ShakeFlashlight/issues)
2. **📋 Use templates** for bug report o feature request
4. **📱 Specify device** model and Android version

## 📄 Licenza & Legal

### GNU General Public License v3.0 **GPL 3.0**

#### ✅ User Rights
- **🆓 Free use** for any purpose
- **📖 Study** of the source code
- **🔄 Distribution** of copies
- **⚡ Modification** and improvement

#### 📋 Developer Obligations
- **📂 Source code** always available
- **©️ Maintain original copyright** and license
- **🔄 GPL derivatives** must remain GPL
- **📝 Document significant changes**

### Copyright Notice

ShakeFlashlight - Shake-to-activate flashlight for Android
Copyright (C) 2025 Ang3l00

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.

### Third-Party Licenses

This project use:

- **Android SDK**: Apache License 2.0
- **Kotlin**: Apache License 2.0  
- **Material Design Icons**: Apache License 2.0
- **AndroidX Libraries**: Apache License 2.0

All compatible with GPL 3.0.

### Privacy Policy

ShakeFlashlight respects your privacy:

- ❌ **No personal data** collection
- ❌ **No information transmission**
- ❌ **No tracking** or analytics
- ✅ **Local data only** (device preferences)
- ✅ **Transparent and inspectable code**

---

<div align="center">

## 🌟 **Made with ❤️ for the Android Community**

### 🚀 If ShakeFlashlight is useful to you, please leave a ⭐ star!

#### 📢 Share with other Android developers and users

[![GitHub stars](https://img.shields.io/github/stars/Ang3l00/ShakeFlashlight?style=social)](https://github.com/Ang3l00/ShakeFlashlight/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/Ang3l00/ShakeFlashlight?style=social)](https://github.com/Ang3l00/ShakeFlashlight/network)
[![GitHub watchers](https://img.shields.io/github/watchers/Ang3l00/ShakeFlashlight?style=social)](https://github.com/Ang3l00/ShakeFlashlight/watchers)

*Last README update: September 2025*
