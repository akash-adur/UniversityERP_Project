package edu.univ.erp.domain;

/**
 * A simple class to hold the logged-in user's data.
 * This is "session" data.
 */
public class UserSession {
    private final int userId;
    private final String username;
    private final String role;

    public UserSession(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    // Quick checks for roles
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }
    public boolean isInstructor() {
        return "Instructor".equalsIgnoreCase(role);
    }
    public boolean isStudent() {
        return "Student".equalsIgnoreCase(role);
    }
}