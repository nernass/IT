from pymongo import MongoClient
from pymongo.database import Database


def get_db() -> Database:
    client = MongoClient()
    return client.get_database("config")
