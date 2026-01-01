# 🎬 CineMax - Movie Streaming Platform

A complete movie streaming solution with an Android app and Python backend.

## 📁 Project Structure

```
movie-app/
├── android/          # Android app (Kotlin + Jetpack Compose)
│   ├── app/
│   └── README.md
└── backend/          # API server (Python + FastAPI)
    ├── main.py
    └── README.md
```

## 🚀 Quick Start

### Backend

```bash
cd backend
pip install -r requirements.txt
cp .env.example .env
# Edit .env with your credentials
python main.py
```

### Android

1. Open `android/` in Android Studio
2. Update API URL in `app/build.gradle.kts`
3. Download Google Fonts (Outfit & Inter)
4. Run on device/emulator

## 📋 Requirements

### Backend
- Telegram API credentials
- MongoDB database
- TMDB API key (optional, for metadata)

### Android
- Android Studio Hedgehog+
- Android SDK 34
- JDK 17

## 📖 Documentation

- [Backend README](backend/README.md)
- [Android README](android/README.md)

## 📄 License

MIT License
