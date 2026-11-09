package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

class BookingValidatorIdentityTest {

    private static final Room ROOM = new Room("R101", "Study Room", 4, Set.of());
    private static final LocalTime OPEN_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(18, 0);
    private static final long MAX_DURATION_MINUTES = 120;

    @Test
    void acceptsBookingWithMatchingRequiredIdentityValues() {
        assertDoesNotThrow(() -> validate(booking("s2026001", "R101"), ROOM));
    }

    @Test
    void rejectsMissingBookingOrRoom() {
        assertThrows(IllegalArgumentException.class, () -> validate(null, ROOM));
        assertThrows(IllegalArgumentException.class, () -> validate(booking("s2026001", "R101"), null));
    }

    @Test
    void rejectsNullOrBlankUserId() {
        assertThrows(IllegalArgumentException.class, () -> validate(booking(null, "R101"), ROOM));
        assertThrows(IllegalArgumentException.class, () -> validate(booking(" ", "R101"), ROOM));
    }

    @Test
    void rejectsNullOrBlankRoomId() {
        assertThrows(IllegalArgumentException.class, () -> validate(booking("s2026001", null), ROOM));
        assertThrows(IllegalArgumentException.class, () -> validate(booking("s2026001", ""), ROOM));
    }

    @Test
    void rejectsRoomIdThatDoesNotMatchSuppliedRoom() {
        assertThrows(IllegalArgumentException.class, () -> validate(booking("s2026001", "R102"), ROOM));
    }

    @Test
    void rejectsRoomIdThatDiffersOnlyInCase() {
        assertThrows(IllegalArgumentException.class, () -> validate(booking("s2026001", "r101"), ROOM));
    }

    private void validate(Booking booking, Room room) {
        BookingValidator.validate(booking, room, OPEN_TIME, CLOSE_TIME, MAX_DURATION_MINUTES);
    }

    private Booking booking(String userId, String roomId) {
        return new Booking(1, userId, roomId,
                LocalDateTime.of(2026, 10, 2, 10, 0),
                LocalDateTime.of(2026, 10, 2, 11, 0),
                2, "Study", BookingStatus.ACTIVE);
    }
}
