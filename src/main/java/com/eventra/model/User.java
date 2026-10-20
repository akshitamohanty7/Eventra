package com.eventra.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * User POJO representing a registered participant or administrator.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private UserRole role;
    private Timestamp createdAt;

    public User() {
        this.role = UserRole.ATTENDEE;
    }

    public User(int id, String name, String email, String passwordHash, UserRole role, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : UserRole.ATTENDEE;
        this.createdAt = createdAt;
    }

    public User(String name, String email, String passwordHash, UserRole role) {
        this(0, name, email, passwordHash, role, new Timestamp(System.currentTimeMillis()));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != 0 && id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}
