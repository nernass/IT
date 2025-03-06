from typing import Dict, List


def order_entity(item: Dict) -> Dict:
    return {
        "id": str(item["_id"]),
        "external_id": item["id"],
        "items": item["items"],
        "status": item["status"],
        "total_price": item["total_price"],
    }


def orders_entity(entity) -> List[Dict]:
    return [order_entity(i) for i in entity]
