import pytest
from unittest.mock import MagicMock, patch
from pymongo.database import Database
from fastapi.testclient import TestClient

from app_part_2.main import app, process_order
from app_part_2.enums import OrderStatus
from app_part_2.models.models import Order
from app_part_2.exceptions.common import DeliveryRegistrationError


@pytest.fixture
def client():
    return TestClient(app)


@pytest.fixture
def mock_db():
    db_mock = MagicMock(spec=Database)
    db_mock.orders = MagicMock()
    db_mock.orders.insert_one = MagicMock()
    db_mock.orders.update_one = MagicMock()
    return db_mock


@pytest.fixture
def sample_order():
    return {
        "id": "12345",
        "customer_name": "Test Customer",
        "customer_address": "123 Test St",
        "items": [
            {"name": "Test Item 1", "price": 10.0, "quantity": 2},
            {"name": "Test Item 2", "price": 15.0, "quantity": 1},
        ],
        "status": OrderStatus.RECEIVED.value
    }


@pytest.mark.integration
class TestMainDbIntegration:
    
    @patch("app_part_2.main.ByCicleAPI")
    def test_process_order_successful(self, mock_bycicle_api, mock_db, sample_order):
        # Setup
        mock_api_instance = mock_bycicle_api.return_value
        mock_api_instance.register_order.return_value = {"status": OrderStatus.PROCESSING.value}
        
        # Execute
        result = process_order(sample_order, mock_db)
        
        # Assert
        mock_db.orders.insert_one.assert_called_once()
        mock_db.orders.update_one.assert_called_once_with(
            {"id": sample_order["id"]}, 
            {"$set": {"status": OrderStatus.PROCESSING.value}}
        )
        assert isinstance(result, Order)
        assert result.status == OrderStatus.PROCESSING
        
    @patch("app_part_2.main.ByCicleAPI")
    def test_process_order_delivery_failure(self, mock_bycicle_api, mock_db, sample_order):
        # Setup
        mock_api_instance = mock_bycicle_api.return_value
        mock_api_instance.register_order.side_effect = DeliveryRegistrationError("Failed to register")
        
        # Execute
        result = process_order(sample_order, mock_db)
        
        # Assert
        mock_db.orders.insert_one.assert_called_once()
        mock_db.orders.update_one.assert_called_once_with(
            {"id": sample_order["id"]},
            {"$set": {"status": OrderStatus.FAILED}}
        )
        assert isinstance(result, Order)
        assert result.status == OrderStatus.FAILED
    
    @patch("app_part_2.db.db.MongoClient")
    def test_db_connection(self, mock_mongo_client):
        # Setup
        from app_part_2.db.db import get_db
        mock_client = MagicMock()
        mock_db = MagicMock()
        mock_client.get_database.return_value = mock_db
        mock_mongo_client.return_value = mock_client
        
        # Execute
        result = get_db()
        
        # Assert
        mock_client.get_database.assert_called_once_with("config")
        assert result == mock_db
    
    @patch("app_part_2.db.db.get_db")
    @patch("app_part_2.main.process_order")
    def test_order_api_integration(self, mock_process_order, mock_get_db, client, mock_db, sample_order):
        # This would test the API route that uses both main.py and db.py
        # For this test, we'd need to know more about the orders_router implementation
        # This is a placeholder showing how you'd structure such a test
        mock_get_db.return_value = mock_db
        mock_process_order.return_value = Order.parse_obj(sample_order)
        mock_process_order.return_value.status = OrderStatus.PROCESSING
        
        # Example API call - assuming there's a POST /orders endpoint
        # response = client.post("/orders", json=sample_order)
        # assert response.status_code == 200
        # assert response.json()["status"] == OrderStatus.PROCESSING.value