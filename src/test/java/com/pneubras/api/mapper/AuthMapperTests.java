package com.pneubras.api.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.pneubras.api.dto.response.LoginResponseDTO;
import com.pneubras.api.dto.response.RegisterResponseDTO;
import com.pneubras.api.entity.User;
import com.pneubras.api.entity.UserRole;

class AuthMapperTests {

    @Test
    void toLoginDTO_mapsUserFieldsAndToken() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        User user = new User();
        user.setId(id);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setRole(UserRole.ADMIN);
        user.setPassword("encoded");

        LoginResponseDTO dto = AuthMapper.toLoginDTO(user, "signed-jwt-value");

        assertEquals(id, dto.id());
        assertEquals("Alice", dto.name());
        assertEquals("alice@example.com", dto.email());
        assertEquals(UserRole.ADMIN, dto.role());
        assertEquals("signed-jwt-value", dto.token());
    }

    @Test
    void toRegisterDTO_mapsUserFieldsIncludingCreatedAt() {
        UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 7, 12, 30);

        User user = new User();
        user.setId(id);
        user.setName("Bob");
        user.setEmail("bob@example.com");
        user.setRole(UserRole.USER);
        user.setPassword("encoded");
        user.setCreatedAt(createdAt);

        RegisterResponseDTO dto = AuthMapper.toRegisterDTO(user);

        assertEquals(id, dto.id());
        assertEquals("Bob", dto.name());
        assertEquals("bob@example.com", dto.email());
        assertEquals(UserRole.USER, dto.role());
        assertEquals(createdAt, dto.createdAt());
    }
}
