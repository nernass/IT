import pytest
import pandas as pd
from src.client.postgres_client import PostgresClient
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass

class TestPostgresClient(AbstractIntegrationTestClass):

    @classmethod
    def setup_class(cls):
        cls.setup()
        cls.postgres_client = PostgresClient(cls.postgres_client_config)

    @classmethod
    def teardown_class(cls):
        cls.tear_down()

    def test_save_and_retrieve_musician(self):
        musician_data = {
            'name': ['John'],
            'surname': ['Doe'],
            'age': [30],
            'instrument': ['Guitar']
        }
        musician_df = pd.DataFrame(musician_data)

        # Save musician
        self.postgres_client.save(musician_df)

        # Retrieve musician
        retrieved_df = self.postgres_client.retrieve_musician('John')

        assert not retrieved_df.empty
        assert retrieved_df.iloc[0]['name'] == 'John'
        assert retrieved_df.iloc[0]['surname'] == 'Doe'
        assert retrieved_df.iloc[0]['age'] == 30
        assert retrieved_df.iloc[0]['instrument'] == 'Guitar'

    def test_retrieve_musicians(self):
        musician_names = ['kurt', 'jim', 'noel']
        retrieved_df = self.postgres_client.retrieve_musicians(musician_names)

        assert not retrieved_df.empty
        assert set(retrieved_df['name']).issubset(set(musician_names))

    def test_save_musician_error_handling(self):
        musician_data = {
            'name': ['John'],
            'surname': ['Doe'],
            'age': ['invalid_age'],  # Invalid age to trigger error
            'instrument': ['Guitar']
        }
        musician_df = pd.DataFrame(musician_data)

        with pytest.raises(Exception):
            self.postgres_client.save(musician_df)