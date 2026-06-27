package com.makeupseven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MakeupSevenApplication {
    public static void main(String[] args) {
        SpringApplication.run(MakeupSevenApplication.class, args);
    }
}
