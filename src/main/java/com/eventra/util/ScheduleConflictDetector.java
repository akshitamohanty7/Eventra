package com.eventra.util;

import com.eventra.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless utility for detecting scheduling conflicts between events.
 * A conflict exists when two events overlap in time:
 *   eventA.startTime < eventB.endTime AND eventA.endTime > eventB.startTime
 * Back-to-back events (end == start) are NOT considered conflicts.
 */
public final class ScheduleConflictDetector {

    private ScheduleConflictDetector() {
        // Prevent instantiation — stateless utility class
    }

    /**
     * Returns every event in the list that overlaps with at least one other event.
     *
     * @param registeredEvents list of events the user is registered for
     * @return list of events involved in at least one time conflict (no duplicates)
     */
    public static List<Event> detectConflicts(List<Event> registeredEvents) {
        List<Event> conflicting = new ArrayList<>();
        if (registeredEvents == null || registeredEvents.size() < 2) {
            return conflicting;
        }
        for (int i = 0; i < registeredEvents.size(); i++) {
            for (int j = i + 1; j < registeredEvents.size(); j++) {
                Event a = registeredEvents.get(i);
                Event b = registeredEvents.get(j);
                if (overlaps(a, b)) {
                    if (!conflicting.contains(a)) conflicting.add(a);
                    if (!conflicting.contains(b)) conflicting.add(b);
                }
            }
        }
        return conflicting;
    }

    /**
     * Finds a same-category, non-conflicting alternative for the given event.
     *
     * @param conflictingEvent the event causing the conflict
     * @param registeredEvents the user's current registered events
     * @param allEvents        full list of available events
     * @return an alternative Event, or null if none found
     */
    public static Event getSuggestedAlternative(Event conflictingEvent,
                                                 List<Event> registeredEvents,
                                                 List<Event> allEvents) {
        if (conflictingEvent == null || allEvents == null) return null;
        for (Event candidate : allEvents) {
            if (candidate.getId() == conflictingEvent.getId()) continue;
            if (!candidate.getCategory().equalsIgnoreCase(conflictingEvent.getCategory())) continue;
            boolean hasConflict = false;
            for (Event registered : registeredEvents) {
                if (registered.getId() == candidate.getId()) {
                    hasConflict = true;
                    break;
                }
                if (overlaps(candidate, registered)) {
                    hasConflict = true;
                    break;
                }
            }
            if (!hasConflict) return candidate;
        }
        return null;
    }

    /**
     * Returns true if two events overlap in time.
     * Back-to-back (a.endTime == b.startTime) is NOT a conflict.
     */
    public static boolean overlaps(Event a, Event b) {
        if (a.getStartTime() == null || a.getEndTime() == null
                || b.getStartTime() == null || b.getEndTime() == null) {
            return false;
        }
        return a.getStartTime().before(b.getEndTime())
                && a.getEndTime().after(b.getStartTime());
    }
}
