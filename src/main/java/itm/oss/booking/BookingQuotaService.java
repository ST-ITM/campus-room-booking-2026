package itm.oss.booking;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public final class BookingQuotaService {

    private BookingQuotaService() {
    }

    /**
     * Issue 8.
     */
    public static QuotaResult checkDailyQuota(
            List<Booking> existing,
            Booking proposed,
            long maxMinutesPerDay) {

        if (proposed == null) {
            throw new IllegalArgumentException("proposed booking must not be null");
        }
        if (maxMinutesPerDay <= 0) {
            throw new IllegalArgumentException("maxMinutesPerDay must be positive");
        }

        LocalDate proposedDate = proposed.getStart().toLocalDate();
        long total = minutesOf(proposed);

        for (int i = 0; i < existing.size(); i++) {
            Booking b = existing.get(i);

            boolean sameUser = b.getUserId().equalsIgnoreCase(proposed.getUserId());
            boolean sameDate = b.getStart().toLocalDate().equals(proposedDate);
            boolean active = b.getStatus() == BookingStatus.ACTIVE;

            if (sameUser && sameDate && active) {
                total += minutesOf(b);
            }
        }

        return new QuotaResult(total <= maxMinutesPerDay, total);
    }

    private static long minutesOf(Booking booking) {
        return Duration.between(booking.getStart(), booking.getEnd()).toMinutes();
    }
}
