class DeliveryRegistrationError(Exception):
    """Raised when a delivery registration fails."""

    def __init__(self, order_id: str, message: str) -> None:
        self.order_id = order_id
        self.message = message
        super().__init__(message)
