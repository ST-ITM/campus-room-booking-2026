package itm.oss.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public final class AvailabilityService {

    private AvailabilityService() {
    }

    /**
     * Issue 3.
     */
    public static List<Room> findAvailableRooms(
            List<Room> rooms,
            List<Booking> bookings,
            LocalDateTime start,
            LocalDateTime end,
            int minCapacity) {

        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end must be non-null");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start must be before end");
        }
        if (minCapacity <= 0) {
            throw new IllegalArgumentException("Minimum capacity must be greater than zero");
        }
        List<Room> result = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getCapacity() < minCapacity) {
                continue;
            }
            if (isAvailable(room, bookings, start, end)) {
                result.add(room);
            }
        }

        return result;
    }

    private static boolean isAvailable(Room room, List<Booking> booking, LocalDateTime start, LocalDateTime end) {

        for (Booking book : booking) {
            if (book.getStatus() != BookingStatus.ACTIVE) {
                continue;
            }
            if (book.getRoomId().equals(room.getId())) {
                // Check if the room's booking overlaps with the requested booking time.
                // Also, touching boundaries won't count as overlapping.
                if (end.isAfter(book.getStart()) && start.isBefore(book.getEnd())) {
                    return false;
                }
            }
        }
        return true;

    }
}
