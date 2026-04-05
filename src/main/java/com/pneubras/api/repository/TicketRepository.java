package com.pneubras.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pneubras.api.entity.Ticket;
import com.pneubras.api.entity.TicketStatus;
import com.pneubras.api.entity.User;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByIdAndDeletedAtIsNull(Long id);
    Page<Ticket> findByDeletedAtIsNull(Pageable pageable);
    Page<Ticket> findByCreatedByAndDeletedAtIsNull(User createdBy, Pageable pageable);
    Page<Ticket> findByCreatedByAndStatusNotAndDeletedAtIsNull(User createdBy, TicketStatus status, Pageable pageable);
}
