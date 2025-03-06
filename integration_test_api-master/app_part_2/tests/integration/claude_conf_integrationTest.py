import pytest
from fastapi.testclient import TestClient
from unittest.mock import Mock, patch

from app_part_2.main import app, process_order
from app_part_2.db.db import get_db
from app_part_2.models.models import Order
from app_part_2.enums import OrderStatus

@pytest.fixture
def test_client():
    return TestClient(app)

@pytest.fixture
def mock_db():
    db = Mock()
    db.orders = Mock()
    db.orders.insert_one = Mock()
    db.orders.update_one = Mock()
    return db

@pytest.fixture
def sample_order():
    return {
        "id": "test_id",
        "items": [{"item_id": "1", "quantity": 2}],
        "status": OrderStatus.NEW,
        "delivery_address": "Test Address",
        "total_amount": 100.0
    }

@patch('app_part_2.main.ByCicleAPI')
def test_process_order_success(mock_bycicle, mock_db, sample_order):
    # Setup mock delivery service
    mock_delivery = Mock()
    mock_delivery.register_order.return_value = {"status": OrderStatus.PROCESSING}
    mock_bycicle.return_value = mock_delivery

    # Process order
    result = process_order(sample_order, mock_db)

    # Verify interactions
    mock_db.orders.insert_one.assert_called_once()
    mock_db.orders.update_one.assert_called_once_with(
        {"id": "test_id"},
        {"$set": {"status": OrderStatus.PROCESSING}}
    )
    assert result.status == OrderStatus.PROCESSING

@patch('app_part_2.main.ByCicleAPI')
def test_process_order_failure(mock_bycicle, mock_db, sample_order):
    # Setup mock delivery service to raise exception
    mock_delivery = Mock()
    mock_delivery.register_order.side_effect = DeliveryRegistrationError
    mock_bycicle.return_value = mock_delivery

    # Process order
    result = process_order(sample_order, mock_db)

    # Verify interactions
    mock_db.orders.insert_one.assert_called_once()
    mock_db.orders.update_one.assert_called_once_with(
        {"id": "test_id"},
        {"$set": {"status": OrderStatus.FAILED}}
    )
    assert result.status == OrderStatus.FAILED

@pytest.mark.integration
def test_db_connection():
    # Test actual DB connection
    db = get_db()
    assert db.name == "config"
    
    # Clean up test data
    db.orders.delete_many({})

def test_root_endpoint(test_client):
    response = test_client.get("/")
    assert response.status_code == 200
    assert response.json() == {"hello": "world"}