import pytest
from unittest.mock import Mock, patch
from pymongo.database import Database
from fastapi.testclient import TestClient

from app_part_2.main import app, process_order
from app_part_2.enums import OrderStatus
from app_part_2.models.models import Order
from app_part_2.exceptions.common import DeliveryRegistrationError

client = TestClient(app)

@pytest.fixture
def mock_db():
    """Mock MongoDB database"""
    mock_db = Mock(spec=Database)
    mock_db.orders = Mock()
    mock_db.orders.insert_one = Mock()
    mock_db.orders.update_one = Mock()
    return mock_db

@pytest.fixture
def valid_order_data():
    return {
        "id": "test_order_1",
        "status": OrderStatus.NEW,
        "items": [
            {"item_id": "item1", "quantity": 2},
            {"item_id": "item2", "quantity": 1}
        ],
        "delivery_info": {
            "address": "Test Street 123",
            "client_name": "John Doe",
            "phone": "+1234567890"
        }
    }

class TestOrderProcessingIntegration:
    
    @patch('app_part_2.main.ByCicleAPI')
    def test_successful_order_processing(self, mock_bycicle_api, mock_db, valid_order_data):
        # Configure mocks
        mock_delivery_service = mock_bycicle_api.return_value
        mock_delivery_service.register_order.return_value = {"status": OrderStatus.PROCESSING}

        # Process order
        result = process_order(valid_order_data, mock_db)

        # Verify database interactions
        mock_db.orders.insert_one.assert_called_once_with(valid_order_data)
        mock_db.orders.update_one.assert_called_once_with(
            {"id": "test_order_1"},
            {"$set": {"status": OrderStatus.PROCESSING}}
        )

        # Verify delivery service interaction
        mock_delivery_service.register_order.assert_called_once()
        
        # Verify result
        assert isinstance(result, Order)
        assert result.status == OrderStatus.PROCESSING
        assert result.id == valid_order_data["id"]

    @patch('app_part_2.main.ByCicleAPI')
    def test_failed_delivery_registration(self, mock_bycicle_api, mock_db, valid_order_data):
        # Configure mock to raise exception
        mock_delivery_service = mock_bycicle_api.return_value
        mock_delivery_service.register_order.side_effect = DeliveryRegistrationError()

        # Process order
        result = process_order(valid_order_data, mock_db)

        # Verify database interactions
        mock_db.orders.insert_one.assert_called_once_with(valid_order_data)
        mock_db.orders.update_one.assert_called_once_with(
            {"id": "test_order_1"},
            {"$set": {"status": OrderStatus.FAILED}}
        )

        # Verify result
        assert isinstance(result, Order)
        assert result.status == OrderStatus.FAILED
        assert result.id == valid_order_data["id"]

    @patch('app_part_2.main.ByCicleAPI')
    def test_edge_case_empty_items(self, mock_bycicle_api, mock_db):
        # Test with empty items list
        order_data = {
            "id": "test_order_2",
            "status": OrderStatus.NEW,
            "items": [],
            "delivery_info": {
                "address": "Test Street 123",
                "client_name": "John Doe",
                "phone": "+1234567890"
            }
        }

        mock_delivery_service = mock_bycicle_api.return_value
        mock_delivery_service.register_order.return_value = {"status": OrderStatus.PROCESSING}

        result = process_order(order_data, mock_db)

        # Verify interactions and results
        mock_db.orders.insert_one.assert_called_once_with(order_data)
        assert isinstance(result, Order)
        assert result.items == []