package itm.oss.booking;

import java.nio.file.Path;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.IOException;


public final class BookingStore {
    private BookingStore() {
    }

    /**
     * Issue 9.
     */
    public static List<Booking> load(Path path) {
        List<Booking> result = new ArrayList<>();

        //null path, missing file, empty file -> return empty list
        if (path == null || Files.notExists(path)) {
            return  result;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String row : lines) {
                
                //trim and skip if row is blank
                row = row.trim();
                if(row.isBlank()){
                    continue;
                }
                
                //skip if is header
                String[] userInfo = row.split(",",-1);
                if(userInfo.length == numOfCol) {
                    for( int i =0; i< numOfCol ; i++) {
                        userInfo[i] = userInfo[i].trim();
                    }
                    if(String.join(",",userInfo).equals("id,userId,roomId,start,end,attendees,purpose,status")){
                        continue;    
                    }
                }
                
                Booking booking=parseBooking(row);
                
                if(booking !=null) {
                    result.add(booking);
                } 

            }   
        }catch(IOException e) {
            return  result;
        }
            
        
        return result;
    }

       

    /*
    number of columns을 static final 필드로 선언 후 비교 
    @ row = "id,userId,roomId,start,end,attendees,purpose,status"
    @ class Booking{
            int id,
            String userId,
            String roomId,
            LocalDateTime start,
            LocalDateTime end,
            int attendees,
            String purpose,
            BookingStatus status
            }
    @ issue
        1)wrong number of columns
        2)Invaild booking ID(not int)
        3)invalid attendee number(not int or <= 0)
        4)invalid date/time (not LocalDateTime or start >= end)
        5)invalid status (other than active or cancelled)
        6)blank user ID 
        7)blank room ID
        >return null
    else) return Booking
    */
    private static final int numOfCol = 8;


    static Booking parseBooking(String row){

        

        
        String[] userInfo = row.split(",", -1);
        for(int i = 0; i < numOfCol; i++) {userInfo[i] = userInfo[i].trim();}
        if(userInfo.length != numOfCol) {return null;}
        
                
        int bookingID;
        try {
            bookingID = Integer.valueOf(userInfo[0]);
        } catch(NumberFormatException e) {
            return null;
        }
        
        int attendees;

        try {
            attendees = Integer.parseInt(userInfo[5]);
        } catch (NumberFormatException e) {
            return null;
        }
        if(attendees<= 0) {
            return null;
        }


        LocalDateTime start;
        try {
            start = LocalDateTime.parse(userInfo[3]);
        } catch (DateTimeParseException e) {
            return null;
        }

        LocalDateTime end;
        try {
            end = LocalDateTime.parse(userInfo[4]);
        } catch (DateTimeParseException e) {
            return null;
        }
        if(start.isAfter(end) || start.isEqual(end)) {
            return null;
        }

        String stat = userInfo[7].toLowerCase();

        if(!stat.equals("active") && !stat.equals("cancelled")) {
            return null;
        }
        
        if(userInfo[1].isBlank()) {
            return null;
        }
        
        
        if(userInfo[2].isBlank()) {
            return null;
        }
        
        BookingStatus status = BookingStatus.valueOf(stat.toUpperCase(Locale.ROOT));


        return new Booking(bookingID, userInfo[1], userInfo[2], start, end, attendees, userInfo[6], status);


    }
}
