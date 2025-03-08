from threading import Thread
from uuid import uuid4

import requests
from flask import Flask, jsonify


class MockServer(Thread):
    def __init__(self, port: int, url: str):
        super().__init__()
        self.port = port
        self.app = Flask(__name__)
        self.url = url

        self.app.add_url_rule("/shutdown", view_func=self._shutdown_server)

    def shutdown_server(self):
        requests.get(f"{self.url}:{self.port}/shutdown")
        self.join()

    def add_callback_response(self, url, callback, methods=('GET',)):
        callback.__name__ = str(uuid4())  # change name of method to mitigate flask exception
        self.app.add_url_rule(url, view_func=callback, methods=methods)

    def add_json_response(self, url, serializable, methods=('GET',)):
        def callback():
            return jsonify(serializable)

        self.add_callback_response(url, callback, methods=methods)

    def run(self):
        self.app.run(port=self.port)

    @staticmethod
    def _shutdown_server():
        from flask import request
        if not 'werkzeug.server.shutdown' in request.environ:
            raise RuntimeError('Not running the development server')
        request.environ['werkzeug.server.shutdown']()
        return 'Server shutting down...'
