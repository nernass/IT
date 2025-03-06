from fastapi import FastAPI

from enums import OrderStatus
from exceptions.common import DeliveryRegistrationError
from models.models import Order
from routers.orders import orders_router
from deliveries.ByCicle.api import ByCicleAPI

app = FastAPI()

app.include_router(orders_router)


@app.get("/")
def read_root():
    return {"hello": "world"}


def process_order(raw_order: dict, db) -> Order:
    """
    Main process order function.

    1. get raw order from request
    2. parse order to Order model
    3. save order to database
    4. send order to delivery service
    5. update order status according to delivery service response
    ...
    """
    delivery_service = ByCicleAPI()

    parsed_order = Order.parse_obj(raw_order)
    db.orders.insert_one(parsed_order.dict())

    try:
        delivery_status: dict = delivery_service.register_order(parsed_order)
    except DeliveryRegistrationError:
        db.orders.update_one(
            {"id": parsed_order.id}, {"$set": {"status": OrderStatus.FAILED}}
        )
        parsed_order.status = OrderStatus.FAILED
        return parsed_order

    db.orders.update_one(
        {"id": parsed_order.id},
        {"$set": {"status": delivery_status.get("status", OrderStatus.PROCESSING)}},
    )
    parsed_order.status = OrderStatus(
        delivery_status.get("status", OrderStatus.PROCESSING)
    )

    return parsed_order
