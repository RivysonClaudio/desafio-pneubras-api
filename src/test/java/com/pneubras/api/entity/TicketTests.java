package com.pneubras.api.entity;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.pneubras.api.exception.custom.InvalidStatusTransitionException;


class TicketTests {

    @Nested
    @DisplayName("Default field values")
    class Defaults {

        @Test
        void newTicketHasLowPriorityAndOpenStatus() {
            Ticket ticket = new Ticket();
            assertEquals(TicketPriority.LOW, ticket.getPriority());
            assertEquals(TicketStatus.OPEN, ticket.getStatus());
        }
    }

    @Nested
    @DisplayName("updateStatus — valid transitions")
    class ValidTransitions {

        @Test
        void openToInProgress() {
            Ticket ticket = new Ticket();
            ticket.setStatus(TicketStatus.OPEN);

            ticket.updateStatus(TicketStatus.IN_PROGRESS);

            assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        }

        @Test
        void fullChainOpenToClosed() {
            Ticket ticket = new Ticket();
            ticket.setStatus(TicketStatus.OPEN);

            ticket.updateStatus(TicketStatus.IN_PROGRESS);
            assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());

            ticket.updateStatus(TicketStatus.RESOLVED);
            assertEquals(TicketStatus.RESOLVED, ticket.getStatus());

            ticket.updateStatus(TicketStatus.CLOSED);
            assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        }
    }

    @Nested
    @DisplayName("updateStatus — invalid transitions")
    class InvalidTransitions {

        static Stream<Arguments> invalidCases() {
            return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.OPEN),
                Arguments.of(TicketStatus.OPEN, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CLOSED)
            );
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @MethodSource("invalidCases")
        void shouldRejectInvalidTransition(TicketStatus current, TicketStatus next) {
            Ticket ticket = new Ticket();
            ticket.setStatus(current);

            InvalidStatusTransitionException ex = assertThrows(
                InvalidStatusTransitionException.class,
                () -> ticket.updateStatus(next)
            );

            assertTrue(ex.getMessage().contains("Cannot change status"));
            assertEquals(current, ticket.getStatus());
        }
    }

    @Nested
    @DisplayName("@PrePersist onCreate — dueAt by priority")
    class OnCreateDueAt {

        private void assertDueAtOffsetHours(TicketPriority priority, long expectedHours) {
            Ticket ticket = new Ticket();
            ticket.setPriority(priority);

            LocalDateTime t0 = LocalDateTime.now();
            ticket.onCreate();
            LocalDateTime t1 = LocalDateTime.now();

            LocalDateTime impliedInstant = ticket.getDueAt().minusHours(expectedHours);
            assertFalse(impliedInstant.isBefore(t0), "onCreate ran too early relative to dueAt");
            assertFalse(impliedInstant.isAfter(t1), "onCreate ran too late relative to dueAt");
        }

        @Test
        void lowPriorityAdds72Hours() {
            assertDueAtOffsetHours(TicketPriority.LOW, 72);
        }

        @Test
        void mediumPriorityAdds48Hours() {
            assertDueAtOffsetHours(TicketPriority.MEDIUM, 48);
        }

        @Test
        void highPriorityAdds24Hours() {
            assertDueAtOffsetHours(TicketPriority.HIGH, 24);
        }

        @Test
        void criticalPriorityAdds8Hours() {
            assertDueAtOffsetHours(TicketPriority.CRITICAL, 8);
        }
    }
}
