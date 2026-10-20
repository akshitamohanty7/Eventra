package com.eventra.dao.interfaces;

import com.eventra.model.CheckIn;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO contract for check-in (attendance) persistence operations.
 */
public interface ICheckInDAO {

    /**
     * Records a physical check-in for a given registration.
     * Inserts into check_ins with the specified method (QR or MANUAL).
     *
     * @param registrationId the registration being checked in
     * @param method         "QR" or "MANUAL"
     * @return created CheckIn object with generated ID
     * @throws SQLException if insert fails or registration already checked in
     */
    CheckIn checkIn(int registrationId, String method) throws SQLException;

    /**
     * Returns true if the registration already has a check-in record.
     *
     * @param registrationId Registration ID
     * @return true if checked in, false otherwise
     * @throws SQLException on database error
     */
    boolean isCheckedIn(int registrationId) throws SQLException;

    /**
     * Retrieves all check-in records for a given event (joined through registrations).
     *
     * @param eventId Event ID
     * @return List of CheckIn records
     * @throws SQLException on database error
     */
    List<CheckIn> getCheckInsByEvent(int eventId) throws SQLException;

    /**
     * Returns the total number of confirmed (non-waitlist, non-cancelled) registrations
     * for an event — used as denominator in attendance rate calculation.
     *
     * @param eventId Event ID
     * @return count of confirmed registrations
     * @throws SQLException on database error
     */
    int getConfirmedRegistrationCount(int eventId) throws SQLException;
}
