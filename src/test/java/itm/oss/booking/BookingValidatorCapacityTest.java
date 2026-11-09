
package itm.oss.booking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

class BookingValidatorCapacityTest {

    void check(int attendees, BookingStatus status) {
        Booking booking = new Booking(
            1, "user1", "R101",
            LocalDateTime.of(2026, 10, 16, 10, 0),
            LocalDateTime.of(2026, 10, 16, 11, 0),
            attendees, "Study", status
        );

        Room room = new Room("R101", "Study Room", 4, Set.of());

        BookingValidator.validate(
            booking, room,
            LocalTime.of(9, 0),
            LocalTime.of(18, 0),
            120
        );
    }

    @Test
    void validBooking() {
        assertDoesNotThrow(() -> check(2, BookingStatus.ACTIVE));
    }

    @Test
    void exactCapacity() {
        assertDoesNotThrow(() -> check(4, BookingStatus.ACTIVE));
    }

    @Test
    void zeroAttendees() {
        assertThrows(IllegalArgumentException.class,
            () -> check(0, BookingStatus.ACTIVE));
    }

    @Test
    void negativeAttendees() {
        assertThrows(IllegalArgumentException.class,
            () -> check(-1, BookingStatus.ACTIVE));
    }

    @Test
    void overCapacity() {
        assertThrows(IllegalArgumentException.class,
            () -> check(5, BookingStatus.ACTIVE));
    }

    @Test
    void nullStatus() {
        assertThrows(IllegalArgumentException.class,
            () -> check(2, null));
    }

    @Test
    void cancelledStatus() {
        assertDoesNotThrow(() -> check(2, BookingStatus.CANCELLED));
    }
}
