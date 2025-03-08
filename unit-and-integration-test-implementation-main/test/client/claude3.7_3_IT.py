import pytest
import pandas as pd
from unittest.mock import patch, MagicMock
from typing import List

from src.model.musician import Musician
from src.service.musician_service import MusicianService
from src.repository.musician_repository import MusicianRepository
from src.client.postgres_client import PostgresClient
from src.client.external_musician_client import ExternalMusicianClient
from src.config.postgres_client_config import PostgresClientConfig
from src.config.external_musicians_client_config import ExternalMusicianClientConfig


@pytest.fixture
def postgres_client_config():
    return PostgresClientConfig(
        url="test_url",
        port="5432",
        database="test_db",
        user_name="test_user",
        password="test_password"
    )


@pytest.fixture
def external_musician_client_config():
    return ExternalMusicianClientConfig(
        url="http://test_url",
        port=8080
    )


@pytest.fixture
def postgres_client(postgres_client_config):
    with patch('psycopg2.connect') as mock_connect:
        # Mock the connection and cursor
        mock_connection = MagicMock()
        mock_cursor = MagicMock()
        mock_connect.return_value = mock_connection
        mock_connection.cursor.return_value = mock_cursor
        
        # Create and return the client
        client = PostgresClient(postgres_client_config)
        yield client


@pytest.fixture
def external_musician_client(external_musician_client_config):
    client = ExternalMusicianClient(external_musician_client_config)
    return client


@pytest.fixture
def musician_repository(postgres_client):
    return MusicianRepository(postgres_client)


@pytest.fixture
def musician_service(musician_repository, external_musician_client):
    return MusicianService(musician_repository, external_musician_client)


class TestMusicianServiceIntegration:
    
    @patch('pandas.read_sql')
    def test_get_musician_by_name_success(self, mock_read_sql, musician_service):
        # Arrange
        test_name = "John"
        mock_data = pd.DataFrame({
            'name': ['John'],
            'surname': ['Lennon'],
            'age': [40],
            'instrument': ['Guitar']
        })
        mock_read_sql.return_value = mock_data
        
        # Act
        result = musician_service.get_musician_by_name(test_name)
        
        # Assert
        assert result is not None
        assert result.name == "John"
        assert result.surname == "Lennon"
        assert result.age == 40
        assert result.instrument == "Guitar"
    
    @patch('pandas.read_sql')
    def test_get_musician_by_name_not_found(self, mock_read_sql, musician_service):
        # Arrange
        test_name = "Unknown"
        mock_read_sql.return_value = pd.DataFrame()
        
        # Act
        result = musician_service.get_musician_by_name(test_name)
        
        # Assert
        assert result is None
    
    @patch('requests.get')
    @patch('pandas.read_sql')
    def test_get_all_musicians(self, mock_read_sql, mock_requests_get, musician_service):
        # Arrange
        musician_names = ["John", "Paul", "George"]
        mock_response = MagicMock()
        mock_response.json.return_value = {"musician_names": musician_names}
        mock_requests_get.return_value = mock_response
        
        mock_data = pd.DataFrame({
            'name': ['John', 'Paul', 'George'],
            'surname': ['Lennon', 'McCartney', 'Harrison'],
            'age': [40, 38, 35],
            'instrument': ['Guitar', 'Bass', 'Guitar']
        })
        mock_read_sql.return_value = mock_data
        
        # Act
        result = musician_service.get_all_musicians()
        
        # Assert
        assert len(result) == 3
        assert all(isinstance(musician, Musician) for musician in result)
        assert result[0].name == "John"
        assert result[1].name == "Paul"
        assert result[2].name == "George"

    @patch('psycopg2.connect')
    def test_save_musician(self, mock_connect, musician_service):
        # Arrange
        mock_connection = MagicMock()
        mock_cursor = MagicMock()
        mock_connect.return_value = mock_connection
        mock_connection.cursor.return_value = mock_cursor
        
        # Act
        musician_service.save(
            name="Ringo",
            surname="Starr",
            age=42,
            instrument="Drums"
        )
        
        # Assert
        mock_cursor.execute.assert_called_once()
        mock_connection.commit.assert_called_once()
        
    @patch('requests.get')
    def test_external_client_integration(self, mock_requests_get, external_musician_client):
        # Arrange
        expected_names = ["John", "Paul", "George", "Ringo"]
        mock_response = MagicMock()
        mock_response.json.return_value = {"musician_names": expected_names}
        mock_requests_get.return_value = mock_response
        
        # Act
        result = external_musician_client.get_all_musicians_names()
        
        # Assert
        assert result == expected_names
        mock_requests_get.assert_called_once_with("http://test_url:8080/fetch-all-names")
        
    @patch('pandas.read_sql')
    def test_postgres_client_retrieve_musicians(self, mock_read_sql, postgres_client):
        # Arrange
        musician_names = ["John", "Paul"]
        mock_data = pd.DataFrame({
            'name': ['John', 'Paul'],
            'surname': ['Lennon', 'McCartney'],
            'age': [40, 38],
            'instrument': ['Guitar', 'Bass']
        })
        mock_read_sql.return_value = mock_data
        
        # Act
        result = postgres_client.retrieve_musicians(musician_names)
        
        # Assert
        assert not result.empty
        assert len(result) == 2
        assert list(result['name']) == ['John', 'Paul']