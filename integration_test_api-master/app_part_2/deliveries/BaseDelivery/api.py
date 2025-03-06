from abc import ABC

import requests

from enums import RequestType
from exceptions.common import DeliveryRegistrationError
from models.models import Order


class BaseDeliveryAPI(ABC):
    def __init__(self) -> None:
        self._session = requests.sessions.Session()
        self._api_url = self._get_api_url()

    def _get_api_url(self) -> str:
        raise NotImplementedError()

    def get_auth_token(self) -> str:
        raise NotImplementedError()

    def perform_api_call(
        self,
        method: RequestType,
        url: str,
        payload: dict,
        **kwargs,
    ) -> requests.Response:
        headers = kwargs.get(
            "headers", {"Authorization": f"Bearer {self.get_auth_token()}"}
        )
        response = self._session.request(
            method=method.name,
            url=url,
            json=payload,
            headers=headers,
        )
        return response

    def register_order(self, order: Order) -> dict:
        url = f"{self._api_url}/register-order/{order.id}"
        response = self.perform_api_call(RequestType.POST, url, order.dict())
        result = response.json()
        if not response.ok:
            raise DeliveryRegistrationError(
                order_id=order.id,
                message=result.get("error_message", "Delivery registration error"),
            )
        return result

    def get_order_status(self, order_id: str) -> dict:
        url = f"{self._api_url}/order-status/{order_id}"
        response = self.perform_api_call(RequestType.GET, url, {})
        if not response.ok:
            return {}
        return response.json()
