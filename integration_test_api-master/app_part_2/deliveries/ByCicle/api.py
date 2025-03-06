"""
He is the delivery service API called ByCicle.
We are using it for delivery orders to customers of course by cycle.

"""
import requests

from deliveries.BaseDelivery.api import BaseDeliveryAPI


class ByCicleAPI(BaseDeliveryAPI):
    def _get_api_url(self) -> str:
        return "https://bycicle-service.com"

    def get_auth_token(self) -> str:
        response = requests.get(self._api_url + "/login")
        if not response.ok:
            raise Exception("Failed to get auth token")
        return response.json().get("token")
