import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final EATCSystem eatc = new EATCSystem();
    private static final Scanner    sc   = new Scanner(System.in);

    public static void main(String[] args) {
        loadStudents();
        eatc.generateTimetable();
        seedData();
        boolean running = true;
        while (running) {
            printMenu();
            switch (prompt("Enter your choice: ")) {
                case "1" -> bookLesson();
                case "2" -> changeOrCancel();
                case "3" -> attendLesson();
                case "4" -> lessonReport();
                case "5" -> incomeReport();
                case "0" -> { System.out.println("\nGoodbye!"); running = false; }
                default  -> System.out.println("\nInvalid choice.Please enter 0-5.");
            }
        }
        sc.close();
    }

    private static void bookLesson() {
        System.out.println("\n--- Book a Lesson ---");

        Student student = pickStudent();
        String sid = student.getStudentId();

        List<TuitionLesson> allLessons = new ArrayList<>();
        int currentWeek = -1;
        int num = 0;

        for (TuitionLesson l : eatc.lessons.values()) {
            if (l.getWeekendNum() != currentWeek) {
                currentWeek = l.getWeekendNum();
                System.out.println("\n  --- Week " + currentWeek + " ---");
                System.out.printf("  %-4s %-10s %-5s %-22s %-7s %s%n",
                        "No.", "Day", "Slot", "Subject", "Price", "Spaces");
                System.out.println("  " + "-".repeat(55));
            }
            num++;
            allLessons.add(l);

            int spaces = l.getMaxCapacity() - l.getActiveCount();
            System.out.printf("  %-4s %-10s %-5s %-22s %-7.2f %s%n",
                    num + ".", l.getDay(), l.getSlot(),
                    l.getSubject(), l.getPrice(), spaces + "/" + l.getMaxCapacity());
        }

        int li = pickNumber("\nSelect lesson number to book: ", 1, allLessons.size()) - 1;
        TuitionLesson chosen = allLessons.get(li);

        if (chosen.isFull()) {
            System.out.println("\n  ERROR: This lesson is full. No spaces available.");
            return;
        }
        if (chosen.hasActiveBookingFor(sid)) {
            System.out.println("\n  ERROR: " + student.getName() + " already has an active booking for this lesson.");
            return;
        }
        if (hasTimeConflict(sid, chosen)) {
            System.out.println("\n  ERROR: Time conflict. " + student.getName()
                    + " already has a booking on Week " + chosen.getWeekendNum()
                    + " " + chosen.getDay() + " " + chosen.getSlot() + ".");
            return;
        }

        System.out.println("\n" + eatc.bookLesson(sid, chosen.getLessonId()));
    }

    private static void changeOrCancel() {
        System.out.println("\n  1 -> Change");
        System.out.println("  2 -> Cancel");
        String sub = prompt("  Enter your choice: ");
        if      (sub.equals("1")) changeBooking();
        else if (sub.equals("2")) cancelBooking();
        else    System.out.println("\n  Invalid choice.");
    }

    private static void changeBooking() {
        Student student = pickStudent();

        List<Booking> active = getActiveBookings(student.getStudentId());
        if (active.isEmpty()) {
            System.out.println("\n  No active bookings found for " + student.getName() + ".");
            return;
        }

        System.out.println("\nCurrent Bookings:");
        printNumberedBookings(active);

        int bi = pickNumber("Choose booking to change: ", 1, active.size()) - 1;
        Booking chosen      = active.get(bi);
        TuitionLesson oldLesson = eatc.lessons.get(chosen.getLessonId());
        String subject      = oldLesson.getSubject();

        List<TuitionLesson> options = new ArrayList<>();
        int currentWeek = -1;

        for (TuitionLesson l : eatc.lessons.values()) {
            if (!l.getSubject().equals(subject)) continue;
            if (l.getLessonId().equals(oldLesson.getLessonId())) continue;
            if (l.isFull()) continue;

            boolean conflict = false;
            for (TuitionLesson t : eatc.lessons.values()) {
                if (t.getLessonId().equals(oldLesson.getLessonId())) continue;
                if (t.getWeekendNum() == l.getWeekendNum()
                        && t.getDay().equals(l.getDay())
                        && t.getSlot().equals(l.getSlot())
                        && t.hasActiveBookingFor(student.getStudentId())) {
                    conflict = true;
                    break;
                }
            }
            if (conflict) continue;

            if (l.getWeekendNum() != currentWeek) {
                currentWeek = l.getWeekendNum();
                System.out.println("\n  Week " + currentWeek + " -- Available " + subject + " Lessons:");
                System.out.printf("  %-4s %-10s %-5s %-22s %s%n", "No.", "Day", "Slot", "Subject", "Spaces");
                System.out.println("  " + "-".repeat(55));
            }
            options.add(l);
            System.out.printf("  %-4d %-10s %-5s %-22s %d/%d%n",
                    options.size(), l.getDay(), l.getSlot(),
                    l.getSubject(), l.getMaxCapacity() - l.getActiveCount(), l.getMaxCapacity());
        }

        if (options.isEmpty()) {
            System.out.println("\n  No alternative " + subject + " lessons available.");
            return;
        }

        int li = pickNumber("\nSelect new lesson: ", 1, options.size()) - 1;
        System.out.println("\n" + eatc.changeBooking(chosen.getBookingId(), options.get(li).getLessonId()));
    }

    private static void cancelBooking() {
        Student student = pickStudent();

        List<Booking> active = getActiveBookings(student.getStudentId());
        if (active.isEmpty()) {
            System.out.println("\n  No active bookings to cancel for " + student.getName() + ".");
            return;
        }

        System.out.println("\nCurrent Bookings:");
        printNumberedBookings(active);

        int bi = pickNumber("Choose booking to cancel: ", 1, active.size()) - 1;
        System.out.println("\n" + eatc.cancelBooking(active.get(bi).getBookingId()));
    }

    private static void attendLesson() {
        System.out.println("\n--- Attend a Lesson ---");

        Student student = pickStudent();

        List<Booking> active = getActiveBookings(student.getStudentId());
        if (active.isEmpty()) {
            System.out.println("\n  No booked lessons found for " + student.getName() + ".");
            return;
        }

        System.out.println("\nBooked Lessons for " + student.getName() + ":");
        printNumberedBookings(active);

        int bi = pickNumber("Select lesson to attend: ", 1, active.size()) - 1;
        Booking booking = active.get(bi);

        System.out.println("\n" + eatc.checkIn(booking.getBookingId()));

        int rating    = pickNumber("Enter rating (1-5): ", 1, 5);
        String review = prompt("Enter review: ");

        System.out.println("\n" + eatc.addReview(booking.getBookingId(), rating, review));
    }

    private static void lessonReport() {
        System.out.println("\n--- Monthly Lesson Report ---");
        int block = pickWeekBlock();
        if (block == 1) eatc.printLessonReport(1, 4);
        else            eatc.printLessonReport(5, 8);
    }

    private static void incomeReport() {
        System.out.println("\n--- Monthly Income Report ---");
        int block = pickWeekBlock();
        if (block == 1) eatc.printIncomeReport(1, 4);
        else            eatc.printIncomeReport(5, 8);
    }

    private static Student pickStudent() {
        List<Student> list = new ArrayList<>(eatc.students.values());
        System.out.println("\nStudents:");
        for (int i = 0; i < list.size(); i++)
            System.out.printf("  %d. %s%n", i + 1, list.get(i).getName());
        int si = pickNumber("Select student: ", 1, list.size()) - 1;
        return list.get(si);
    }

    private static List<Booking> getActiveBookings(String studentId) {
        List<Booking> list = new ArrayList<>();
        for (Booking b : eatc.bookings.values())
            if (b.getStudentId().equals(studentId) && b.isActive())
                list.add(b);
        return list;
    }

    private static boolean hasTimeConflict(String studentId, TuitionLesson target) {
        for (TuitionLesson t : eatc.lessons.values()) {
            if (t.getWeekendNum() == target.getWeekendNum()
                    && t.getDay().equals(target.getDay())
                    && t.getSlot().equals(target.getSlot())
                    && t.hasActiveBookingFor(studentId))
                return true;
        }
        return false;
    }

    private static void printNumberedBookings(List<Booking> list) {
        for (int i = 0; i < list.size(); i++) {
            Booking b = list.get(i);
            TuitionLesson l = eatc.lessons.get(b.getLessonId());
            System.out.printf("  %d. Week %d  %-10s %-5s %-22s [%s]%n",
                    i + 1, l.getWeekendNum(), l.getDay(), l.getSlot(),
                    l.getSubject(), b.getStatus());
        }
    }

    private static int pickWeekBlock() {
        System.out.println("\n  1. Weeks 1 to 4  (Month 1)");
        System.out.println("  2. Weeks 5 to 8  (Month 2)");
        return pickNumber("Select month: ", 1, 2);
    }

    private static int pickNumber(String msg, int min, int max) {
        while (true) {
            System.out.print(msg);
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("  Please enter a number between " + min + " and " + max + ".");
        }
    }

    private static String prompt(String msg) {
        System.out.print(msg);
        return sc.nextLine().trim();
    }

    private static void printMenu() {
        System.out.println("\n=====================================");
        System.out.println(" Excel Academy Tuition Centre (EATC)");
        System.out.println("=====================================");
        System.out.println("\n1. Book a Lesson");
        System.out.println("2. Change/Cancel a Lesson Booking");
        System.out.println("3. Attend a Lesson");
        System.out.println("4. Generate Monthly Lesson Report");
        System.out.println("5. Generate Monthly Income Report");
        System.out.println("0. Exit");
    }

    private static void loadStudents() {
        eatc.addStudent("Oliver Bennett",   "Male",   "2015-04-12", "12 Oak Street, London",       "07700900077");
        eatc.addStudent("Emily Clarke",     "Female", "2014-08-22", "45 Baker Road, Manchester",   "07700900078");
        eatc.addStudent("Harry Thompson",   "Male",   "2015-01-05", "8 Church Lane, Birmingham",   "07700900079");
        eatc.addStudent("Sophie Williams",  "Female", "2014-11-30", "19 Victoria Avenue, Leeds",   "07700900080");
        eatc.addStudent("George Harrison",  "Male",   "2015-06-15", "32 Greenfield Road, Bristol", "07700900081");
        eatc.addStudent("Charlotte Davies", "Female", "2014-02-18", "7 Windsor Drive, Liverpool",  "07700900082");
        eatc.addStudent("Jack Robinson",    "Male",   "2015-09-09", "14 Regent Street, Sheffield", "07700900083");
        eatc.addStudent("Amelia Turner",    "Female", "2014-05-14", "3 Elm Park, Newcastle",       "07700900084");
        eatc.addStudent("Alfie Edwards",    "Male",   "2015-03-24", "22 Queens Road, Nottingham",  "07700900085");
        eatc.addStudent("Isla Mitchell",    "Female", "2014-10-10", "9 Maple Close, Edinburgh",    "07700900086");
    }

    private static void seedData() {
        String[][] data = {
            {"S01", "L001", "5", "Excellent maths lesson, very clear explanations."},
            {"S02", "L001", "4", "Good session, enjoyed the problem solving."},
            {"S03", "L001", "5", "Brilliant lesson, learned a lot."},
            {"S04", "L001", "3", "Decent lesson but a bit fast paced."},
            {"S05", "L002", "4", "Really helpful English class."},
            {"S06", "L002", "5", "Fantastic teacher, very engaging."},
            {"S07", "L002", "4", "Good lesson overall."},
            {"S08", "L003", "3", "Verbal reasoning was challenging but useful."},
            {"S09", "L003", "4", "Enjoyed the session, great practice."},
            {"S10", "L004", "5", "Non-verbal reasoning was very well taught."},
            {"S01", "L005", "4", "Another great maths session."},
            {"S02", "L005", "5", "Loved the content this week."},
            {"S03", "L006", "4", "English class was very productive."},
            {"S04", "L006", "3", "Good but could be more interactive."},
            {"S05", "L007", "5", "Excellent verbal reasoning class."},
            {"S06", "L008", "4", "Non-verbal reasoning made much more sense now."},
            {"S07", "L009", "5", "Outstanding maths lesson."},
            {"S08", "L009", "4", "Very well structured."},
            {"S09", "L010", "4", "English lesson was very helpful."},
            {"S10", "L010", "5", "Loved every minute of it."},
        };
        for (String[] row : data) {
            String bId = extractBookingId(eatc.bookLesson(row[0], row[1]));
            if (bId != null) {
                eatc.checkIn(bId);
                eatc.addReview(bId, Integer.parseInt(row[2]), row[3]);
            }
        }
    }

    private static String extractBookingId(String bookResult) {
        if (!bookResult.startsWith("Booking confirmed")) return null;
        int idx = bookResult.lastIndexOf("Booking ID: ");
        return idx >= 0 ? bookResult.substring(idx + 12).trim() : null;
    }
}
