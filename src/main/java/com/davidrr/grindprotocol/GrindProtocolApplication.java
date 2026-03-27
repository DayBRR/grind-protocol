package com.davidrr.grindprotocol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.davidrr.grindprotocol",
        "com.davidrr.security"
})
public class GrindProtocolApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrindProtocolApplication.class, args);
    }

}
