from bottle import Bottle, request, response


class Service:

    def __init__(self, engine, bind_address, bind_port):
        self.__engine = engine
        self.__bind_address = bind_address
        self.__bind_port = bind_port
        self._app = Bottle()
        self._routing()

    def _routing(self):
        self._app.route(path="/<key>", method="GET", callback=self.get)
        self._app.route(path="/<key>", method="PUT", callback=self.put)
        self._app.route(path="/<key>", method="POST", callback=self.post)
        self._app.route(path="/<key>", method="DELETE", callback=self.delete)

    def start(self):
        self._app.run(host=self.__bind_address, port=self.__bind_port)

    def get(self, key):
        result = self.__engine.get(key)

        if result is not None:
            response.body = result.decode("utf-8")
        else:
            response.status = 404

        return response

    def put(self, key):
        data = request.body.getvalue()
        result = self.__engine.put(key, data)
        if not result:
            response.status = 500  # invalid value supplied to engine

        return response

    def post(self, key): return self.put(key)

    def delete(self, key):
        self.__engine.put(key, b'')
        return response
