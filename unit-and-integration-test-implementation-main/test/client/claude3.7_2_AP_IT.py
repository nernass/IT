import unittest
import pandas as pd
import time

from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass
from src.client.postgres_client import PostgresClient
from src.config.postgres_client_config import PostgresClientConfig


class TestPostgresClientIntegration(AbstractIntegrationTestClass):
    
    @classmethod
    def setUpClass(cls):
        super().setup()
        
        # Reset test database and create necessary schema/table
        conn = cls.create_connection(cls)
        cursor = conn.cursor()
        cursor.execute("DROP SCHEMA IF EXISTS test CASCADE;")
        cursor.execute("CREATE SCHEMA test;")
        cursor.execute("""
            CREATE TABLE test.musician (
                id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                surname VARCHAR(100) NOT NULL,
                age INTEGER NOT NULL,
                instrument VARCHAR(100) NOT NULL
            );
        """)
        conn.commit()
        cursor.close()
        conn.close()
        
        # Create client instance for testing
        cls.postgres_client = PostgresClient(cls.postgres_client_config)
    
    @classmethod
    def tearDownClass(cls):
        super().tear_down()
    
    def setUp(self):
        # Clear the table before each test
        conn = self.create_connection()
        cursor = conn.cursor()
        cursor.execute("DELETE FROM test.musician;")
        conn.commit()
        cursor.close()
        conn.close()
    
    def test_save_and_retrieve_musician(self):
        # Create test data
        musician_data = {
            'name': ['kurt'],
            'surname': ['cobain'],
            'age': [27],
            'instrument': ['guitar']
        }
        musician_df = pd.DataFrame(musician_data)
        
        # Save musician to database
        self.postgres_client.save(musician_df)
        
        # Retrieve musician by name
        retrieved_df = self.postgres_client.retrieve_musician("kurt")
        
        # Assertions
        self.assertEqual(len(retrieved_df), 1)
        self.assertEqual(retrieved_df.iloc[0]['name'], 'kurt')
        self.assertEqual(retrieved_df.iloc[0]['surname'], 'cobain')
        self.assertEqual(retrieved_df.iloc[0]['age'], 27)
        self.assertEqual(retrieved_df.iloc[0]['instrument'], 'guitar')
    
    def test_save_multiple_and_retrieve_musicians(self):
        # Create test data for multiple musicians
        musicians = [
            {'name': 'jim', 'surname': 'morrison', 'age': 27, 'instrument': 'vocals'},
            {'name': 'noel', 'surname': 'gallagher', 'age': 56, 'instrument': 'guitar'}
        ]
        
        # Save each musician
        for musician in musicians:
            musician_df = pd.DataFrame([musician])
            self.postgres_client.save(musician_df)
        
        # Retrieve multiple musicians
        names = ['jim', 'noel']
        retrieved_df = self.postgres_client.retrieve_musicians(names)
        
        # Assertions
        self.assertEqual(len(retrieved_df), 2)
        self.assertTrue('jim' in retrieved_df['name'].values)
        self.assertTrue('noel' in retrieved_df['name'].values)
    
    def test_retrieve_nonexistent_musician(self):
        # Try to retrieve a musician that doesn't exist
        retrieved_df = self.postgres_client.retrieve_musician("nonexistent")
        
        # Should return empty DataFrame
        self.assertEqual(len(retrieved_df), 0)
    
    def test_config_connection_parameters(self):
        # Test that connection parameters are correctly passed from config to client
        # Create a connection directly using the same config to verify
        conn = self.create_connection()
        cursor = conn.cursor()
        
        # Simple query to verify connection works
        cursor.execute("SELECT 1 as test_value;")
        result = cursor.fetchone()
        
        # Close connection
        cursor.close()
        conn.close()
        
        # If we get here without exceptions, connection parameters worked
        self.assertEqual(result[0], 1)
    
    def test_save_invalid_data_handling(self):
        # Test saving a musician with NULL name (should fail due to NOT NULL constraint)
        musician_data = {
            'name': [None],  # NULL name should cause database error
            'surname': ['test'],
            'age': [30],
            'instrument': ['piano']
        }
        musician_df = pd.DataFrame(musician_data)
        
        # Should not raise an exception (error is handled inside the save method)
        self.postgres_client.save(musician_df)
        
        # Verify no data was inserted (transaction should have been rolled back)
        conn = self.create_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT COUNT(*) FROM test.musician;")
        count = cursor.fetchone()[0]
        cursor.close()
        conn.close()
        
        self.assertEqual(count, 0)


if __name__ == "__main__":
    unittest.main()