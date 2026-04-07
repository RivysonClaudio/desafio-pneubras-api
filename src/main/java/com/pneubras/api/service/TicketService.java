package com.pneubras.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pneubras.api.dto.request.TicketCreateResquestDTO;
import com.pneubras.api.dto.request.TicketEditRequestDTO;
import com.pneubras.api.entity.Ticket;
import com.pneubras.api.entity.TicketStatus;
import com.pneubras.api.entity.User;
import com.pneubras.api.entity.UserRole;
import com.pneubras.api.exception.base.ForbiddenException;
import com.pneubras.api.exception.base.NotFoundException;
import com.pneubras.api.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public Page<Ticket> findAll(User user, TicketStatus status, Pageable pageable) 
    {
        if (user.getRole().equals(UserRole.USER)) {
            return ticketRepository.searchTickets(user, status, pageable);
        }
        return ticketRepository.searchTickets(null, status, pageable);
    }

    @Transactional
    public Ticket create(User user, TicketCreateResquestDTO dto) {
        Ticket ticket = new Ticket();
        ticket.setTitle(dto.title());
        ticket.setDescription(dto.description());
        ticket.setPriority(dto.priority());
        ticket.setCreatedBy(user);
        return ticketRepository.save(ticket);
    }

    public Ticket findById(User user, Long id) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new NotFoundException("Ticket not found"));

        if (user.getRole().equals(UserRole.USER) && !ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not allowed to access this ticket");
        }

        return ticket;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Transactional
    public Ticket transitionStatus(User user, Long id, TicketStatus status) {

        Ticket ticket = findById(user, id);
        ticket.updateStatus(status);
        
        return ticket;
    }

    @Transactional
    public Ticket update(User user, Long id, TicketEditRequestDTO dto) {
        Ticket ticket = findById(user, id);

        if(dto.title() != null) {
            ticket.setTitle(dto.title());
        }

        if(dto.description() != null) {
            ticket.setDescription(dto.description());
        }
        
        return ticket;
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @Transactional
    public void delete(User user, Long id) {
        ticketRepository.deleteById(id);
    }
}
