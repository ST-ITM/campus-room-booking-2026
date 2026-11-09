package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class BookingQuotaServiceCountingRulesTest {

    private static final long MAX_MINUTES = 240;

    private static Booking booking(int id, String userId, String start, String end,
            BookingStatus status) {
        return new Booking(id, userId, "R101",
                LocalDateTime.parse(start), LocalDateTime.parse(end),
                2, "Study", status);
    }

    private static Booking active(int id, String userId, String start, String end) {
        return booking(id, userId, start, end, BookingStatus.ACTIVE);
    }

    private static Booking proposedForS1() {
        return active(99, "s1", "2026-10-02T14:00", "2026-10-02T15:00");
    }

    @Test
    void bookingsFromOtherUsersAreExcluded() {
        List<Booking> existing = List.of(
                active(1, "s2", "2026-10-02T09:00", "2026-10-02T13:00"));

        QuotaResult result = BookingQuotaService.checkDailyQuota(
                existing, proposedForS1(), MAX_MINUTES);

        assertTrue(result.isAllowed());
        assertEquals(60, result.getMinutesAfterBooking());
    }

    @Test
    void bookingsOnOtherDatesAreExcluded() {
        List<Booking> existing = List.of(
                active(1, "s1", "2026-10-01T09:00", "2026-10-01T13:00"),
                active(2, "s1", "2026-10-03T09:00", "2026-10-03T13:00"));

        QuotaResult result = BookingQuotaService.checkDailyQuota(
                existing, proposedForS1(), MAX_MINUTES);

        assertTrue(result.isAllowed());
        assertEquals(60, result.getMinutesAfterBooking());
    }

    @Test
    void cancelledBookingsAreExcluded() {
        List<Booking> existing = List.of(
                booking(1, "s1", "2026-10-02T09:00", "2026-10-02T13:00",
                        BookingStatus.CANCELLED));

        QuotaResult result = BookingQuotaService.checkDailyQuota(
                existing, proposedForS1(), MAX_MINUTES);

        assertTrue(result.isAllowed());
        assertEquals(60, result.getMinutesAfterBooking());
    }

    @Test
    void activeSameUserSameDateBookingIsIncluded() {
        List<Booking> existing = List.of(
                active(1, "s1", "2026-10-02T09:00", "2026-10-02T11:00"));

        QuotaResult result = BookingQuotaService.checkDailyQuota(
                existing, proposedForS1(), MAX_MINUTES);

        assertTrue(result.isAllowed());
        assertEquals(180, result.getMinutesAfterBooking());
    }

    @Test
    void userMatchingIsCaseInsensitive() {
        List<Booking> existing = List.of(
                active(1, "S1", "2026-10-02T09:00", "2026-10-02T12:20"));

        QuotaResult result = BookingQuotaService.checkDailyQuota(
                existing, proposedForS1(), MAX_MINUTES);

        assertFalse(result.isAllowed());
        assertEquals(260, result.getMinutesAfterBooking());
    }

    @Test
    void onlyMatchingBookingsAreCountedInMixedList() {
        List<Booking> existing = List.of(
                active(1, "s1", "2026-10-02T09:00", "2026-10-02T10:00"),
                active(2, "S1", "2026-10-02T10:30", "2026-10-02T11:00"),
                active(3, "s2", "2026-10-02T09:00", "2026-10-02T13:00"),
                active(4, "s1", "2026-10-01T09:00", "2026-10-01T13:00"),
                booking(5, "s1", "2026-10-02T16:00", "2026-10-02T20:00",
                        BookingStatus.CANCELLED));

        QuotaResult result = BookingQuotaService.checkDailyQuota(
                existing, proposedForS1(), MAX_MINUTES);

        assertTrue(result.isAllowed());
        assertEquals(150, result.getMinutesAfterBooking());
    }
}
