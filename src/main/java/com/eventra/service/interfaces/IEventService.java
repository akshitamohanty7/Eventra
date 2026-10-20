package com.eventra.service.interfaces;

import com.eventra.model.Event;
import com.eventra.model.EventStatus;

import java.util.List;

/**
 * Service contract for event lifecycle management, catalog queries, and capacity tracking.
 */
public interface IEventService {

    /**
     * Retrieves all events in the system.
     *
     * @return List of all events
     */
    List<Event> getAllEvents();

    /**
     * Filters events by category.
     *
     * @param category category name (e.g. Workshop, Hackathon, Seminar, Cultural)
     * @return List of matching events
     */
    List<Event> getEventsByCategory(String category);

    /**
     * Filters events by status.
     *
     * @param status EventStatus enum value
     * @return List of matching events
     */
    List<Event> getEventsByStatus(EventStatus status);

    /**
     * Creates a new event with optional associated interest tags.
     *
     * @param event Event object
     * @param interestIds list of interest IDs linked to this event
     * @return persisted Event object with generated ID
     */
    Event createEvent(Event event, List<Integer> interestIds);

    /**
     * Updates an existing event and its interest associations.
     *
     * @param event Event object
     * @param interestIds list of interest IDs
     * @return true if updated successfully, false otherwise
     */
    boolean updateEvent(Event event, List<Integer> interestIds);

    /**
     * Deletes an event by ID.
     *
     * @param eventId Event ID
     * @return true if deleted, false otherwise
     */
    boolean deleteEvent(int eventId);

    /**
     * Retrieves an event by ID with full details.
     *
     * @param eventId Event ID
     * @return Event object or null if not found
     */
    Event getEventWithDetails(int eventId);
}
