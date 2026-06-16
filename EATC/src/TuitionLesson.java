import java.util.ArrayList;
import java.util.List;

public class TuitionLesson {

    private final String lessonId;
    private final int    weekendNum;
    private final String day;
    private final String slot;
    private final String subject;
    private final double price;
    private final int    maxCapacity = 4;

    private final List<Booking> bookings = new ArrayList<>();

    public TuitionLesson(String lessonId, int weekendNum, String day,
                         String slot, String subject, double price) {
        this.lessonId   = lessonId;
        this.weekendNum = weekendNum;
        this.day        = day;
        this.slot       = slot;
        this.subject    = subject;
        this.price      = price;
    }

    public String getLessonId()   {
        return lessonId;
    }
    public int getWeekendNum() {
        return weekendNum;
    }
    public String getDay()        {
        return day;
    }
    public String getSlot(){
        return slot;
    }
    public String getSubject(){
        return subject;
    }
    public double getPrice(){
        return price;
    }
    public int getMaxCapacity(){
        return maxCapacity;
    }
    public void addBooking(Booking b)
    { bookings.add(b);
    }
    public void releaseBooking(Booking b) {
        bookings.remove(b);
    }
    public int getActiveCount() {
        return (int) bookings.stream().filter(Booking::isActive).count();
    }
    public boolean isFull() {
        return getActiveCount() >= maxCapacity;
    }
    public boolean hasActiveBookingFor(String studentId) {
        return bookings.stream().anyMatch(b -> b.getStudentId().equals(studentId) && b.isActive());
    }
    public int getAttendedCount() {
        return (int) bookings.stream().filter(b -> b.getStatus() == Booking.Status.ATTENDED).count();
    }
    public double getAverageRating() {
        List<Integer> ratings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.ATTENDED && b.getRating() > 0)
                .map(Booking::getRating)
                .toList();
        if (ratings.isEmpty()) return 0.0;
        return Math.round(ratings.stream().mapToInt(i -> i).average().orElse(0) * 10.0) / 10.0;
    }
    public double getIncome() {
        return getAttendedCount() * price;
    }
    @Override
    public String toString() {
        return String.format("[%s] W%d %s %s - %-22s %.2f  (%d/%d seats)",
                lessonId, weekendNum, day, slot, subject, price,
                getActiveCount(), maxCapacity);
    }
}
