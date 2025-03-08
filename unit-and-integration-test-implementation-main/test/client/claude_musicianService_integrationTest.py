import pytest
from unittest.mock import patch
import pandas as pd

from src.service.musician_service import MusicianService
from src.repository.musician_repository import MusicianRepository
from src.client.postgres_client import PostgresClient
from src.client.external_musician_client import ExternalMusicianClient
from src.config.postgres_client_config import PostgresClientConfig
from src.config.external_musicians_client_config import ExternalMusicianClientConfig
from src.model.musician import Musician

@pytest.fixture
def postgres_config():
    return PostgresClientConfig(
        url="localhost",
        port="5432",
        database="test_db",
        user_name="test_user",
        password="test_pass"
    )

@pytest.fixture
def external_config():
    return ExternalMusicianClientConfig(
        url="http://localhost",
        port=8080
    )

@pytest.fixture
def postgres_client(postgres_config):
    return PostgresClient(postgres_config)

@pytest.fixture
def external_client(external_config):
    return ExternalMusicianClient(external_config)

@pytest.fixture
def musician_repository(postgres_client):
    return MusicianRepository(postgres_client)

@pytest.fixture
def musician_service(musician_repository, external_client):
    return MusicianService(musician_repository, external_client)

@pytest.fixture
def sample_musician():
    return Musician(
        name="John",
        surname="Doe",
        age=30,
        instrument="Guitar"
    )

@pytest.mark.integration
class TestMusicianIntegration:
    
    def test_save_and_retrieve_musician(self, musician_service, sample_musician):
        # Save musician
        musician_service.save(
            name=sample_musician.name,
            surname=sample_musician.surname,
            age=sample_musician.age,
            instrument=sample_musician.instrument
        )

        # Retrieve and verify
        retrieved_musician = musician_service.get_musician_by_name(sample_musician.name)
        assert retrieved_musician.name == sample_musician.name
        assert retrieved_musician.surname == sample_musician.surname
        assert retrieved_musician.age == sample_musician.age
        assert retrieved_musician.instrument == sample_musician.instrument

    @patch('requests.get')
    def test_get_all_musicians(self, mock_get, musician_service):
        # Mock external service response
        mock_get.return_value.json.return_value = {
            "musician_names": ["John", "Jane"]
        }

        # Mock database data
        with patch.object(PostgresClient, 'retrieve_musicians') as mock_retrieve:
            mock_df = pd.DataFrame({
                'name': ['John', 'Jane'],
                'surname': ['Doe', 'Smith'],
                'age': [30, 25],
                'instrument': ['Guitar', 'Piano']
            })
            mock_retrieve.return_value = mock_df

            # Get all musicians
            musicians = musician_service.get_all_musicians()
            
            assert len(musicians) == 2
            assert musicians[0].name == "John"
            assert musicians[1].name == "Jane"

    def test_get_nonexistent_musician(self, musician_service):
        with patch.object(PostgresClient, 'retrieve_musician') as mock_retrieve:
            mock_retrieve.return_value = pd.DataFrame()
            
            result = musician_service.get_musician_by_name("NonExistent")
            assert result is None

    @pytest.mark.parametrize("invalid_name", ["", " ", None])
    def test_get_musician_invalid_name(self, musician_service, invalid_name):
        with pytest.raises(ValueError):
            musician_service.get_musician_by_name(invalid_name)

    def test_database_connection_error(self, postgres_config):
        invalid_config = PostgresClientConfig(
            url="invalid_host",
            port="5432",
            database="invalid_db",
            user_name="invalid_user",
            password="invalid_pass"
        )
        
        client = PostgresClient(invalid_config)
        with pytest.raises(Exception):
            client.retrieve_musician("John")

    @patch('requests.get')
    def test_external_service_error(self, mock_get, musician_service):
        mock_get.side_effect = Exception("External service error")
        
        with pytest.raises(Exception):
            musician_service.get_all_musicians()