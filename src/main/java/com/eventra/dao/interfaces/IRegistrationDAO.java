package com.eventra.dao.interfaces;

import com.eventra.exception.DuplicateRegistrationException;
import com.eventra.exception.EventFullException;
import com.eventra.model.Registration;
import com.eventra.model.RegistrationStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object contract for Registration and seat allocation operations.
 */
public interface IRegistrationDAO {

    /**
     * Executes the registration flow in a database transaction:
     * checks duplicate registration, checks seats, allocates seat, decrements seats_left,
     * updates event status, and returns the confirmed or waitlisted Registration.
     *
     * @param userId attendee user ID
     * @param eventId target event ID
     * @return created Registration object
     * @throws EventFullException if the event is full and cannot take waitlist
     * @throws DuplicateRegistrationException if user is already registered
     * @throws SQLException on database error
     */
    Registration register(int userId, int eventId) throws EventFullException, DuplicateRegistrationException, SQLException;

    /**
     * Cancels an existing registration in a database transaction:
     * sets status to CANCELLED, increments seats_left, and automatically promotes
     * the first waitlisted attendee (if any).
     *
     * @param registrationId Registration ID
     * @return true if cancelled successfully, false otherwise
     * @throws SQLException on database error
     */
    boolean cancel(int registrationId) throws SQLException;

    /**
     * Finds a registration by its ID.
     *
     * @param id Registration ID
     * @return Registration object if found, null otherwise
     * @throws SQLException on database error
     */
    Registration findById(int id) throws SQLException;

    /**
     * Retrieves all registrations made by a specific user with event details joined.
     *
     * @param userId User ID
     * @return List of registrations
     * @throws SQLException on database error
     */
    List<Registration> findByUserId(int userId) throws SQLException;

    /**
     * Retrieves all registrations for a specific event with attendee details joined.
     *
     * @param eventId Event ID
     * @return List of registrations
     * @throws SQLException on database error
     */
    List<Registration> findByEventId(int eventId) throws SQLException;

    /**
     * Checks if a user already has an active (non-cancelled) registration for an event.
     *
     * @param userId User ID
     * @param eventId Event ID
     * @return true if registered, false otherwise
     * @throws SQLException on database error
     */
    boolean existsByUserAndEvent(int userId, int eventId) throws SQLException;

    /**
     * Retrieves the set/list of event IDs the user is currently registered for.
     *
     * @param userId User ID
     * @return List of event IDs
     * @throws SQLException on database error
     */
    List<Integer> findRegisteredEventIdsByUserId(int userId) throws SQLException;

    /**
     * Finds the earliest waitlisted registration for an event inside a transaction.
     *
     * @param eventId Event ID
     * @param conn active transaction connection
     * @return Registration if found, null otherwise
     * @throws SQLException on database error
     */
    Registration findFirstWaitlistByEventId(int eventId, Connection conn) throws SQLException;

    /**
     * Updates the status of a registration within an active transaction.
     *
     * @param registrationId Registration ID
     * @param status new RegistrationStatus
     * @param conn active transaction connection
     * @return true if updated, false otherwise
     * @throws SQLException on database error
     */
    boolean updateStatus(int registrationId, RegistrationStatus status, Connection conn) throws SQLException;
}
