import unittest

from webtest import TestApp
from thespian.actors import ActorSystem

from nonamedb.api.Service import Service
from nonamedb.storage.engines.memory.MemoryEngine import MemoryEngine


class ServiceSpec(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()
        cls.__test_key = "test key"
        cls.__test_value = b"test value"
        cls.__updated_test_value = bytes("test updated value", "utf-8")
        cls.__system = ActorSystem()
        cls.__engine = MemoryEngine(cls.__system, request_timeout=3)
        cls.__service = Service(cls.__engine, bind_address="localhost", bind_port=9090)
        cls.__app = TestApp(cls.__service._app)

    def test_should_reject_invalid_requests(self):
        self.assertEqual(self.__app.get(url='/', expect_errors=True).status_code, 404)

    def test_should_fail_to_retrieve_missing_data(self):
        self.assertEqual(self.__app.get(url="/" + self.__test_key, expect_errors=True).status_code, 404)

    def test_should_successfully_add_and_retrieve_data(self):
        put_result = self.__app.put(url="/" + self.__test_key, params=self.__test_value)
        self.assertEqual(put_result.status_code, 200)

        get_result = self.__app.get(url="/" + self.__test_key)
        self.assertEqual(get_result.status_code, 200)
        self.assertEqual(get_result.body, self.__test_value)

    def test_should_successfully_retrieve_updated_data(self):
        put_result = self.__app.put(url="/" + self.__test_key, params=self.__updated_test_value)
        self.assertEqual(put_result.status_code, 200)

        get_result = self.__app.get(url="/" + self.__test_key)
        self.assertEqual(get_result.status_code, 200)
        self.assertEqual(get_result.body, self.__updated_test_value)

    def test_should_fail_to_retrieve_deleted_data(self):
        self.assertEqual(self.__app.delete(url="/" + self.__test_key).status_code, 200)
        self.assertEqual(self.__app.get(url="/" + self.__test_key, expect_errors=True).status_code, 404)
