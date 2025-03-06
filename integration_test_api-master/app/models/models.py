from pydantic import BaseModel


class OrderStatus:
    NEW: str = "NEW"
    PROCESSING: str = "PROCESSING"
    DONE: str = "DONE"


class Item(BaseModel):
    title: str
    description: str | None
    quantity: int
    price: float


class Order(BaseModel):
    id: str
    status: str
    items: list[Item]
    total_price: float
