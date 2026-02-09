package com.shadsluiter.ordersapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // Configuration class is reference by Spring Boot when the application starts
public class PasswordEncoderConfig {
  
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt is a secure password hashing algorithm. It is the recommended password hashing algorithm by OWASP

        // could also use these options: 
        // return NoOpPasswordEncoder.getInstance(); // not recommended for production since it does not encrypt passwords

        // or:
        // return new Pbkdf2PasswordEncoder(); // PBKDF2 with HMAC SHA-1, 1024 iterations, and 128-bit salt

        // or:
        // return new SCryptPasswordEncoder(); // SCrypt is more secure than PBKDF2

        // or:
        // return new BCryptPasswordEncoder(10); // BCrypt with strength of 10 means 2^10 rounds of hashing
    }
}
