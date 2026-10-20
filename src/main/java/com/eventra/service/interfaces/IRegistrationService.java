package com.eventra.service.interfaces;

import com.eventra.exception.DuplicateRegistrationException;
import com.eventra.exception.EventFullException;
import com.eventra.model.Registration;

import java.util.List;

/**
 * Service contract for transactional event registration, cancellation, and booking queries.
 */
public interface IRegistrationService {

    /**
     * Atomically registers an attendee for an event.
     * Checks duplicate bookings and capacity constraints within a database transaction.
     *
     * @param userId Attendee user ID
     * @param eventId Target event ID
     * @return confirmed or waitlisted Registration
     * @throws EventFullException if capacity is reached
     * @throws DuplicateRegistrationException if attendee is already registered
     */
    Registration registerForEvent(int userId, int eventId) throws EventFullException, DuplicateRegistrationException;

    /**
     * Cancels an existing booking and auto-promotes the first waitlisted attendee (if any).
     *
     * @param registrationId Registration ID
     * @return true if successfully cancelled, false otherwise
     */
    boolean cancelRegistration(int registrationId);

    /**
     * Retrieves all bookings for a user, sorted with upcoming events first.
     *
     * @param userId User ID
     * @return List of Registration records
     */
    List<Registration> getUserBookings(int userId);

    /**
     * Retrieves all registrations for an event (used by organizers and admins).
     *
     * @param eventId Event ID
     * @return List of Registration records
     */
    List<Registration> getEventRegistrations(int eventId);

    /**
     * Retrieves a list of event IDs the user is currently registered for.
     *
     * @param userId User ID
     * @return List of event IDs
     */
    List<Integer> getUserRegisteredEventIds(int userId);

    /**
     * Checks if a user has an active registration for an event.
     *
     * @param userId User ID
     * @param eventId Event ID
     * @return true if registered, false otherwise
     */
    boolean isUserRegistered(int userId, int eventId);
}
