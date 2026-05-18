package com.example.demo.user;

import com.example.demo.user.infrastructure.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.example.demo.user")
@EnableConfigurationProperties(JwtProperties.class)
public class UserModuleConfiguration {
}
