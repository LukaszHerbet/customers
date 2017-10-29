package com.herbet.ffm.service;

import com.herbet.ffm.entity.Address;
import com.herbet.ffm.entity.Customer;
import com.herbet.ffm.exception.ApplicationException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Value("${input.file.encoding}")
    private String inputfileEncoding;

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

    /**
     * Loads customers data from data file dent from upload form. All necessary checks are performed for loaded
     * customers.
     * File is completetly loaded or not loaded at all. Every exception thrown during execution causes transation
     * to be rolled back.
     *
     * @param file file containing customer data sent from upload form
     * @throws IOException
     * @throws ApplicationException
     */
    @Transactional(rollbackFor = Exception.class)
    public void uploadCustomersFromFile(MultipartFile file) throws IOException, ApplicationException {

        if (file.isEmpty()) {
            throw new ApplicationException("The uploaded file is empty or no file was chosen.");
        }

        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);

        // throw exception if extension is other that csv or prn
        if (!(StringUtils.equals(fileExtension.toLowerCase(), "csv") || StringUtils.equals(fileExtension.toLowerCase(),
                                                                                           "prn"))) {
            throw new ApplicationException("Uploaded file extention: '." + fileExtension + "' is not supported.");
        }

        // extract all lines of file containing customer data (header is ignored)
        List<String> fileLines = extractLines(file);

        if (StringUtils.equals(fileExtension.toLowerCase(), "csv")) {
            uploadCustomersFromCsv(fileLines, fileName);
        }

        if (StringUtils.equals(fileExtension.toLowerCase(), "prn")) {
            uploadCustomersFromPrn(fileLines, fileName);
        }
    }

    /**
     * Extracts all lines except header from file containing customer
     *
     * @param file file containing customer data
     * @return list containing lines with customer data
     * @throws IOException
     */
    private List<String> extractLines(MultipartFile file) throws IOException {
        LinkedList<String> fileLines = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), inputfileEncoding))) {
            br.lines().forEach(fileLines::add);
        }

        // remove first line containing header
        fileLines.removeFirst();

        return fileLines;
    }

    /**
     * Creates customers from list containing lines with customer data in csv format and persist them.
     * @param csvFileLines list containing lines with customer data in csv format
     * @param fileName name of related csv file
     * @throws ApplicationException
     */
    private void uploadCustomersFromCsv(List<String> csvFileLines, String fileName) throws ApplicationException {
        for (String line : csvFileLines) {
            createCustomerFromCsvLine(line, fileName);
        }
    }

    /**
     * Creates customers from list containing lines with customer data in prn format and persist them.
     * @param prnFileLines list containing lines with customer data in prn format
     * @param fileName name of related csv file
     * @throws ApplicationException
     */
    private void uploadCustomersFromPrn(List<String> prnFileLines, String fileName) throws ApplicationException {
        for (String line : prnFileLines) {
            createCustomerFromPrnLine(line, fileName);
        }
    }

    /**
     * Creates customer from line with customer data in prn format and persist them.
     * @param prnFileLine line with customer data in prn format
     * @param fileName name of related prn file
     * @throws ApplicationException
     */
    private void createCustomerFromPrnLine(String prnFileLine, String fileName) throws ApplicationException {

        // extract all fields from line using properties defined in application.properties
        String nameString = StringUtils.substring(prnFileLine, prnNameStart, prnNameEnd);
        String[] names = extractNames(nameString);
        String addressString = StringUtils.substring(prnFileLine, prnAddressStart, prnAddressEnd);
        String postcode = StringUtils.substring(prnFileLine, prnPostcodeStart, prnPostcodeEnd);
        String phone = StringUtils.substring(prnFileLine, prnPhoneStart, prnPhoneEnd);
        String creditLimit = StringUtils.substring(prnFileLine, prnCreditLimitStart, prnCreditLimitEnd);
        String birthday = StringUtils.substring(prnFileLine, prnBirthdayStart, prnBirthdayEnd);

        // for all fields check if are not empty
        for (String field : ArrayUtils.addAll(names, addressString, postcode, phone, creditLimit, birthday)) {
            if (field.trim().length() == 0) {
                throw new ApplicationException("Following line has incorrect format (all fields are mandatory and " +
                                                       "can not be empty or spaces): " + prnFileLine);
            }
        }

        SimpleDateFormat format = new SimpleDateFormat(prnDateFormat);
        // not accept not existing dates line 29-02-2015
        format.setLenient(false);

        // create address from line fields
        Address address = new Address(addressString.trim(), postcode.trim());
        // ensure that no additional address is saved in DB if address with same location already exist in DB
        address = addressCrudService.findSameAddress(address);
        addressCrudService.save(address);

        // create address from line fields
        Customer customer = new Customer();

        customer.setFirstName(names[1].trim());
        customer.setLastName(names[0].trim());
        customer.setAddress(address);
        customer.setPhone(phone.trim());
        // extract and set double value of Credit Limit
        customer.setCreditLimit(extractCreditLimitFromPrn(creditLimit));
        // extract and set Birthday
        try {
            customer.setBirthday(new Date(format.parse(birthday.trim()).getTime()));
        } catch (ParseException pe) {
            throw new ApplicationException(
                    "Column Birthday has incorrect format (should be " + csvDateFormat + "): " + birthday.trim(), pe);
        }

        // set source to mark from which file customer was loaded
        customer.setSource(fileName);

        // save customer in db
        customerCrudService.save(customer);

    }

    /**
     * Creates customer from line with customer data in csv format and persist them.
     * @param csvFileLine line with customer data in csv format
     * @param fileName name of related csv file
     * @throws ApplicationException
     */
    private void createCustomerFromCsvLine(String csvFileLine, String fileName) throws ApplicationException {

        // extract Name column using quotation
        String[] quotaSplitted = StringUtils.split(csvFileLine, '"');

        // check if there are two columns after split
        if (quotaSplitted.length < 2) {
            throw new ApplicationException("Following line has incorrect format (not enough fields): " + csvFileLine);
        }

        // extract Lastname and Firstname
        String[] names = extractNames(quotaSplitted[0]);

        // extract columns other than Name
        String[] fields = StringUtils.split(quotaSplitted[1], ',');

        // check if number of columns is correct
        if (fields.length < 5) {
            throw new ApplicationException("Following line has incorrect format (not enough fields): " + csvFileLine);
        }

        // for all fields check if are not empty
        for (String field : ArrayUtils.addAll(names, fields)) {
            if (field.trim().length() == 0) {
                throw new ApplicationException("Following line has incorrect format (all fields are mandatory and " +
                                                       "can not be empty or spaces): " + csvFileLine);
            }
        }

        SimpleDateFormat format = new SimpleDateFormat(csvDateFormat);
        // not accept not existing dates line 29-02-2015
        format.setLenient(false);

        // create address from line fields
        Address address = new Address(fields[0].trim(), fields[1].trim());
        // ensure that no additional address is saved in DB if address with same location already exist in DB
        address = addressCrudService.findSameAddress(address);
        addressCrudService.save(address);

        Customer customer = new Customer();

        // create address from line fields
        customer.setFirstName(names[1].trim());
        customer.setLastName(names[0].trim());
        customer.setAddress(address);
        customer.setPhone(fields[2].trim());
        // extract and set double value of Credit Limit
        try {
            customer.setCreditLimit(Double.parseDouble(fields[3].trim()));
        } catch (NumberFormatException nfe) {
            throw new ApplicationException(
                    "Column Credit Limit has incorrect format (can't be parsed to double): " + fields[3].trim(), nfe);
        }
        // extract and set Birthday
        try {
            customer.setBirthday(new Date(format.parse(fields[4].trim()).getTime()));
        } catch (ParseException pe) {
            throw new ApplicationException(
                    "Column Birthday has incorrect format (should be " + csvDateFormat + "): " + fields[4].trim(), pe);
        }

        // set source to mark from which file customer was loaded
        customer.setSource(fileName);

        // save customer in db
        customerCrudService.save(customer);
    }

    /**
     * Extracts Lastname and Firstname from string in format "Lastname, Firstname" and return them as array of String.
     * First element in array is Lastname, second is Firstname. If format of input param is not correct
     * ApplicationException is thrown with dedicated message.
     *
     * @param nameString string in format "Lastname, Firstname"
     * @throws ApplicationException
     */
    private String[] extractNames(String nameString) throws ApplicationException {
        String[] names = StringUtils.split(nameString, ',');

        if (names.length < 2) {
            throw new ApplicationException(
                    "Column Name has incorrect format (should be \"Lastname, Firstname\"): \"" + nameString + "\"");
        }
        return names;
    }

    /**
     * Extracts double value of Credit Limit delivered as String.
     * If creditLimitString is longer or equal 3 then last 3 characters are treated as fractional part.
     * If creditLimitString is smaller that 3 then no fractional part is used.
     * If format of input param is not correct ApplicationException is thrown with dedicated message.
     * @param creditLimitString prn format string representing Credit Limit
     * @throws ApplicationException
     */
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
