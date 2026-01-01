"""
Database Module - MongoDB Connection and Collections
"""

from motor.motor_asyncio import AsyncIOMotorClient
from config import MONGO_URI, DATABASE_NAME
import logging

logger = logging.getLogger(__name__)


class Database:
    def __init__(self):
        self.client = None
        self.db = None
        self.movies = None
        self.banners = None
        self.stats = None

    async def connect(self):
        """Connect to MongoDB"""
        try:
            self.client = AsyncIOMotorClient(MONGO_URI)
            self.db = self.client[DATABASE_NAME]
            self.movies = self.db["movies"]
            self.banners = self.db["banners"]
            self.stats = self.db["stats"]
            
            # Create indexes
            await self.movies.create_index("telegram_file_id", unique=True, sparse=True)
            await self.movies.create_index("title")
            await self.movies.create_index("category")
            await self.movies.create_index("created_at")
            await self.movies.create_index([("title", "text"), ("original_title", "text")])
            
            logger.info("Connected to MongoDB successfully!")
            return True
        except Exception as e:
            logger.error(f"MongoDB connection error: {e}")
            return False

    async def disconnect(self):
        """Disconnect from MongoDB"""
        if self.client:
            self.client.close()
            logger.info("Disconnected from MongoDB")


# Singleton instance
db = Database()
