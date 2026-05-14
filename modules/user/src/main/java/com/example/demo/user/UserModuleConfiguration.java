package com.example.demo.user;

import com.example.demo.user.infrastructure.persistence.entity.AccountEntity;
import com.example.demo.user.infrastructure.persistence.repo.AccountJpaRepository;
import com.example.demo.user.infrastructure.security.JwtProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.example.demo.user")
@EnableConfigurationProperties(JwtProperties.class)
@EnableJpaRepositories(basePackageClasses = AccountJpaRepository.class)
@EntityScan(basePackageClasses = AccountEntity.class)
public class UserModuleConfiguration {
}
