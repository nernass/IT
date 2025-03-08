import pytest
from unittest.mock import patch, MagicMock
import pandas as pd
from src.client.postgres_client import PostgresClient
from src.config.postgres_client_config import PostgresClientConfig
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass

class TestPostgresClientIntegration(AbstractIntegrationTestClass):

    @classmethod
    def setup_class(cls):
        super().setup()

    @classmethod
    def teardown_class(cls):
        super().tear_down()

    @patch('psycopg2.connect')
    def test_retrieve_musician_success(self, mock_connect):
        # Mock the database connection and query result
        mock_connection = MagicMock()
        mock_connect.return_value = mock_connection
        mock_cursor = mock_connection.cursor.return_value
        mock_cursor.fetchall.return_value = [('kurt', 'cobain', 27, 'guitar')]

        # Initialize PostgresClient
        postgres_client = PostgresClient(self.postgres_client_config)

        # Call the method and assert the result
        result = postgres_client.retrieve_musician('kurt')
        expected_result = pd.DataFrame([{'name': 'kurt', 'surname': 'cobain', 'age': 27, 'instrument': 'guitar'}])
        pd.testing.assert_frame_equal(result, expected_result)

    @patch('psycopg2.connect')
    def test_retrieve_musicians_success(self, mock_connect):
        # Mock the database connection and query result
        mock_connection = MagicMock()
        mock_connect.return_value = mock_connection
        mock_cursor = mock_connection.cursor.return_value
        mock_cursor.fetchall.return_value = [
            ('kurt', 'cobain', 27, 'guitar'),
            ('jim', 'morrison', 27, 'vocals'),
            ('noel', 'gallagher', 53, 'guitar')
        ]

        # Initialize PostgresClient
        postgres_client = PostgresClient(self.postgres_client_config)

        # Call the method and assert the result
        result = postgres_client.retrieve_musicians(['kurt', 'jim', 'noel'])
        expected_result = pd.DataFrame([
            {'name': 'kurt', 'surname': 'cobain', 'age': 27, 'instrument': 'guitar'},
            {'name': 'jim', 'surname': 'morrison', 'age': 27, 'instrument': 'vocals'},
            {'name': 'noel', 'surname': 'gallagher', 'age': 53, 'instrument': 'guitar'}
        ])
        pd.testing.assert_frame_equal(result, expected_result)

    @patch('psycopg2.connect')
    def test_save_musician_success(self, mock_connect):
        # Mock the database connection and cursor
        mock_connection = MagicMock()
        mock_connect.return_value = mock_connection
        mock_cursor = mock_connection.cursor.return_value

        # Initialize PostgresClient
        postgres_client = PostgresClient(self.postgres_client_config)

        # Create a DataFrame to save
        musician_df = pd.DataFrame([{'name': 'kurt', 'surname': 'cobain', 'age': 27, 'instrument': 'guitar'}])

        # Call the method
        postgres_client.save(musician_df)

        # Assert the query execution
        mock_cursor.execute.assert_called_once_with(
            "INSERT INTO test.musician (name, surname, age, instrument) values('kurt','cobain','27', 'guitar');"
        )
        mock_connection.commit.assert_called_once()

    @patch('psycopg2.connect')
    def test_retrieve_musician_failure(self, mock_connect):
        # Mock the database connection to raise an exception
        mock_connect.side_effect = psycopg2.DatabaseError("Database connection error")

        # Initialize PostgresClient
        postgres_client = PostgresClient(self.postgres_client_config)

        # Call the method and assert the exception
        with pytest.raises(psycopg2.DatabaseError):
            postgres_client.retrieve_musician('kurt')

    @patch('psycopg2.connect')
    def test_save_musician_failure(self, mock_connect):
        # Mock the database connection and cursor
        mock_connection = MagicMock()
        mock_connect.return_value = mock_connection
        mock_cursor = mock_connection.cursor.return_value
        mock_cursor.execute.side_effect = psycopg2.DatabaseError("Insert error")

        # Initialize PostgresClient
        postgres_client = PostgresClient(self.postgres_client_config)

        # Create a DataFrame to save
        musician_df = pd.DataFrame([{'name': 'kurt', 'surname': 'cobain', 'age': 27, 'instrument': 'guitar'}])

        # Call the method and assert the exception
        with pytest.raises(psycopg2.DatabaseError):
            postgres_client.save(musician_df)