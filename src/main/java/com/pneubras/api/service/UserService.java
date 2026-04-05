package com.pneubras.api.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pneubras.api.dto.request.RegisterRequestDTO;
import com.pneubras.api.entity.User;
import com.pneubras.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User register(RegisterRequestDTO dto) {
        
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setRole(dto.role());

        userRepository.save(user);

        return user;
    }

}
