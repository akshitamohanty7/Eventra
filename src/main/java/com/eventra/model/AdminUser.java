package com.eventra.model;

import java.sql.Timestamp;

/**
 * AdminUser represents a privileged administrator, demonstrating OOP inheritance
 * and role-based permissions over the base User model.
 */
public class AdminUser extends User {
    private static final long serialVersionUID = 1L;

    public AdminUser() {
        super();
        setRole(UserRole.ADMIN);
    }

    public AdminUser(int id, String name, String email, String passwordHash, Timestamp createdAt) {
        super(id, name, email, passwordHash, UserRole.ADMIN, createdAt);
    }

    public AdminUser(String name, String email, String passwordHash) {
        super(name, email, passwordHash, UserRole.ADMIN);
    }

    /**
     * Checks if this user has permission to manage events (create, edit, publish).
     */
    public boolean canManageEvents() {
        return getRole() == UserRole.ADMIN;
    }

    /**
     * Checks if this user has permission to delete events.
     */
    public boolean canDeleteEvent() {
        return getRole() == UserRole.ADMIN;
    }

    @Override
    public String toString() {
        return "AdminUser{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}
