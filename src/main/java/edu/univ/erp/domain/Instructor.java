package edu.univ.erp.domain;

public class Instructor {
    private final int userId;
    private final String name; // <--- NEW FIELD
    private final String department;
    private final String title;

    public Instructor(int userId, String name, String department, String title) {
        this.userId = userId;
        this.name = name; // <--- Initialize it
        this.department = department;
        this.title = title;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getTitle() { return title; }

    // Override toString() to show Name instead of ID
    @Override
    public String toString() {
        return name + " - " + title + " (" + department + ")";
    }
}