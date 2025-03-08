import pytest
from unittest.mock import Mock, patch
from src.client.external_musician_client import ExternalMusicianClient
from src.client.postgres_client import PostgresClient
from src.config.external_musicians_client_config import ExternalMusicianClientConfig
from src.config.postgres_client_config import PostgresClientConfig
from src.repository.musician_repository import MusicianRepository
from src.service.musician_service import MusicianService
from src.model.musician import Musician

@pytest.fixture
def external_musician_client():
    config = ExternalMusicianClientConfig(url="http://mockurl.com", port=8080)
    client = ExternalMusicianClient(config)
    return client

@pytest.fixture
def postgres_client():
    config = PostgresClientConfig(
        url="mockdburl",
        port="5432",
        database="testdb",
        user_name="testuser",
        password="testpass"
    )
    client = PostgresClient(config)
    return client

@pytest.fixture
def musician_repository(postgres_client):
    return MusicianRepository(postgres_client)

@pytest.fixture
def musician_service(musician_repository, external_musician_client):
    return MusicianService(musician_repository, external_musician_client)

def test_get_all_musicians_success(musician_service, external_musician_client, musician_repository):
    mock_musician_names = ["John", "Paul"]
    mock_musicians = [
        Musician(name="John", surname="Doe", age=30, instrument="Guitar"),
        Musician(name="Paul", surname="Smith", age=25, instrument="Drums")
    ]

    with patch.object(external_musician_client, 'get_all_musicians_names', return_value=mock_musician_names):
        with patch.object(musician_repository, 'get_musicians_by_names', return_value=mock_musicians):
            result = musician_service.get_all_musicians()
            assert result == mock_musicians

def test_get_all_musicians_partial_failure(musician_service, external_musician_client, musician_repository):
    mock_musician_names = ["John", "Paul"]

    with patch.object(external_musician_client, 'get_all_musicians_names', return_value=mock_musician_names):
        with patch.object(musician_repository, 'get_musicians_by_names', side_effect=Exception("Database error")):
            with pytest.raises(Exception, match="Database error"):
                musician_service.get_all_musicians()

def test_get_all_musicians_edge_case(musician_service, external_musician_client, musician_repository):
    mock_musician_names = []

    with patch.object(external_musician_client, 'get_all_musicians_names', return_value=mock_musician_names):
        with patch.object(musician_repository, 'get_musicians_by_names', return_value=[]):
            result = musician_service.get_all_musicians()
            assert result == []