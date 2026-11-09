package itm.oss.booking;

import java.util.List;

public final class ConflictService {

    private ConflictService() {
    }

    /**
     * Issue 2.
     * Returns true if any ACTIVE existing booking uses the same room as the
     * proposed booking and overlaps it in time. Bookings that only touch at
     * a boundary do not conflict. 
     * The status of the proposed booking is not checked; only existing bookings' status matters.
     */
    public static boolean hasConflict(
            List<Booking> existing,
            Booking proposed) {

        for (Booking booking : existing) {
            if (booking.getStatus() != BookingStatus.ACTIVE) {
                continue;
            }
            if (!booking.getRoomId().equals(proposed.getRoomId())) {
                continue;
            }
            if (overlaps(booking, proposed)) {
                return true;
            }
        }
        return false;
    }

    private static boolean overlaps(Booking a, Booking b) {
        return a.getStart().isBefore(b.getEnd())
                && b.getStart().isBefore(a.getEnd());
    }
}
