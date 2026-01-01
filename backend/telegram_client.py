"""
Telegram Client Module - Pyrogram Integration for Movie Streaming
"""

from pyrogram import Client
from pyrogram.types import Message
from pyrogram.errors import FloodWait, AuthKeyUnregistered
from config import API_ID, API_HASH, BOT_TOKEN, MOVIE_CHANNEL_ID
import asyncio
import logging
import re
from typing import Optional, AsyncGenerator, Tuple, List, Dict
from datetime import datetime

logger = logging.getLogger(__name__)


class TelegramClient:
    """Telegram client for fetching and streaming movies"""
    
    def __init__(self):
        self.client: Optional[Client] = None
        self.is_connected = False
        
    async def start(self):
        """Initialize and start the Pyrogram client"""
        try:
            self.client = Client(
                "movie_bot_session",
                api_id=API_ID,
                api_hash=API_HASH,
                bot_token=BOT_TOKEN,
                workdir="./sessions"
            )
            await self.client.start()
            self.is_connected = True
            logger.info("Telegram client started successfully!")
            
            # Verify channel access
            try:
                chat = await self.client.get_chat(MOVIE_CHANNEL_ID)
                logger.info(f"Connected to channel: {chat.title}")
            except Exception as e:
                logger.warning(f"Could not access movie channel: {e}")
                
        except Exception as e:
            logger.error(f"Failed to start Telegram client: {e}")
            self.is_connected = False
            raise
            
    async def stop(self):
        """Stop the Pyrogram client"""
        if self.client and self.is_connected:
            await self.client.stop()
            self.is_connected = False
            logger.info("Telegram client stopped")
            
    def parse_movie_info(self, text: str) -> Dict:
        """Parse movie information from message caption"""
        info = {
            "title": "",
            "year": None,
            "quality": "HD",
            "size": None,
            "language": "Hindi",
            "genres": []
        }
        
        if not text:
            return info
            
        lines = text.strip().split('\n')
        
        # Try to extract title (usually first line)
        if lines:
            title_line = lines[0].strip()
            # Remove emojis and clean title
            title_line = re.sub(r'[🎬🎥🎞️📽️🔥⚡💥🌟⭐]', '', title_line).strip()
            
            # Extract year if present in title
            year_match = re.search(r'\((\d{4})\)', title_line)
            if year_match:
                info["year"] = int(year_match.group(1))
                title_line = title_line.replace(year_match.group(0), '').strip()
            
            info["title"] = title_line
        
        # Extract quality
        quality_patterns = [
            r'(4K|2160p)', r'(1080p|FHD)', r'(720p|HD)', 
            r'(480p|SD)', r'(HDRip)', r'(WEB-DL)', r'(BluRay)'
        ]
        for pattern in quality_patterns:
            match = re.search(pattern, text, re.IGNORECASE)
            if match:
                info["quality"] = match.group(1).upper()
                break
                
        # Extract size
        size_match = re.search(r'(\d+(?:\.\d+)?)\s*(GB|MB)', text, re.IGNORECASE)
        if size_match:
            info["size"] = f"{size_match.group(1)} {size_match.group(2).upper()}"
            
        # Extract language
        lang_patterns = {
            "Hindi": r'(Hindi|हिंदी)',
            "English": r'(English|Eng)',
            "Tamil": r'(Tamil|தமிழ்)',
            "Telugu": r'(Telugu|తెలుగు)',
            "Dual Audio": r'(Dual\s*Audio)',
            "Multi Audio": r'(Multi\s*Audio)'
        }
        for lang, pattern in lang_patterns.items():
            if re.search(pattern, text, re.IGNORECASE):
                info["language"] = lang
                break
                
        return info
        
    async def get_channel_messages(
        self, 
        limit: int = 100, 
        offset_id: int = 0
    ) -> List[Dict]:
        """Fetch messages from movie channel"""
        if not self.is_connected:
            raise Exception("Telegram client not connected")
            
        movies = []
        
        try:
            async for message in self.client.get_chat_history(
                MOVIE_CHANNEL_ID,
                limit=limit,
                offset_id=offset_id
            ):
                if message.document or message.video:
                    media = message.document or message.video
                    
                    # Parse movie info from caption
                    movie_info = self.parse_movie_info(message.caption or "")
                    
                    # Get file details
                    file_name = getattr(media, 'file_name', None) or movie_info["title"] or f"movie_{message.id}"
                    file_size = media.file_size if hasattr(media, 'file_size') else 0
                    mime_type = getattr(media, 'mime_type', 'video/mp4')
                    
                    # Use filename as title if not parsed
                    if not movie_info["title"]:
                        # Clean filename
                        clean_name = re.sub(r'\.(mkv|mp4|avi|mov|webm)$', '', file_name, flags=re.IGNORECASE)
                        clean_name = re.sub(r'[._]', ' ', clean_name)
                        movie_info["title"] = clean_name.strip()
                    
                    movie_data = {
                        "telegram_message_id": message.id,
                        "telegram_file_id": media.file_id,
                        "telegram_file_unique_id": media.file_unique_id,
                        "title": movie_info["title"],
                        "year": movie_info["year"],
                        "quality": movie_info["quality"],
                        "size": movie_info["size"],
                        "size_bytes": file_size,
                        "language": movie_info["language"],
                        "mime_type": mime_type,
                        "caption": message.caption,
                        "message_date": message.date,
                        "duration_seconds": getattr(media, 'duration', None),
                        "thumbnail_file_id": getattr(media, 'thumbs', [{}])[0].get('file_id') if hasattr(media, 'thumbs') and media.thumbs else None
                    }
                    
                    movies.append(movie_data)
                    
        except FloodWait as e:
            logger.warning(f"FloodWait: sleeping for {e.value} seconds")
            await asyncio.sleep(e.value)
        except Exception as e:
            logger.error(f"Error fetching messages: {e}")
            raise
            
        return movies
        
    async def stream_file(
        self, 
        file_id: str, 
        offset: int = 0, 
        limit: int = 0
    ) -> AsyncGenerator[bytes, None]:
        """Stream file bytes with optional range support"""
        if not self.is_connected:
            raise Exception("Telegram client not connected")
            
        try:
            async for chunk in self.client.stream_media(
                message=file_id,
                offset=offset,
                limit=limit
            ):
                yield chunk
        except Exception as e:
            logger.error(f"Error streaming file: {e}")
            raise
            
    async def get_file_info(self, message_id: int) -> Optional[Dict]:
        """Get file information from message"""
        if not self.is_connected:
            raise Exception("Telegram client not connected")
            
        try:
            message = await self.client.get_messages(MOVIE_CHANNEL_ID, message_id)
            if message and (message.document or message.video):
                media = message.document or message.video
                return {
                    "file_id": media.file_id,
                    "file_size": media.file_size,
                    "mime_type": getattr(media, 'mime_type', 'video/mp4'),
                    "file_name": getattr(media, 'file_name', None),
                    "duration": getattr(media, 'duration', None)
                }
        except Exception as e:
            logger.error(f"Error getting file info: {e}")
            
        return None
        
    async def download_file(self, file_id: str) -> bytes:
        """Download complete file"""
        if not self.is_connected:
            raise Exception("Telegram client not connected")
            
        try:
            file_data = await self.client.download_media(file_id, in_memory=True)
            return file_data.getvalue() if hasattr(file_data, 'getvalue') else file_data
        except Exception as e:
            logger.error(f"Error downloading file: {e}")
            raise


# Singleton instance
telegram_client = TelegramClient()
