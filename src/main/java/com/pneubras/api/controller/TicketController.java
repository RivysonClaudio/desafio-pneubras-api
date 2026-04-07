package com.pneubras.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pneubras.api.dto.request.TicketCreateResquestDTO;
import com.pneubras.api.dto.request.TicketEditRequestDTO;
import com.pneubras.api.dto.request.TicketStatusUpdateRequestDTO;
import com.pneubras.api.dto.response.TicketDetailsResponseDTO;
import com.pneubras.api.dto.response.TicketResumeResponseDTO;
import com.pneubras.api.entity.Ticket;
import com.pneubras.api.entity.TicketStatus;
import com.pneubras.api.entity.User;
import com.pneubras.api.mapper.TicketMapper;
import com.pneubras.api.security.AuthenticationService;
import com.pneubras.api.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = """
    CRUD de chamados. Todas as rotas **exigem JWT**.

    - **USER**: vê e edita apenas os próprios tickets; na listagem não aparecem tickets **CLOSED** nem removidos.
    - **ADMIN** e **AGENT**: veem todos os tickets ativos na listagem.
    - Alteração de status: **ADMIN** ou **AGENT** apenas.
    - Exclusão (soft delete): **ADMIN** apenas.
    """)
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final AuthenticationService authenticationService;

    @GetMapping
    @Operation(
        summary = "Listar tickets (paginado)",
        description = """
            Lista paginada de tickets. Query: `page` (0-based), `size`, `sort` (ex.: `createdAt,desc`).

            **USER**: apenas tickets criados por si, excluindo registros removidos.
            **ADMIN** e **AGENT**: todos os tickets não removidos.
    """)
    public ResponseEntity<Page<TicketResumeResponseDTO>> findAll(@RequestParam(required = false) TicketStatus status, Pageable pageable) {
        User user = authenticationService.getUser();
        Page<Ticket> tickets = ticketService.findAll(user, status, pageable);
        return ResponseEntity.ok(tickets.map(TicketMapper::toResumeDTO));
    }

    @PostMapping
    @Operation(
        summary = "Criar ticket",
        description = """
            Abre um novo chamado com título, descrição e prioridade. O prazo (`dueAt`) é calculado na criação conforme a prioridade.

            O criador é o usuário autenticado.
    """)
    public ResponseEntity<TicketDetailsResponseDTO> create(@RequestBody TicketCreateResquestDTO dto) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.create(user, dto);
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar ticket por id",
        description = """
            Retorna o detalhe completo do ticket.

            **USER** só acessa se for o criador. **ADMIN** e **AGENT** acessam qualquer ticket (desde que exista e não esteja removido).
    """)
    public ResponseEntity<TicketDetailsResponseDTO> findById(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.findById(user, id);
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Atualizar título e descrição",
        description = """
            Atualiza parcialmente título e/ou descrição (apenas campos enviados com valor não nulo são aplicados).

            Mesmas regras de acesso que a busca por id (**USER** = apenas criador).
    """)
    public ResponseEntity<TicketDetailsResponseDTO> update(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id,
            @RequestBody TicketEditRequestDTO dto) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.update(user, id, dto);
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Alterar status do ticket",
        description = """
            Avança o fluxo de status do chamado. **Apenas ADMIN ou AGENT** (demais papéis recebem 403).

            Transições válidas: **OPEN** → **IN_PROGRESS** → **RESOLVED** → **CLOSED**.

            No JSON, o campo `status` aceita os enums em inglês ou aliases em português (ex.: `EM_PROGRESSO`, `RESOLVIDO`).
    """)
    public ResponseEntity<TicketDetailsResponseDTO> changeStatus(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id,
            @RequestBody TicketStatusUpdateRequestDTO dto) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.transitionStatus(user, id, dto.status());
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Remover ticket",
        description = """
            Exclusão lógica (soft delete). **Apenas ADMIN** (outros papéis recebem 403).

            Resposta **204** sem corpo em caso de sucesso.
    """)
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id) {
        User user = authenticationService.getUser();
        ticketService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
