import pytest
from unittest.mock import MagicMock, patch
import mongomock
import uuid
from pymongo.database import Database

from app_part_2.enums import OrderStatus
from app_part_2.models.models import Order
from app_part_2.main import process_order
from app_part_2.exceptions.common import DeliveryRegistrationError
from app_part_2.deliveries.ByCicle.api import ByCicleAPI


@pytest.fixture
def mock_db():
    """Create a mock MongoDB database using mongomock."""
    client = mongomock.MongoClient()
    db = client.get_database("config")
    # Ensure orders collection exists
    if "orders" not in db.list_collection_names():
        db.create_collection("orders")
    return db


@pytest.fixture
def sample_order():
    """Create a sample order for testing."""
    return {
        "id": str(uuid.uuid4()),
        "customer_name": "Test Customer",
        "customer_address": "123 Test St",
        "items": [
            {"name": "Test Item", "price": 10.0, "quantity": 2}
        ],
        "status": OrderStatus.PENDING.value
    }


class TestProcessOrderIntegration:

    @patch("app_part_2.deliveries.ByCicle.api.ByCicleAPI")
    def test_process_order_success_path(self, mock_bycicle_api, mock_db, sample_order):
        """Test successful order processing end-to-end."""
        # Setup mock delivery service
        mock_api_instance = mock_bycicle_api.return_value
        mock_api_instance.register_order.return_value = {"status": OrderStatus.PROCESSING.value}

        # Process the order
        result = process_order(sample_order, mock_db)

        # Assert order was properly parsed
        assert isinstance(result, Order)
        assert result.id == sample_order["id"]
        
        # Assert order was saved to database
        db_order = mock_db.orders.find_one({"id": sample_order["id"]})
        assert db_order is not None
        
        # Assert delivery service was called
        mock_api_instance.register_order.assert_called_once()
        
        # Assert order status was updated based on delivery service response
        assert result.status == OrderStatus.PROCESSING
        assert db_order["status"] == OrderStatus.PROCESSING.value

    @patch("app_part_2.deliveries.ByCicle.api.ByCicleAPI")
    def test_process_order_delivery_failure(self, mock_bycicle_api, mock_db, sample_order):
        """Test order processing when delivery service fails."""
        # Setup mock delivery service to raise an error
        mock_api_instance = mock_bycicle_api.return_value
        mock_api_instance.register_order.side_effect = DeliveryRegistrationError("Delivery service down")

        # Process the order
        result = process_order(sample_order, mock_db)

        # Assert order was properly parsed
        assert isinstance(result, Order)
        assert result.id == sample_order["id"]
        
        # Assert order was saved to database
        db_order = mock_db.orders.find_one({"id": sample_order["id"]})
        assert db_order is not None
        
        # Assert delivery service was called
        mock_api_instance.register_order.assert_called_once()
        
        # Assert order status was updated to FAILED
        assert result.status == OrderStatus.FAILED
        assert db_order["status"] == OrderStatus.FAILED.value

    @patch("app_part_2.deliveries.ByCicle.api.ByCicleAPI")
    def test_process_order_with_invalid_status(self, mock_bycicle_api, mock_db, sample_order):
        """Test order processing when delivery service returns invalid status."""
        # Setup mock delivery service to return unexpected status
        mock_api_instance = mock_bycicle_api.return_value
        mock_api_instance.register_order.return_value = {"status": "UNKNOWN_STATUS"}

        # Process the order
        result = process_order(sample_order, mock_db)

        # Assert order was saved to database with default PROCESSING status
        db_order = mock_db.orders.find_one({"id": sample_order["id"]})
        assert db_order is not None
        assert result.status == OrderStatus.PROCESSING
        assert db_order["status"] == OrderStatus.PROCESSING.value

    @patch("app_part_2.deliveries.ByCicle.api.ByCicleAPI")
    def test_process_order_with_edge_case(self, mock_bycicle_api, mock_db):
        """Test order processing with edge case - empty items list."""
        # Create order with empty items list
        edge_case_order = {
            "id": str(uuid.uuid4()),
            "customer_name": "Edge Case",
            "customer_address": "456 Edge St",
            "items": [],  # Empty items list
            "status": OrderStatus.PENDING.value
        }
        
        # Setup mock delivery service
        mock_api_instance = mock_bycicle_api.return_value
        mock_api_instance.register_order.return_value = {"status": OrderStatus.PROCESSING.value}

        # Process the order
        result = process_order(edge_case_order, mock_db)

        # Assert order was processed correctly despite empty items
        assert isinstance(result, Order)
        assert result.items == []
        assert result.status == OrderStatus.PROCESSING


@pytest.fixture
def real_db_integration(monkeypatch):
    """
    This fixture would set up a test database instead of mocking.
    Only use for true end-to-end integration tests when needed.
    """
    # For true integration tests, you would:
    # 1. Set up a test MongoDB instance
    # 2. Configure app to use test database
    # 3. Tear down/clean up after tests
    
    # This is just a placeholder showing how you might structure this
    test_client = mongomock.MongoClient()
    test_db = test_client.get_database("config")
    
    def mock_get_db():
        return test_db
        
    # Replace the actual get_db function
    monkeypatch.setattr("app_part_2.db.db.get_db", mock_get_db)
    
    return test_db