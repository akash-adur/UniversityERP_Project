package edu.univ.erp.domain;

/**
 * A simple data class to hold Section information.
 * This is an updated version that includes fields for joins.
 */
public class Section {
    private final int sectionId;
    private final int courseId;
    private final String dayTime;
    private final String room;
    private final int capacity;

    // Fields from JOINs
    private String courseCode;
    private String instructorName;

    // Constructor for basic section creation
    public Section(int sectionId, int courseId, String dayTime, String room, int capacity) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
    }

    // --- Getters ---
    public int getSectionId() { return sectionId; }
    public int getCourseId() { return courseId; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getCourseCode() { return courseCode; }
    public String getInstructorName() { return instructorName; }

    // --- Setters for joined fields ---
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }
}