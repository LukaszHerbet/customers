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

    @Value("${prn.format.date}")
    private String prnDateFormat;

    @Value("${prn.format.name.start}")
    private Integer prnNameStart;

    @Value("${prn.format.name.end}")
    private Integer prnNameEnd;

    @Value("${prn.format.address.start}")
    private Integer prnAddressStart;

    @Value("${prn.format.address.end}")
    private Integer prnAddressEnd;

    @Value("${prn.format.postcode.start}")
    private Integer prnPostcodeStart;

    @Value("${prn.format.postcode.end}")
    private Integer prnPostcodeEnd;

    @Value("${prn.format.phone.start}")
    private Integer prnPhoneStart;

    @Value("${prn.format.phone.end}")
    private Integer prnPhoneEnd;

    @Value("${prn.format.credit.limit.start}")
    private Integer prnCreditLimitStart;

    @Value("${prn.format.credit.limit.end}")
    private Integer prnCreditLimitEnd;

    @Value("${prn.format.birthday.start}")
    private Integer prnBirthdayStart;

    @Value("${prn.format.birthday.end}")
    private Integer prnBirthdayEnd;

    Logger logger = Logger.getLogger(UploadCustomersService.class.getName());

    public void uploadCustomersFromFile(MultipartFile file) throws IOException, ApplicationException {

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

    private void uploadCustomersFromCsv(List<String> csvFileLines, String fileName) throws ApplicationException {
        for (String line : csvFileLines) {
            createCustomerFromCsvLine(line, fileName);
        }
    }

    private void uploadCustomersFromPrn(List<String> prnFileLines, String fileName) throws ApplicationException {
        for (String line : prnFileLines) {
            createCustomerFromPrnLine(line, fileName);
        }
    }

    private void createCustomerFromPrnLine(String prnFileLine, String fileName) throws ApplicationException {

        String nameString = StringUtils.substring(prnFileLine, prnNameStart, prnNameEnd);
        String[] names = extractNames(nameString);
        String addressString = StringUtils.substring(prnFileLine, prnAddressStart, prnAddressEnd);
        String postcode = StringUtils.substring(prnFileLine, prnPostcodeStart, prnPostcodeEnd);
        String phone = StringUtils.substring(prnFileLine, prnPhoneStart, prnPhoneEnd);
        String creditLimit = StringUtils.substring(prnFileLine, prnCreditLimitStart, prnCreditLimitEnd);
        String birthday = StringUtils.substring(prnFileLine, prnBirthdayStart, prnBirthdayEnd);

        for (String field : ArrayUtils.addAll(names, addressString, postcode, phone, creditLimit, birthday)) {
            if (field.trim().length() == 0) {
                throw new ApplicationException("Following line has incorrect format (all fields are mandatory and " +
                                                       "can not be empty or spaces): " + prnFileLine);
            }
        }

        SimpleDateFormat format = new SimpleDateFormat(prnDateFormat);

        Address address = new Address(addressString.trim(), postcode.trim());
        address = addressCrudService.findSameAddress(address);
        addressCrudService.save(address);

        Customer customer = new Customer();

        customer.setFirstName(names[1].trim());
        customer.setLastName(names[0].trim());
        customer.setAddress(address);
        customer.setPhone(phone.trim());
        customer.setCreditLimit(extractCreditLimitFromPrn(creditLimit));
        try {
            customer.setBirthday(new Date(format.parse(birthday.trim()).getTime()));
        } catch (ParseException pe) {
            throw new ApplicationException(
                    "Column Birthday has incorrect format (should be " + csvDateFormat + "): " + birthday.trim(), pe);
        }

        customer.setSource(fileName);
        customerCrudService.save(customer);

    }

    public void createCustomerFromCsvLine(String csvFileLine, String fileName) throws ApplicationException {
        String[] quotaSplitted = StringUtils.split(csvFileLine, '"');

        if (quotaSplitted.length < 2) {
            throw new ApplicationException("Following line has incorrect format (not enough fields): " + csvFileLine);
        }

        String[] names = extractNames(quotaSplitted[0]);

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
        try {
            customer.setCreditLimit(Double.parseDouble(fields[3].trim()));
        } catch (NumberFormatException nfe) {
            throw new ApplicationException(
                    "Column Credit Limit has incorrect format (can't be parsed to double): " + fields[3].trim(), nfe);
        }
        try {
            customer.setBirthday(new Date(format.parse(fields[4].trim()).getTime()));
        } catch (ParseException pe) {
            throw new ApplicationException(
                    "Column Birthday has incorrect format (should be " + csvDateFormat + "): " + fields[4].trim(), pe);
        }

        customer.setSource(fileName);
        customerCrudService.save(customer);
    }

    private String[] extractNames(String nameString) throws ApplicationException {
        String[] names = StringUtils.split(nameString, ',');

        if (names.length < 2) {
            throw new ApplicationException(
                    "Column Name has incorrect format (should be \"Lastname, Firstname\"): \"" + nameString + "\"");
        }
        return names;
    }

    private double extractCreditLimitFromPrn(String creditLimitString) throws ApplicationException {
        double creditLimit;

        String adjustedCreditLimitString = creditLimitString.trim();

        if (adjustedCreditLimitString.length() >= 3) {
            adjustedCreditLimitString = new StringBuilder(adjustedCreditLimitString).insert(
                    adjustedCreditLimitString.length() - 2, '.').toString();
        }

        try {
            creditLimit = Double.parseDouble(adjustedCreditLimitString);
        } catch (NumberFormatException nfe) {
            throw new ApplicationException(
                    "Column Credit Limit has incorrect format (can't be parsed to double): " + creditLimitString, nfe);
        }
        return creditLimit;
    }
}
