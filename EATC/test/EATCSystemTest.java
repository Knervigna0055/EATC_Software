import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EATCSystemTest {

    private EATCSystem eatc;
    private String bookingIdS1L001;

    @Before
    public void setUp() {
        eatc = new EATCSystem();
        eatc.addStudent("Oliver Bennett",   "Male",   "2015-04-12", "12 Oak Street, London",       "07700900077");
        eatc.addStudent("Emily Clarke",     "Female", "2014-08-22", "45 Baker Road, Manchester",   "07700900078");
        eatc.addStudent("Harry Thompson",   "Male",   "2015-01-05", "8 Church Lane, Birmingham",   "07700900079");
        eatc.addStudent("Sophie Williams",  "Female", "2014-11-30", "19 Victoria Avenue, Leeds",   "07700900080");
        eatc.generateTimetable();
    }
    @Test
    public void testSuccessfulBooking() {
        String result = eatc.bookLesson("S01", "L001");
        assertTrue("Should confirm booking", result.startsWith("Booking confirmed"));
        assertTrue("Lesson should have 1 active booking",
                eatc.lessons.get("L001").getActiveCount() == 1);
    }
    @Test
    public void testCapacityLimit() {
        eatc.bookLesson("S01", "L001");
        eatc.bookLesson("S02", "L001");
        eatc.bookLesson("S03", "L001");
        eatc.bookLesson("S04", "L001");
        String result = eatc.bookLesson("S05", "L001");
        assertTrue("5th booking should be rejected as full", result.contains("ERROR"));
        assertEquals("Lesson must have exactly 4 active bookings", 4,
                eatc.lessons.get("L001").getActiveCount());
    }

    @Test
    public void testTimeConflict() {
        eatc.lessons.put("LX01", new TuitionLesson("LX01", 1, "Saturday", "AM", "Math",    45.0));
        eatc.lessons.put("LX02", new TuitionLesson("LX02", 1, "Saturday", "AM", "English", 40.0));
        eatc.bookLesson("S01", "LX01");
        String result = eatc.bookLesson("S01", "LX02");
        assertTrue(result.contains("ERROR"));
    }

    @Test
    public void testSameSubjectBookingChange() {
        eatc.bookLesson("S01", "L001");
        String bookingId = eatc.bookings.values().iterator().next().getBookingId();
        String result = eatc.changeBooking(bookingId, "L005");
        assertTrue("Same-subject change should succeed", result.contains("Booking updated"));
        assertEquals("Old lesson L001 should have 0 active seats", 0,
                eatc.lessons.get("L001").getActiveCount());
        assertEquals("New lesson L005 should have 1 active seat", 1,
                eatc.lessons.get("L005").getActiveCount());
    }
    @Test
    public void testDifferentSubjectRejection() {
        eatc.bookLesson("S01", "L001");
        String bookingId = eatc.bookings.values().iterator().next().getBookingId();
        String result = eatc.changeBooking(bookingId, "L003");
        assertTrue("Different subject change should be rejected",
                result.contains("Cannot change to a different subject"));
    }
    @Test
    public void testCancellation() {
        eatc.bookLesson("S01", "L001");
        String bookingId = eatc.bookings.values().iterator().next().getBookingId();

        assertEquals("Lesson should have 1 seat taken before cancel", 1,
                eatc.lessons.get("L001").getActiveCount());

        String result = eatc.cancelBooking(bookingId);
        assertTrue("Cancellation should succeed", result.contains("Booking cancelled successfully"));
        assertEquals("Seat should be released after cancellation", 0,
                eatc.lessons.get("L001").getActiveCount());
        assertEquals("Booking status should be CANCELLED",
                Booking.Status.CANCELLED,
                eatc.bookings.get(bookingId).getStatus());
    }
    @Test
    public void testAttendanceStatusUpdate() {
        eatc.bookLesson("S01", "L001");
        String bookingId = eatc.bookings.values().iterator().next().getBookingId();

        String result = eatc.checkIn(bookingId);
        assertTrue("Check-in should succeed", result.contains("Check-in successful"));
        assertEquals("Status should be ATTENDED after check-in",
                Booking.Status.ATTENDED,
                eatc.bookings.get(bookingId).getStatus());
    }
    @Test
    public void testValidReview() {
        eatc.bookLesson("S01", "L001");
        String bookingId = eatc.bookings.values().iterator().next().getBookingId();
        eatc.checkIn(bookingId);

        String result = eatc.addReview(bookingId, 5, "Excellent lesson.");
        assertTrue("Valid review should be recorded", result.contains("Review recorded successfully"));
        assertEquals("Rating should be saved as 5", 5,
                eatc.bookings.get(bookingId).getRating());
        assertEquals("Review text should be saved",
                "Excellent lesson.",
                eatc.bookings.get(bookingId).getReview());
    }
    @Test
    public void testInvalidRatingRejection() {
        eatc.bookLesson("S01", "L001");
        String bookingId = eatc.bookings.values().iterator().next().getBookingId();
        eatc.checkIn(bookingId);

        String tooLow  = eatc.addReview(bookingId, 0, "Too low.");
        String tooHigh = eatc.addReview(bookingId, 6, "Too high.");

        assertTrue("Rating 0 should be rejected",  tooLow.contains("Rating must be between"));
        assertTrue("Rating 6 should be rejected", tooHigh.contains("Rating must be between"));
    }
    @Test
    public void testAverageRatingCalculation() {
        eatc.bookLesson("S01", "L001");
        eatc.bookLesson("S02", "L001");
        eatc.bookLesson("S03", "L001");

        String b1 = getLastBookingId();
        eatc.bookLesson("S02", "L005");
        String[] ids = eatc.bookings.keySet().toArray(new String[0]);

        eatc.checkIn(ids[0]); eatc.addReview(ids[0], 4, "Good.");
        eatc.checkIn(ids[1]); eatc.addReview(ids[1], 2, "Poor.");
        eatc.checkIn(ids[2]); eatc.addReview(ids[2], 3, "OK.");
        TuitionLesson lesson = eatc.lessons.get("L001");
        assertEquals("Average rating should be 3.0", 3.0, lesson.getAverageRating(), 0.01);
    }

    @Test
    public void testAttendedStudentCount() {
        eatc.bookLesson("S01", "L001");
        eatc.bookLesson("S02", "L001");
        eatc.bookLesson("S03", "L001");

        String[] ids = eatc.bookings.keySet().toArray(new String[0]);
        eatc.checkIn(ids[0]);
        eatc.checkIn(ids[1]);
        TuitionLesson lesson = eatc.lessons.get("L001");
        assertEquals("Only 2 students attended", 2, lesson.getAttendedCount());
    }

    @Test
    public void testHighestIncomeSubject() {
        eatc.bookLesson("S01", "L001");
        eatc.bookLesson("S02", "L001");
        eatc.bookLesson("S03", "L001");
        eatc.bookLesson("S04", "L001");
        eatc.bookLesson("S01", "L006");
        eatc.bookLesson("S02", "L006");

        String[] ids = eatc.bookings.keySet().toArray(new String[0]);
        for (String id : ids) eatc.checkIn(id);

        double mathIncome    = eatc.lessons.get("L001").getIncome();
        double englishIncome = eatc.lessons.get("L006").getIncome();

        assertEquals("Math income should be 180.0",   180.0, mathIncome,    0.01);
        assertEquals("English income should be 80.0",  80.0, englishIncome, 0.01);
        assertTrue("Math should generate more income than English",
                mathIncome > englishIncome);
    }

    private String getLastBookingId() {
        String last = null;
        for (String id : eatc.bookings.keySet()) last = id;
        return last;
    }
}
