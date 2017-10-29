package com.herbet.ffm.repository;

import com.herbet.ffm.entity.Address;
import org.springframework.data.repository.CrudRepository;

/**
 * CRUD Repository for Address used as DAO.
 */
public interface AddressRepository extends CrudRepository<Address, Long> {

}
