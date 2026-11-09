package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class BookingQuotaServiceTest {

    private static final long MAX = 240;

    private static Booking booking(int id, String userId, String start, String end,
            BookingStatus status) {
        return new Booking(id, userId, "R101",
                LocalDateTime.parse(start), LocalDateTime.parse(end),
                2, "Study", status);
    }

    private static Booking active(int id, String userId, String start, String end) {
        return booking(id, userId, start, end, BookingStatus.ACTIVE);
    }

    // ---- Happy path / limit boundaries ----

    @Test
    void exactLimitIsAllowed() {
        // Example from the Issue: 150 existing + 90 proposed = 240 of 240
        List<Booking> existing = List.of(
                active(1, "s1", "2026-10-20T09:00", "2026-10-20T10:30"),  // 90
                active(2, "s1", "2026-10-20T13:00", "2026-10-20T14:00")); // 60
        Booking proposed = active(99, "s1", "2026-10-20T15:00", "2026-10-20T16:30"); // 90

        QuotaResult result = BookingQuotaService.checkDailyQuota(existing, proposed, MAX);

        assertTrue(result.isAllowed());
        assertEquals(240, result.getMinutesAfterBooking());
    }

    @Test
    void oneMinuteOverLimitIsRejected() {
        List<Booking> existing = List.of(
                active(1, "s1", "2026-10-20T09:00", "2026-10-20T11:31")); // 151
        Booking proposed = active(99, "s1", "2026-10-20T15:00", "2026-10-20T16:30"); // 90

        QuotaResult result = BookingQuotaService.checkDailyQuota(existing, proposed, MAX);

        assertFalse(result.isAllowed());
        assertEquals(241, result.getMinutesAfterBooking());
    }

    @Test
    void proposedAloneOverLimitIsRejected() {
        Booking proposed = active(99, "s1", "2026-10-20T08:00", "2026-10-20T13:00"); // 300

        QuotaResult result = BookingQuotaService.checkDailyQuota(List.of(), proposed, MAX);

        assertFalse(result.isAllowed());
        assertEquals(300, result.getMinutesAfterBooking());
    }

    @Test
    void emptyExistingListCountsOnlyProposed() {
        Booking proposed = active(99, "s1", "2026-10-20T10:00", "2026-10-20T11:00");

        QuotaResult result = BookingQuotaService.checkDailyQuota(List.of(), proposed, MAX);

        assertTrue(result.isAllowed());
        assertEquals(60, result.getMinutesAfterBooking());
    }

    // ---- Invalid input ----

    @Test
    void nullProposedThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> BookingQuotaService.checkDailyQuota(List.of(), null, MAX));
    }

    @Test
    void zeroMaxMinutesThrows() {
        Booking proposed = active(99, "s1", "2026-10-20T10:00", "2026-10-20T11:00");
        assertThrows(IllegalArgumentException.class,
                () -> BookingQuotaService.checkDailyQuota(List.of(), proposed, 0));
    }

    @Test
    void negativeMaxMinutesThrows() {
        Booking proposed = active(99, "s1", "2026-10-20T10:00", "2026-10-20T11:00");
        assertThrows(IllegalArgumentException.class,
                () -> BookingQuotaService.checkDailyQuota(List.of(), proposed, -1));
    }
}
