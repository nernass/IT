def order_entity(item: dict) -> dict:
    return {
        "id": str(item["_id"]),
        "external_id": item["id"],
        "items": item["items"],
        "status": item["status"],
        "total_price": item["total_price"],
    }


def orders_entity(entity) -> list[dict]:
    return [order_entity(i) for i in entity]
