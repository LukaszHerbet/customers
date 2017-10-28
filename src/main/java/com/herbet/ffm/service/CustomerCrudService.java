package com.herbet.ffm.service;

import com.herbet.ffm.entity.Customer;
import com.herbet.ffm.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerCrudService {

    @Autowired
    private CustomerRepository repository;

    public Customer findOne(Long id) {
        return repository.findOne(id);
    }

    public Iterable<Customer> findAll() {
        return repository.findAll();
    }

    public Customer save(Customer customer) {
        return repository.save(customer);
    }

}