package com.eventra.service;

import com.eventra.dao.interfaces.IRegistrationDAO;
import com.eventra.exception.DuplicateRegistrationException;
import com.eventra.exception.EventFullException;
import com.eventra.model.Registration;
import com.eventra.model.RegistrationStatus;
import com.eventra.service.impl.RegistrationServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegistrationServiceImpl using Mockito mocks.
 * No live database connections used.
 */
public class RegistrationServiceImplTest {

    private IRegistrationDAO mockRegistrationDAO;
    private RegistrationServiceImpl registrationService;

    @Before
    public void setUp() {
        mockRegistrationDAO = Mockito.mock(IRegistrationDAO.class);
        registrationService = new RegistrationServiceImpl(mockRegistrationDAO);
    }

    // ── Test 1: Happy path — successful registration ──────────────────────────
    @Test
    public void testRegisterForEvent_happyPath_returnsConfirmedRegistration()
            throws EventFullException, DuplicateRegistrationException, SQLException {
        Registration confirmed = buildRegistration(1, 10, 5, "EVT-ABC123", 3, RegistrationStatus.CONFIRMED);
        when(mockRegistrationDAO.register(10, 5)).thenReturn(confirmed);

        Registration result = registrationService.registerForEvent(10, 5);

        assertNotNull(result);
        assertEquals(RegistrationStatus.CONFIRMED, result.getStatus());
        assertEquals("EVT-ABC123", result.getBookingRef());
        verify(mockRegistrationDAO).register(10, 5);
    }

    // ── Test 2: EventFullException propagates cleanly ─────────────────────────
    @Test(expected = EventFullException.class)
    public void testRegisterForEvent_whenEventFull_throwsEventFullException()
            throws EventFullException, DuplicateRegistrationException, SQLException {
        when(mockRegistrationDAO.register(2, 99))
                .thenThrow(new EventFullException("Event 99 is full."));

        registrationService.registerForEvent(2, 99);
    }

    // ── Test 3: DuplicateRegistrationException propagates cleanly ────────────
    @Test(expected = DuplicateRegistrationException.class)
    public void testRegisterForEvent_duplicate_throwsDuplicateException()
            throws EventFullException, DuplicateRegistrationException, SQLException {
        when(mockRegistrationDAO.register(3, 7))
                .thenThrow(new DuplicateRegistrationException("Already registered."));

        registrationService.registerForEvent(3, 7);
    }

    // ── Test 4: Cancellation happy path ──────────────────────────────────────
    @Test
    public void testCancelRegistration_happyPath_returnsTrue() throws SQLException {
        when(mockRegistrationDAO.cancel(42)).thenReturn(true);

        boolean result = registrationService.cancelRegistration(42);

        assertTrue(result);
        verify(mockRegistrationDAO).cancel(42);
    }

    // ── Test 5: Cancellation on DB error returns false gracefully ────────────
    @Test
    public void testCancelRegistration_onSQLException_returnsFalse() throws SQLException {
        when(mockRegistrationDAO.cancel(99)).thenThrow(new SQLException("Timeout"));

        boolean result = registrationService.cancelRegistration(99);

        assertFalse(result);
    }

    // ── Test 6: getUserBookings returns sorted list ───────────────────────────
    @Test
    public void testGetUserBookings_returnsList() throws SQLException {
        Registration r1 = buildRegistration(1, 10, 1, "REF-001", 1, RegistrationStatus.CONFIRMED);
        Registration r2 = buildRegistration(2, 10, 2, "REF-002", 2, RegistrationStatus.WAITLIST);
        when(mockRegistrationDAO.findByUserId(10)).thenReturn(Arrays.asList(r1, r2));

        List<Registration> result = registrationService.getUserBookings(10);

        assertEquals(2, result.size());
        verify(mockRegistrationDAO).findByUserId(10);
    }

    // ── Test 7: Waitlist registration returned when event is full ────────────
    @Test
    public void testRegisterForEvent_waitlistReturned()
            throws EventFullException, DuplicateRegistrationException, SQLException {
        Registration waitlisted = buildRegistration(5, 10, 3, "EVT-WAIT01", 0, RegistrationStatus.WAITLIST);
        when(mockRegistrationDAO.register(10, 3)).thenReturn(waitlisted);

        Registration result = registrationService.registerForEvent(10, 3);

        assertNotNull(result);
        assertEquals(RegistrationStatus.WAITLIST, result.getStatus());
        assertEquals(0, result.getSeatNumber()); // 0 means no seat assigned yet
    }

    // ── Test 8: Invalid userId throws IllegalArgumentException ─────────────────
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterForEvent_invalidUserId_throwsException()
            throws EventFullException, DuplicateRegistrationException {
        registrationService.registerForEvent(0, 5);
    }

    // ── Test 9: isUserRegistered delegates to DAO ─────────────────────────────
    @Test
    public void testIsUserRegistered_delegatesToDAO() throws SQLException {
        when(mockRegistrationDAO.existsByUserAndEvent(1, 2)).thenReturn(true);

        boolean result = registrationService.isUserRegistered(1, 2);

        assertTrue(result);
        verify(mockRegistrationDAO).existsByUserAndEvent(1, 2);
    }

    // ── Test 10: getUserBookings — DB error returns empty list ────────────────
    @Test
    public void testGetUserBookings_onSQLException_returnsEmptyList() throws SQLException {
        when(mockRegistrationDAO.findByUserId(55)).thenThrow(new SQLException("DB down"));

        List<Registration> result = registrationService.getUserBookings(55);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Registration buildRegistration(int id, int userId, int eventId,
                                            String bookingRef, int seatNumber,
                                            RegistrationStatus status) {
        Registration r = new Registration();
        r.setId(id);
        r.setUserId(userId);
        r.setEventId(eventId);
        r.setBookingRef(bookingRef);
        r.setSeatNumber(seatNumber);
        r.setStatus(status);
        r.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
        return r;
    }
}
