
# 🔦 ShakeFlashlight

**Accendi la torcia con una semplice scossa - anche a schermo spento!**

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![Release](https://img.shields.io/github/v/release/Ang3l00/ShakeFlashlight)](https://github.com/Ang3l00/ShakeFlashlight/releases)

## ✨ Caratteristiche

- 🔦 **Shake to Light**: Scuoti il telefono per accendere/spegnere il LED
- 📱 **Funziona a schermo spento**: Servizio in background sempre attivo
- 🎛️ **Toggle semplice**: Attiva/disattiva il servizio con un tap
- 📊 **Notifica**: Controllo rapido dello stato del servizio
- ⚡ **Ultra leggero**: Consumi minimi di batteria
- 🔒 **Privacy first**: Nessun permesso di rete, solo sensori locali
- 🎯 **Sensibilità ottimizzata**: Evita accensioni accidentali

## 🚀 Download

### Scarica APK

[<img src="https://img.shields.io/badge/Download-APK-green?style=for-the-badge&logo=android" alt="Download APK" height="60">](https://github.com/Ang3l00/ShakeFlashlight/releases/latest)


*Nota: L'app non è disponibile sul Google Play Store per mantenere la filosofia open source*

### Installazione APK

1. Abilita **"Installa app sconosciute"** nelle impostazioni Android
2. Scarica il file **APK** dalle releases
3. Tocca il file APK per installare
4. Concedi i **permessi** richiesti al primo avvio

## 🔧 Come Funziona

### Setup Iniziale

1. **Apri** ShakeFlashlight
2. **Attiva** il servizio con il toggle principale
3. **Testa** scuotendo lo smartphone

### Utilizzo Quotidiano

- **Shake rapido**: 2-3 movimenti per attivare
- **Shake prolungato**: Mantieni la scossa per 1 secondo
- **Widget tap**: Tocca per stato/controllo diretto
- **Toggle app**: Disattiva temporaneamente il servizio

## 📲 Requisiti di Sistema

- **Android 12.0** (API 31) o superiore
- **Storage**: 8MB spazio libero
- **Hardware**: Accelerometro + Torcia LED
- **Battery optimization** disabilitata per l'app

### Compatibilità Testata

- ✅ **Google Pixel** (8 series) vanilla
- ✅ **Google Pixel** (8 series) GrapheneOS

## 🔑 Permessi Richiesti

<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

### Spiegazione Permessi

| Permesso | Utilizzo | Necessità |
|----------|----------|-----------|
| `CAMERA` | Accesso hardware torcia | **Essenziale** |
| `NOTIFICHE` | Controllo LED torcia | **Essenziale** |
| `SENSORS` | Rilevamento shake | **Essenziale** |

### Privacy e Sicurezza

- 🔒 **Nessun permesso internet**: Zero trasmissione dati
- 🛡️ **Nessun accesso file**: Solo controllo hardware
- 👁️ **Open source**: Codice ispezionabile pubblicamente
- 🔐 **Dati locali**: Tutte le impostazioni rimangono sul device

## 🤝 Contribuire al Progetto

Contribuzioni di ogni tipo sono **benvenute**! Ecco come partecipare:

### Come Contribuire

1. **🍴 Fork** il repository
2. **🌿 Crea** feature branch
3. **💻 Sviluppa** e testa le modifiche
4. **📝 Commit** con messaggi descrittivi
5. **🚀 Push** della branch 
6. **🔄 Apri** Pull Request con descrizione dettagliata

### Tipi di Contribuzioni Richieste

🐛 Bug Fixes
✨ New Features
🚀 Performance
📚 Translation
🎨 UI/UX Material Design 3

## 🐛 Bug Report & Feature Requests

### Segnalazione Bug

Usa il [**Bug Report Template**](https://github.com/Ang3l00/ShakeFlashlight/issues/new?template=bug_report.md) includendo:

#### Informazioni Essenziali

- **📱 Device**: Marca, modello, Android version
- **📊 App Version**: Versione ShakeFlashlight installata
- **🔋 Battery**: Livello batteria e optimization settings
- **⚙️ ROM**: Stock Android, MIUI, OneUI, ecc.

#### Descrizione Problema

- **🎯 Expected behavior**: Cosa dovrebbe accadere
- **❌ Actual behavior**: Cosa accade invece
- **📝 Steps to reproduce**: Come riprodurre il bug
- **📹 Media**: Screenshot, video, logs se disponibili

#### Crash Reports


# Per ottenere logcat
adb logcat -s ShakeFlashlight

Oppure da device:
Settings > Developer Options > Take Bug Report

# Feature Requests

Usa il [**Feature Request Template**](https://github.com/Ang3l00/ShakeFlashlight/issues/new?template=feature_request.md) con:

- **💡 Feature description**: Descrizione chiara della funzionalità
- **🎯 Use case**: Quando/perché sarebbe utile
- **🔄 Alternatives**: Soluzioni alternative considerate
- **📊 Priority**: Bassa, media, alta e motivazione

### Issue Labels

| Label | Descrizione | Priorità |
|-------|-------------|----------|
| `bug` | Malfunzionamenti confermati | 🔥 Alta |
| `enhancement` | Nuove funzionalità | 📈 Media |
| `help-wanted` | Contributi esterni benvenuti | 🤝 Variabile |
| `documentation` | Miglioramenti docs | 📚 Bassa |
| `question` | Supporto utenti | ❓ Bassa |
| `duplicate` | Issue già esistente | ➡️ Chiusura |

### Support & Community

- **💬 Discussions**: [GitHub Discussions](https://github.com/Ang3l00/ShakeFlashlight/discussions) per domande generali

## 🗺️ Roadmap & Sviluppi Futuri
- [ ] 🎨 **UI refresh** con Material Design 3 completo
- [ ] ⚙️ **Settings page** per configurazione avanzata
- [ ] 🌐 **Multi-language support** (EN, IT, ES, FR, DE)
- [ ] 🔊 **Feedback vibrazione** configurabile
- [ ] 📊 **Advanced shake sensitivity** con slider configurazione
- [ ] ⏰ **Auto-off timer** per prevenire consumo batteria
- [ ] 🎯 **Gesture customization** (double shake, long shake, etc.)

## 🏆 Contributors & Riconoscimenti

### Core Team

- **👑 [@Ang3l00](https://github.com/Ang3l00)** - *Creator & Lead Developer*

### Community Contributors

*La lista crescerà con i vostri contributi! Primi contributori riceveranno menzione speciale.*

### Special Thanks

- **🤖 Android Open Source Project** - Per le API fondamentali
- **💎 Kotlin Team** - Per il linguaggio straordinario  
- **📱 Material Design Team** - Per le linee guida UI/UX
- **🔧 Android Studio Team** - Per l'ambiente di sviluppo
- **🌍 Open Source Community** - Per l'ispirazione continua

### FAQ Comune

#### L'app non rileva lo shake
1. ✅ Verifica servizio attivo (toggle ON)
2. ✅ Controlla permessi app in Settings Android
3. ✅ Disabilita battery optimization per ShakeFlashlight
4. ✅ Testa con shake più deciso ma non eccessivo
3. ✅ Riavvia ShakeFlashlight completamente

#### Widget notifica non funziona
1. ✅ Verifica servizio attivo (toggle ON)
2. ✅ Controlla permessi app in Settings Android
3. ✅ Disabilita battery optimization per ShakeFlashlight
3. ✅ Riavvia ShakeFlashlight completamente

#### Torcia non si accende
1. ✅ Verifica hardware torcia funzionante (fotocamera app)
2. ✅ Chiudi altre app che usano fotocamera/torcia
3. ✅ Riavvia ShakeFlashlight completamente

### Performance Optimization
- ⚙️ **Disabilita** background app refresh non necessari
- 🔋 **Whitelist** ShakeFlashlight da battery optimization
- 📱 **Mantieni** app in memoria (non swipe-close frequent)

### Problemi Noti & Workarounds

| Problema | Dispositivi | Workaround |
|----------|-------------|------------|


### Getting Help

1. **🔍 Search existing** [Issues](https://github.com/Ang3l00/ShakeFlashlight/issues)
2. **📋 Use templates** per bug report o feature request
4. **📱 Specify device** model e Android version

## 📄 Licenza & Legal

### GNU General Public License v3.0

Questo progetto è rilasciato sotto **GPL 3.0**, che garantisce:

#### ✅ Diritti Utenti
- **🆓 Uso libero** per qualsiasi scopo
- **📖 Studio** del codice sorgente
- **🔄 Distribuzione** di copie
- **⚡ Modifica** e miglioramento

#### 📋 Obblighi Sviluppatori
- **📂 Codice sorgente** sempre disponibile
- **©️ Mantenere copyright** e licenza originali
- **🔄 Derivati GPL** devono rimanere GPL
- **📝 Documentare modifiche** significative

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

Questo progetto utilizza:

- **Android SDK**: Apache License 2.0
- **Kotlin**: Apache License 2.0  
- **Material Design Icons**: Apache License 2.0
- **AndroidX Libraries**: Apache License 2.0

Tutte compatibili con GPL 3.0.

### Privacy Policy

ShakeFlashlight rispetta la privacy:

- ❌ **Nessuna raccolta dati** personali
- ❌ **Nessuna trasmissione** di informazioni
- ❌ **Nessun tracking** o analytics
- ✅ **Solo dati locali** (preferenze sul device)
- ✅ **Codice trasparente** e ispezionabile

---

<div align="center">

## 🌟 **Made with ❤️ for the Android Community**

### 🚀 Se ShakeFlashlight ti è utile, lascia una ⭐ stella!

#### 📢 Condividi con altri sviluppatori e utenti Android

[![GitHub stars](https://img.shields.io/github/stars/Ang3l00/ShakeFlashlight?style=social)](https://github.com/Ang3l00/ShakeFlashlight/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/Ang3l00/ShakeFlashlight?style=social)](https://github.com/Ang3l00/ShakeFlashlight/network)
[![GitHub watchers](https://img.shields.io/github/watchers/Ang3l00/ShakeFlashlight?style=social)](https://github.com/Ang3l00/ShakeFlashlight/watchers)

*Ultimo aggiornamento README: Settembre 2025*
