package com.eventra.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Interest POJO representing a topic/domain tag for personalization and matching.
 */
public class Interest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String label;

    public Interest() {
    }

    public Interest(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public Interest(String label) {
        this(0, label);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interest interest = (Interest) o;
        return id != 0 && id == interest.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Interest{" +
                "id=" + id +
                ", label='" + label + '\'' +
                '}';
    }
}
