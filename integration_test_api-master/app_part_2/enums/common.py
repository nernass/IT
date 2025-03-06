import enum


class RequestType(enum.IntEnum):
    GET = 1
    POST = 2
    PUT = 3
    DELETE = 4


class OrderStatus(enum.StrEnum):
    NEW = "NEW"
    PROCESSING = "PROCESSING"
    DONE = "DONE"
    CANCELLED = "CANCELLED"
    REJECTED = "REJECTED"
    FAILED = "FAILED"
    REGISTERED = "REGISTERED"

    def __missing__(self, key):
        return self.NEW

    def isFailed(self) -> bool:
        return self.value in [self.CANCELLED, self.REJECTED, self.FAILED]
