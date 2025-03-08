import pytest

from src.client.external_musician_client import ExternalMusicianClient
from src.config.external_musicians_client_config import ExternalMusicianClientConfig
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass


class TestExternalMusicianClientIntegration(AbstractIntegrationTestClass):
    
    @classmethod
    def setup_class(cls):
        cls.setup()
    
    @classmethod
    def teardown_class(cls):
        cls.tear_down()
    
    def setup_method(self):
        # Reset mock server for each test if needed
        self.server.reset()
        self.server.add_json_response("/fetch-all-names", dict(musician_names=["kurt", "jim", "noel"]))
    
    def test_get_all_musicians_names_success(self):
        # Arrange
        client = ExternalMusicianClient(self.client_config)
        
        # Act
        result = client.get_all_musicians_names()
        
        # Assert
        assert isinstance(result, list)
        assert len(result) == 3
        assert "kurt" in result
        assert "jim" in result
        assert "noel" in result
    
    def test_get_all_musicians_names_empty_response(self):
        # Arrange
        self.server.reset()
        self.server.add_json_response("/fetch-all-names", dict(musician_names=[]))
        client = ExternalMusicianClient(self.client_config)
        
        # Act
        result = client.get_all_musicians_names()
        
        # Assert
        assert isinstance(result, list)
        assert len(result) == 0
    
    def test_get_all_musicians_names_server_error(self):
        # Arrange
        self.server.reset()
        self.server.add_error_response("/fetch-all-names", status_code=500)
        client = ExternalMusicianClient(self.client_config)
        
        # Act & Assert
        with pytest.raises(requests.exceptions.HTTPError):
            client.get_all_musicians_names()
    
    def test_client_configuration(self):
        # Arrange
        custom_url = "http://testserver"
        custom_port = 9999
        config = ExternalMusicianClientConfig(url=custom_url, port=custom_port)
        
        # Act & Assert
        assert config.url == custom_url
        assert config.port == custom_port
        
        # Create a client with the custom config to verify it sets values properly
        client = ExternalMusicianClient(config)
        # This will fail as the server doesn't exist, but we can check the URL being formed
        with pytest.raises(requests.exceptions.ConnectionError):
            client.get_all_musicians_names()