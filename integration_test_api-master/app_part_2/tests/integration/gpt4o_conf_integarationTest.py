import pytest
from fastapi.testclient import TestClient
from pymongo import MongoClient
from app_part_2.main import app, process_order
from app_part_2.db.db import get_db
from models.models import Order
from enums import OrderStatus
from exceptions.common import DeliveryRegistrationError

@pytest.fixture
def client():
    return TestClient(app)

@pytest.fixture
def db():
    client = MongoClient()
    test_db = client.get_database("test_config")
    yield test_db
    client.drop_database("test_config")

def test_read_root(client):
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"hello": "world"}

def test_process_order_success(db, mocker):
    raw_order = {"id": "123", "item": "test_item", "quantity": 1}
    mocker.patch("deliveries.ByCicle.api.ByCicleAPI.register_order", return_value={"status": OrderStatus.DELIVERED})

    order = process_order(raw_order, db)
    
    assert order.status == OrderStatus.DELIVERED
    assert db.orders.find_one({"id": "123"})["status"] == OrderStatus.DELIVERED

def test_process_order_failure(db, mocker):
    raw_order = {"id": "123", "item": "test_item", "quantity": 1}
    mocker.patch("deliveries.ByCicle.api.ByCicleAPI.register_order", side_effect=DeliveryRegistrationError)

    order = process_order(raw_order, db)
    
    assert order.status == OrderStatus.FAILED
    assert db.orders.find_one({"id": "123"})["status"] == OrderStatus.FAILED