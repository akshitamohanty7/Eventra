package com.eventra.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * CheckIn POJO representing physical attendance recorded at an event venue.
 */
public class CheckIn implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int registrationId;
    private Timestamp checkedInAt;
    private String method;

    public CheckIn() {
        this.checkedInAt = new Timestamp(System.currentTimeMillis());
        this.method = "MANUAL";
    }

    public CheckIn(int id, int registrationId, Timestamp checkedInAt, String method) {
        this.id = id;
        this.registrationId = registrationId;
        this.checkedInAt = checkedInAt != null ? checkedInAt : new Timestamp(System.currentTimeMillis());
        this.method = method != null ? method : "MANUAL";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public Timestamp getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(Timestamp checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckIn checkIn = (CheckIn) o;
        return id != 0 && id == checkIn.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CheckIn{" +
                "id=" + id +
                ", registrationId=" + registrationId +
                ", checkedInAt=" + checkedInAt +
                ", method='" + method + '\'' +
                '}';
    }
}
