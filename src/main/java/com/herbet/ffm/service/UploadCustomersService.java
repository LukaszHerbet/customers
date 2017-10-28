package com.herbet.ffm.service;

import com.herbet.ffm.entity.Address;
import com.herbet.ffm.entity.Customer;
import com.herbet.ffm.exception.ApplicationException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class UploadCustomersService {

    @Autowired
    private AddressCrudService addressCrudService;

    @Autowired
    private CustomerCrudService customerCrudService;

    @Value("${csv.format.date}")
    private String csvDateFormat;

    Logger logger = Logger.getLogger(UploadCustomersService.class.getName());

    public void uploadCustomersFromFile(MultipartFile file) throws IOException, ApplicationException, ParseException {

        if (file.isEmpty()) {
            throw new ApplicationException("The uploaded file is empty or no file was chosen.");
        }

        String fileName = file.getOriginalFilename();
        String fileExtention = fileName.substring(fileName.lastIndexOf('.') + 1);

        if (!(StringUtils.equals(fileExtention.toLowerCase(), "csv") || StringUtils.equals(fileExtention.toLowerCase(),
                                                                                           "prn"))) {
            throw new ApplicationException("Uploaded file extention: '." + fileExtention + "' is not supported.");
        }

        List<String> fileLines = extractLines(file);

        if (StringUtils.equals(fileExtention.toLowerCase(), "csv")) {
            uploadCustomersFromCsv(fileLines, fileName);
        }

        if (StringUtils.equals(fileExtention.toLowerCase(), "prn")) {
            uploadCustomersFromPrn(fileLines, fileName);
        }
    }

    private List<String> extractLines(MultipartFile file) throws IOException {
        LinkedList<String> fileLines = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            br.lines().forEach(fileLines::add);
        }

        fileLines.removeFirst();

        return fileLines;
    }

    private void uploadCustomersFromCsv(List<String> csvFileLines, String fileName)
            throws ParseException, ApplicationException {
        for (String line : csvFileLines) {
            createCustomerFromCsvLine(line, fileName);
        }
    }

    private void uploadCustomersFromPrn(List<String> prnFileLines, String fileName) {
        // to be implemented
    }

    public void createCustomerFromCsvLine(String csvFileLine, String fileName)
            throws ApplicationException, ParseException {
        String[] quotaSplitted = StringUtils.split(csvFileLine, '"');

        if (quotaSplitted.length < 2) {
            throw new ApplicationException("Following line has incorrect format (not enough fields): " + csvFileLine);
        }

        String[] names = StringUtils.split(quotaSplitted[0], ',');

        if (names.length < 2) {
            throw new ApplicationException(
                    "Column Name has incorrect format (should be \"Lastname, Firstname\"): \"" + quotaSplitted[0] +
                            "\"");
        }

        String[] fields = StringUtils.split(quotaSplitted[1], ',');

        if (fields.length < 5) {
            throw new ApplicationException("Following line has incorrect format (not enough fields): " + csvFileLine);
        }

        for (String field : ArrayUtils.addAll(names, fields)) {
            if (field.trim().length() == 0) {
                throw new ApplicationException("Following line has incorrect format (all fields are mandatory and " +
                                                       "can not be empty or spaces): " + csvFileLine);
            }
        }

        SimpleDateFormat format = new SimpleDateFormat(csvDateFormat);

        Address address = new Address(fields[0].trim(), fields[1].trim());
        address = addressCrudService.findSameAddress(address);
        addressCrudService.save(address);

        Customer customer = new Customer();

        customer.setFirstName(names[1].trim());
        customer.setLastName(names[0].trim());
        customer.setAddress(address);
        customer.setPhone(fields[2].trim());
        customer.setCreditLimit(Double.parseDouble(fields[3].trim()));
        customer.setBirthday(new Date(format.parse(fields[4].trim()).getTime()));
        customer.setSource(fileName);

        customerCrudService.save(customer);
    }
}
