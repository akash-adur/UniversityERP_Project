package edu.univ.erp.domain;

/**
 * A data class to hold detailed information about a student's enrollment
 * for the "My Registrations" tab.
 */
public class EnrollmentDetails {
    private final int enrollmentId;
    private final int sectionId;
    private final String courseCode;
    private final String courseTitle;
    private final String dayTime;
    private final String room;
    private final String instructorName;
    private final String status;

    public EnrollmentDetails(int enrollmentId, int sectionId, String courseCode, String courseTitle,
                             String dayTime, String room, String instructorName, String status) {
        this.enrollmentId = enrollmentId;
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.dayTime = dayTime;
        this.room = room;
        this.instructorName = instructorName;
        this.status = status;
    }

    // --- Getters ---
    public int getEnrollmentId() { return enrollmentId; }
    public int getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public String getInstructorName() { return instructorName; }
    public String getStatus() { return status; }
}