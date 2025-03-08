import pytest
from unittest.mock import patch
from src.client.external_musician_client import ExternalMusicianClient
from src.config.external_musicians_client_config import ExternalMusicianClientConfig
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass


class TestExternalMusicianClient(AbstractIntegrationTestClass):

    @classmethod
    def setup_class(cls):
        super().setup_class()
        cls.client_config = ExternalMusicianClientConfig(url=cls.client_url, port=cls.client_port)
        cls.client = ExternalMusicianClient(config=cls.client_config)

    @classmethod
    def teardown_class(cls):
        super().tear_down()

    def test_get_all_musicians_names_success(self):
        expected_names = ["kurt", "jim", "noel"]
        actual_names = self.client.get_all_musicians_names()
        assert actual_names == expected_names

    @patch('src.client.external_musician_client.requests.get')
    def test_get_all_musicians_names_failure(self, mock_get):
        mock_get.side_effect = Exception("API failure")
        with pytest.raises(Exception) as excinfo:
            self.client.get_all_musicians_names()
        assert "API failure" in str(excinfo.value)

    def test_get_all_musicians_names_edge_case_empty(self):
        self.server.add_json_response("/fetch-all-names", dict(musician_names=[]))
        actual_names = self.client.get_all_musicians_names()
        assert actual_names == []

    def test_get_all_musicians_names_edge_case_large_input(self):
        large_input = ["musician" + str(i) for i in range(1000)]
        self.server.add_json_response("/fetch-all-names", dict(musician_names=large_input))
        actual_names = self.client.get_all_musicians_names()
        assert actual_names == large_input