from http import HTTPStatus
from fastapi import APIRouter, Depends
from db.db import get_db
from models.models import Order
from schemas.order import orders_entity
from fastapi.responses import JSONResponse


orders_router = APIRouter()


@orders_router.get("/orders", status_code=HTTPStatus.OK)
def get_orders(db=Depends(get_db)) -> list:
    orders = db.orders.find()
    return orders_entity(orders)


@orders_router.post("/order-new")
def new_order(order: dict, db=Depends(get_db)) -> JSONResponse:
    """
    Receive order from frontend or any other resource.

    Args:
        order: raw order
        db: database

    Returns:
        HttpStatus, message

    """
    from main import process_order

    parsed_order: Order = process_order(order, db)
    if parsed_order.status.isFailed():
        return JSONResponse(
            status_code=HTTPStatus.BAD_REQUEST,
            content={"message": "Order was not processed. Please try again."},
        )
    return JSONResponse(
        status_code=HTTPStatus.CREATED,
        content={"message": "Successfully created"},
    )


@orders_router.patch("/order-status-update", status_code=HTTPStatus.NO_CONTENT)
def order_status_update(update: dict, db=Depends(get_db)):
    order_id: str = update.get("id", "")
    order_status: str = update.get("status", "")
    db.orders.update_one({"id": order_id}, {"$set": {"status": order_status}})
    return {"massage": "Order status has been updated"}
