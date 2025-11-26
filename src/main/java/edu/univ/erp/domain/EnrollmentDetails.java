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
    private final String finalGrade;
    private final String sectionName;

    private final int credits;
    private final String semester;
    private final int year;

    // Scores
    private final double quiz;
    private final double midterm;
    private final double finals;

    public EnrollmentDetails(int enrollmentId, int sectionId, String courseCode, String courseTitle,
                             String dayTime, String room, String instructorName, String status,
                             String finalGrade, int credits, String semester, int year,
                             double quiz, double midterm, double finals, String sectionName) {
        this.enrollmentId = enrollmentId;
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.dayTime = dayTime;
        this.room = room;
        this.instructorName = instructorName;
        this.status = status;
        this.finalGrade = finalGrade;
        this.credits = credits;
        this.semester = semester;
        this.year = year;
        this.quiz = quiz;
        this.midterm = midterm;
        this.finals = finals;
        // Fix nulls to "N/A"
        this.sectionName = (sectionName == null || sectionName.isEmpty()) ? "N/A" : sectionName;
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
    public String getFinalGrade() { return finalGrade; }
    public int getCredits() { return credits; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }
    public double getQuiz() { return quiz; }
    public double getMidterm() { return midterm; }
    public double getFinals() { return finals; }
    public String getSectionName() { return sectionName; }

    // --- THIS IS THE MISSING METHOD CAUSING YOUR ERROR ---
    public String getTerm() {
        return semester + " " + year;
    }
}