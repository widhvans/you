# 🎬 CineMax - Professional Movie Streaming App

A beautiful, feature-rich Android movie streaming application that integrates with Telegram for content delivery.

![CineMax](https://img.shields.io/badge/CineMax-Movie%20App-E50914?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=flat-square)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square)

## ✨ Features

- 🏠 **Beautiful Home Screen** - Netflix-style UI with hero banners and category rows
- 🔍 **Smart Search** - Real-time search with debouncing and recent searches
- 🎬 **Movie Details** - Rich detail pages with cast, plot, and similar movies
- ▶️ **Video Player** - ExoPlayer with custom controls and seeking
- 📥 **Downloads** - Background download with offline viewing
- 🌙 **Dark Theme** - Premium dark theme with red accent

## 📱 Screenshots

| Home | Detail | Player |
|------|--------|--------|
| Hero banners, category rows | Movie info, cast, actions | Fullscreen with controls |

## 🏗️ Architecture

```
MVVM + Clean Architecture
├── Data Layer
│   ├── API (Retrofit)
│   ├── Local (Room Database)
│   └── Repository
├── Domain Layer
│   └── Use Cases
└── Presentation Layer
    ├── ViewModels
    └── Compose UI
```

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Networking | Retrofit + OkHttp |
| Database | Room |
| Player | ExoPlayer (Media3) |
| Image | Coil |
| Navigation | Navigation Compose |

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/widhvans/you.git
   cd you/android
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Click **File > Open**
   - Select the `android` folder (NOT the root folder)
   - Wait for Gradle sync to complete (may take a few minutes on first open)
   - If prompted to install Android SDK components, click "Install"

3. **Configure API URL**
   
   Update `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"https://your-api-url.com\"")
   ```

4. **Build the app**
   - Click **Build > Make Project** or press **Ctrl+F9**
   - Run on device using **Run > Run 'app'** or press **Shift+F10**

## 📂 Project Structure

```
app/src/main/java/com/cinemax/app/
├── CineMaxApp.kt              # Application class
├── MainActivity.kt            # Single activity
├── data/
│   ├── api/                   # Retrofit service
│   ├── local/                 # Room database
│   ├── model/                 # Data classes
│   └── repository/            # Repositories
├── ui/
│   ├── components/            # Reusable composables
│   ├── navigation/            # Navigation graph
│   ├── screens/               # Screen composables
│   └── theme/                 # Colors, typography, theme
└── util/                      # Utilities
```

## 🔧 Configuration

### Backend URL

For development, the app connects to `http://10.0.2.2:8000` (Android emulator localhost).

For production, update the URL in `build.gradle.kts`:

```kotlin
buildTypes {
    release {
        buildConfigField("String", "API_BASE_URL", "\"https://your-production-api.com\"")
    }
}
```

## 📦 Building for Release

```bash
# Generate signed APK
./gradlew assembleRelease

# Generate AAB for Play Store
./gradlew bundleRelease
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- [ExoPlayer](https://github.com/google/ExoPlayer) - Video playback
- [Coil](https://github.com/coil-kt/coil) - Image loading
- [TMDB](https://www.themoviedb.org/) - Movie metadata
