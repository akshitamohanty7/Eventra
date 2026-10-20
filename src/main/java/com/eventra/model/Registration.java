package com.eventra.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Registration POJO representing an attendee's booking for a specific event.
 */
public class Registration implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private int eventId;
    private String bookingRef;
    private int seatNumber;
    private RegistrationStatus status;
    private Timestamp registeredAt;

    // View / Joined fields for JSP presentation convenience
    private String eventTitle;
    private String eventCategory;
    private String eventVenue;
    private Timestamp eventStartTime;
    private Timestamp eventEndTime;
    private String attendeeName;
    private String attendeeEmail;

    public Registration() {
        this.status = RegistrationStatus.CONFIRMED;
    }

    public Registration(int id, int userId, int eventId, String bookingRef, int seatNumber,
                        RegistrationStatus status, Timestamp registeredAt) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.bookingRef = bookingRef;
        this.seatNumber = seatNumber;
        this.status = status != null ? status : RegistrationStatus.CONFIRMED;
        this.registeredAt = registeredAt;
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

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getBookingRef() {
        return bookingRef;
    }

    public void setBookingRef(String bookingRef) {
        this.bookingRef = bookingRef;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getEventVenue() {
        return eventVenue;
    }

    public void setEventVenue(String eventVenue) {
        this.eventVenue = eventVenue;
    }

    public Timestamp getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(Timestamp eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public Timestamp getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(Timestamp eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public String getAttendeeName() {
        return attendeeName;
    }

    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public String getAttendeeEmail() {
        return attendeeEmail;
    }

    public void setAttendeeEmail(String attendeeEmail) {
        this.attendeeEmail = attendeeEmail;
    }

    public boolean isConfirmed() {
        return status == RegistrationStatus.CONFIRMED;
    }

    public boolean isWaitlisted() {
        return status == RegistrationStatus.WAITLIST;
    }

    public boolean isCancelled() {
        return status == RegistrationStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registration that = (Registration) o;
        return id != 0 && id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Registration{" +
                "id=" + id +
                ", userId=" + userId +
                ", eventId=" + eventId +
                ", bookingRef='" + bookingRef + '\'' +
                ", seatNumber=" + seatNumber +
                ", status=" + status +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
