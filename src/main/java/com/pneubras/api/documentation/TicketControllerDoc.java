package com.pneubras.api.documentation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.pneubras.api.dto.request.TicketCreateResquestDTO;
import com.pneubras.api.dto.request.TicketEditRequestDTO;
import com.pneubras.api.dto.request.TicketStatusUpdateRequestDTO;
import com.pneubras.api.dto.response.TicketDetailsResponseDTO;
import com.pneubras.api.dto.response.TicketResumeResponseDTO;
import com.pneubras.api.entity.TicketStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Tickets", description = """
    CRUD de chamados. Todas as rotas **exigem JWT**.

    - **USER**: vê e edita apenas os próprios tickets, excluindo registros removidos.
    - **ADMIN** e **AGENT**: veem todos os tickets não removidos na listagem.
    - Alteração de status: **ADMIN** ou **AGENT** apenas.
    - Exclusão (soft delete): **ADMIN** apenas.
""")
@SecurityRequirement(name = "bearer-jwt")
public interface TicketControllerDoc {

    @Operation(
        summary = "Listar tickets (paginado)",
        description = """
            Lista paginada de tickets. Query: `page` (0-based), `size`, `sort` (ex.: `createdAt,desc`).

            **USER**: apenas tickets criados por si, excluindo registros removidos.
            **ADMIN** e **AGENT**: todos os tickets não removidos.
    """)
    public ResponseEntity<Page<TicketResumeResponseDTO>> listTickets(
            @Parameter(description = "Status do ticket", required = false) @RequestParam(required = false) TicketStatus status,
            @Parameter(description = "Página", required = false) Pageable pageable);

    @Operation(
        summary = "Criar ticket",
        description = """
            Abre um novo chamado com título, descrição e prioridade. O prazo (`dueAt`) é calculado na criação conforme a prioridade.

            O criador é o usuário autenticado.
    """)
    public ResponseEntity<TicketDetailsResponseDTO> createTicket(@RequestBody @Valid TicketCreateResquestDTO dto);

    @Operation(
        summary = "Buscar ticket por id",
        description = """
            Retorna o detalhe completo do ticket.

            **USER** só acessa se for o criador. **ADMIN** e **AGENT** acessam qualquer ticket (desde que exista e não esteja removido).
    """)
    public ResponseEntity<TicketDetailsResponseDTO> findTicket(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id);

    @Operation(
        summary = "Atualizar título e descrição",
        description = """
            Atualiza parcialmente título e/ou descrição (apenas campos enviados com valor não nulo são aplicados).

            Mesmas regras de acesso que a busca por id (**USER** = apenas criador).
    """)
    public ResponseEntity<TicketDetailsResponseDTO> updateTicket(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id, 
            @RequestBody @Valid TicketEditRequestDTO dto);

    @Operation(
        summary = "Alterar status do ticket",
        description = """
            Avança o fluxo de status do chamado. **Apenas ADMIN ou AGENT** (demais papéis recebem 403).

            Transições válidas: **OPEN** → **IN_PROGRESS** → **RESOLVED** → **CLOSED**.
    """)
    public ResponseEntity<TicketDetailsResponseDTO> changeStatus(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id, 
            @RequestBody @Valid TicketStatusUpdateRequestDTO dto);

    @Operation(
        summary = "Remover ticket",
        description = """
            Exclusão lógica (soft delete). **Apenas ADMIN** (outros papéis recebem 403).

            Resposta **204** sem corpo em caso de sucesso.
    """)
    public ResponseEntity<Void> deleteTicket(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id);
}
