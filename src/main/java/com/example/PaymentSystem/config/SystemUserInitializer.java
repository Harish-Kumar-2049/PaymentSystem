package com.example.PaymentSystem.config;

import com.example.PaymentSystem.entity.User;
import com.example.PaymentSystem.enums.UserRole;
import com.example.PaymentSystem.repository.UserRepository;
import com.example.PaymentSystem.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class SystemUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner ensureSystemUser() {
        return args -> {
            if (userRepository.existsByEmail(AuditLogService.SYSTEM_USER_EMAIL)) {
                return;
            }

            User systemUser = new User();
            systemUser.setEmail(AuditLogService.SYSTEM_USER_EMAIL);
            systemUser.setFullName("System User");
            systemUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            systemUser.setRole(UserRole.ADMIN);
            userRepository.save(systemUser);
        };
    }
}

