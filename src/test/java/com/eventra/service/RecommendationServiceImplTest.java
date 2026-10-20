package com.eventra.service;

import com.eventra.dao.interfaces.IEventDAO;
import com.eventra.dao.interfaces.IInterestDAO;
import com.eventra.model.Event;
import com.eventra.model.EventStatus;
import com.eventra.model.Interest;
import com.eventra.service.impl.RecommendationServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecommendationServiceImpl.
 * Covers 0% match, 100% match, partial match, and empty interests.
 */
public class RecommendationServiceImplTest {

    private IInterestDAO mockInterestDAO;
    private IEventDAO mockEventDAO;
    private RecommendationServiceImpl service;

    @Before
    public void setUp() {
        mockInterestDAO = Mockito.mock(IInterestDAO.class);
        mockEventDAO = Mockito.mock(IEventDAO.class);
        service = new RecommendationServiceImpl(mockInterestDAO, mockEventDAO);
    }

    // ── Test 1: User with no interests → empty recommendations ───────────────
    @Test
    public void testGetRecommendations_noUserInterests_returnsEmpty() throws SQLException {
        when(mockInterestDAO.findByUserId(1)).thenReturn(Collections.emptyList());

        List<Event> result = service.getRecommendations(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── Test 2: 100% match — event has same interests as user ────────────────
    @Test
    public void testGetScoreMap_perfectMatch_returns100() throws SQLException {
        Interest workshop = new Interest(1, "Workshop");
        when(mockInterestDAO.findByUserId(2)).thenReturn(Collections.singletonList(workshop));

        Event e = buildEvent(10, "Workshop A", EventStatus.UPCOMING);
        when(mockEventDAO.findAll()).thenReturn(Collections.singletonList(e));
        when(mockInterestDAO.findByEventId(10)).thenReturn(Collections.singletonList(workshop));

        Map<Integer, Integer> scoreMap = service.getScoreMap(2);

        assertEquals(1, scoreMap.size());
        assertEquals(Integer.valueOf(100), scoreMap.get(10));
    }

    // ── Test 3: 0% match — no overlapping interests ──────────────────────────
    @Test
    public void testGetScoreMap_noMatch_eventNotInMap() throws SQLException {
        Interest workshop = new Interest(1, "Workshop");
        Interest hackathon = new Interest(2, "Hackathon");
        when(mockInterestDAO.findByUserId(3)).thenReturn(Collections.singletonList(workshop));

        Event e = buildEvent(20, "Hackathon Z", EventStatus.UPCOMING);
        when(mockEventDAO.findAll()).thenReturn(Collections.singletonList(e));
        when(mockInterestDAO.findByEventId(20)).thenReturn(Collections.singletonList(hackathon));

        Map<Integer, Integer> scoreMap = service.getScoreMap(3);

        assertFalse(scoreMap.containsKey(20));
    }

    // ── Test 4: Partial match — 1 out of 2 interests match → 50% ────────────
    @Test
    public void testGetScoreMap_partialMatch_returns50() throws SQLException {
        Interest workshop = new Interest(1, "Workshop");
        Interest seminar = new Interest(3, "Seminar");
        when(mockInterestDAO.findByUserId(4)).thenReturn(Arrays.asList(workshop, seminar));

        Event e = buildEvent(30, "Mixed Event", EventStatus.UPCOMING);
        when(mockEventDAO.findAll()).thenReturn(Collections.singletonList(e));
        when(mockInterestDAO.findByEventId(30)).thenReturn(Collections.singletonList(workshop));

        Map<Integer, Integer> scoreMap = service.getScoreMap(4);

        assertEquals(Integer.valueOf(50), scoreMap.get(30));
    }

    // ── Test 5: FULL events excluded from recommendations ────────────────────
    @Test
    public void testGetRecommendations_fullEventExcluded() throws SQLException {
        Interest workshop = new Interest(1, "Workshop");
        when(mockInterestDAO.findByUserId(5)).thenReturn(Collections.singletonList(workshop));

        Event fullEvent = buildEvent(40, "Full Workshop", EventStatus.FULL);
        when(mockEventDAO.findAll()).thenReturn(Collections.singletonList(fullEvent));
        when(mockInterestDAO.findByEventId(40)).thenReturn(Collections.singletonList(workshop));

        List<Event> result = service.getRecommendations(5);

        assertTrue(result.isEmpty());
    }

    // ── Test 6: Results capped at 5 ──────────────────────────────────────────
    @Test
    public void testGetRecommendations_capsAtFive() throws SQLException {
        Interest w = new Interest(1, "Workshop");
        when(mockInterestDAO.findByUserId(6)).thenReturn(Collections.singletonList(w));

        List<Event> sixEvents = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Event e = buildEvent(i * 100, "Event " + i, EventStatus.UPCOMING);
            sixEvents.add(e);
            when(mockInterestDAO.findByEventId(i * 100)).thenReturn(Collections.singletonList(w));
        }
        when(mockEventDAO.findAll()).thenReturn(sixEvents);

        List<Event> result = service.getRecommendations(6);

        assertTrue(result.size() <= 5);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Event buildEvent(int id, String title, EventStatus status) {
        Event e = new Event();
        e.setId(id);
        e.setTitle(title);
        e.setCategory("Workshop");
        e.setVenue("Test Venue");
        e.setCapacity(50);
        e.setSeatsLeft(status == EventStatus.FULL ? 0 : 20);
        e.setStatus(status);
        e.setStartTime(new Timestamp(System.currentTimeMillis() + 86400000));
        e.setEndTime(new Timestamp(System.currentTimeMillis() + 90000000));
        return e;
    }
}
