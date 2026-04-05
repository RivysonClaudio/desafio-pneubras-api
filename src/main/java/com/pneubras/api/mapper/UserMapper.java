package com.pneubras.api.mapper;

import com.pneubras.api.dto.response.UserResponseDTO;
import com.pneubras.api.entity.User;

public class UserMapper {

    public static UserResponseDTO toDTO(User user) {
        return new UserResponseDTO(
            user.getId(), 
            user.getName(), 
            user.getEmail(), 
            user.getRole()
        );
    }
}
