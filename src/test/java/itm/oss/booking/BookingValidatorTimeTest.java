package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

class BookingValidatorTimeTest {

    private static final Room ROOM = new Room("R101", "Study Room", 4, Set.of());

    @Test
    void acceptsExactOpeningClosingAndMaximumDurationBoundaries() {
        Booking booking = booking(
                LocalDateTime.of(2026, 10, 2, 9, 0),
                LocalDateTime.of(2026, 10, 2, 11, 0));

        assertDoesNotThrow(() -> BookingValidator.validate(
                booking, ROOM, LocalTime.of(9, 0), LocalTime.of(11, 0), 120));
    }

    @Test
    void rejectsInvalidBookingTimeRangesAndDates() {
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                booking(null, LocalDateTime.of(2026, 10, 2, 11, 0)),
                ROOM, LocalTime.of(9, 0), LocalTime.of(18, 0), 120));
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                booking(LocalDateTime.of(2026, 10, 2, 11, 0), LocalDateTime.of(2026, 10, 2, 10, 0)),
                ROOM, LocalTime.of(9, 0), LocalTime.of(18, 0), 120));
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                booking(LocalDateTime.of(2026, 10, 2, 23, 0), LocalDateTime.of(2026, 10, 3, 1, 0)),
                ROOM, LocalTime.of(9, 0), LocalTime.of(18, 0), 120));
    }

    @Test
    void rejectsInvalidOperatingHoursOutsideHoursAndLongDuration() {
        Booking normal = booking(
                LocalDateTime.of(2026, 10, 2, 10, 0), LocalDateTime.of(2026, 10, 2, 11, 0));

        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                normal, ROOM, LocalTime.of(18, 0), LocalTime.of(9, 0), 120));
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                booking(LocalDateTime.of(2026, 10, 2, 8, 59), LocalDateTime.of(2026, 10, 2, 10, 0)),
                ROOM, LocalTime.of(9, 0), LocalTime.of(18, 0), 120));
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                booking(LocalDateTime.of(2026, 10, 2, 17, 30), LocalDateTime.of(2026, 10, 2, 18, 1)),
                ROOM, LocalTime.of(9, 0), LocalTime.of(18, 0), 120));
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                normal, ROOM, LocalTime.of(9, 0), LocalTime.of(18, 0), 0));
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                normal, ROOM, LocalTime.of(9, 0), LocalTime.of(18, 0), 59));
    }

    @Test
    void rejectsZeroLengthBooking() {
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                booking(LocalDateTime.of(2026, 10, 2, 10, 0), LocalDateTime.of(2026, 10, 2, 10, 0)),
                ROOM, LocalTime.of(9, 0), LocalTime.of(18, 0), 120));
    }

    @Test
    void rejectsNullEnd() {
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                booking(LocalDateTime.of(2026, 10, 2, 10, 0), null),
                ROOM, LocalTime.of(9, 0), LocalTime.of(18, 0), 120));
    }

    @Test
    void rejectsNullOpenTime() {
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                booking(LocalDateTime.of(2026, 10, 2, 10, 0), LocalDateTime.of(2026, 10, 2, 11, 0)),
                ROOM, null, LocalTime.of(18, 0), 120));
    }

    @Test
    void rejectsNullCloseTime() {
        assertThrows(IllegalArgumentException.class, () -> BookingValidator.validate(
                booking(LocalDateTime.of(2026, 10, 2, 10, 0), LocalDateTime.of(2026, 10, 2, 11, 0)),
                ROOM, LocalTime.of(9, 0), null, 120));
    }

    private Booking booking(LocalDateTime start, LocalDateTime end) {
        return new Booking(1, "s2026001", "R101", start, end, 2, "Study", BookingStatus.ACTIVE);
    }
}
