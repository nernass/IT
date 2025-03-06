from fastapi import FastAPI

from routers.orders import orders_router

app = FastAPI()

app.include_router(orders_router)


@app.get("/")
def read_root():
    return {"hello": "world"}
