import unittest
import pytest
import requests
from unittest.mock import patch

from src.config.external_musicians_client_config import ExternalMusicianClientConfig
from src.client.external_musician_client import ExternalMusicianClient
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass


class TestExternalMusicianClientIntegration(AbstractIntegrationTestClass):
    """
    Integration tests for ExternalMusicianClient to verify interactions
    between components and external dependencies.
    """

    @classmethod
    def setup_class(cls):
        """Set up test environment with mock server and database."""
        super().setup()

    @classmethod
    def teardown_class(cls):
        """Clean up resources after tests."""
        super().tear_down()

    def test_get_all_musicians_names_success_path(self):
        """
        Test the success path of fetching musician names through the client.
        Verifies the full integration between configuration, client, and mock server.
        """
        # Create client with configuration from the abstract test class
        client_config = ExternalMusicianClientConfig(url=self.client_url, port=self.client_port)
        musician_client = ExternalMusicianClient(config=client_config)

        # Execute the method that interacts with the mock server
        musician_names = musician_client.get_all_musicians_names()

        # Assert the response matches what we configured in the mock server
        assert isinstance(musician_names, list)
        assert len(musician_names) == 3
        assert "kurt" in musician_names
        assert "jim" in musician_names
        assert "noel" in musician_names

    def test_client_with_different_config(self):
        """
        Test that client correctly uses the provided configuration.
        Uses a different port to verify configuration is properly applied.
        """
        # Setup a different configuration
        alternative_port = 8082
        self.server.add_json_response("/fetch-all-names", {"musician_names": ["alternative", "musicians"]}, port=alternative_port)
        
        # Create client with alternative configuration
        client_config = ExternalMusicianClientConfig(url=self.client_url, port=alternative_port)
        musician_client = ExternalMusicianClient(config=client_config)
        
        # Execute the method
        musician_names = musician_client.get_all_musicians_names()
        
        # Assert the response matches what we configured in the alternative mock server
        assert "alternative" in musician_names
        assert "musicians" in musician_names
        assert len(musician_names) == 2

    def test_client_handles_connection_error(self):
        """
        Test that client properly handles connection errors when the server is unreachable.
        """
        # Setup client with incorrect port/url
        invalid_port = 9999  # A port where no server is running
        client_config = ExternalMusicianClientConfig(url=self.client_url, port=invalid_port)
        musician_client = ExternalMusicianClient(config=client_config)
        
        # Expect a ConnectionError when trying to connect to a non-existent server
        with pytest.raises(requests.exceptions.ConnectionError):
            musician_client.get_all_musicians_names()

    def test_client_handles_invalid_response(self):
        """
        Test that client handles unexpected response formats from the server.
        """
        # Setup mock server with invalid response (missing required key)
        self.server.add_json_response("/fetch-all-names", {"wrong_key": ["data"]}, port=8083)
        
        client_config = ExternalMusicianClientConfig(url=self.client_url, port=8083)
        musician_client = ExternalMusicianClient(config=client_config)
        
        # Expect a KeyError when the response doesn't contain expected data
        with pytest.raises(KeyError):
            musician_client.get_all_musicians_names()


if __name__ == "__main__":
    unittest.main()