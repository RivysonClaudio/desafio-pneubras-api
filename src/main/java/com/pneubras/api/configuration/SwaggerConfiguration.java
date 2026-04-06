package com.pneubras.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Pneubras API")
                .version("1.0.0")
                .description("""
                    API REST de chamados (tickets) com autenticação JWT e papéis **ADMIN**, **AGENT** e **USER**.

                    **Como testar rotas protegidas**
                    1. Use **POST /api/v1/auth/login** com email e senha.
                    2. Copie o `token` da resposta.
                    3. Clique em **Authorize**, informe: `Bearer <token>` (ou só o token, conforme a UI).
                    """))
            .components(new Components().addSecuritySchemes("bearer-jwt",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT retornado pelo login (`token` no JSON de resposta).")));
    }
}
