import pytest
import pandas as pd
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass


class TestPostgresIntegration(AbstractIntegrationTestClass):
    
    @classmethod
    def setup_class(cls):
        super().setup()
        cls.test_data = {
            'name': ['kurt'],
            'surname': ['cobain'],
            'age': [27],
            'instrument': ['guitar']
        }
        cls.test_df = pd.DataFrame(cls.test_data)
    
    def test_save_and_retrieve_musician(self):
        # Test saving musician
        connection = self.create_connection()
        cursor = connection.cursor()
        
        # First, clean up any existing test data
        cursor.execute("DELETE FROM test.musician WHERE name = 'kurt'")
        connection.commit()
        
        # Create postgres client with config
        from src.client.postgres_client import PostgresClient
        postgres_client = PostgresClient(self.postgres_client_config)
        
        # Save test data
        postgres_client.save(self.test_df)
        
        # Retrieve and verify
        result_df = postgres_client.retrieve_musician('kurt')
        
        # Assertions
        assert not result_df.empty
        assert result_df.iloc[0]['name'] == 'kurt'
        assert result_df.iloc[0]['surname'] == 'cobain'
        assert result_df.iloc[0]['age'] == 27
        assert result_df.iloc[0]['instrument'] == 'guitar'
        
        # Clean up
        cursor.execute("DELETE FROM test.musician WHERE name = 'kurt'")
        connection.commit()
        cursor.close()
        connection.close()
    
    def test_retrieve_multiple_musicians(self):
        connection = self.create_connection()
        cursor = connection.cursor()
        
        # Clean up existing test data
        cursor.execute("DELETE FROM test.musician WHERE name IN ('kurt', 'jim')")
        connection.commit()
        
        # Insert test data
        test_data_multiple = pd.DataFrame({
            'name': ['kurt', 'jim'],
            'surname': ['cobain', 'morrison'],
            'age': [27, 27],
            'instrument': ['guitar', 'vocals']
        })
        
        postgres_client = PostgresClient(self.postgres_client_config)
        
        # Save both musicians
        for i in range(len(test_data_multiple)):
            postgres_client.save(test_data_multiple.iloc[[i]])
        
        # Test retrieving multiple musicians
        result_df = postgres_client.retrieve_musicians(['kurt', 'jim'])
        
        # Assertions
        assert len(result_df) == 2
        assert set(result_df['name'].values) == {'kurt', 'jim'}
        assert set(result_df['instrument'].values) == {'guitar', 'vocals'}
        
        # Clean up
        cursor.execute("DELETE FROM test.musician WHERE name IN ('kurt', 'jim')")
        connection.commit()
        cursor.close()
        connection.close()
    
    def test_invalid_musician_retrieval(self):
        postgres_client = PostgresClient(self.postgres_client_config)
        result_df = postgres_client.retrieve_musician('nonexistent')
        assert result_df.empty
    
    @classmethod
    def teardown_class(cls):
        super().tear_down()

if __name__ == '__main__':
    pytest.main([__file__])