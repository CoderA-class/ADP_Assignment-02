package repository;

import factory.EmployeeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeRepositoryTest {

    private EmployeeRepository repository;
    private EmployeeFactory factory;

    @BeforeEach
    void setUp() {
        repository = new EmployeeRepositoryImpl();
        factory = new EmployeeFactory();
    }

    @AfterEach
    void tearDown() {
        repository = null;
        factory = null;
    }

    @Test
    void create() {
    }

    @Test
    void read() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void getAll() {
    }
}