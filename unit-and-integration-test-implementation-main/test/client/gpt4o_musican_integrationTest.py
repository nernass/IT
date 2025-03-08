import pytest
from src.client.external_musician_client import ExternalMusicianClient
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass

class TestExternalMusicianClient(AbstractIntegrationTestClass):

    @classmethod
    def setup_class(cls):
        cls.setup()
        cls.client_config = cls.client_config

    @classmethod
    def teardown_class(cls):
        cls.tear_down()

    def test_get_all_musicians_names(self):
        client = ExternalMusicianClient(config=self.client_config)
        musician_names = client.get_all_musicians_names()
        assert musician_names == ["kurt", "jim", "noel"]

    def test_get_all_musicians_names_error_handling(self):
        self.server.add_json_response("/fetch-all-names", dict(error="Not Found"), status=404)
        client = ExternalMusicianClient(config=self.client_config)
        with pytest.raises(KeyError):
            client.get_all_musicians_names()