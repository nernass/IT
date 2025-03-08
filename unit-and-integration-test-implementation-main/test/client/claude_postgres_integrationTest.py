import pandas as pd
import pytest
from pandas.testing import assert_frame_equal

from src.client.postgres_client import PostgresClient
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass


class TestPostgresClientIntegration(AbstractIntegrationTestClass):

    @classmethod
    def setup_class(cls):
        super().setup()
        cls.postgres_client = PostgresClient(cls.postgres_client_config)

    @classmethod
    def teardown_class(cls):
        super().tear_down()

    def test_save_and_retrieve_musician(self):
        # Given
        test_data = pd.DataFrame({
            'name': ['john'],
            'surname': ['lennon'],
            'age': [40],
            'instrument': ['guitar']
        })

        # When
        self.postgres_client.save(test_data)
        result = self.postgres_client.retrieve_musician('john')

        # Then
        assert_frame_equal(test_data, result)

    def test_retrieve_multiple_musicians(self):
        # Given
        musicians = [
            {'name': 'paul', 'surname': 'mccartney', 'age': 38, 'instrument': 'bass'},
            {'name': 'george', 'surname': 'harrison', 'age': 35, 'instrument': 'guitar'}
        ]
        for musician in musicians:
            self.postgres_client.save(pd.DataFrame([musician]))

        # When
        result = self.postgres_client.retrieve_musicians(['paul', 'george'])

        # Then
        expected = pd.DataFrame(musicians)
        assert_frame_equal(expected, result.sort_index())

    def test_retrieve_nonexistent_musician(self):
        # When
        result = self.postgres_client.retrieve_musician('nonexistent')

        # Then
        assert result.empty

    @pytest.mark.xfail(raises=Exception)
    def test_save_invalid_data(self):
        # Given
        invalid_data = pd.DataFrame({
            'invalid_column': ['test']
        })

        # When/Then
        self.postgres_client.save(invalid_data)

    def test_connection_with_invalid_credentials(self):
        # Given
        invalid_config = self.postgres_client_config
        invalid_config.password = "wrong_password"
        invalid_client = PostgresClient(invalid_config)

        # When/Then
        with pytest.raises(Exception):
            invalid_client.retrieve_musician('test')