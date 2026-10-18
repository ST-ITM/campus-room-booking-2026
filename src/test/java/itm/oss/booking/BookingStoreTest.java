package itm.oss.booking;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BookingStoreTest {
    private static final String HEADER =
            "id,userId,roomId,start,end,attendees,purpose,status";
 
    @TempDir
    Path tempDir;
 
    private Path writeCsv(String... lines) throws IOException {
        Path file = tempDir.resolve("bookings.csv");
        Files.write(file, List.of(lines), StandardCharsets.UTF_8);
        return file;
    }

    @Test
    @DisplayName("Testing function load with a sample-bookings")
    void testLoadWithSample(){
        Path path = Path.of("data", "sample-bookings.csv");
        List<Booking> result = BookingStore.load(path);

        assertFalse(result.isEmpty());

        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(BookingStatus.ACTIVE, result.get(0).getStatus());
        assertEquals(3, result.get(2).getId());
        assertEquals(BookingStatus.CANCELLED, result.get(2).getStatus());
    }

    @Test
    @DisplayName("Null path test")
    void testNullPath(){
        assertTrue(BookingStore.load(null).isEmpty());
    }

    @Test 
    @DisplayName("Missing file test")
    void testMissingFile(){
        Path path = Path.of("data", "no-file.csv");
        List<Booking> result = BookingStore.load(path);

        assertTrue(result.isEmpty());
    }

    @Test 
    @DisplayName("Empty file test")
    void testEmptyFile() throws IOException{
        Path path = tempDir.resolve("empty.csv");
        Files.createFile(path);
        List<Booking> result = BookingStore.load(path);

        assertTrue(result.isEmpty());
    }

    @Test 
    @DisplayName("Valid Malformed Valid test (testing requirement)")
    void testVMV() throws IOException {
        Path file = writeCsv(
                HEADER,
                "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE",
                "a,s2026002,R102,2026-10-02T10:00,2026-10-02T11:00,2,Bad id,ACTIVE",
                "3,s2026003,R103,2026-10-03T09:00,2026-10-03T10:00,4,Study,CANCELLED");
 
        List<Booking> result = BookingStore.load(file);
 
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(3, result.get(1).getId());
    }

    @Test 
    @DisplayName("Trim test")
    void testTrim() throws IOException {
        Path file = writeCsv(
                HEADER,
                "  1  ,  s2026001  ,  R101  ,  2026-10-02T10:00  ,  2026-10-02T11:00  ,  2 ,      Project meeting  ,  ACTIVE  ");
 
        List<Booking> result = BookingStore.load(file);

        assertEquals(1, result.size());

        assertEquals(1, result.get(0).getId());
        assertEquals("s2026001", result.get(0).getUserId());
        assertEquals("R101", result.get(0).getRoomId());
        assertEquals(LocalDateTime.parse("2026-10-02T10:00"), result.get(0).getStart());
        assertEquals(LocalDateTime.parse("2026-10-02T11:00"), result.get(0).getEnd());
        assertEquals(2, result.get(0).getAttendees());
        assertEquals("Project meeting", result.get(0).getPurpose());
        assertEquals(BookingStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test 
    @DisplayName("Blank row test")
    void testBlankRow() throws IOException {
        Path file = writeCsv(
                HEADER,
                "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE",
                "",
                "          ",
                "3,s2026003,R103,2026-10-03T09:00,2026-10-03T10:00,4,Study,CANCELLED");
 
        List<Booking> result = BookingStore.load(file);

        assertEquals(2, result.size());
        assertEquals(3, result.get(1).getId());
        assertEquals(BookingStatus.CANCELLED, result.get(1).getStatus());
    }

    @Test 
    @DisplayName("Only Header or not")
    void testAboutHeader() throws IOException{
        Path file = writeCsv(HEADER);
        List<Booking> result = BookingStore.load(file);

        assertTrue(result.isEmpty());

        file = writeCsv("1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE");
        result = BookingStore.load(file);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }

    @Test 
    @DisplayName("Malformed row with wrong column count test")
    void testMalformedWithColumnCount() {
        String validRow     = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow1 = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE,aa";
        String malformedRow2 = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting";

        assertNotNull(BookingStore.parseBooking(validRow));
        assertNull(BookingStore.parseBooking(malformedRow1));
        assertNull(BookingStore.parseBooking(malformedRow2));
    }

    @Test 
    @DisplayName("Malformed row with invalid id test")
    void testMalformedWithInvalidId() {
        String validRow      = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow1 = "a,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow2 = "-1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";

        assertNotNull(BookingStore.parseBooking(validRow));
        assertNull(BookingStore.parseBooking(malformedRow1));
        assertEquals(-1, BookingStore.parseBooking(malformedRow2).getId());
    }

    @Test 
    @DisplayName("Malformed row with invalid attendee number test")
    void testMalformedWithInvalidAttendees() {
        String validRow     = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,a,Project meeting,ACTIVE";

        assertNotNull(BookingStore.parseBooking(validRow));
        assertNull(BookingStore.parseBooking(malformedRow));
    }

    @Test 
    @DisplayName("Malformed row with invalid date test")
    void testMalformedWithInvalidDate() {
        String validRow     = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow1 = "1,s2026001,R101,aaaaaaaaaaaaaaaa,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow2 = "1,s2026001,R101,2026-10-02T10:00,aaaaaaaaaaaaaaaa,2,Project meeting,ACTIVE";

        assertNotNull(BookingStore.parseBooking(validRow));
        assertNull(BookingStore.parseBooking(malformedRow1));
        assertNull(BookingStore.parseBooking(malformedRow2));
    }

    @Test 
    @DisplayName("Malformed row with invalid status test")
    void testMalformedWithInvalidStatus() {
        String validRow     = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIV";

        assertNotNull(BookingStore.parseBooking(validRow));
        assertNull(BookingStore.parseBooking(malformedRow));
    }

    @Test 
    @DisplayName("Malformed row with blank user Id test")
    void testMalformedWithBlankUserId() {
        String validRow     = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow = "1, ,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";

        assertNotNull(BookingStore.parseBooking(validRow));
        assertNull(BookingStore.parseBooking(malformedRow));
    }

    @Test 
    @DisplayName("Malformed row with blank room Id test")
    void testMalformedWithBlankRoomId() {
        String validRow     = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow = "1,s2026001, ,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";

        assertNotNull(BookingStore.parseBooking(validRow));
        assertNull(BookingStore.parseBooking(malformedRow));
    }

    @Test 
    @DisplayName("Malformed row with Error condition test")
    void testMalformedWithErrorCondition() {
        String validRow      = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,ACTIVE";
        String malformedRow1 = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T10:00,2,Project meeting,ACTIVE";
        String malformedRow2 = "1,s2026001,R101,2026-10-02T11:00,2026-10-02T10:00,2,Project meeting,ACTIVE";
        String malformedRow3 = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,-1,Project meeting,ACTIVE";
        String malformedRow4 = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,0,Project meeting,ACTIVE";

        assertNotNull(BookingStore.parseBooking(validRow));
        assertNull(BookingStore.parseBooking(malformedRow1));
        assertNull(BookingStore.parseBooking(malformedRow2));
        assertNull(BookingStore.parseBooking(malformedRow3));
        assertNull(BookingStore.parseBooking(malformedRow4));
    }

    @Test 
    @DisplayName("Lowercase Test")
    void testLowercase(){
        String row1 = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,acTive";
        Booking booking1 = BookingStore.parseBooking(row1);
        String row2 = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,Project meeting,cancellED";
        Booking booking2 = BookingStore.parseBooking(row2);

        assertNotNull(booking1);
        assertNotNull(booking2);

        assertEquals(BookingStatus.ACTIVE, booking1.getStatus());
        assertEquals(BookingStatus.CANCELLED, booking2.getStatus());
    }

    @Test 
    @DisplayName("Empty purpose test")
    void testEmptyPurpose(){
        String row = "1,s2026001,R101,2026-10-02T10:00,2026-10-02T11:00,2,,ACTIVE";
        Booking booking = BookingStore.parseBooking(row);

        assertNotNull(booking);

        assertEquals("", booking.getPurpose());
    }
}