package itm.oss.booking;

import java.util.ArrayList;
import java.util.List;

public final class BookingFilter {

    private BookingFilter() {
    }

    public static List<Booking> filter(
            List<Booking> bookings,
            String userId,
            String roomId,
            BookingStatus status,
            Integer minAttendees) {

        if (minAttendees != null && minAttendees <= 0) {
            throw new IllegalArgumentException(
                    "minAttendees must be greater than zero: " + minAttendees);
        }

        String userCriterion = normalize(userId);
        String roomCriterion = normalize(roomId);

        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookings) {
            if (matches(booking, userCriterion, roomCriterion, status, minAttendees)) {
                result.add(booking);
            }
        }
        return result;
    }

    private static boolean matches(
            Booking booking,
            String userCriterion,
            String roomCriterion,
            BookingStatus status,
            Integer minAttendees) {

        if (userCriterion != null
                && !userCriterion.equalsIgnoreCase(booking.getUserId())) {
            return false;
        }
        if (roomCriterion != null
                && !roomCriterion.equalsIgnoreCase(booking.getRoomId())) {
            return false;
        }
        if (status != null && booking.getStatus() != status) {
            return false;
        }
        if (minAttendees != null && booking.getAttendees() < minAttendees) {
            return false;
        }
        return true;
    }

    private static String normalize(String criterion) {
        if (criterion == null || criterion.isBlank()) {
            return null;
        }
        return criterion.trim();
    }
}