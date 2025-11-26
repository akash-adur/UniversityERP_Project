package edu.univ.erp.domain;

/**
 * A simple data class to hold Section information.
 */
public class Section {
    private final int sectionId;
    private final int courseId;
    private final String dayTime;
    private final String room;
    private final int capacity;
    // --- New Fields ---
    private final String semester;
    private final int year;

    // Fields from JOINs
    private String courseCode;
    private String instructorName;

    public Section(int sectionId, int courseId, String dayTime, String room, int capacity, String semester, int year) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    // --- Getters ---
    public int getSectionId() { return sectionId; }
    public int getCourseId() { return courseId; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }

    public String getCourseCode() { return courseCode; }
    public String getInstructorName() { return instructorName; }

    // --- Setters for joined fields ---
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getTerm() {
        return semester + " " + year;
    }

    @Override
    public String toString() {
        String label = (courseCode != null ? courseCode : "Section " + sectionId);
        return label + " - " + dayTime + " (" + semester + " " + year + ")";
    }
}