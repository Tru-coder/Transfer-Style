package com.example.transferstylerebuildmaven.configs.security;

import com.example.transferstylerebuildmaven.auditing.ApplicationAuditAware;
import com.example.transferstylerebuildmaven.repositories.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserRepository userRepository;

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Bean
    public UserDetailsService userDetailsService(){

        return username ->  userRepository
                .findByUsername(username)
                .orElseThrow(()
                        -> new UsernameNotFoundException("User with name " + username + " not found"));
    }

    @Bean
    public AuthenticationProvider myAuthenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuditorAware<Long> auditorAware(){
        return new ApplicationAuditAware();
    }
}
