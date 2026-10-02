package com.shinhan.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import com.shinhan.backend.config.TimeConfiguration;
import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(TimeConfiguration.SEOUL));
        SpringApplication.run(BackendApplication.class, args);
    }

}
