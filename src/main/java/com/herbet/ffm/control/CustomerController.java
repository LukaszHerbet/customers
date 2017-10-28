package com.herbet.ffm.control;

import com.herbet.ffm.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomerController {

    @Autowired
    private CustomerRepository repository;

    @RequestMapping("/customer/{id}")
    public String customer(@PathVariable Long id, Model model) {
        model.addAttribute("customer", repository.findOne(id));
        return "customer";
    }

    @RequestMapping("/customers")
    public String customersList(Model model) {
        model.addAttribute("customers", repository.findAll());
        return "customers";
    }
}
