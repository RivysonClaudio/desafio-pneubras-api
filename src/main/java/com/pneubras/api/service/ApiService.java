package com.pneubras.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pneubras.api.entity.User;
import com.pneubras.api.entity.UserRole;
import com.pneubras.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String ADMIN_EMAIL;

    @Value("${admin.password}")
    private String ADMIN_PASSWORD;

    public void bootstrap() {

        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            System.out.println("[API - BOOTSTRAP] Admin user already exists");
            return;
        }

        User user = new User();
        user.setName("Api Administrator");
        user.setEmail(ADMIN_EMAIL);
        user.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        user.setRole(UserRole.ADMIN);

        userRepository.save(user);

        System.out.println("[API - BOOTSTRAP] Admin user created: " + user.getEmail());
    }
}
