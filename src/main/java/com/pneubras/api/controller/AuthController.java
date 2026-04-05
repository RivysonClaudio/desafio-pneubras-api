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
import com.pneubras.api.exception.custom.EmailAlreadyExistsException;
import com.pneubras.api.mapper.AuthMapper;
import com.pneubras.api.security.AuthenticatedUser;
import com.pneubras.api.security.TokenService;
import com.pneubras.api.security.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final AuthenticationService authService;


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        User user = ((AuthenticatedUser) authentication.getPrincipal()).getUser();
        String token = tokenService.generateToken(user);
        return ResponseEntity.ok(AuthMapper.toLoginDTO(user, token));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody @Valid RegisterRequestDTO dto) {

        if (authService.findByEmail(dto.email()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = authService.register(dto);
        return ResponseEntity.ok(AuthMapper.toRegisterDTO(user));
    }

}
