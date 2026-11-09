
package itm.oss.booking;

import java.time.LocalTime;
import java.time.Duration;

public final class BookingValidator {

    private BookingValidator() {
    }

    public static void validate(
            Booking booking,
            Room room,
            LocalTime openTime,
            LocalTime closeTime,
            long maxDurationMinutes) {

        // Member 1: Required objects and identity
        validateRequiredObjects(booking, room);
        validateIdentity(booking, room);

        // Member 2: Time validation
        validateBookingTimes(booking);
        validateOperatingHours(booking, openTime, closeTime);
        validateDuration(booking, maxDurationMinutes);

        // Member 3: Capacity and status validation
        validateAttendees(booking, room);

        if (booking.getStatus() == null) {
            throw new IllegalArgumentException(
                "Booking status cannot be null");
        }
    }

    // Member 1
    private static void validateRequiredObjects(Booking booking, Room room) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking is required.");
        }
        if (room == null) {
            throw new IllegalArgumentException("Room is required.");
        }
    }

    private static void validateIdentity(Booking booking, Room room) {
        if (isBlank(booking.getUserId())) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (isBlank(booking.getRoomId())) {
            throw new IllegalArgumentException("Booking room ID is required.");
        }
        if (!booking.getRoomId().equals(room.getId())) {
            throw new IllegalArgumentException("Booking room ID must match the supplied room.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // Member 2
    private static void validateBookingTimes(Booking booking) {
        if (booking.getStart() == null || booking.getEnd() == null) {
            throw new IllegalArgumentException("Booking start and end times are required.");
        }
        if (!booking.getStart().isBefore(booking.getEnd())) {
            throw new IllegalArgumentException("Booking start must be before its end.");
        }
        if (!booking.getStart().toLocalDate().equals(booking.getEnd().toLocalDate())) {
            throw new IllegalArgumentException("Booking must start and end on the same date.");
        }
    }

    private static void validateOperatingHours(
            Booking booking, LocalTime openTime, LocalTime closeTime) {

        if (openTime == null || closeTime == null || !openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("Operating hours are invalid.");
        }

        LocalTime startTime = booking.getStart().toLocalTime();
        LocalTime endTime = booking.getEnd().toLocalTime();

        if (startTime.isBefore(openTime)) {
            throw new IllegalArgumentException("Booking starts before opening time.");
        }
        if (endTime.isAfter(closeTime)) {
            throw new IllegalArgumentException("Booking ends after closing time.");
        }
    }

    private static void validateDuration(Booking booking, long maxDurationMinutes) {
        if (maxDurationMinutes <= 0) {
            throw new IllegalArgumentException("Maximum duration must be positive.");
        }

        long durationMinutes = Duration.between(
            booking.getStart(), booking.getEnd()).toMinutes();

        if (durationMinutes > maxDurationMinutes) {
            throw new IllegalArgumentException("Booking exceeds the maximum duration.");
        }
    }

    // Member 3
    private static void validateAttendees(Booking booking, Room room) {
        if (booking.getAttendees() <= 0) {
            throw new IllegalArgumentException(
                "Attendees must be greater than zero");
        }

        if (booking.getAttendees() > room.getCapacity()) {
            throw new IllegalArgumentException(
                "Room capacity exceeded");
        }
    }
}
