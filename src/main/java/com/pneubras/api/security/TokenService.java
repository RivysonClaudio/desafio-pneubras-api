package com.pneubras.api.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.pneubras.api.entity.User;

@Service
public class TokenService {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

            return JWT.create()
                .withIssuer("pneubras-api")
                .withSubject(user.getId().toString())
                .withIssuedAt(Instant.now())
                .withExpiresAt(generateExpirationDate())
                .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error generating token", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

            return JWT.require(algorithm)
                .build()
                .verify(token)
                .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

}
