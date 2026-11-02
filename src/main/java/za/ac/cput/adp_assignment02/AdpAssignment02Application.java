package za.ac.cput.adp_assignment02;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdpAssignment02Application {

    public static void main(String[] args) {
        SpringApplication.run(AdpAssignment02Application.class, args);
    }

}
