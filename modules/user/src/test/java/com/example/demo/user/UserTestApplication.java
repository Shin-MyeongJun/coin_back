package com.example.demo.user;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boot entry-point for slice tests (@WebMvcTest, @DataJpaTest) in the :user module.
 * Without this, Spring's upward package search for @SpringBootConfiguration fails because
 * the module is a library (no production @SpringBootApplication). Component scan from this
 * package picks up controllers and advices automatically.
 */
@SpringBootApplication
public class UserTestApplication {
}
