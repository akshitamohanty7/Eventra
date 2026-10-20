package com.eventra.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * SkillProfile POJO representing an attendee's networking bio and collaboration objective.
 */
public class SkillProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String bio;
    private String collaborationGoal;

    public SkillProfile() {
    }

    public SkillProfile(int id, int userId, String bio, String collaborationGoal) {
        this.id = id;
        this.userId = userId;
        this.bio = bio;
        this.collaborationGoal = collaborationGoal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCollaborationGoal() {
        return collaborationGoal;
    }

    public void setCollaborationGoal(String collaborationGoal) {
        this.collaborationGoal = collaborationGoal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillProfile that = (SkillProfile) o;
        return id != 0 && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SkillProfile{" +
                "id=" + id +
                ", userId=" + userId +
                ", bio='" + bio + '\'' +
                ", collaborationGoal='" + collaborationGoal + '\'' +
                '}';
    }
}
