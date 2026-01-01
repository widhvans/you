# 🎬 CineMax Backend API

A FastAPI-based backend for the CineMax Movie Streaming App that integrates with Telegram.

## Features

- 📺 Stream movies directly from Telegram channels
- 🔍 Search movies with text search
- 🎭 Movies organized by category (Hollywood, Bollywood, Series)
- 🌟 TMDB integration for movie metadata (posters, ratings, plot)
- 📥 Download support with progress tracking

## Prerequisites

- Python 3.10+
- MongoDB (Atlas or local)
- Telegram API credentials
- TMDB API key

## Setup

### 1. Clone and Install Dependencies

```bash
cd backend
pip install -r requirements.txt
```

### 2. Configure Environment

Copy `.env.example` to `.env` and fill in your credentials:

```bash
cp .env.example .env
```

Required environment variables:
| Variable | Description |
|----------|-------------|
| `API_ID` | Telegram API ID from [my.telegram.org](https://my.telegram.org) |
| `API_HASH` | Telegram API Hash |
| `BOT_TOKEN` | Bot token from @BotFather |
| `MOVIE_CHANNEL_ID` | Your movie channel ID (negative number) |
| `MONGO_URI` | MongoDB connection string |
| `TMDB_API_KEY` | TMDB API key from [themoviedb.org](https://www.themoviedb.org/settings/api) |

### 3. Run the Server

```bash
# Development
uvicorn main:app --reload --host 0.0.0.0 --port 8000

# Or directly
python main.py
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Health check |
| `/api/movies` | GET | Get paginated movies |
| `/api/movies/{id}` | GET | Get movie by ID |
| `/api/movies/trending` | GET | Get trending movies |
| `/api/movies/latest` | GET | Get latest movies |
| `/api/search?q=query` | GET | Search movies |
| `/api/banners` | GET | Get featured banners |
| `/api/categories` | GET | Get movie categories |
| `/api/stream/{id}` | GET | Stream movie (supports Range) |
| `/api/download/{id}` | GET | Get download info |
| `/api/sync` | POST | Trigger movie sync |

## Deployment

### Railway

1. Create a new project on [Railway](https://railway.app)
2. Connect your GitHub repository
3. Add environment variables
4. Deploy!

### Render

1. Create a new Web Service on [Render](https://render.com)
2. Connect your repository
3. Set build command: `pip install -r requirements.txt`
4. Set start command: `uvicorn main:app --host 0.0.0.0 --port $PORT`

## Project Structure

```
backend/
├── main.py              # FastAPI application
├── config.py            # Configuration
├── telegram_client.py   # Telegram integration
├── tmdb_service.py      # TMDB API service
├── requirements.txt     # Dependencies
├── database/
│   └── __init__.py      # MongoDB connection
└── models/
    ├── __init__.py
    └── movie.py         # Pydantic models
```

## License

MIT
