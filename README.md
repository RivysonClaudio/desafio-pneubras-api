# Pneubras API

API REST em **Java 21** com **Spring Boot 4** para gestão de chamados (tickets), autenticação **JWT** e autorização por papéis (**ADMIN**, **AGENT**, **USER**). Persistência em **PostgreSQL** com **JPA/Hibernate**; documentação via **OpenAPI (Swagger)**.

---

## Sumário

- [Requisitos](#requisitos)
- [Configuração](#configuração)
- [Como executar](#como-executar)
- [Usuário administrador (bootstrap)](#usuário-administrador-bootstrap)
- [Autenticação](#autenticação)
- [Papéis e permissões](#papéis-e-permissões)
- [Endpoints](#endpoints)
- [Regras de negócio dos tickets](#regras-de-negócio-dos-tickets)
- [Documentação OpenAPI (Swagger)](#documentação-openapi-swagger)
- [Coleção Postman](#coleção-postman)
- [Testes](#testes)
- [Estrutura do projeto](#estrutura-do-projeto)

---

## Requisitos

| Ferramenta   | Versão / observação        |
|-------------|----------------------------|
| Java        | **21**                     |
| Maven       | Wrapper incluído (`./mvnw`) |
| PostgreSQL  | Banco acessível via JDBC   |

---

## Configuração

As variáveis abaixo são lidas pelo `application.properties`. Em desenvolvimento, o script `run.sh` carrega um arquivo **`.env`** na raiz do projeto (`export $(cat .env | xargs)`).

| Variável           | Descrição |
|--------------------|-----------|
| `DB_URL`           | JDBC URL (ex.: `jdbc:postgresql://localhost:5432/nome_do_banco`) |
| `DB_USERNAME`      | Usuário do banco |
| `DB_PASSWORD`      | Senha do banco |
| `DB_DRIVER`        | Classe do driver (ex.: `org.postgresql.Driver`) |
| `JWT_SECRET_KEY`   | Segredo para assinatura/validação dos tokens JWT |
| `ADMIN_EMAIL`      | E-mail do usuário **admin** criado no bootstrap |
| `ADMIN_PASSWORD`   | Senha em texto; é armazenada com **BCrypt** no primeiro start |

Outras propriedades relevantes (definidas no código):

- `spring.jpa.hibernate.ddl-auto=update` — esquema atualizado automaticamente (adequado a desenvolvimento; em produção prefira migrações explícitas).
- Entidades `User` e `Ticket` com **soft delete** (`@SQLDelete` / coluna `deleted_at`).

Crie um arquivo `.env` na raiz (não versionar credenciais reais) com todas as variáveis obrigatórias.

---

## Como executar

1. Suba o PostgreSQL e crie o banco referenciado em `DB_URL`.
2. Configure o `.env` conforme a tabela acima.
3. Na raiz do repositório:

```bash
chmod +x run.sh   # uma vez, se necessário
./run.sh
```

Alternativa sem o script (defina as variáveis de ambiente no shell ou IDE):

```bash
./mvnw spring-boot:run
```

A API sobe na porta padrão do Spring Boot (**8080**), salvo configuração contrária.

---

## Usuário administrador (bootstrap)

Na subida da aplicação, `ApiService.bootstrap()` verifica se já existe usuário com `ADMIN_EMAIL`. Se **não** existir, cria um usuário:

- Nome: `Api Administrator`
- E-mail e senha: `ADMIN_EMAIL` e `ADMIN_PASSWORD`
- Papel: **ADMIN**

Use esse usuário para **login** e para chamar rotas restritas a administrador (ex.: registro de novos usuários, exclusão de tickets).

---

## Autenticação

1. **Login:** `POST /api/v1/auth/login` com JSON `{ "email", "password" }`.
2. Resposta inclui um campo **`token`** (JWT).
3. Nas demais rotas protegidas, envie o header:

```http
Authorization: Bearer <token>
```

O filtro `SecurityFilter` valida o JWT, carrega o usuário e preenche o contexto de segurança do Spring.

Rotas **públicas** (sem JWT): `POST /api/v1/auth/login`, `GET` em `/swagger-ui/**` e `/v3/api-docs/**`. As demais exigem autenticação.

---

## Papéis e permissões

| Papel   | Uso típico |
|---------|------------|
| `ADMIN` | Gestão total: registrar usuários, apagar tickets, ver todos os tickets (lista/detalhe conforme regras no serviço). |
| `AGENT` | Atendimento: transição de status dos tickets; vê todos os tickets não removidos na listagem. |
| `USER`  | Abre e edita os **próprios** tickets; na listagem não vê tickets **CLOSED** nem soft-deleted. |

Autorização extra:

- `@PreAuthorize` em `TicketService`: transição de status — **ADMIN** ou **AGENT**; exclusão — **ADMIN** apenas.
- É necessário **`@EnableMethodSecurity`** na configuração de segurança para que `@PreAuthorize` seja aplicado.

---

## Endpoints

Base: `/api/v1`

### Autenticação (`/auth`)

| Método | Caminho        | JWT | Descrição |
|--------|----------------|-----|-----------|
| `POST` | `/auth/login`  | Não | Login; retorna token e dados do usuário. |
| `POST` | `/auth/register` | Sim (ADMIN) | Cadastro de novo usuário (`RegisterRequestDTO`). |

### Tickets (`/tickets`)

| Método   | Caminho              | Descrição |
|----------|----------------------|-----------|
| `GET`    | `/tickets`           | Lista paginada (`page`, `size`, `sort`). |
| `POST`   | `/tickets`           | Cria ticket (`TicketCreateResquestDTO`). |
| `GET`    | `/tickets/{id}`      | Detalhe do ticket. |
| `PATCH`  | `/tickets/{id}`      | Atualiza título/descrição (`TicketEditRequestDTO`); campos não nulos são aplicados. |
| `PATCH`  | `/tickets/{id}/status` | Altera status (`TicketStatusUpdateRequestDTO`); **ADMIN** ou **AGENT**. |
| `DELETE` | `/tickets/{id}`      | Remove ticket (soft delete via Hibernate); **ADMIN** apenas. Resposta **204 No Content**. |

---

## Regras de negócio dos tickets

### Prioridade e prazo (`dueAt`)

No `@PrePersist`, o prazo é calculado a partir da prioridade:

| Prioridade | Prazo (a partir da criação) |
|------------|-----------------------------|
| `LOW`      | +72 h |
| `MEDIUM`   | +48 h |
| `HIGH`     | +24 h |
| `CRITICAL` | +8 h  |

### Status

Valores do enum: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`.

Transições permitidas (sequência):

`OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`

A API aceita também aliases em português na deserialização JSON (ex.: `ABERTO`, `EM_PROGRESSO`, `RESOLVIDO`, `FECHADO`), conforme `TicketStatus.fromString`.

### Acesso por criador

Usuários **USER** só acessam ticket pelo `id` se forem o **criador** (comparação por **ID** do usuário, não por referência de entidade).

### Listagem

- **USER:** `findByCreatedByAndStatusNotAndDeletedAtIsNull(..., CLOSED)` — não lista tickets fechados nem removidos.
- **ADMIN / AGENT:** `findByDeletedAtIsNull` — todos os tickets ativos (não soft-deleted).

### Exclusão

`delete` usa repositório JPA; a entidade `Ticket` está anotada com `@SQLDelete` para **soft delete** (`deleted_at`), em linha com o padrão do projeto.

---

## Documentação OpenAPI (Swagger)

Com a API em execução:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (caminho padrão do springdoc-openapi; ajuste se `springdoc` for customizado).
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Esses caminhos são acessíveis **sem** JWT (configurados em `SecurityConfiguration`).

---

## Coleção Postman

O arquivo **`Pneubras-API.postman_collection.json`** na raiz contém exemplos de login (com script que grava o token), registro, CRUD de tickets, alteração de status e exclusão. Importe no Postman e ajuste `baseUrl` e credenciais.

---

## Testes

```bash
./mvnw test
```

> Os testes de integração podem exigir variáveis de ambiente ou perfil com banco em memória/H2, conforme a evolução do projeto. Se `contextLoads` falhar por `DB_DRIVER` ou similar, configure o ambiente de teste ou um `application-test.properties`.

---

## Estrutura do projeto (visão geral)

```
src/main/java/com/pneubras/api/
├── ApiApplication.java          # Bootstrap + execução do seed do admin
├── configuration/               # Segurança, Swagger
├── controller/                  # REST (Auth, Tickets)
├── dto/                         # Request/response records
├── entity/                      # JPA (User, Ticket, enums)
├── exception/                   # Handlers e exceções
├── mapper/                      # Mapeamento entidade → DTO
├── repository/                # Spring Data JPA
├── security/                  # JWT, filtro, UserDetails
└── service/                   # Regras de negócio (tickets, bootstrap)
```

---

## Licença e créditos

Projeto de desafio técnico **Pneubras**. Ajuste licença e autores conforme o repositório oficial.
