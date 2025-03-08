import pytest
from fastapi.testclient import TestClient
from src.controller.employee_controller import EmployeeController
from src.model.dummy_employee import DummyEmployee
from src.dto.dummy_employee_response import DummyEmployeeResponse
from src.dto.employee_response_dto import EmployeeResponseDto
from test.resources.abstract_integration_test_class import AbstractIntegrationTestClass


class TestEmployeeIntegration(AbstractIntegrationTestClass):
    
    @classmethod
    def setup_class(cls):
        super().setup()
        cls.test_client = TestClient(cls.client.app)
        
    def test_end_to_end_employee_flow(self):
        # Test data
        dummy_employee = DummyEmployee(
            id=1,
            name="John Doe",
            department="IT",
            salary=75000.0
        )
        
        # Create employee
        response = self.test_client.post(
            "/api/employees",
            json=dummy_employee.dict()
        )
        assert response.status_code == 201
        created_employee = EmployeeResponseDto(**response.json())
        assert created_employee.name == dummy_employee.name
        
        # Get employee by ID
        response = self.test_client.get(f"/api/employees/{created_employee.id}")
        assert response.status_code == 200
        retrieved_employee = EmployeeResponseDto(**response.json())
        assert retrieved_employee.id == created_employee.id
        
        # Update employee
        updated_data = {
            "name": "John Updated",
            "department": "HR",
            "salary": 80000.0
        }
        response = self.test_client.put(
            f"/api/employees/{created_employee.id}",
            json=updated_data
        )
        assert response.status_code == 200
        updated_employee = EmployeeResponseDto(**response.json())
        assert updated_employee.name == "John Updated"
        
        # Get all employees
        response = self.test_client.get("/api/employees")
        assert response.status_code == 200
        employees = [EmployeeResponseDto(**emp) for emp in response.json()]
        assert len(employees) > 0
        assert any(emp.id == created_employee.id for emp in employees)
        
        # Delete employee
        response = self.test_client.delete(f"/api/employees/{created_employee.id}")
        assert response.status_code == 204
        
        # Verify deletion
        response = self.test_client.get(f"/api/employees/{created_employee.id}")
        assert response.status_code == 404

    def test_invalid_employee_creation(self):
        # Test invalid data
        invalid_employee = {
            "name": "",  # Invalid empty name
            "department": "IT",
            "salary": -1000  # Invalid negative salary
        }
        
        response = self.test_client.post(
            "/api/employees",
            json=invalid_employee
        )
        assert response.status_code == 400
        
    def test_edge_cases(self):
        # Test with maximum values
        max_employee = DummyEmployee(
            id=999999999,
            name="X" * 100,  # Assuming max length is 100
            department="Y" * 50,  # Assuming max length is 50
            salary=999999999.99
        )
        
        response = self.test_client.post(
            "/api/employees",
            json=max_employee.dict()
        )
        assert response.status_code == 201
        
        # Clean up
        created_emp = EmployeeResponseDto(**response.json())
        self.test_client.delete(f"/api/employees/{created_emp.id}")
        
    @classmethod
    def teardown_class(cls):
        super().tear_down()