import pytest
import pandas as pd
from unittest.mock import Mock, patch
from src.client.external_musician_client import ExternalMusicianClient
from src.client.postgres_client import PostgresClient
from src.config.external_musicians_client_config import ExternalMusicianClientConfig
from src.config.postgres_client_config import PostgresClientConfig
from src.repository.musician_repository import MusicianRepository
from src.service.musician_service import MusicianService
from src.model.musician import Musician

class TestMusicianIntegration:
    @pytest.fixture
    def setup_components(self):
        # Configure external components
        external_config = ExternalMusicianClientConfig(url="http://test-api", port=8080)
        postgres_config = PostgresClientConfig(
            url="localhost",
            port="5432",
            database="test_db",
            user_name="test_user",
            password="test_pass"
        )
        
        # Create real components with mocked external dependencies
        with patch('requests.get') as mock_requests, \
             patch('psycopg2.connect') as mock_db_connect:
            
            # Mock external API response
            mock_requests.return_value.json.return_value = {
                "musician_names": ["John", "Paul"]
            }
            
            # Mock database connection and cursor
            mock_cursor = Mock()
            mock_connection = Mock()
            mock_connection.cursor.return_value = mock_cursor
            mock_db_connect.return_value = mock_connection
            
            # Create component chain
            external_client = ExternalMusicianClient(external_config)
            postgres_client = PostgresClient(postgres_config)
            musician_repository = MusicianRepository(postgres_client)
            musician_service = MusicianService(musician_repository, external_client)
            
            yield {
                'service': musician_service,
                'repository': musician_repository,
                'postgres_client': postgres_client,
                'external_client': external_client,
                'mock_cursor': mock_cursor,
                'mock_connection': mock_connection
            }

    def test_get_all_musicians_integration(self, setup_components):
        # Arrange
        components = setup_components
        
        # Mock database query response
        test_data = pd.DataFrame({
            'name': ['John', 'Paul'],
            'surname': ['Lennon', 'McCartney'],
            'age': [40, 42],
            'instrument': ['Guitar', 'Bass']
        })
        
        with patch('pandas.read_sql', return_value=test_data):
            # Act
            musicians = components['service'].get_all_musicians()
            
            # Assert
            assert len(musicians) == 2
            assert isinstance(musicians[0], Musician)
            assert musicians[0].name == 'John'
            assert musicians[1].name == 'Paul'

    def test_save_musician_integration(self, setup_components):
        # Arrange
        components = setup_components
        
        # Act
        components['service'].save(
            name="George",
            surname="Harrison",
            age=43,
            instrument="Guitar"
        )
        
        # Assert
        mock_cursor = components['mock_cursor']
        mock_connection = components['mock_connection']
        
        assert mock_cursor.execute.called
        assert mock_connection.commit.called
        
        # Verify the SQL query contains correct data
        execute_args = mock_cursor.execute.call_args[0][0]
        assert "George" in execute_args
        assert "Harrison" in execute_args
        assert "43" in execute_args
        assert "Guitar" in execute_args

    def test_get_musician_by_name_integration(self, setup_components):
        # Arrange
        components = setup_components
        
        test_data = pd.DataFrame({
            'name': ['Ringo'],
            'surname': ['Starr'],
            'age': [44],
            'instrument': ['Drums']
        })
        
        with patch('pandas.read_sql', return_value=test_data):
            # Act
            musician = components['service'].get_musician_by_name("Ringo")
            
            # Assert
            assert isinstance(musician, Musician)
            assert musician.name == 'Ringo'
            assert musician.surname == 'Starr'
            assert musician.age == 44
            assert musician.instrument == 'Drums'

    def test_error_handling_integration(self, setup_components):
        # Arrange
        components = setup_components
        components['mock_cursor'].execute.side_effect = Exception("Database error")
        
        # Act & Assert
        with pytest.raises(Exception):
            components['service'].save(
                name="Error",
                surname="Test",
                age=30,
                instrument="Piano"
            )
            
        # Verify rollback was called
        assert components['mock_connection'].rollback.called