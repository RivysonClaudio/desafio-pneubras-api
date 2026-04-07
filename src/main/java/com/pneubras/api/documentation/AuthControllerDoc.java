package com.pneubras.api.documentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.pneubras.api.dto.request.LoginRequestDTO;
import com.pneubras.api.dto.request.RegisterRequestDTO;
import com.pneubras.api.dto.response.LoginResponseDTO;
import com.pneubras.api.dto.response.RegisterResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "1. Autenticação", description = """
    Login e cadastro de usuários. Registro de novos usuários exige JWT de um **ADMIN**.
""")
public interface AuthControllerDoc {

    @Operation(
        summary = "Login",
        description = """
            Autentica por e-mail e senha. **Não exige JWT.**

            Retorna dados do usuário e o **token** JWT para uso no header `Authorization: Bearer <token>` nas demais rotas.
    """)
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto);

    @Operation(
        summary = "Registrar usuário",
        description = """
            Cria um novo usuário com papel **ADMIN**, **AGENT** ou **USER**.

            **Exige JWT** de um usuário com papel **ADMIN**. E-mail duplicado retorna erro de negócio.
    """)
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody @Valid RegisterRequestDTO dto);
}
