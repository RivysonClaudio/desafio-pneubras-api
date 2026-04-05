package com.pneubras.api.mapper;

import com.pneubras.api.dto.response.LoginResponseDTO;
import com.pneubras.api.dto.response.RegisterResponseDTO;
import com.pneubras.api.entity.User;

public class AuthMapper {
    public static LoginResponseDTO toLoginDTO(User user, String token) {
        return new LoginResponseDTO(
            user.getId(), 
            user.getName(), 
            user.getEmail(), 
            user.getRole(), 
            token
        );
    }

    public static RegisterResponseDTO toRegisterDTO(User user) {
        return new RegisterResponseDTO(
            user.getId(), 
            user.getName(), 
            user.getEmail(), 
            user.getRole(),
            user.getCreatedAt()
        );
    }
}
