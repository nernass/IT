import pytest
from unittest.mock import patch, MagicMock
from fastapi.testclient import TestClient
from app_part_2.main import app, process_order
from app_part_2.db.db import get_db
from models.models import Order
from enums import OrderStatus
from exceptions.common import DeliveryRegistrationError

client = TestClient(app)

@pytest.fixture
def mock_db():
    with patch("app_part_2.db.db.MongoClient") as MockClient:
        mock_client = MockClient.return_value
        mock_db = mock_client.get_database.return_value
        yield mock_db

@pytest.fixture
def mock_delivery_service():
    with patch("app_part_2.main.ByCicleAPI") as MockDeliveryService:
        yield MockDeliveryService.return_value

def test_process_order_success(mock_db, mock_delivery_service):
    raw_order = {"id": "123", "item": "test_item", "quantity": 1}
    mock_delivery_service.register_order.return_value = {"status": OrderStatus.DELIVERED}

    order = process_order(raw_order, mock_db)

    mock_db.orders.insert_one.assert_called_once()
    mock_db.orders.update_one.assert_called_once()
    assert order.status == OrderStatus.DELIVERED

def test_process_order_failure(mock_db, mock_delivery_service):
    raw_order = {"id": "123", "item": "test_item", "quantity": 1}
    mock_delivery_service.register_order.side_effect = DeliveryRegistrationError

    order = process_order(raw_order, mock_db)

    mock_db.orders.insert_one.assert_called_once()
    mock_db.orders.update_one.assert_called_once()
    assert order.status == OrderStatus.FAILED

def test_process_order_edge_case(mock_db, mock_delivery_service):
    raw_order = {"id": "123", "item": "test_item", "quantity": 0}
    mock_delivery_service.register_order.return_value = {"status": OrderStatus.PROCESSING}

    order = process_order(raw_order, mock_db)

    mock_db.orders.insert_one.assert_called_once()
    mock_db.orders.update_one.assert_called_once()
    assert order.status == OrderStatus.PROCESSING