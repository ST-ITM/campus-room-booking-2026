package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class BookingFilterValidationTest {

    private final Booking b1 = booking(1, "s1", "R101", 2, BookingStatus.ACTIVE);
    private final Booking b2 = booking(2, "s2", "R102", 5, BookingStatus.ACTIVE);
    private final Booking b3 = booking(3, "s1", "R102", 4, BookingStatus.CANCELLED);

    private final List<Booking> bookings = List.of(b1, b2, b3);

    // Creates a booking with fixed times so each test only sets the fields it needs
    private static Booking booking(int id, String userId, String roomId,
            int attendees, BookingStatus status) {
        return new Booking(id, userId, roomId,
                LocalDateTime.parse("2026-10-02T10:00"),
                LocalDateTime.parse("2026-10-02T11:00"),
                attendees, "Study", status);
    }

    @Test
    void zeroMinAttendeesThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> BookingFilter.filter(bookings, null, null, null, 0));
    }

    @Test
    void negativeMinAttendeesThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> BookingFilter.filter(bookings, null, null, null, -1));
    }

    @Test
    void invalidMinAttendeesThrowsExceptionEvenForEmptyList() {
        assertThrows(IllegalArgumentException.class,
                () -> BookingFilter.filter(List.of(), null, null, null, 0));
    }

    @Test
    void blankStringCriteriaAreNotApplied() {
        String[] blanks = {"", "   ", "\t", " \t "};

        // Apply the same blank inputs to both fields so they stay consistent
        for (String blank : blanks) {
            assertEquals(List.of(b1, b2, b3),
                    BookingFilter.filter(bookings, blank, null, null, null),
                    "userId blank: [" + blank + "]");
            assertEquals(List.of(b1, b2, b3),
                    BookingFilter.filter(bookings, null, blank, null, null),
                    "roomId blank: [" + blank + "]");
        }
    }

    @Test
    void blankStringCriteriaDoNotDisableOtherCriteria() {
        List<Booking> result = BookingFilter.filter(
                bookings, "  ", "", BookingStatus.CANCELLED, null);

        assertEquals(List.of(b3), result);
    }

    @Test
    void returnsEmptyListWhenNothingMatches() {
        List<Booking> result = BookingFilter.filter(bookings, "unknown", null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyListForEmptyBookingList() {
        List<Booking> result = BookingFilter.filter(List.of(), "s1", null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void excludesBookingThatMatchesOnlySomeCriteria() {
        List<Booking> result = BookingFilter.filter(bookings, "s1", "R101", null, 3);

        // b1 matches user and room but has only 2 attendees, b3 is in another room
        assertTrue(result.isEmpty());
    }

    @Test
    void doesNotModifySuppliedList() {
        List<Booking> supplied = new ArrayList<>(bookings);

        BookingFilter.filter(supplied, "s1", null, BookingStatus.ACTIVE, 1);

        assertEquals(List.of(b1, b2, b3), supplied);
    }

    @Test
    void returnedListIsIndependentOfSuppliedList() {
        List<Booking> supplied = new ArrayList<>(bookings);

        List<Booking> result = BookingFilter.filter(supplied, null, null, null, null);

        // Mutate the supplied list instead of the result, so an unmodifiable
        // result (e.g. from Stream.toList()) is still accepted
        supplied.clear();

        assertEquals(List.of(b1, b2, b3), result);
    }
}