package edu.univ.erp.domain;

public class Instructor {
    private final int userId;
    private final String department;
    private final String title;

    public Instructor(int userId, String department, String title) {
        this.userId = userId;
        this.department = department;
        this.title = title;
    }

    public int getUserId() { return userId; }
    public String getDepartment() { return department; }
    public String getTitle() { return title; }

    // Override toString() for a nicer display in JComboBox
    @Override
    public String toString() {
        return userId + " - " + title + " (" + department + ")";
    }
}