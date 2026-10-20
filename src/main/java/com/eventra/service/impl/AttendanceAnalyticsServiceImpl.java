package com.eventra.service.impl;

import com.eventra.dao.impl.CheckInDAOImpl;
import com.eventra.dao.impl.EventDAOImpl;
import com.eventra.dao.interfaces.ICheckInDAO;
import com.eventra.dao.interfaces.IEventDAO;
import com.eventra.model.Event;
import com.eventra.service.interfaces.IAttendanceAnalyticsService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes per-event attendance analytics.
 * Reads check-in and registration counts via DAOs — no SQL here.
 */
public class AttendanceAnalyticsServiceImpl implements IAttendanceAnalyticsService {

    private final ICheckInDAO checkInDAO;
    private final IEventDAO eventDAO;

    public AttendanceAnalyticsServiceImpl() {
        this.checkInDAO = new CheckInDAOImpl();
        this.eventDAO = new EventDAOImpl();
    }

    public AttendanceAnalyticsServiceImpl(ICheckInDAO checkInDAO, IEventDAO eventDAO) {
        this.checkInDAO = checkInDAO;
        this.eventDAO = eventDAO;
    }

    @Override
    public int getAttendanceRate(int eventId) {
        try {
            int confirmed = checkInDAO.getConfirmedRegistrationCount(eventId);
            if (confirmed == 0) return 0;
            int checkIns = checkInDAO.getCheckInsByEvent(eventId).size();
            return (int) Math.round((double) checkIns / confirmed * 100);
        } catch (SQLException e) {
            System.err.println("[AttendanceAnalytics.getAttendanceRate] DB error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int getCapacityUtilization(int eventId) {
        try {
            Event event = eventDAO.findById(eventId);
            if (event == null || event.getCapacity() <= 0) return 0;
            int booked = event.getCapacity() - event.getSeatsLeft();
            return (int) Math.round((double) booked / event.getCapacity() * 100);
        } catch (SQLException e) {
            System.err.println("[AttendanceAnalytics.getCapacityUtilization] DB error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int getWaitlistConversionRate(int eventId) {
        // Waitlist conversion = seats promoted to CONFIRMED / total waitlisted ever
        // Approximated as: (confirmed - original capacity + seatsLeft) is complex;
        // Instead: count current CONFIRMED that had seat_number set via waitlist promotion.
        // Simple approach: query directly.
        try {
            int total = getWaitlistCount(eventId, null);
            int promoted = getWaitlistCount(eventId, "CONFIRMED");
            if (total == 0) return 0;
            return (int) Math.round((double) promoted / total * 100);
        } catch (SQLException e) {
            System.err.println("[AttendanceAnalytics.getWaitlistConversionRate] DB error: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Map<String, Object> getEventStats(int eventId) {
        Map<String, Object> stats = new HashMap<>();
        try {
            int confirmed = checkInDAO.getConfirmedRegistrationCount(eventId);
            int checkIns = checkInDAO.getCheckInsByEvent(eventId).size();
            Event event = eventDAO.findById(eventId);
            int cancelled = getCancelledCount(eventId);

            stats.put("attendanceRate", confirmed > 0
                    ? (int) Math.round((double) checkIns / confirmed * 100) : 0);
            stats.put("capacityUtilization", (event != null && event.getCapacity() > 0)
                    ? (int) Math.round((double)(event.getCapacity() - event.getSeatsLeft())
                            / event.getCapacity() * 100) : 0);
            stats.put("waitlistConversionRate", getWaitlistConversionRate(eventId));
            stats.put("totalCheckIns", checkIns);
            stats.put("confirmedCount", confirmed);
            stats.put("cancelledCount", cancelled);
        } catch (SQLException e) {
            System.err.println("[AttendanceAnalytics.getEventStats] DB error: " + e.getMessage());
        }
        return stats;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private int getWaitlistCount(int eventId, String statusFilter) throws SQLException {
        // statusFilter = null → count all originally waitlisted (current WAITLIST)
        // statusFilter = "CONFIRMED" → count promoted (seat_number > 0, originally WAITLIST)
        // Approximation: seat_number = 0 means still on waitlist
        String sql = statusFilter == null
                ? "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND seat_number = 0"
                : "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'CONFIRMED' AND seat_number > 0";
        try (java.sql.Connection conn = new CheckInDAOImpl().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int getCancelledCount(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'CANCELLED'";
        try (java.sql.Connection conn = new CheckInDAOImpl().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
