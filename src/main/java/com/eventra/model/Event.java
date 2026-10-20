package com.eventra.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Event POJO representing an organized event, workshop, seminar, or hackathon.
 */
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String description;
    private String category;
    private String venue;
    private Timestamp startTime;
    private Timestamp endTime;
    private int capacity;
    private int seatsLeft;
    private EventStatus status;
    private int organizerId;

    public Event() {
        this.status = EventStatus.UPCOMING;
    }

    public Event(int id, String title, String description, String category, String venue,
                 Timestamp startTime, Timestamp endTime, int capacity, int seatsLeft,
                 EventStatus status, int organizerId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.venue = venue;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
        this.seatsLeft = seatsLeft;
        this.status = status != null ? status : EventStatus.UPCOMING;
        this.organizerId = organizerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getSeatsLeft() {
        return seatsLeft;
    }

    public void setSeatsLeft(int seatsLeft) {
        this.seatsLeft = seatsLeft;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    // Calculated / Helper Methods
    public int getSeatsBooked() {
        return Math.max(0, capacity - seatsLeft);
    }

    public int getOccupancyPercentage() {
        if (capacity <= 0) return 0;
        return (int) Math.round(((double) getSeatsBooked() / capacity) * 100);
    }

    public boolean isFull() {
        return seatsLeft <= 0 || status == EventStatus.FULL;
    }

    public boolean isAlmostFull() {
        return status == EventStatus.ALMOST_FULL || (seatsLeft > 0 && ((double) seatsLeft / capacity) <= 0.10);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return id != 0 && id == event.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", venue='" + venue + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", capacity=" + capacity +
                ", seatsLeft=" + seatsLeft +
                ", status=" + status +
                ", organizerId=" + organizerId +
                '}';
    }
}
