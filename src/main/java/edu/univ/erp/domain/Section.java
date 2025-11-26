package edu.univ.erp.domain;

public class Section {
    private final int sectionId;
    private final int courseId;
    private final String dayTime;
    private final String room;
    private final int capacity;
    private final String semester;
    private final int year;
    private final String sectionName; // <--- 1. NEW FIELD

    // Fields from JOINs
    private String courseCode;
    private String instructorName;

    // 2. UPDATE CONSTRUCTOR
    public Section(int sectionId, int courseId, String dayTime, String room, int capacity, String semester, int year, String sectionName) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
        this.sectionName = (sectionName == null || sectionName.isEmpty()) ? "N/A" : sectionName;
    }

    // --- Getters ---
    public int getSectionId() { return sectionId; }
    public int getCourseId() { return courseId; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }
    public String getSectionName() { return sectionName; } // <--- 3. NEW GETTER

    public String getCourseCode() { return courseCode; }
    public String getInstructorName() { return instructorName; }

    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getTerm() { return semester + " " + year; }

    // 4. UPDATE toString() to show "Sec A" if it exists
    @Override
    public String toString() {
        String label = (courseCode != null ? courseCode : "Section " + sectionId);
        String secLabel = "N/A".equals(sectionName) ? "" : " - Sec " + sectionName;
        return label + secLabel + " [" + dayTime + "]";
    }
}