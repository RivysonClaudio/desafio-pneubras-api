package com.pneubras.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pneubras.api.dto.request.LoginRequestDTO;
import com.pneubras.api.dto.request.RegisterRequestDTO;
import com.pneubras.api.dto.response.LoginResponseDTO;
import com.pneubras.api.dto.response.RegisterResponseDTO;
import com.pneubras.api.entity.User;
import com.pneubras.api.exception.EmailAlreadyExistsException;
import com.pneubras.api.security.AuthenticatedUser;
import com.pneubras.api.security.TokenService;
import com.pneubras.api.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        User user = ((AuthenticatedUser) authentication.getPrincipal()).getUser();
        String token = tokenService.generateToken(user);
        LoginResponseDTO response = new LoginResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole(), token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody @Valid RegisterRequestDTO dto) {

        if (userService.findByEmail(dto.email()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = userService.register(dto);
        RegisterResponseDTO response = new RegisterResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
        return ResponseEntity.ok(response);
    }

}
