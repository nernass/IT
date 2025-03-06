from http import HTTPStatus
from uuid import uuid1
from fastapi.testclient import TestClient
from pymongo.database import Database
from models.models import OrderStatus


def test_get_orders(test_client: TestClient) -> None:
    response = test_client.get(url="/orders")

    assert response.status_code == HTTPStatus.OK
    assert not len(response.json())


def test_make_order(
    test_client: TestClient, database: Database, test_order: dict
) -> None:
    order_id = str(uuid1())
    test_order["id"] = order_id
    response = test_client.post("/orders", json=test_order)

    assert response.status_code == HTTPStatus.CREATED
    assert database.orders.find_one({"id": order_id})


def test_order_status_update(
    test_client: TestClient,
    database: Database,
    test_order: dict,
) -> None:
    # prepare test order
    order_id = str(uuid1())
    test_order["id"] = order_id

    response = test_client.post("/orders", json=test_order)

    # check that order was created
    assert response.status_code == HTTPStatus.CREATED
    assert database.orders.find_one({"id": order_id})

    status_update_payload = {
        "id": order_id,
        "status": OrderStatus.PROCESSING,
    }
    # status transition from NEW to PROCESSING
    response = test_client.patch("/order-status-update", json=status_update_payload)

    assert response.status_code == HTTPStatus.NO_CONTENT
    order = database.orders.find_one({"id": order_id})
    assert order.get("status") == OrderStatus.PROCESSING

    status_update_payload = {
        "id": order_id,
        "status": OrderStatus.DONE,
    }
    # status transition from PROCESSING to DONE
    response = test_client.patch("/order-status-update", json=status_update_payload)

    assert response.status_code == HTTPStatus.NO_CONTENT
    order = database.orders.find_one({"id": order_id})
    assert order.get("status") == OrderStatus.DONE
