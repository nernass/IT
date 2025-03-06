import pytest

from pymongo import MongoClient
from pymongo.database import Database
from fastapi.testclient import TestClient


from db.db import get_db
from main import app


def override_get_db():
    client = MongoClient()
    try:
        db = client.get_database("test_db")
        yield db
    finally:
        client.close()


def reset_database(db: Database) -> None:
    """Clear database after each test."""
    for name in db.list_collection_names():
        collection = db.get_collection(name)
        collection.delete_many({})


@pytest.fixture(scope="session")
def database() -> Database:
    return MongoClient().get_database("test_db")


@pytest.fixture(scope="session")
def test_client(database):
    with TestClient(app) as client:
        app.dependency_overrides[get_db] = override_get_db
        yield client
        reset_database(database)
        app.dependency_overrides = {}


@pytest.fixture()
def test_order() -> dict:
    return {
        "id": "order_id",
        "status": "NEW",
        "items": [
            {
                "title": "avocado",
                "description": "useful description",
                "quantity": 2,
                "price": 27.0,
            }
        ],
        "total_price": 54.0,
    }
