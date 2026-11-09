package itm.oss.booking;

import java.util.ArrayList;
import java.util.List;

public class BookingBook {
    private final ArrayList<Booking> bookings;

    public BookingBook(List<Booking> initialBookings) {
        this.bookings = new ArrayList<>(
                initialBookings == null ? List.of() : initialBookings);
    }

    public List<Booking> all() {
        return new ArrayList<>(bookings);
    }

    /**
     * Issue 4.
     * Replaces every editable value of the booking with the given id by the values
     * of replacement. The original id is always preserved.
     *
     * @throws IllegalArgumentException if replacement is null or no booking with
     *                                  the given id exists
     */

    public Booking update(int id, Booking replacement) {
        if (replacement == null) {
            throw new IllegalArgumentException("replacement must not be null");
        }

        int index = indexOfId(id);

        Booking updated = new Booking(
                id,
                replacement.getUserId(),
                replacement.getRoomId(),
                replacement.getStart(),
                replacement.getEnd(),
                replacement.getAttendees(),
                replacement.getPurpose(),
                replacement.getStatus());

        bookings.set(index, updated);
        return updated;
    }

    /**
     * Issue 4.
     * Marks the booking with the given id as CANCELLED. Cancelling an already
     * cancelled booking is allowed and leaves it cancelled.
     *
     * @throws IllegalArgumentException if no booking with the given id exists
     */
    public Booking cancel(int id) {
        int index = indexOfId(id);
        Booking cancelled = bookings.get(index).withStatus(BookingStatus.CANCELLED);
        bookings.set(index, cancelled);
        return cancelled;
    }

    private int indexOfId(int id) {
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getId() == id) {
                return i;
            }
        }

        throw new IllegalArgumentException("No booking with id " + id);
    }

}
