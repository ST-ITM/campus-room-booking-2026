package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class BookingFilterTest {

    private final Booking b1 = booking(1, "s1", "R101", 2, BookingStatus.ACTIVE);
    private final Booking b2 = booking(2, "s2", "R102", 5, BookingStatus.ACTIVE);
    private final Booking b3 = booking(3, "s1", "R102", 4, BookingStatus.CANCELLED);
    private final Booking b4 = booking(4, "s3", "R101", 6, BookingStatus.ACTIVE);

    private final List<Booking> bookings = List.of(b1, b2, b3, b4);

    // Creates a booking with fixed times so each test only sets the fields it needs
    private static Booking booking(int id, String userId, String roomId,
            int attendees, BookingStatus status) {
        return new Booking(id, userId, roomId,
                LocalDateTime.parse("2026-10-02T10:00"),
                LocalDateTime.parse("2026-10-02T11:00"),
                attendees, "Study", status);
    }

    @Test
    void returnsAllBookingsWhenNoCriteriaAreGiven() {
        List<Booking> result = BookingFilter.filter(bookings, null, null, null, null);

        assertEquals(List.of(b1, b2, b3, b4), result);
    }

    @Test
    void filtersByUserId() {
        List<Booking> result = BookingFilter.filter(bookings, "s1", null, null, null);

        assertEquals(List.of(b1, b3), result);
    }

    @Test
    void filtersByRoomId() {
        List<Booking> result = BookingFilter.filter(bookings, null, "R102", null, null);

        assertEquals(List.of(b2, b3), result);
    }

    @Test
    void filtersByStatus() {
        List<Booking> result = BookingFilter.filter(
                bookings, null, null, BookingStatus.CANCELLED, null);

        assertEquals(List.of(b3), result);
    }

    @Test
    void filtersByMinimumAttendees() {
        // No booking has exactly 3 attendees, so the equality case is left to
        // includesBookingWhoseAttendeesEqualMinimum
        List<Booking> result = BookingFilter.filter(bookings, null, null, null, 3);

        assertEquals(List.of(b2, b3, b4), result);
    }

    @Test
    void includesBookingWhoseAttendeesEqualMinimum() {
        List<Booking> result = BookingFilter.filter(bookings, null, null, null, 4);

        // b3 has exactly 4 attendees and must be included
        assertEquals(List.of(b2, b3, b4), result);
    }

    @Test
    void matchesUserIdCaseInsensitively() {
        List<Booking> result = BookingFilter.filter(bookings, "S1", null, null, null);

        assertEquals(List.of(b1, b3), result);
    }

    @Test
    void matchesRoomIdCaseInsensitively() {
        List<Booking> result = BookingFilter.filter(bookings, null, "r101", null, null);

        assertEquals(List.of(b1, b4), result);
    }

    @Test
    void trimsUserIdAndRoomIdCriteriaBeforeMatching() {
        List<Booking> result = BookingFilter.filter(bookings, "  s1 ", " R101  ", null, null);

        assertEquals(List.of(b1), result);
    }

    @Test
    void matchesWholeIdsNotPrefixes() {
        // "s1" and "R101" are prefixes of "s10" and "R1010", so a partial match
        // such as startsWith or contains would wrongly include this booking
        Booking similar = booking(5, "s10", "R1010", 3, BookingStatus.ACTIVE);
        List<Booking> withSimilarIds = List.of(b1, similar);

        assertEquals(List.of(b1),
                BookingFilter.filter(withSimilarIds, "s1", null, null, null));
        assertEquals(List.of(b1),
                BookingFilter.filter(withSimilarIds, null, "R101", null, null));
    }

    @Test
    void combinesUserIdAndStatusUsingAnd() {
        List<Booking> result = BookingFilter.filter(
                bookings, "s1", null, BookingStatus.ACTIVE, null);

        // b3 matches the user ID but is cancelled, so only b1 matches both criteria
        assertEquals(List.of(b1), result);
    }

    @Test
    void combinesAllFourCriteriaUsingAnd() {
        List<Booking> result = BookingFilter.filter(
                bookings, "s2", "r102", BookingStatus.ACTIVE, 5);

        assertEquals(List.of(b2), result);
    }

    @Test
    void preservesOriginalOrderOfSuppliedList() {
        List<Booking> reversed = List.of(b4, b3, b2, b1);

        List<Booking> result = BookingFilter.filter(
                reversed, null, null, BookingStatus.ACTIVE, null);

        assertEquals(List.of(b4, b2, b1), result);
    }
}