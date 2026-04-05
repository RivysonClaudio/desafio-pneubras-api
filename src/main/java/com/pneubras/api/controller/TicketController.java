package com.pneubras.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pneubras.api.dto.request.TicketCreateResquestDTO;
import com.pneubras.api.dto.request.TicketEditRequestDTO;
import com.pneubras.api.dto.request.TicketStatusUpdateRequestDTO;
import com.pneubras.api.dto.response.TicketDetailsResponseDTO;
import com.pneubras.api.dto.response.TicketResumeResponseDTO;
import com.pneubras.api.entity.Ticket;
import com.pneubras.api.entity.User;
import com.pneubras.api.mapper.TicketMapper;
import com.pneubras.api.security.AuthenticationService;
import com.pneubras.api.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<Page<TicketResumeResponseDTO>> findAll(Pageable pageable) {
        User user = authenticationService.getUser();
        Page<Ticket> tickets = ticketService.findAll(user, pageable);
        return ResponseEntity.ok(tickets.map(TicketMapper::toResumeDTO));
    }

    @PostMapping
    public ResponseEntity<TicketDetailsResponseDTO> create(@RequestBody TicketCreateResquestDTO dto) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.create(user, dto);
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailsResponseDTO> findById(@PathVariable Long id) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.findById(user, id);
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketDetailsResponseDTO> update(@PathVariable Long id, @RequestBody TicketEditRequestDTO dto) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.update(user, id, dto);
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketDetailsResponseDTO> changeStatus(@PathVariable Long id, @RequestBody TicketStatusUpdateRequestDTO dto) {
        User user = authenticationService.getUser();
        Ticket ticket = ticketService.transitionStatus(user, id, dto.status());
        return ResponseEntity.ok(TicketMapper.toDetailsDTO(ticket));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = authenticationService.getUser();
        ticketService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
