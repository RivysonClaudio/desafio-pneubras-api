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

import com.pneubras.api.documentation.TicketControllerDoc;
import com.pneubras.api.dto.request.TicketCreateRequestDTO;
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

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController implements TicketControllerDoc {

    private final TicketService ticketService;
    private final AuthenticationService authenticationService;

    @Override
    @GetMapping
    public ResponseEntity<Page<TicketResumeResponseDTO>> listTickets(
            @Parameter(description = "Status do ticket", required = false) @RequestParam(required = false) TicketStatus status,
            @Parameter(description = "Página", required = false) Pageable pageable
    ) {
        User user = authenticationService.getUser();
        Page<Ticket> tickets = ticketService.findAll(user, status, pageable);
        return ResponseEntity.ok(tickets.map(TicketMapper::toResumeDTO));
    }

    @Override
    @PostMapping
    public ResponseEntity<TicketDetailsResponseDTO> createTicket(@RequestBody @Valid TicketCreateRequestDTO dto)
    {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.create(user, dto);
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailsResponseDTO> findTicket(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id
    ) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.findById(user, id);
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<TicketDetailsResponseDTO> updateTicket(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id,
            @RequestBody @Valid TicketEditRequestDTO dto
    ) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.update(user, id, dto);
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @Override
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketDetailsResponseDTO> changeStatus(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id,
            @RequestBody @Valid TicketStatusUpdateRequestDTO dto
    ) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.transitionStatus(user, id, dto.status());
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @Parameter(description = "Identificador numérico do ticket", required = true) @PathVariable Long id
    ) {
        User user = authenticationService.getUser();
        ticketService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
