import java.util.LinkedHashMap;
import java.util.Map;

public class EATCSystem {

    final Map<String, Student>       students = new LinkedHashMap<>();
    final Map<String, TuitionLesson> lessons  = new LinkedHashMap<>();
    final Map<String, Booking>       bookings = new LinkedHashMap<>();

    private int bookingCounter = 1;

    public void addStudent(String name, String gender, String dob,
                           String address, String emergencyContact) {
        String id = String.format("S%02d", students.size() + 1);
        students.put(id, new Student(id, name, gender, dob, address, emergencyContact));
    }

    public void generateTimetable() {
        Object[][] data = {
            {"L001", 1, "Saturday", "AM", "Math",                 45.0},
            {"L002", 1, "Saturday", "PM", "English",              40.0},
            {"L003", 1, "Sunday",   "AM", "Verbal Reasoning",     35.0},
            {"L004", 1, "Sunday",   "PM", "Non-Verbal Reasoning", 35.0},
            {"L005", 2, "Saturday", "AM", "Math",                 45.0},
            {"L006", 2, "Saturday", "PM", "English",              40.0},
            {"L007", 2, "Sunday",   "AM", "Verbal Reasoning",     35.0},
            {"L008", 2, "Sunday",   "PM", "Non-Verbal Reasoning", 35.0},
            {"L009", 3, "Saturday", "AM", "Math",                 45.0},
            {"L010", 3, "Saturday", "PM", "English",              40.0},
            {"L011", 3, "Sunday",   "AM", "Verbal Reasoning",     35.0},
            {"L012", 3, "Sunday",   "PM", "Non-Verbal Reasoning", 35.0},
            {"L013", 4, "Saturday", "AM", "Math",                 45.0},
            {"L014", 4, "Saturday", "PM", "English",              40.0},
            {"L015", 4, "Sunday",   "AM", "Verbal Reasoning",     35.0},
            {"L016", 4, "Sunday",   "PM", "Non-Verbal Reasoning", 35.0},
            {"L017", 5, "Saturday", "AM", "Math",                 45.0},
            {"L018", 5, "Saturday", "PM", "English",              40.0},
            {"L019", 5, "Sunday",   "AM", "Verbal Reasoning",     35.0},
            {"L020", 5, "Sunday",   "PM", "Non-Verbal Reasoning", 35.0},
            {"L021", 6, "Saturday", "AM", "Math",                 45.0},
            {"L022", 6, "Saturday", "PM", "English",              40.0},
            {"L023", 6, "Sunday",   "AM", "Verbal Reasoning",     35.0},
            {"L024", 6, "Sunday",   "PM", "Non-Verbal Reasoning", 35.0},
            {"L025", 7, "Saturday", "AM", "Math",                 45.0},
            {"L026", 7, "Saturday", "PM", "English",              40.0},
            {"L027", 7, "Sunday",   "AM", "Verbal Reasoning",     35.0},
            {"L028", 7, "Sunday",   "PM", "Non-Verbal Reasoning", 35.0},
            {"L029", 8, "Saturday", "AM", "Math",                 45.0},
            {"L030", 8, "Saturday", "PM", "English",              40.0},
            {"L031", 8, "Sunday",   "AM", "Verbal Reasoning",     35.0},
            {"L032", 8, "Sunday",   "PM", "Non-Verbal Reasoning", 35.0},
        };
        for (Object[] r : data)
            lessons.put((String) r[0], new TuitionLesson(
                    (String) r[0], (int) r[1], (String) r[2],
                    (String) r[3], (String) r[4], (double) r[5]));
    }

    public String bookLesson(String studentId, String lessonId) {
        Student       student = students.get(studentId);
        TuitionLesson lesson  = lessons.get(lessonId);

        if (student == null || lesson == null)
            return "ERROR: Student or Lesson not found.";

        if (lesson.hasActiveBookingFor(studentId))
            return "ERROR: Duplicate booking not allowed. " + student.getName()
                    + " is already booked in this lesson.";

        for (TuitionLesson t : lessons.values()) {
            if (t.getWeekendNum() == lesson.getWeekendNum()
                    && t.getDay().equals(lesson.getDay())
                    && t.getSlot().equals(lesson.getSlot())
                    && t.hasActiveBookingFor(studentId))
                return "ERROR: Time conflict. " + student.getName()
                        + " already has a booking on Week " + lesson.getWeekendNum()
                        + " " + lesson.getDay() + " " + lesson.getSlot() + ".";
        }

        if (lesson.isFull())
            return "ERROR: Lesson " + lessonId + " is full (max 4 students).";

        String bId = String.format("B%03d", bookingCounter++);
        Booking b  = new Booking(bId, studentId, lessonId);
        bookings.put(bId, b);
        lesson.addBooking(b);

        return "Booking confirmed. " + student.getName() + " booked for "
               + lesson.getSubject() + " -- Week " + lesson.getWeekendNum()
               + " " + lesson.getDay() + " " + lesson.getSlot()
               + ". Booking ID: " + bId;
    }

    public String changeBooking(String bookingId, String newLessonId) {
        Booking       booking   = bookings.get(bookingId);
        TuitionLesson newLesson = lessons.get(newLessonId);

        if (booking == null)
            return "Booking not found.";
        if (!booking.isActive())
            return "Cannot change. Booking status is: " + booking.getStatus() + ".";
        if (newLesson == null)
            return "Lesson not found.";

        TuitionLesson oldLesson = lessons.get(booking.getLessonId());

        if (!oldLesson.getSubject().equals(newLesson.getSubject()))
            return "Cannot change to a different subject ("
                    + oldLesson.getSubject() + " -> " + newLesson.getSubject() + ").";

        if (newLesson.isFull())
            return "Lesson is full (max " + newLesson.getMaxCapacity() + " students).";

        String sid = booking.getStudentId();
        for (TuitionLesson t : lessons.values()) {
            if (t.getLessonId().equals(oldLesson.getLessonId())) continue;
            if (t.getWeekendNum() == newLesson.getWeekendNum()
                    && t.getDay().equals(newLesson.getDay())
                    && t.getSlot().equals(newLesson.getSlot())
                    && t.hasActiveBookingFor(sid))
                return "Time conflict. " + students.get(sid).getName()
                        + " already has a booking on Week " + newLesson.getWeekendNum()
                        + " " + newLesson.getDay() + " " + newLesson.getSlot() + ".";
        }

        oldLesson.releaseBooking(booking);
        booking.moveTo(newLessonId);
        newLesson.addBooking(booking);

        return "Booking updated successfully.\n  Moved from Week " + oldLesson.getWeekendNum()
               + " " + oldLesson.getDay() + " " + oldLesson.getSlot()
               + " -> Week " + newLesson.getWeekendNum()
               + " " + newLesson.getDay() + " " + newLesson.getSlot()
               + " [" + newLesson.getSubject() + "]";
    }

    public String cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);

        if (booking == null)
            return "Booking not found.";
        if (!booking.isActive())
            return "Cannot cancel. Booking status is: " + booking.getStatus() + ".";

        TuitionLesson lesson = lessons.get(booking.getLessonId());
        lesson.releaseBooking(booking);
        booking.cancel();
        return "Booking cancelled successfully. Seat released from "
               + lesson.getSubject() + " Week " + lesson.getWeekendNum()
               + " " + lesson.getDay() + " " + lesson.getSlot() + ".";
    }

    public String checkIn(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null)
            return "Booking not found.";
        if (!booking.isActive())
            return "Cannot check in. Status is: " + booking.getStatus() + ".";
        booking.checkIn();
        TuitionLesson lesson = lessons.get(booking.getLessonId());
        return "Check-in successful. Status updated to ATTENDED.\n  "
               + students.get(booking.getStudentId()).getName()
               + " -> " + lesson.getSubject() + " Week " + lesson.getWeekendNum()
               + " " + lesson.getDay() + " " + lesson.getSlot();
    }

    public String addReview(String bookingId, int rating, String review) {
        Booking booking = bookings.get(bookingId);
        if (booking == null)
            return "Booking not found.";
        if (booking.getStatus() != Booking.Status.ATTENDED)
            return "Cannot add review. Student must attend the lesson first.";
        if (booking.getRating() > 0)
            return "Review already submitted for this booking.";
        if (rating < 1 || rating > 5)
            return "Rating must be between 1 and 5.";
        booking.attend(rating, review);
        return "Review recorded successfully.";
    }

    public void printLessonReport(int fromWeekend, int toWeekend) {
        System.out.println("\n======================================================================");
        System.out.printf("  LESSON REPORT -- Weekends %d to %d%n", fromWeekend, toWeekend);
        System.out.println("======================================================================");
        System.out.printf("%-6s  %-9s  %-4s  %-22s  %-8s  %-10s%n",
                "ID", "Day", "Slot", "Subject", "Attended", "Avg Rating");
        System.out.println("-".repeat(68));

        for (TuitionLesson l : lessons.values()) {
            if (l.getWeekendNum() < fromWeekend || l.getWeekendNum() > toWeekend) continue;
            double avg    = l.getAverageRating();
            String avgStr = avg > 0 ? String.format("%.1f/5.0", avg) : "N/A";
            System.out.printf("%-6s  %-9s  %-4s  %-22s  %-8d  %-10s%n",
                    l.getLessonId(), l.getDay(), l.getSlot(),
                    l.getSubject(), l.getAttendedCount(), avgStr);
        }
        System.out.println("-".repeat(68));
    }

    public void printIncomeReport(int fromWeekend, int toWeekend) {
        System.out.println("\n======================================================================");
        System.out.printf("  INCOME REPORT -- Weekends %d to %d%n", fromWeekend, toWeekend);
        System.out.println("======================================================================");

        Map<String, Double> income = new LinkedHashMap<>();
        income.put("Math",                 0.0);
        income.put("English",              0.0);
        income.put("Verbal Reasoning",     0.0);
        income.put("Non-Verbal Reasoning", 0.0);

        for (TuitionLesson l : lessons.values()) {
            if (l.getWeekendNum() < fromWeekend || l.getWeekendNum() > toWeekend) continue;
            income.merge(l.getSubject(), l.getIncome(), Double::sum);
        }

        double total = income.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0.0) {
            System.out.println("  No income generated for this month.");
            System.out.println("-".repeat(68));
            return;
        }

        String top = income.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("N/A");

        income.forEach((subj, amt) ->
            System.out.printf("  %-25s  %.2f%s%n",
                    subj, amt, subj.equals(top) ? "  << HIGHEST" : ""));

        System.out.println("-".repeat(68));
        System.out.printf("  Highest Income Subject: %s  (%.2f)%n", top, income.get(top));
        System.out.println("-".repeat(68));
    }
}
