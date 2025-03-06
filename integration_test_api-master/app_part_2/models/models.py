from pydantic import BaseModel

from enums import OrderStatus


class Item(BaseModel):
    title: str
    description: str | None
    quantity: int
    price: float


class Order(BaseModel):
    id: str
    status: OrderStatus
    items: list[Item]
    total_price: float
