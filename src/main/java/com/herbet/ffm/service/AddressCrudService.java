package com.herbet.ffm.service;

import com.herbet.ffm.entity.Address;
import com.herbet.ffm.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service used for persisting Address objects. Additional layer between DAO and application logic.
 */
@Service
public class AddressCrudService {

    @Autowired
    private AddressRepository repository;

    public Address findOne(Long id) {
        return repository.findOne(id);
    }

    public Iterable<Address> findAll() {
        return repository.findAll();
    }

    public Address save(Address address) {
        return repository.save(address);
    }

    public long count() {
        return repository.count();
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    /**
     * Check if addres with the same location already exist in DB.
     *
     * @param address address to check
     * @return if address with the same location that exist in DB then address from DB; ; address param otherwise.
     */
    public Address findSameAddress(Address address) {

        // TODO in production application it should be reimplemented to use DB query and not extract all orders
        for (Address existingAddress : findAll()) {
            if (address.isSameLocation(existingAddress)) {
                return existingAddress;
            }
        }
        return address;
    }
}
