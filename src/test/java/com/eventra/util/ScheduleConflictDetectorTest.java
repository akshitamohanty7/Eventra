package com.eventra.util;

import com.eventra.model.Event;
import com.eventra.model.EventStatus;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for ScheduleConflictDetector.
 * Covers: exact overlap, partial overlap, no overlap, back-to-back (not a conflict).
 */
public class ScheduleConflictDetectorTest {

    // Base times for tests
    // T0 = 10:00, T1 = 11:00, T2 = 12:00, T3 = 13:00
    private static final long T0 = 1700000000000L;
    private static final long T1 = T0 + 3600_000L;
    private static final long T2 = T0 + 7200_000L;
    private static final long T3 = T0 + 10800_000L;

    // ── Test 1: Exact same timeslot — both events conflict ────────────────────
    @Test
    public void testDetectConflicts_exactOverlap_bothReturned() {
        Event a = buildEvent(1, T0, T2);
        Event b = buildEvent(2, T0, T2);

        List<Event> conflicts = ScheduleConflictDetector.detectConflicts(Arrays.asList(a, b));

        assertEquals(2, conflicts.size());
        assertTrue(conflicts.contains(a));
        assertTrue(conflicts.contains(b));
    }

    // ── Test 2: Partial overlap (A starts before B ends) ─────────────────────
    @Test
    public void testDetectConflicts_partialOverlap_bothFlagged() {
        // A: 10:00 → 12:00, B: 11:00 → 13:00 — overlap from 11:00–12:00
        Event a = buildEvent(3, T0, T2);
        Event b = buildEvent(4, T1, T3);

        List<Event> conflicts = ScheduleConflictDetector.detectConflicts(Arrays.asList(a, b));

        assertEquals(2, conflicts.size());
    }

    // ── Test 3: No overlap — completely separate timeslots ───────────────────
    @Test
    public void testDetectConflicts_noOverlap_emptyList() {
        // A: 10:00 → 11:00, B: 12:00 → 13:00 — no overlap
        Event a = buildEvent(5, T0, T1);
        Event b = buildEvent(6, T2, T3);

        List<Event> conflicts = ScheduleConflictDetector.detectConflicts(Arrays.asList(a, b));

        assertTrue(conflicts.isEmpty());
    }

    // ── Test 4: Back-to-back — end of A == start of B — NOT a conflict ───────
    @Test
    public void testDetectConflicts_backToBack_notAConflict() {
        // A: 10:00 → 11:00, B: 11:00 → 12:00 — back-to-back, not overlapping
        Event a = buildEvent(7, T0, T1);
        Event b = buildEvent(8, T1, T2);

        List<Event> conflicts = ScheduleConflictDetector.detectConflicts(Arrays.asList(a, b));

        assertTrue(conflicts.isEmpty());
    }

    // ── Test 5: Single event list — cannot conflict with itself ──────────────
    @Test
    public void testDetectConflicts_singleEvent_emptyResult() {
        Event a = buildEvent(9, T0, T2);

        List<Event> conflicts = ScheduleConflictDetector.detectConflicts(Collections.singletonList(a));

        assertTrue(conflicts.isEmpty());
    }

    // ── Test 6: Null list → empty result, no NPE ─────────────────────────────
    @Test
    public void testDetectConflicts_nullInput_returnsEmpty() {
        List<Event> conflicts = ScheduleConflictDetector.detectConflicts(null);
        assertNotNull(conflicts);
        assertTrue(conflicts.isEmpty());
    }

    // ── Test 7: Three events — only 2 overlap, third is clean ────────────────
    @Test
    public void testDetectConflicts_threeEvents_onlyConflictingOnesReturned() {
        // A and B overlap; C is separate
        Event a = buildEvent(10, T0, T2); // 10:00–12:00
        Event b = buildEvent(11, T1, T3); // 11:00–13:00
        Event c = buildEvent(12, T3, T3 + 3600_000L); // 13:00–14:00

        List<Event> conflicts = ScheduleConflictDetector.detectConflicts(Arrays.asList(a, b, c));

        assertEquals(2, conflicts.size());
        assertTrue(conflicts.contains(a));
        assertTrue(conflicts.contains(b));
        assertFalse(conflicts.contains(c));
    }

    // ── Test 8: overlaps() static method directly ────────────────────────────
    @Test
    public void testOverlaps_directCheck() {
        Event a = buildEvent(13, T0, T2);
        Event b = buildEvent(14, T1, T3);
        Event c = buildEvent(15, T2, T3); // back-to-back with a

        assertTrue(ScheduleConflictDetector.overlaps(a, b));
        assertFalse(ScheduleConflictDetector.overlaps(a, c));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Event buildEvent(int id, long startMs, long endMs) {
        Event e = new Event();
        e.setId(id);
        e.setTitle("Event " + id);
        e.setCategory("Workshop");
        e.setVenue("Room " + id);
        e.setCapacity(50);
        e.setSeatsLeft(20);
        e.setStatus(EventStatus.UPCOMING);
        e.setStartTime(new Timestamp(startMs));
        e.setEndTime(new Timestamp(endMs));
        return e;
    }
}
