import os
import pandas as pd
import pytest
import uuid
from pathlib import Path

from src.client.postgres_client import PostgresClient
from src.config.postgres_client_config import PostgresClientConfig
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass

class TestPostgresClientIntegration(AbstractIntegrationTestClass):
    
    @classmethod
    def setup_class(cls):
        super().setup()
        # Ensure the test schema exists
        with cls.create_connection(cls) as conn:
            cursor = conn.cursor()
            cursor.execute("CREATE SCHEMA IF NOT EXISTS test;")
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS test.musician (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100),
                    surname VARCHAR(100),
                    age INTEGER,
                    instrument VARCHAR(100)
                );
            """)
            conn.commit()
        
        # Create client under test
        cls.postgres_client = PostgresClient(cls.postgres_client_config)
        
        # Insert test data
        cls.test_musicians = [
            {"name": "kurt", "surname": "cobain", "age": 27, "instrument": "guitar"},
            {"name": "jim", "surname": "morrison", "age": 27, "instrument": "vocals"},
            {"name": "noel", "surname": "gallagher", "age": 56, "instrument": "guitar"}
        ]
        
        with cls.create_connection(cls) as conn:
            cursor = conn.cursor()
            for musician in cls.test_musicians:
                cursor.execute(
                    "INSERT INTO test.musician (name, surname, age, instrument) VALUES (%s, %s, %s, %s)",
                    (musician["name"], musician["surname"], musician["age"], musician["instrument"])
                )
            conn.commit()

    @classmethod
    def teardown_class(cls):
        # Clean up test data
        with cls.create_connection(cls) as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM test.musician;")
            conn.commit()
        super().tear_down()

    def test_retrieve_musician_by_name(self):
        # Given
        test_name = "kurt"
        
        # When
        result = self.postgres_client.retrieve_musician(test_name)
        
        # Then
        assert not result.empty
        assert len(result) == 1
        assert result.iloc[0]['name'] == "kurt"
        assert result.iloc[0]['surname'] == "cobain"
        assert result.iloc[0]['age'] == 27
        assert result.iloc[0]['instrument'] == "guitar"
    
    def test_retrieve_musicians_multiple_names(self):
        # Given
        test_names = ["kurt", "jim"]
        
        # When
        result = self.postgres_client.retrieve_musicians(test_names)
        
        # Then
        assert not result.empty
        assert len(result) == 2
        assert set(result['name'].tolist()) == set(test_names)
    
    def test_retrieve_musician_nonexistent_name(self):
        # Given
        nonexistent_name = f"nonexistent_{uuid.uuid4().hex[:8]}"
        
        # When
        result = self.postgres_client.retrieve_musician(nonexistent_name)
        
        # Then
        assert result.empty
    
    def test_save_new_musician(self):
        # Given
        new_musician = pd.DataFrame([{
            "name": "david", 
            "surname": "gilmour", 
            "age": 78, 
            "instrument": "guitar"
        }])
        
        # When
        self.postgres_client.save(new_musician)
        
        # Then
        result = self.postgres_client.retrieve_musician("david")
        assert not result.empty
        assert result.iloc[0]['name'] == "david"
        assert result.iloc[0]['surname'] == "gilmour"
        assert result.iloc[0]['age'] == 78
        assert result.iloc[0]['instrument'] == "guitar"
    
    def test_config_properly_initialized(self):
        # Given/When
        config = self.postgres_client_config
        
        # Then
        assert config.url == "localhost"
        assert config.port == "5432"
        assert config.database == "test-db"
        assert config.user_name == "username"
        assert config.password == "password"