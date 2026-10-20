package com.eventra.service.impl;

import com.eventra.dao.impl.RegistrationDAOImpl;
import com.eventra.dao.interfaces.IRegistrationDAO;
import com.eventra.exception.DuplicateRegistrationException;
import com.eventra.exception.EventFullException;
import com.eventra.model.Registration;
import com.eventra.service.interfaces.IRegistrationService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Business logic for transactional event registration and cancellation.
 * Delegates all DB work to IRegistrationDAO — no SQL here.
 */
public class RegistrationServiceImpl implements IRegistrationService {

    private final IRegistrationDAO registrationDAO;

    public RegistrationServiceImpl() {
        this.registrationDAO = new RegistrationDAOImpl();
    }

    /** Injection constructor for unit testing with mocks. */
    public RegistrationServiceImpl(IRegistrationDAO registrationDAO) {
        this.registrationDAO = registrationDAO;
    }

    @Override
    public Registration registerForEvent(int userId, int eventId)
            throws EventFullException, DuplicateRegistrationException {
        if (userId <= 0 || eventId <= 0) {
            throw new IllegalArgumentException("Valid userId and eventId are required.");
        }
        try {
            return registrationDAO.register(userId, eventId);
        } catch (EventFullException | DuplicateRegistrationException e) {
            throw e;
        } catch (SQLException e) {
            System.err.println("[RegistrationService.registerForEvent] DB error: " + e.getMessage());
            throw new RuntimeException("Registration failed due to a database error.", e);
        }
    }

    @Override
    public boolean cancelRegistration(int registrationId) {
        if (registrationId <= 0) {
            throw new IllegalArgumentException("Valid registrationId is required.");
        }
        try {
            return registrationDAO.cancel(registrationId);
        } catch (SQLException e) {
            System.err.println("[RegistrationService.cancelRegistration] DB error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Registration> getUserBookings(int userId) {
        try {
            return registrationDAO.findByUserId(userId);
        } catch (SQLException e) {
            System.err.println("[RegistrationService.getUserBookings] DB error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Registration> getEventRegistrations(int eventId) {
        try {
            return registrationDAO.findByEventId(eventId);
        } catch (SQLException e) {
            System.err.println("[RegistrationService.getEventRegistrations] DB error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Integer> getUserRegisteredEventIds(int userId) {
        try {
            return registrationDAO.findRegisteredEventIdsByUserId(userId);
        } catch (SQLException e) {
            System.err.println("[RegistrationService.getUserRegisteredEventIds] DB error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isUserRegistered(int userId, int eventId) {
        try {
            return registrationDAO.existsByUserAndEvent(userId, eventId);
        } catch (SQLException e) {
            System.err.println("[RegistrationService.isUserRegistered] DB error: " + e.getMessage());
            return false;
        }
    }
}
