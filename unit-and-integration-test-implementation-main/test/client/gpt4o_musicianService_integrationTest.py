import pytest
from unittest.mock import MagicMock
from src.service.musician_service import MusicianService
from src.repository.musician_repository import MusicianRepository
from src.client.external_musician_client import ExternalMusicianClient
from src.client.postgres_client import PostgresClient
from src.config.postgres_client_config import PostgresClientConfig
from src.config.external_musicians_client_config import ExternalMusicianClientConfig
from src.model.musician import Musician

@pytest.fixture
def postgres_client_config():
    return PostgresClientConfig(url="localhost", port="5432", database="test_db", user_name="user", password="password")

@pytest.fixture
def external_musician_client_config():
    return ExternalMusicianClientConfig(url="http://localhost", port=8000)

@pytest.fixture
def postgres_client(postgres_client_config):
    client = PostgresClient(postgres_client_config)
    client.__create_connection = MagicMock()
    return client

@pytest.fixture
def external_musician_client(external_musician_client_config):
    client = ExternalMusicianClient(external_musician_client_config)
    client.get_all_musicians_names = MagicMock(return_value=["John", "Paul"])
    return client

@pytest.fixture
def musician_repository(postgres_client):
    repository = MusicianRepository(postgres_client)
    repository.get_musician = MagicMock(return_value=Musician(name="John", surname="Doe", age=30, instrument="Guitar"))
    repository.get_musicians_by_names = MagicMock(return_value=[
        Musician(name="John", surname="Doe", age=30, instrument="Guitar"),
        Musician(name="Paul", surname="Smith", age=28, instrument="Bass")
    ])
    repository.save = MagicMock()
    return repository

@pytest.fixture
def musician_service(musician_repository, external_musician_client):
    return MusicianService(musician_repository, external_musician_client)

def test_get_musician_by_name(musician_service):
    musician = musician_service.get_musician_by_name("John")
    assert musician.name == "John"
    assert musician.surname == "Doe"

def test_get_all_musicians(musician_service):
    musicians = musician_service.get_all_musicians()
    assert len(musicians) == 2
    assert musicians[0].name == "John"
    assert musicians[1].name == "Paul"

def test_save_musician(musician_service, musician_repository):
    musician_service.save(name="George", surname="Harrison", age=27, instrument="Guitar")
    musician_repository.save.assert_called_once()