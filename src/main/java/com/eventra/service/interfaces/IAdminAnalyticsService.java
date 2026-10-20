package com.eventra.service.interfaces;

import java.util.Map;

/**
 * Service contract for admin-level aggregate analytics queries.
 */
public interface IAdminAnalyticsService {

    /**
     * Returns the total number of events in the system.
     *
     * @return total event count
     */
    int getTotalEventCount();

    /**
     * Returns the total number of registrations across all events.
     *
     * @return total registration count
     */
    int getTotalRegistrationCount();

    /**
     * Returns the count of events with status UPCOMING or ALMOST_FULL.
     *
     * @return upcoming event count
     */
    int getUpcomingEventCount();

    /**
     * Returns the sum of seats_left across all non-cancelled events.
     *
     * @return total seats remaining
     */
    int getTotalSeatsLeft();

    /**
     * Returns all four metrics in a single Map for convenient JSP access.
     * Keys: "totalEvents", "totalRegistrations", "upcomingEvents", "totalSeatsLeft"
     *
     * @return Map of stat key to integer value
     */
    Map<String, Integer> getAllStats();
}
