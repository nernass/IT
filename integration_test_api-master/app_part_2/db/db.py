from pymongo import MongoClient
from pymongo.database import Database

__all__ = [
    "get_db",
]


def get_db() -> Database:
    client = MongoClient()
    return client.get_database("config")
