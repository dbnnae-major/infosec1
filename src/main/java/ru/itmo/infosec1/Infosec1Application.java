package ru.itmo.infosec1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class Infosec1Application {
    public static void main(String[] args) {
        SpringApplication.run(Infosec1Application.class, args);
    }
}
