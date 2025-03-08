import pytest
import pandas as pd
from unittest.mock import Mock, patch, MagicMock
from src.config.external_musicians_client_config import ExternalMusicianClientConfig
from src.config.postgres_client_config import PostgresClientConfig
from src.client.external_musician_client import ExternalMusicianClient
from src.client.postgres_client import PostgresClient
from src.repository.musician_repository import MusicianRepository
from src.service.musician_service import MusicianService
from src.model.musician import Musician


class TestMusicianIntegration:
    @pytest.fixture
    def setup_components(self):
        # Configure mocks
        self.mock_external_config = ExternalMusicianClientConfig(url="http://localhost", port=8080)
        self.mock_postgres_config = PostgresClientConfig(
            url="localhost",
            port="5432",
            database="test_db",
            user_name="test_user",
            password="test_password"
        )
        
        # Create real components but with mocked external dependencies
        with patch('requests.get') as mock_requests_get:
            # Mock the external API response
            mock_response = Mock()
            mock_response.json.return_value = {"musician_names": ["John", "Paul", "George"]}
            mock_requests_get.return_value = mock_response
            
            # Create external musician client with real config
            self.external_musician_client = ExternalMusicianClient(self.mock_external_config)
        
        with patch('psycopg2.connect') as mock_db_connect:
            # Mock the database connection and cursor
            mock_connection = MagicMock()
            mock_cursor = MagicMock()
            mock_connection.cursor.return_value = mock_cursor
            mock_db_connect.return_value = mock_connection
            
            # Create postgres client with real config
            self.postgres_client = PostgresClient(self.mock_postgres_config)
            
            # Mock the pd.read_sql function
            self.mock_musician_data = pd.DataFrame({
                'name': ['John'],
                'surname': ['Lennon'],
                'age': [40],
                'instrument': ['Guitar']
            })
            
            self.mock_musicians_data = pd.DataFrame({
                'name': ['John', 'Paul', 'George'],
                'surname': ['Lennon', 'McCartney', 'Harrison'],
                'age': [40, 38, 37],
                'instrument': ['Guitar', 'Bass', 'Guitar']
            })
        
        # Create repository with real postgres client
        self.musician_repository = MusicianRepository(self.postgres_client)
        
        # Create service with real repository and client
        self.musician_service = MusicianService(self.musician_repository, self.external_musician_client)
        
        return self.musician_service

    def test_get_musician_by_name_integration(self, setup_components):
        with patch('pandas.read_sql', return_value=self.mock_musician_data):
            # Test the integration flow from service -> repository -> postgres client
            musician = self.musician_service.get_musician_by_name("John")
            
            # Verify the musician was retrieved correctly
            assert musician is not None
            assert musician.name == "John"
            assert musician.surname == "Lennon"
            assert musician.age == 40
            assert musician.instrument == "Guitar"
    
    def test_get_all_musicians_integration(self, setup_components):
        with patch('pandas.read_sql', return_value=self.mock_musicians_data):
            # Test the integration flow from service -> external client -> repository -> postgres client
            musicians = self.musician_service.get_all_musicians()
            
            # Verify all musicians were retrieved correctly
            assert len(musicians) == 3
            assert musicians[0].name == "John"
            assert musicians[1].name == "Paul"
            assert musicians[2].name == "George"
    
    def test_save_musician_integration(self, setup_components):
        with patch.object(self.postgres_client, 'save') as mock_save:
            # Test the integration flow for saving a musician
            self.musician_service.save(
                name="Ringo",
                surname="Starr",
                age=42,
                instrument="Drums"
            )
            
            # Verify the save method was called with the correct data
            mock_save.assert_called_once()
            # Extract the dataframe that was passed to save
            saved_df = mock_save.call_args[0][0]
            assert saved_df.iloc[0]['name'] == "Ringo"
            assert saved_df.iloc[0]['surname'] == "Starr"
            assert saved_df.iloc[0]['age'] == 42
            assert saved_df.iloc[0]['instrument'] == "Drums"
    
    def test_database_connection_error_handling(self, setup_components):
        with patch('psycopg2.connect', side_effect=Exception("Database connection error")):
            with pytest.raises(Exception) as exc_info:
                # Force a database connection error and see how it propagates
                self.musician_service.get_musician_by_name("John")
            
            # Verify the error was properly propagated
            assert "Database connection error" in str(exc_info.value)
    
    def test_external_api_error_handling(self, setup_components):
        with patch('requests.get', side_effect=Exception("API connection error")):
            with pytest.raises(Exception) as exc_info:
                # Force an API connection error and see how it propagates
                self.musician_service.get_all_musicians()
            
            # Verify the error was properly propagated
            assert "API connection error" in str(exc_info.value)