package com.jordanmruczynski.testing.customer;

import com.jordanmruczynski.testing.utils.PhoneNumberValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerRegistrationService {

    private final CustomerRepository customerRepository;
    private final PhoneNumberValidator phoneNumberValidator;

    @Autowired
    public CustomerRegistrationService(CustomerRepository customerRepository, PhoneNumberValidator phoneNumberValidator) {
        this.customerRepository = customerRepository;
        this.phoneNumberValidator = phoneNumberValidator;
    }

    public void registerNewCustomer(CustomerRegistrationRequest request) {
        String phoneNumber = request.getCustomer().getPhoneNumber();

        if (!phoneNumberValidator.test(phoneNumber)) {
            throw new IllegalStateException("Phone number " + phoneNumber + " is not valid");
        }
        Optional<Customer> customerOptional = customerRepository.findCustomerByPhoneNumber(phoneNumber);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            if (phoneNumber.equals(customer.getPhoneNumber())) {
                return;
            }
            throw new IllegalStateException("Phone number already taken");
        }
        if (request.getCustomer().getId() == null) {
            request.getCustomer().setId(UUID.randomUUID());
        }
        customerRepository.save(request.getCustomer());
    }
}
