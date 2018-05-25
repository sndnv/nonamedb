import unittest

from thespian.actors import ActorSystem
from nonamedb.storage.engines.memory.MemoryEngine import MemoryEngine


class MemoryEngineSpec(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        super().setUpClass()
        cls.__test_key = "some key"
        cls.__test_value = b"some value"
        cls.__updated_test_value = bytes("some updated value", "utf-8")
        cls.__system = ActorSystem()
        cls.__engine = MemoryEngine(cls.__system, request_timeout=3)

    def test_should_fail_to_retrieve_missing_data(self):
        self.assertEqual(self.__engine.get(self.__test_key), None)

    def test_should_successfully_add_and_retrieve_data(self):
        self.assertEqual(self.__engine.put(self.__test_key, self.__test_value), True)
        self.assertEqual(self.__engine.get(self.__test_key), self.__test_value)

    def test_should_successfully_update_and_retrieve_data(self):
        self.assertEqual(self.__engine.put(self.__test_key, self.__updated_test_value), True)
        self.assertEqual(self.__engine.get(self.__test_key), self.__updated_test_value)

    def test_should_successfully_remove_data(self):
        self.assertEqual(self.__engine.put(self.__test_key, bytes()), True)
        self.assertEqual(self.__engine.get(self.__test_key), None)

    def test_should_fail_to_add_invalid_data(self):
        self.assertEqual(self.__engine.put(self.__test_key, "some string value"), False)
        self.assertEqual(self.__engine.get(self.__test_key), None)
