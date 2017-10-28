package com.herbet.ffm.repository;

import com.herbet.ffm.entity.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

}
