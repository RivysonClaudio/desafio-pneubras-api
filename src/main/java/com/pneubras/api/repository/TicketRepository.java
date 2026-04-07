package com.pneubras.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pneubras.api.entity.Ticket;
import com.pneubras.api.entity.TicketStatus;
import com.pneubras.api.entity.User;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
            SELECT t 
            FROM Ticket t 
            WHERE 
                    (:user IS NULL OR t.createdBy = :user) 
                AND (:status IS NULL OR t.status = :status) 
                AND t.deletedAt IS NULL
    """)
    Page<Ticket> searchTickets(User user, TicketStatus status, Pageable pageable);
}
