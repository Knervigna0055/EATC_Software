public class Booking {

    public enum Status {
        BOOKED,
        CHANGED,
        ATTENDED,
        CANCELLED
    }
    private final String bookingId;
    private final String studentId;
    private String lessonId;
    private Status status;
    private int rating;
    private String review;
    public Booking(String bookingId, String studentId, String lessonId) {
        this.bookingId = bookingId;
        this.studentId = studentId;
        this.lessonId  = lessonId;
        this.status    = Status.BOOKED;
        this.rating    = 0;
        this.review    = "";
    }
    public String getBookingId() {
        return bookingId;
    }
    public String getStudentId() {
        return studentId;
    }
    public String getLessonId()  {
        return lessonId;
    }
    public Status getStatus(){
        return status;
    }
    public int getRating(){
        return rating;
    }
    public String getReview(){
        return review;
    }
    public void moveTo(String newLessonId) {
        this.lessonId = newLessonId;
        this.status   = Status.CHANGED;
    }
    public void cancel(){
        this.status = Status.CANCELLED;
    }
    public void checkIn() {
        this.status = Status.ATTENDED;
    }
    public void attend(int rating, String review) {
        this.rating = rating;
        this.review = review;
    }
    public boolean isActive() {
        return status == Status.BOOKED || status == Status.CHANGED;
    }
    @Override
    public String toString() {
        return String.format("Booking[%s] Student:%s Lesson:%s Status:%s",
                bookingId, studentId, lessonId, status);
    }
}
