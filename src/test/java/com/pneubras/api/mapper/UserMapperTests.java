package com.pneubras.api.mapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.pneubras.api.dto.response.UserResponseDTO;
import com.pneubras.api.entity.User;
import com.pneubras.api.entity.UserRole;

class UserMapperTests {

    @Test
    void toDTO_mapsIdNameEmailRole() {
        UUID id = UUID.fromString("33333333-3333-3333-3333-333333333333");

        User user = new User();
        user.setId(id);
        user.setName("Carol");
        user.setEmail("carol@example.com");
        user.setRole(UserRole.AGENT);
        user.setPassword("encoded");

        UserResponseDTO dto = UserMapper.toDTO(user);

        assertEquals(id, dto.id());
        assertEquals("Carol", dto.name());
        assertEquals("carol@example.com", dto.email());
        assertEquals(UserRole.AGENT, dto.role());
    }
}
