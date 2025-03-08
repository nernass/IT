import pytest

from src.client.external_musician_client import ExternalMusicianClient
from src.config.external_musicians_client_config import ExternalMusicianClientConfig
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass


class TestExternalMusicianClient(AbstractIntegrationTestClass):

    @classmethod
    def setup_class(cls):
        super().setup()
        cls.client_config = ExternalMusicianClientConfig(url=cls.client_url, port=cls.client_port)
        cls.musician_client = ExternalMusicianClient(cls.client_config)

    @classmethod
    def teardown_class(cls):
        super().tear_down()

    def test_get_all_musicians_names_returns_expected_list(self):
        # Given mock server is configured with response in setup
        expected_names = ["kurt", "jim", "noel"]

        # When
        actual_names = self.musician_client.get_all_musicians_names()

        # Then
        assert actual_names == expected_names

    def test_get_all_musicians_names_handles_empty_response(self):
        # Given
        self.server.add_json_response("/fetch-all-names", {"musician_names": []})

        # When
        result = self.musician_client.get_all_musicians_names()

        # Then
        assert result == []

    def test_get_all_musicians_names_with_invalid_url_raises_error(self):
        # Given
        invalid_config = ExternalMusicianClientConfig(url="http://invalid", port=self.client_port)
        invalid_client = ExternalMusicianClient(invalid_config)

        # When/Then
        with pytest.raises(requests.exceptions.ConnectionError):
            invalid_client.get_all_musicians_names()

    def test_get_all_musicians_names_with_invalid_response_format(self):
        # Given
        self.server.add_json_response("/fetch-all-names", {"invalid_key": []})

        # When/Then
        with pytest.raises(KeyError):
            self.musician_client.get_all_musicians_names()