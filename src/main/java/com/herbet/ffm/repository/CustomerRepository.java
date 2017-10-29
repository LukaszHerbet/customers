package com.herbet.ffm.repository;

import com.herbet.ffm.entity.Customer;
import org.springframework.data.repository.CrudRepository;

/**
 * CRUD Repository for Customer used as DAO.
 */
public interface CustomerRepository extends CrudRepository<Customer, Long> {

}
