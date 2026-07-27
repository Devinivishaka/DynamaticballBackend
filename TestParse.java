import java.time.*;
import java.time.format.*;

public class TestParse {
    public static void main(String[] args) {
        String timestamp = "00:00:13";
        try {
            LocalDateTime dt = LocalDateTime.parse(timestamp);
            System.out.println("Parsed ISO: " + dt);
            return;
        } catch (Exception e) {}
        try {
            Instant instant = Instant.parse(timestamp);
            LocalDateTime dt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            System.out.println("Parsed Instant: " + dt);
            return;
        } catch (Exception e) {}
        try {
            DateTimeFormatter formatter;
            if (timestamp.matches("^\\d{2}:\\d{2}:\\d{2}$")) {
                formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            } else if (timestamp.matches("^\\d{2}:\\d{2}$")) {
                formatter = DateTimeFormatter.ofPattern("HH:mm");
            } else {
                throw new RuntimeException("Invalid time format. Expected 'HH:mm' or 'HH:mm:ss'.");
            }
            LocalTime time = LocalTime.parse(timestamp, formatter);
            System.out.println("Parsed time: " + time);
            LocalDateTime dt = LocalDateTime.of(LocalDate.now(), time);
            System.out.println("Parsed HH:mm:ss : " + dt);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
