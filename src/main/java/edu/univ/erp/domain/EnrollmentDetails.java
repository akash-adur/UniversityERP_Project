package edu.univ.erp.domain;

public class EnrollmentDetails {
    private final int enrollmentId;
    private final int sectionId;
    private final String courseCode;
    private final String courseTitle;
    private final String dayTime;
    private final String room;
    private final String instructorName;
    private final String status;

    // This is the new field causing the error
    private final String finalGrade;

    public EnrollmentDetails(int enrollmentId, int sectionId, String courseCode, String courseTitle,
                             String dayTime, String room, String instructorName, String status, String finalGrade) {
        this.enrollmentId = enrollmentId;
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.dayTime = dayTime;
        this.room = room;
        this.instructorName = instructorName;
        this.status = status;

        // You MUST have this line to fix the "might not have been initialized" error:
        this.finalGrade = finalGrade;
    }

    // Getters
    public int getEnrollmentId() { return enrollmentId; }
    public int getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public String getInstructorName() { return instructorName; }
    public String getStatus() { return status; }

    // New getter
    public String getFinalGrade() { return finalGrade; }
}