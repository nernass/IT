package com.jordanmruczynski.testing.customer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {

    boolean existsCustomerByPhoneNumber(String phoneNumber);

    Optional<Customer> findCustomerByPhoneNumber(String phoneNumber);
}
