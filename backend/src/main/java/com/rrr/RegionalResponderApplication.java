package com.rrr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RegionalResponderApplication {
    public static void main(String[] args) {
        SpringApplication.run(RegionalResponderApplication.class, args);
    }
}
