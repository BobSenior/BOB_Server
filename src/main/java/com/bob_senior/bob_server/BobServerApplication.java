package com.bob_senior.bob_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BobServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BobServerApplication.class, args);
    }


}
