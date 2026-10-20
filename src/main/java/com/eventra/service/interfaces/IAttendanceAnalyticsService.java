package com.eventra.service.interfaces;

import java.util.Map;

/**
 * Service contract for per-event post-event attendance analytics.
 */
public interface IAttendanceAnalyticsService {

    /**
     * Returns the check-in rate for an event as a percentage.
     * Formula: (checkIns / confirmedRegistrations) * 100
     *
     * @param eventId Event ID
     * @return attendance rate 0–100, or 0 if no confirmed registrations
     */
    int getAttendanceRate(int eventId);

    /**
     * Returns the capacity utilization for an event as a percentage.
     * Formula: ((capacity - seatsLeft) / capacity) * 100
     *
     * @param eventId Event ID
     * @return capacity utilization 0–100
     */
    int getCapacityUtilization(int eventId);

    /**
     * Returns the waitlist-to-confirmed conversion rate for an event.
     * Formula: (promoted waitlist entries / total waitlist entries) * 100
     *
     * @param eventId Event ID
     * @return conversion rate 0–100
     */
    int getWaitlistConversionRate(int eventId);

    /**
     * Returns all analytics for an event as a map for JSP rendering.
     * Keys: "attendanceRate", "capacityUtilization", "waitlistConversionRate",
     *       "totalCheckIns", "confirmedCount", "cancelledCount"
     *
     * @param eventId Event ID
     * @return Map of stat key to value
     */
    Map<String, Object> getEventStats(int eventId);
}
