package com.herbet.ffm.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.herbet.ffm.Application;
import com.herbet.ffm.entity.Customer;
import com.herbet.ffm.exception.ApplicationException;
import com.herbet.ffm.repository.AddressRepository;
import com.herbet.ffm.repository.CustomerRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UploadCustomersService.class, AddressCrudService.class, CustomerCrudService.class, AddressRepository.class, CustomerRepository.class, Application.class})
@Transactional
public class UploadCustomersServiceTest {

    // path to dir with testfiles used by this testSuite
    private static final String TEST_DATA_DIR = "./src/test/resources/testfiles/";

    private static final String CONTENT_TYPE = "text/*";

    @Autowired
    private UploadCustomersService uploadCustomersService;

    @Autowired
    private CustomerCrudService customerCrudService;

    @Autowired
    private AddressCrudService addressCrudService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void clearRepositories() {
        // deleting all objects from db2 before running test
        customerCrudService.deleteAll();
        addressCrudService.deleteAll();
    }

    @Test
    public void csvAndPrnSuccessfullyLoaded() throws Exception {
        // test if csv and prn files are successfully loaded into db

        // make sure no customers available
        assertThat(customerCrudService.count()).isEqualTo(0L);

        // load customers from csv
        String csvFileName = "Workbook2.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);

        // load customers from prn
        String prnFileName = "Workbook2.prn";
        MultipartFile prnMultipartFile = new MockMultipartFile(prnFileName, prnFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + prnFileName)));
        uploadCustomersService.uploadCustomersFromFile(prnMultipartFile);

        // make sure all 14 customers have been loaded
        assertThat(customerCrudService.count()).isEqualTo(14L);
    }

    @Test
    public void addressesAreReused() throws Exception {
        // test if existing addresses are used and no additional address objects are saved in db

        // make sure no customers available
        assertThat(addressCrudService.count()).isEqualTo(0L);

        // load customers from csv
        String csvFileName = "Workbook2.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);

        assertThat(addressCrudService.count()).isEqualTo(7L);

        // load customers from prn (in prn file there are only the same addresses as in csv)
        String prnFileName = "Workbook2.prn";
        MultipartFile prnMultipartFile = new MockMultipartFile(prnFileName, prnFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + prnFileName)));
        uploadCustomersService.uploadCustomersFromFile(prnMultipartFile);

        // make sure only 7 addresse have been saved  in db
        assertThat(addressCrudService.count()).isEqualTo(7L);

        // make sure all 14 customers have been loaded
        assertThat(customerCrudService.count()).isEqualTo(14L);
    }

    @Test
    public void customerFieldsSetCorrectlyCsv() throws Exception {
        // test that all fields of customer object loaded from csv file are set correctly

        String csvFileName = "customer1.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);

        Customer customer1 = customerCrudService.findAll().iterator().next();

        // Customer definition in file:
        // "Anderson, Paul",Dorpsplein 3A,4532 AA,030 3458986,109093,03/12/1965

        // make sure all fields are set correctly
        assertThat(customer1.getFirstName()).isEqualTo("Paul");
        assertThat(customer1.getLastName()).isEqualTo("Anderson");
        assertThat(customer1.getAddress().getStreet()).isEqualTo("Dorpsplein 3A");
        assertThat(customer1.getAddress().getPostcode()).isEqualTo("4532 AA");
        assertThat(customer1.getPhone()).isEqualTo("030 3458986");
        assertThat(customer1.getCreditLimit()).isEqualTo(109093.00);
        assertThat(customer1.getBirthday()).isEqualTo(new SimpleDateFormat("yyyy-MM-dd").parse("1965-12-03"));
        assertThat(customer1.getSource()).isEqualTo(csvFileName);

        customerCrudService.deleteAll();

        String csvFileName2 = "customer2.csv";
        MultipartFile csvMultipartFile2 = new MockMultipartFile(csvFileName2, csvFileName2, CONTENT_TYPE,
                                                                new FileInputStream(
                                                                        new File(TEST_DATA_DIR + csvFileName2)));
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile2);

        Customer customer2 = customerCrudService.findAll().iterator().next();

        // Customer definition in file:
        // "Benetar, Pat",Driehoog 3zwart,2340 CC,06-28938945,54,04/09/1964

        // make sure all fields are set correctly
        assertThat(customer2.getFirstName()).isEqualTo("Pat");
        assertThat(customer2.getLastName()).isEqualTo("Benetar");
        assertThat(customer2.getAddress().getStreet()).isEqualTo("Driehoog 3zwart");
        assertThat(customer2.getAddress().getPostcode()).isEqualTo("2340 CC");
        assertThat(customer2.getPhone()).isEqualTo("06-28938945");
        assertThat(customer2.getCreditLimit()).isEqualTo(54.00);
        assertThat(customer2.getBirthday()).isEqualTo(new SimpleDateFormat("yyyy-MM-dd").parse("1964-09-04"));
        assertThat(customer2.getSource()).isEqualTo(csvFileName2);
    }

    @Test
    public void customerFieldsSetCorrectlyPrn() throws Exception {
        // test that all fields of customer object loaded from prn file are set correctly

        String prnFileName = "customer1.prn";
        MultipartFile prnMultipartFile = new MockMultipartFile(prnFileName, prnFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + prnFileName)));
        uploadCustomersService.uploadCustomersFromFile(prnMultipartFile);

        Customer customer1 = customerCrudService.findAll().iterator().next();

        // Customer definition in file:
        // Anderson, Paul  Dorpsplein 3A         4532 AA  030 3458986       10909300 19651203

        // make sure all fields are set correctly
        assertThat(customer1.getFirstName()).isEqualTo("Paul");
        assertThat(customer1.getLastName()).isEqualTo("Anderson");
        assertThat(customer1.getAddress().getStreet()).isEqualTo("Dorpsplein 3A");
        assertThat(customer1.getAddress().getPostcode()).isEqualTo("4532 AA");
        assertThat(customer1.getPhone()).isEqualTo("030 3458986");
        assertThat(customer1.getCreditLimit()).isEqualTo(109093.00);
        assertThat(customer1.getBirthday()).isEqualTo(new SimpleDateFormat("yyyy-MM-dd").parse("1965-12-03"));
        assertThat(customer1.getSource()).isEqualTo(prnFileName);

        customerCrudService.deleteAll();

        String prnFileName2 = "customer2.prn";
        MultipartFile prnMultipartFile2 = new MockMultipartFile(prnFileName2, prnFileName2, CONTENT_TYPE,
                                                                new FileInputStream(
                                                                        new File(TEST_DATA_DIR + prnFileName2)));
        uploadCustomersService.uploadCustomersFromFile(prnMultipartFile2);

        Customer customer2 = customerCrudService.findAll().iterator().next();

        // Customer definition in file:
        // Benetar, Pat    Driehoog 3zwart       2340 CC  06-28938945             54 19640904

        // make sure all fields are set correctly
        assertThat(customer2.getFirstName()).isEqualTo("Pat");
        assertThat(customer2.getLastName()).isEqualTo("Benetar");
        assertThat(customer2.getAddress().getStreet()).isEqualTo("Driehoog 3zwart");
        assertThat(customer2.getAddress().getPostcode()).isEqualTo("2340 CC");
        assertThat(customer2.getPhone()).isEqualTo("06-28938945");
        assertThat(customer2.getCreditLimit()).isEqualTo(54.00);
        assertThat(customer2.getBirthday()).isEqualTo(new SimpleDateFormat("yyyy-MM-dd").parse("1964-09-04"));
        assertThat(customer2.getSource()).isEqualTo(prnFileName2);
    }

    @Test
    public void emptyFile() throws Exception {
        // test that exception is thrown for empty file

        String csvFileName = "empty.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));

        thrown.expect(ApplicationException.class);
        thrown.expectMessage("The uploaded file is empty or no file was chosen.");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void wrongExtention() throws Exception {
        // test that exception is thrown for file with extention other than csv or prn

        String csvFileName = "Workbook2.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile("empty.txt", "empty.txt", CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));

        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Uploaded file extention: '.txt' is not supported.");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void notEnoughColumnsCsv() throws Exception {
        // test that exception is thrown if csv file has not enough columns

        String csvFileName = "lessColumns.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Following line has incorrect format (not enough fields): " +
                                     "\"Johnson, John\",Voorstraat 32,3122gg,020 3849381,01/01/1987");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void notQuotationsCsv() throws Exception {
        // test that exception is thrown if first column [Name] is not surrounded with double quotations

        String csvFileName = "noQuotations.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Following line has incorrect format (not enough fields): " +
                                     "Johnson, John,Voorstraat 32,3122gg,020 3849381,10000,01/01/1987");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void incorrectNamesCsv() throws Exception {
        // test that exception is thrown if column [Name] has wrong format in csv file

        String csvFileName = "incorrectNames.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Column Name has incorrect format (should be \"Lastname, Firstname\"): \"Benetar Pat\"");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void incorrectNamesPrn() throws Exception {
        // test that exception is thrown if column [Name] has wrong format in prn file

        String prnFileName = "incorrectNames.prn";
        MultipartFile csvMultipartFile = new MockMultipartFile(prnFileName, prnFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + prnFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(
                "Column Name has incorrect format (should be \"Lastname, Firstname\"): \"Wicket  Steve   \"");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void emptyFieldCsv() throws Exception {
        // test that exception is thrown if any of columns is empty in csv file

        String csvFileName = "emptyField.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(
                "Following line has incorrect format (all fields are mandatory and " + "can not be empty or spaces): ");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void emptyFieldCsPrn() throws Exception {
        // test that exception is thrown if column [Name] has wrong format in prn file

        String prnFileName = "emptyField.prn";
        MultipartFile csvMultipartFile = new MockMultipartFile(prnFileName, prnFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + prnFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage(
                "Following line has incorrect format (all fields are mandatory and " + "can not be empty or spaces): ");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void wrongCreditLimitFormatCsv() throws Exception {
        // test that exception is thrown if column [Credit Limit] has wrong format in csv file

        String csvFileName = "wrongLimit.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Column Credit Limit has incorrect format (can't be parsed to double): ");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void wrongCreditLimitFormatPrn() throws Exception {
        // test that exception is thrown if column [Credit Limit] has wrong format in prn file

        String csvFileName = "wrongLimit.prn";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Column Credit Limit has incorrect format (can't be parsed to double): ");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void wrongDateCsv() throws Exception {
        // test that exception is thrown if column [Birthday] is set wrong (not existing 29-02-2018) in csv file

        String csvFileName = "wrongDate1.csv";
        MultipartFile csvMultipartFile = new MockMultipartFile(csvFileName, csvFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + csvFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Column Birthday has incorrect format");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile);
    }

    @Test
    public void wrongDateFormatCsv() throws Exception {
        // test that exception is thrown if column [Birthday] has wrong format in csv file

        String csvFileName2 = "wrongDate2.csv";
        MultipartFile csvMultipartFile2 = new MockMultipartFile(csvFileName2, csvFileName2, CONTENT_TYPE,
                                                                new FileInputStream(
                                                                        new File(TEST_DATA_DIR + csvFileName2)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Column Birthday has incorrect format");
        uploadCustomersService.uploadCustomersFromFile(csvMultipartFile2);
    }

    @Test
    public void wrongDatePrn() throws Exception {
        // test that exception is thrown if column [Birthday] is set wrong (not existing 29-02-2015) in prn file

        String prnFileName = "wrongDate1.prn";
        MultipartFile prnMultipartFile = new MockMultipartFile(prnFileName, prnFileName, CONTENT_TYPE,
                                                               new FileInputStream(
                                                                       new File(TEST_DATA_DIR + prnFileName)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Column Birthday has incorrect format");
        uploadCustomersService.uploadCustomersFromFile(prnMultipartFile);
    }

    @Test
    public void wrongDateFormatPrn() throws Exception {
        // test that exception is thrown if column [Birthday] has wrong format in prn file

        String prnFileName2 = "wrongDate2.prn";
        MultipartFile prnMultipartFile2 = new MockMultipartFile(prnFileName2, prnFileName2, CONTENT_TYPE,
                                                                new FileInputStream(
                                                                        new File(TEST_DATA_DIR + prnFileName2)));

        // make sure appropriate exception is thrown
        thrown.expect(ApplicationException.class);
        thrown.expectMessage("Column Birthday has incorrect format");
        uploadCustomersService.uploadCustomersFromFile(prnMultipartFile2);
    }
}