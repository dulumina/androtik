# 📡 Androtik

> A powerful, open-source Android client for managing MikroTik RouterOS devices — your Winbox, in your pocket.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Status](https://img.shields.io/badge/Status-In%20Development-orange.svg)]()

---

## 🧩 About

**Androtik** is a native Android application built with **Kotlin** and **Jetpack Compose** that allows network administrators to remotely monitor and configure MikroTik routers directly from their Android device.

Inspired by tools like Winbox and Androtik (desktop), this project aims to provide a modern, intuitive, and secure mobile experience for RouterOS management — without needing a laptop.

> 🔗 Repository: [https://github.com/dulumina/androtik](https://github.com/dulumina/androtik)

---

## ✅ Todo — Feature Roadmap

Track the progress of planned features below.

### 🔴 Core (High Priority)
- [x] MikroTik API connection (port 8728 plain)
- [x] MikroTik API-SSL connection (port 8729)
- [x] Login & authentication to RouterOS
- [x] Multi-router profile management (save, edit, delete)
- [x] Dashboard — CPU, memory, uptime, board info
- [x] Interface list — view status (up/down), IP, type
- [ ] IP Address management — view, add, remove
- [ ] IP Routes — view routing table
- [ ] DHCP Server — view leases, add/remove static leases
- [ ] Firewall — browse Filter, NAT, and Mangle rules
- [ ] Logout & secure session handling

### 🟠 Important (Medium Priority)
- [ ] Simple Queue — view and manage bandwidth limits
- [ ] Wireless — view interface info and registration table
- [ ] IP Pool — view and manage address pools
- [ ] DNS — view and configure DNS settings
- [ ] Log viewer — system event & error logs
- [ ] Real-time traffic monitor per interface (Torch equivalent)
- [ ] Ping & Traceroute tool
- [ ] Bandwidth test tool
- [ ] Dark mode / Light mode toggle

### 🟡 Advanced (Lower Priority)
- [ ] SSH terminal (built-in CLI access via port 22)
- [ ] PPP — view active connections (PPPoE, PPTP, L2TP)
- [ ] Hotspot — view active users and host list
- [ ] VLAN management
- [ ] Bridge management
- [ ] CAPsMAN — remote AP management
- [ ] Scheduler — view and manage task schedules
- [ ] Scripts — view and trigger RouterOS scripts

### 🟢 Nice to Have (Future)
- [ ] Widget — quick router status on home screen
- [ ] Push notification for router alerts (via polling)
- [ ] Export configuration / backup trigger
- [ ] Multi-language support (EN, ID, etc.)
- [ ] Biometric lock for app security
- [ ] RouterOS v7 full compatibility check

---

## 🛠️ Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Kotlin                              |
| UI           | Jetpack Compose + Material 3        |
| Networking   | Custom MikroTik API Client (TCP)    |
| Async        | Kotlin Coroutines + Flow            |
| Architecture | MVVM + Clean Architecture           |
| Local DB     | Room Database                       |
| Security     | EncryptedSharedPreferences          |
| SSH Terminal | JSch / Sshj                         |

---

## 📡 Connection Protocol

Androtik communicates with RouterOS using the official **MikroTik API protocol** over TCP:

| Protocol     | Port   | Description                          |
|--------------|--------|--------------------------------------|
| MikroTik API | `8728` | Plain API connection                 |
| MikroTik API-SSL | `8729` | Encrypted API (recommended)     |
| SSH          | `22`   | For terminal / CLI access            |

> ⚠️ Make sure the API service is enabled on your router:
> `IP → Services → api` or `api-ssl`

---

## 📱 Requirements

- Android **8.0 (API level 26)** or higher
- MikroTik router running **RouterOS v6.x** or **v7.x**
- API service enabled on the router

---

## 🚀 Getting Started

```bash
# Clone the repository
git clone https://github.com/dulumina/androtik.git

# Open the project in Android Studio
# Sync Gradle dependencies
# Build & run on your device or emulator
```

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork this repository
2. Create your feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

Please make sure your code follows the existing code style and includes relevant comments.

---

## 📄 License

```
Copyright 2024 dulumina

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## ⚠️ Disclaimer

This project is **not affiliated with or endorsed by MikroTik.**
MikroTik and RouterOS are trademarks of **Mikrotikls SIA.**

---

<p align="center">Made with ❤️ for network administrators on the go.</p>
