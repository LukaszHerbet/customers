package com.herbet.ffm;

import com.herbet.ffm.entity.Address;
import com.herbet.ffm.entity.Customer;
import com.herbet.ffm.repository.AddressRepository;
import com.herbet.ffm.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Date;
import java.text.SimpleDateFormat;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Address address1 = new Address("Straussa 1/15", "50-129");
        addressRepository.save(address1);

        Address address2 = new Address("Stawowa 13", "50-118");
        addressRepository.save(address2);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Customer customer1 = new Customer("Lukasz", "Herbet", address1, "089 4777333", 1000.00,
                                          new Date(format.parse("1985-09-26").getTime()));
        customerRepository.save(customer1);

        Customer customer2 = new Customer("Jan", "Kowalski", address2, "089 9788795", 112321.33,
                                          new Date(format.parse("1980-01-05").getTime()));
        customerRepository.save(customer2);
    }

}
