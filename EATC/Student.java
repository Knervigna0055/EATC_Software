public class Student {
    private final String studentId;
    private final String name;
    private final String gender;
    private final String dob;
    private final String address;
    private final String emergencyContact;

    public Student(String studentId, String name, String gender, String dob, String address, String emergencyContact) {
        this.studentId = studentId;
        this.name = name;
        this.gender = gender;
        this.dob = dob;
        this.address = address;
        this.emergencyContact = emergencyContact;
    }

    public String getStudentId(){
        return studentId;
    }
    public String getName(){
        return name;
    }
    public String getGender(){
        return gender;
    }
    public String getDob(){
        return dob;
    }
    public String getAddress(){
        return address;
    }
    public String getEmergencyContact() {
        return emergencyContact;
    }
    @Override
    public String toString() {
        return name + " (ID: " + studentId + ")";
    }
}
