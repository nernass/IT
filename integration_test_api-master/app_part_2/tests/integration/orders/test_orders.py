from http import HTTPStatus
from uuid import uuid1

import responses
from fastapi.testclient import TestClient
from pymongo.database import Database

from enums import OrderStatus
from unittest import mock


def test_get_orders(test_client: TestClient) -> None:
    response = test_client.get(url="/orders")

    assert response.status_code == HTTPStatus.OK
    assert not len(response.json())


def test_make_order(
    test_client: TestClient,
    database: Database,
    test_order: dict,
    mocked_requests: responses.RequestsMock,
) -> None:
    order_id = str(uuid1())
    test_order["id"] = order_id
    mocked_requests.get(
        url="https://bycicle-service.com/login",
        status=HTTPStatus.OK,
        json={"token": "bearer_token"},
    )
    mocked_requests.post(
        url=f"https://bycicle-service.com/register-order/{order_id}",
        status=HTTPStatus.OK,
        json={"status": "REGISTERED"},
    )
    response = test_client.post("/order-new", json=test_order)

    assert response.status_code == HTTPStatus.CREATED
    assert database.orders.find_one({"id": order_id})


def test_new_order_delivery_registration_error(
    test_client: TestClient,
    database: Database,
    test_order: dict,
    mocked_requests: responses.RequestsMock,
) -> None:
    order_id = str(uuid1())
    test_order["id"] = order_id
    mocked_requests.post(
        url=f"https://bycicle-service.com/register-order/{order_id}",
        status=HTTPStatus.BAD_REQUEST,
        json={
            "error_message": "ByCicle service is not available, too many orders",
            "status": "REJECTED",
        },
    )
    response = test_client.post("/order-new", json=test_order)

    assert response.status_code == HTTPStatus.BAD_REQUEST
    order = database.orders.find_one({"id": order_id})
    assert order.get("status") == OrderStatus.FAILED


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
