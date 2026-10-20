package com.eventra.service.impl;

import com.eventra.dao.impl.EventDAOImpl;
import com.eventra.dao.impl.InterestDAOImpl;
import com.eventra.dao.interfaces.IEventDAO;
import com.eventra.dao.interfaces.IInterestDAO;
import com.eventra.model.Event;
import com.eventra.model.EventStatus;
import com.eventra.service.interfaces.IEventService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Business logic for event catalog management and capacity tracking.
 * Calls DAO layer only — no SQL here.
 */
public class EventServiceImpl implements IEventService {

    private final IEventDAO eventDAO;
    private final IInterestDAO interestDAO;

    public EventServiceImpl() {
        this.eventDAO = new EventDAOImpl();
        this.interestDAO = new InterestDAOImpl();
    }

    /** Injection constructor for unit testing with mocks. */
    public EventServiceImpl(IEventDAO eventDAO, IInterestDAO interestDAO) {
        this.eventDAO = eventDAO;
        this.interestDAO = interestDAO;
    }

    @Override
    public List<Event> getAllEvents() {
        try {
            return eventDAO.findAll();
        } catch (SQLException e) {
            System.err.println("[EventService.getAllEvents] DB error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Event> getEventsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllEvents();
        }
        try {
            return eventDAO.findByCategory(category.trim());
        } catch (SQLException e) {
            System.err.println("[EventService.getEventsByCategory] DB error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Event> getEventsByStatus(EventStatus status) {
        try {
            return eventDAO.findByStatus(status);
        } catch (SQLException e) {
            System.err.println("[EventService.getEventsByStatus] DB error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Event createEvent(Event event, List<Integer> interestIds) {
        if (event == null || event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Event title is required.");
        }
        // Initialize seats_left = capacity on creation
        event.setSeatsLeft(event.getCapacity());
        event.setStatus(EventStatus.UPCOMING);
        try {
            boolean saved = eventDAO.save(event);
            if (!saved) {
                throw new RuntimeException("Event could not be persisted.");
            }
            if (interestIds != null && !interestIds.isEmpty()) {
                interestDAO.saveEventInterests(event.getId(), interestIds);
            }
            return event;
        } catch (SQLException e) {
            System.err.println("[EventService.createEvent] DB error: " + e.getMessage());
            throw new RuntimeException("Event creation failed due to a database error.", e);
        }
    }

    @Override
    public boolean updateEvent(Event event, List<Integer> interestIds) {
        if (event == null || event.getId() <= 0) {
            throw new IllegalArgumentException("Valid event with ID is required for update.");
        }
        try {
            boolean updated = eventDAO.update(event);
            if (updated && interestIds != null) {
                interestDAO.saveEventInterests(event.getId(), interestIds);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("[EventService.updateEvent] DB error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteEvent(int eventId) {
        try {
            return eventDAO.delete(eventId);
        } catch (SQLException e) {
            System.err.println("[EventService.deleteEvent] DB error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Event getEventWithDetails(int eventId) {
        try {
            return eventDAO.findById(eventId);
        } catch (SQLException e) {
            System.err.println("[EventService.getEventWithDetails] DB error: " + e.getMessage());
            return null;
        }
    }
}
