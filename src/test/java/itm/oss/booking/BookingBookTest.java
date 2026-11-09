package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingBookTest {

    private Booking first;
    private Booking second;
    private BookingBook book;

    @BeforeEach
    void setUp() {
        first = new Booking(
                5, "s1", "R101",
                LocalDateTime.parse("2026-10-20T14:00"),
                LocalDateTime.parse("2026-10-20T15:00"),
                3, "Team meeting", BookingStatus.ACTIVE);
        second = new Booking(
                6, "s2", "R102",
                LocalDateTime.parse("2026-10-21T09:00"),
                LocalDateTime.parse("2026-10-21T10:00"),
                2, "Study", BookingStatus.ACTIVE);
        book = new BookingBook(List.of(first, second));
    }

    private Booking replacementWithId(int id) {
        return new Booking(
                id, "s3", "R205",
                LocalDateTime.parse("2026-10-20T16:00"),
                LocalDateTime.parse("2026-10-20T17:00"),
                4, "Project review", BookingStatus.CANCELLED);
    }

    // ---------- update ----------

    @Test
    void updateReplacesAllEditableFields() {
        Booking result = book.update(5, replacementWithId(5));

        assertEquals("s3", result.getUserId());
        assertEquals("R205", result.getRoomId());
        assertEquals(LocalDateTime.parse("2026-10-20T16:00"), result.getStart());
        assertEquals(LocalDateTime.parse("2026-10-20T17:00"), result.getEnd());
        assertEquals(4, result.getAttendees());
        assertEquals("Project review", result.getPurpose());
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    void updatePreservesOriginalIdEvenIfReplacementHasAnotherId() {
        Booking result = book.update(5, replacementWithId(99));

        assertEquals(5, result.getId());
    }

    @Test
    void updateIsStoredInCollection() {
        Booking result = book.update(5, replacementWithId(99));

        List<Booking> all = book.all();
        assertEquals(2, all.size());
        assertEquals(result, all.get(0));
        assertEquals(second, all.get(1));
    }

    @Test
    void updateTargetsBookingThatIsNotFirst() {
        Booking result = book.update(6, replacementWithId(6));

        assertEquals(first, book.all().get(0));
        assertEquals(result, book.all().get(1));
        assertEquals("s3", book.all().get(1).getUserId());
    }

    @Test
    void updateWithNullReplacementThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> book.update(5, null));
    }

    @Test
    void updateWithUnknownIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> book.update(42, replacementWithId(42)));
    }

    @Test
    void failedUpdateLeavesCollectionUnchanged() {
        List<Booking> before = book.all();

        assertThrows(IllegalArgumentException.class,
                () -> book.update(42, replacementWithId(42)));
        assertThrows(IllegalArgumentException.class,
                () -> book.update(5, null));

        assertEquals(before, book.all());
    }

    // ---------- cancel ----------

    @Test
    void cancelChangesStatusOnly() {
        Booking result = book.cancel(5);

        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        assertEquals(first.getId(), result.getId());
        assertEquals(first.getUserId(), result.getUserId());
        assertEquals(first.getRoomId(), result.getRoomId());
        assertEquals(first.getStart(), result.getStart());
        assertEquals(first.getEnd(), result.getEnd());
        assertEquals(first.getAttendees(), result.getAttendees());
        assertEquals(first.getPurpose(), result.getPurpose());
    }

    @Test
    void cancelIsStoredInCollection() {
        Booking result = book.cancel(5);

        assertEquals(result, book.all().get(0));
        assertEquals(BookingStatus.ACTIVE, book.all().get(1).getStatus());
    }

    @Test
    void cancelTargetsBookingThatIsNotFirst() {
        Booking result = book.cancel(6);

        assertEquals(first, book.all().get(0));
        assertEquals(BookingStatus.ACTIVE, book.all().get(0).getStatus());
        assertEquals(result, book.all().get(1));
        assertEquals(BookingStatus.CANCELLED, book.all().get(1).getStatus());
    }

    @Test
    void cancellingAlreadyCancelledBookingKeepsItCancelled() {
        book.cancel(5);
        Booking result = book.cancel(5);

        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        assertEquals(5, result.getId());
    }

    @Test
    void cancelWithUnknownIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> book.cancel(42));
    }

    @Test
    void failedCancelLeavesCollectionUnchanged() {
        List<Booking> before = book.all();

        assertThrows(IllegalArgumentException.class,
                () -> book.cancel(42));

        assertEquals(before, book.all());
    }
}
