package com.eventra.service;

import com.eventra.dao.interfaces.IEventDAO;
import com.eventra.dao.interfaces.IInterestDAO;
import com.eventra.model.Event;
import com.eventra.model.EventStatus;
import com.eventra.service.impl.EventServiceImpl;
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
 * Unit tests for EventServiceImpl using Mockito mocks.
 * No live database connections used.
 */
public class EventServiceImplTest {

    private IEventDAO mockEventDAO;
    private IInterestDAO mockInterestDAO;
    private EventServiceImpl eventService;

    @Before
    public void setUp() {
        mockEventDAO = Mockito.mock(IEventDAO.class);
        mockInterestDAO = Mockito.mock(IInterestDAO.class);
        eventService = new EventServiceImpl(mockEventDAO, mockInterestDAO);
    }

    // ── Test 1: getAllEvents returns list ─────────────────────────────────────
    @Test
    public void testGetAllEvents_returnsList() throws SQLException {
        Event e1 = buildEvent(1, "Workshop A", "Workshop", 50, 50, EventStatus.UPCOMING);
        Event e2 = buildEvent(2, "Hackathon X", "Hackathon", 100, 80, EventStatus.UPCOMING);
        when(mockEventDAO.findAll()).thenReturn(Arrays.asList(e1, e2));

        List<Event> result = eventService.getAllEvents();

        assertEquals(2, result.size());
        verify(mockEventDAO, times(1)).findAll();
    }

    // ── Test 2: getAllEvents — DB error returns empty list ────────────────────
    @Test
    public void testGetAllEvents_onSQLException_returnsEmptyList() throws SQLException {
        when(mockEventDAO.findAll()).thenThrow(new SQLException("Connection refused"));

        List<Event> result = eventService.getAllEvents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── Test 3: getEventsByCategory filters correctly ─────────────────────────
    @Test
    public void testGetEventsByCategory_returnsFilteredEvents() throws SQLException {
        Event e = buildEvent(3, "Seminar Z", "Seminar", 30, 30, EventStatus.UPCOMING);
        when(mockEventDAO.findByCategory("Seminar")).thenReturn(Collections.singletonList(e));

        List<Event> result = eventService.getEventsByCategory("Seminar");

        assertEquals(1, result.size());
        assertEquals("Seminar", result.get(0).getCategory());
        verify(mockEventDAO).findByCategory("Seminar");
    }

    // ── Test 4: createEvent initializes seatsLeft = capacity ─────────────────
    @Test
    public void testCreateEvent_initializesSeatsLeftAndStatus() throws SQLException {
        Event input = buildEvent(0, "New Event", "Cultural", 100, 0, null);
        when(mockEventDAO.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            saved.setId(99);
            return true;
        });
        when(mockInterestDAO.saveEventInterests(anyInt(), anyList())).thenReturn(true);

        Event result = eventService.createEvent(input, Collections.singletonList(1));

        assertEquals(EventStatus.UPCOMING, result.getStatus());
        assertEquals(100, result.getSeatsLeft()); // must be set to capacity
        assertEquals(99, result.getId());
        verify(mockEventDAO).save(any(Event.class));
    }

    // ── Test 5: createEvent with null title throws IllegalArgumentException ───
    @Test(expected = IllegalArgumentException.class)
    public void testCreateEvent_nullTitle_throwsException() {
        Event bad = new Event();
        bad.setTitle(null);
        eventService.createEvent(bad, null);
    }

    // ── Test 6: deleteEvent delegates to DAO and returns true ─────────────────
    @Test
    public void testDeleteEvent_returnsTrue() throws SQLException {
        when(mockEventDAO.delete(5)).thenReturn(true);

        boolean result = eventService.deleteEvent(5);

        assertTrue(result);
        verify(mockEventDAO).delete(5);
    }

    // ── Test 7: getEventsByStatus delegates correctly ─────────────────────────
    @Test
    public void testGetEventsByStatus_delegatesToDAO() throws SQLException {
        Event almostFull = buildEvent(4, "Tech Talk", "Tech Talk", 5, 1, EventStatus.ALMOST_FULL);
        when(mockEventDAO.findByStatus(EventStatus.ALMOST_FULL))
                .thenReturn(Collections.singletonList(almostFull));

        List<Event> result = eventService.getEventsByStatus(EventStatus.ALMOST_FULL);

        assertEquals(1, result.size());
        assertEquals(EventStatus.ALMOST_FULL, result.get(0).getStatus());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Event buildEvent(int id, String title, String category,
                              int capacity, int seatsLeft, EventStatus status) {
        Event e = new Event();
        e.setId(id);
        e.setTitle(title);
        e.setCategory(category);
        e.setVenue("Test Venue");
        e.setCapacity(capacity);
        e.setSeatsLeft(seatsLeft);
        e.setStatus(status != null ? status : EventStatus.UPCOMING);
        e.setStartTime(new Timestamp(System.currentTimeMillis()));
        e.setEndTime(new Timestamp(System.currentTimeMillis() + 3600000));
        return e;
    }
}
