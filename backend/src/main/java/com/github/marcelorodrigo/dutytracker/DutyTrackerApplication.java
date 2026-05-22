package com.github.marcelorodrigo.dutytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DutyTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DutyTrackerApplication.class, args);
    }
}
