package com.herbet.ffm.service;

import com.herbet.ffm.entity.Address;
import com.herbet.ffm.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Address findSameAddress(Address address) {
        for (Address existingAddress : findAll()) {
            if (address.isSameLocation(existingAddress)) {
                return existingAddress;
            }
        }
        return address;
    }
}
