package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConflictService tests for Issue 2")
class ConflictServiceTest {

    private static final String ROOM_A = "R101";
    private static final String ROOM_B = "R202";
    
    private Booking booking(int id, String roomId, String start, String end, BookingStatus status) {
        return new Booking(
                id,
                "user" + id,
                roomId,
                LocalDateTime.parse("2026-10-02T" + start),
                LocalDateTime.parse("2026-10-02T" + end),
                5,
                "study",
                status);
    }

    @Test
    @DisplayName("Partial overlap in the same room is a conflict")
    void partialOverlapIsConflict() {
        Booking existing = booking(1, ROOM_A, "10:00", "11:00", BookingStatus.ACTIVE);
        Booking proposed = booking(2, ROOM_A, "10:30", "11:30", BookingStatus.ACTIVE);

        assertTrue(ConflictService.hasConflict(List.of(existing), proposed),
                "10:00-11:00 and 10:30-11:30 overlap, so they should conflict");
    }

    @Test
    @DisplayName("Proposed booking contained inside existing booking is a conflict")
    void proposedInsideExistingIsConflict() {
        Booking existing = booking(1, ROOM_A, "10:00", "12:00", BookingStatus.ACTIVE);
        Booking proposed = booking(2, ROOM_A, "10:30", "11:00", BookingStatus.ACTIVE);

        assertTrue(ConflictService.hasConflict(List.of(existing), proposed),
                "10:30-11:00 is inside 10:00-12:00, so they should conflict");
    }

    @Test
    @DisplayName("Existing booking contained inside proposed booking is a conflict")
    void existingInsideProposedIsConflict() {
        Booking existing = booking(1, ROOM_A, "10:30", "11:00", BookingStatus.ACTIVE);
        Booking proposed = booking(2, ROOM_A, "10:00", "12:00", BookingStatus.ACTIVE);

        assertTrue(ConflictService.hasConflict(List.of(existing), proposed),
                "10:00-12:00 covers 10:30-11:00, so they should conflict");
    }

    @Test
    @DisplayName("Identical time range in the same room is a conflict")
    void identicalTimeIsConflict() {
        Booking existing = booking(1, ROOM_A, "10:00", "11:00", BookingStatus.ACTIVE);
        Booking proposed = booking(2, ROOM_A, "10:00", "11:00", BookingStatus.ACTIVE);

        assertTrue(ConflictService.hasConflict(List.of(existing), proposed),
                "Same room and same time should conflict");
    }

    @Test
    @DisplayName("Touching at the end boundary is not a conflict")
    void touchingAtEndIsNotConflict() {
        Booking existing = booking(1, ROOM_A, "10:00", "11:00", BookingStatus.ACTIVE);
        Booking proposed = booking(2, ROOM_A, "11:00", "12:00", BookingStatus.ACTIVE);

        assertFalse(ConflictService.hasConflict(List.of(existing), proposed),
                "10:00-11:00 and 11:00-12:00 only touch at 11:00, so no conflict");
    }

    @Test
    @DisplayName("Touching at the start boundary is not a conflict")
    void touchingAtStartIsNotConflict() {
        Booking existing = booking(1, ROOM_A, "11:00", "12:00", BookingStatus.ACTIVE);
        Booking proposed = booking(2, ROOM_A, "10:00", "11:00", BookingStatus.ACTIVE);

        assertFalse(ConflictService.hasConflict(List.of(existing), proposed),
                "10:00-11:00 and 11:00-12:00 only touch at 11:00, so no conflict");
    }

    @Test
    @DisplayName("Same time in a different room is not a conflict")
    void differentRoomIsNotConflict() {
        Booking existing = booking(1, ROOM_A, "10:00", "11:00", BookingStatus.ACTIVE);
        Booking proposed = booking(2, ROOM_B, "10:00", "11:00", BookingStatus.ACTIVE);

        assertFalse(ConflictService.hasConflict(List.of(existing), proposed),
                "Bookings in different rooms should not conflict");
    }

    @Test
    @DisplayName("Room IDs differing only in letter case are different rooms")
    void roomIdDifferingOnlyInCaseIsNotConflict() {
        Booking existing = booking(1, "R101", "10:00", "11:00", BookingStatus.ACTIVE);
        Booking proposed = booking(2, "r101", "10:30", "11:30", BookingStatus.ACTIVE);

        assertFalse(ConflictService.hasConflict(List.of(existing), proposed),
                "Room IDs use exact matching, so R101 and r101 should not conflict");
    }

    @Test
    @DisplayName("Overlapping CANCELLED booking is not a conflict")
    void cancelledBookingIsNotConflict() {
        Booking existing = booking(1, ROOM_A, "10:00", "11:00", BookingStatus.CANCELLED);
        Booking proposed = booking(2, ROOM_A, "10:30", "11:30", BookingStatus.ACTIVE);

        assertFalse(ConflictService.hasConflict(List.of(existing), proposed),
                "CANCELLED bookings should be ignored");
    }

    @Test
    @DisplayName("No existing bookings means no conflict")
    void emptyListIsNotConflict() {
        Booking proposed = booking(1, ROOM_A, "10:00", "11:00", BookingStatus.ACTIVE);

        assertFalse(ConflictService.hasConflict(List.of(), proposed),
                "With no existing bookings there is nothing to conflict with");
    }
    
    @Test
    @DisplayName("Skips a CANCELLED booking and still finds a later overlapping ACTIVE booking")
    void cancelledBookingFirstThenOverlappingActiveIsConflict() {
        Booking cancelled = booking(1, ROOM_A, "10:00", "11:00", BookingStatus.CANCELLED);
        Booking active = booking(2, ROOM_A, "10:30", "11:30", BookingStatus.ACTIVE);
        Booking proposed = booking(3, ROOM_A, "10:45", "11:15", BookingStatus.ACTIVE);

        assertTrue(ConflictService.hasConflict(List.of(cancelled, active), proposed),
                "A CANCELLED booking must be skipped without ending the search, "
                        + "so the later overlapping ACTIVE booking is still found");
    }
}