from http import HTTPStatus
from fastapi import APIRouter, Depends
from db.db import get_db
from models.models import Order
from schemas.order import orders_entity

orders_router = APIRouter()


@orders_router.get("/orders", status_code=HTTPStatus.OK)
def get_orders(db=Depends(get_db)) -> list:
    orders = db.orders.find()
    return orders_entity(orders)


@orders_router.post("/orders", status_code=HTTPStatus.CREATED)
def process_order(order: dict, db=Depends(get_db)) -> Order:
    parsed_order = Order.parse_obj(order)
    db.orders.insert_one(parsed_order.dict())
    return parsed_order


@orders_router.patch("/order-status-update", status_code=HTTPStatus.NO_CONTENT)
def order_status_update(update: dict, db=Depends(get_db)):
    order_id: str = update.get("id", "")
    order_status: str = update.get("status", "")
    db.orders.update_one({"id": order_id}, {"$set": {"status": order_status}})
    return {"massage": "Order status has been updated"}
