package com.eventra.service.impl;

import com.eventra.dao.AbstractDAO;
import com.eventra.service.interfaces.IAdminAnalyticsService;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides aggregate COUNT/SUM analytics queries for the admin dashboard.
 * Extends AbstractDAO to access getConnection() — this is analytical read-only SQL,
 * not a DAO pattern violation since there are no corresponding domain objects.
 */
public class AdminAnalyticsServiceImpl extends AbstractDAO implements IAdminAnalyticsService {

    private static final String SQL_TOTAL_EVENTS =
            "SELECT COUNT(*) FROM events WHERE status != 'CANCELLED'";
    private static final String SQL_TOTAL_REGISTRATIONS =
            "SELECT COUNT(*) FROM registrations WHERE status != 'CANCELLED'";
    private static final String SQL_UPCOMING_EVENTS =
            "SELECT COUNT(*) FROM events WHERE status IN ('UPCOMING', 'ALMOST_FULL')";
    private static final String SQL_TOTAL_SEATS_LEFT =
            "SELECT COALESCE(SUM(seats_left), 0) FROM events WHERE status NOT IN ('CANCELLED', 'COMPLETED')";

    @Override
    public int getTotalEventCount() {
        return runCountQuery(SQL_TOTAL_EVENTS);
    }

    @Override
    public int getTotalRegistrationCount() {
        return runCountQuery(SQL_TOTAL_REGISTRATIONS);
    }

    @Override
    public int getUpcomingEventCount() {
        return runCountQuery(SQL_UPCOMING_EVENTS);
    }

    @Override
    public int getTotalSeatsLeft() {
        return runCountQuery(SQL_TOTAL_SEATS_LEFT);
    }

    @Override
    public Map<String, Integer> getAllStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalEvents", getTotalEventCount());
        stats.put("totalRegistrations", getTotalRegistrationCount());
        stats.put("upcomingEvents", getUpcomingEventCount());
        stats.put("totalSeatsLeft", getTotalSeatsLeft());
        return stats;
    }

    private int runCountQuery(String sql) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("[AdminAnalytics] DB error on query '" + sql + "': " + e.getMessage());
            return 0;
        }
    }
}
