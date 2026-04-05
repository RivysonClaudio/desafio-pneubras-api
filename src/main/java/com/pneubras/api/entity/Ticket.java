package com.pneubras.api.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;

import com.pneubras.api.exception.custom.InvalidStatusTransitionException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE tickets SET deleted_at = NOW() WHERE id = ?")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TicketPriority priority = TicketPriority.LOW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TicketStatus status = TicketStatus.OPEN;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @PrePersist
    public void onCreate() {
        switch (this.priority) {
            case LOW        -> this.dueAt = LocalDateTime.now().plusHours(72);
            case MEDIUM     -> this.dueAt = LocalDateTime.now().plusHours(48);
            case HIGH       -> this.dueAt = LocalDateTime.now().plusHours(24);
            case CRITICAL   -> this.dueAt = LocalDateTime.now().plusHours(8);
        }
    }

    public void updateStatus(TicketStatus next) {
        if (!this.status.canChangeTo(next)) {
            throw new InvalidStatusTransitionException("Cannot change status from " + this.status + " to " + next);
        }

        this.status = next;
    }
}
