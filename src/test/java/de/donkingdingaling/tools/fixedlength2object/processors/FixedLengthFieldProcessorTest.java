package de.donkingdingaling.tools.fixedlength2object.processors;

import de.donkingdingaling.tools.fixedlength2object.processors.testclasses.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FixedLengthFieldProcessorTest {
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @Test
    void testStringToObject_withSimpleObject_shouldReturnCorrectObject() throws Exception {
        Customer expectedCustomer = new Customer();
        expectedCustomer.setName("John Doe");
        expectedCustomer.setAmountOfOrders(5);

        String stringCustomer = "John Doe                                         5          ";
        FixedLengthFieldProcessor<Customer> processor = new FixedLengthFieldProcessor<>(Customer.class);
        Customer actualCustomer = processor.stringToObject(stringCustomer);

        assertEquals(expectedCustomer.getName(), actualCustomer.getName());
        assertEquals(expectedCustomer.getAmountOfOrders(), actualCustomer.getAmountOfOrders());
    }

    @Test
    void testObjectToString_withSimpleString_shouldReturnCorrectString() throws Exception {
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setAmountOfOrders(5);

        String expectedCustomer = "John Doe                                         5       0.0";
        FixedLengthFieldProcessor<Customer> processor = new FixedLengthFieldProcessor<>(Customer.class);
        String actualCustomer = processor.objectToString(customer);

        assertEquals(expectedCustomer, actualCustomer);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }
}