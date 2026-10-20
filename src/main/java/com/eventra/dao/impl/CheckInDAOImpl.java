package com.eventra.dao.impl;

import com.eventra.dao.AbstractDAO;
import com.eventra.dao.interfaces.ICheckInDAO;
import com.eventra.model.CheckIn;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ICheckInDAO.
 * Handles check-in recording and attendance queries.
 */
public class CheckInDAOImpl extends AbstractDAO implements ICheckInDAO {

    private static final String SQL_INSERT =
            "INSERT INTO check_ins (registration_id, method) VALUES (?, ?)";
    private static final String SQL_EXISTS =
            "SELECT COUNT(*) FROM check_ins WHERE registration_id = ?";
    private static final String SQL_FIND_BY_EVENT =
            "SELECT ci.id, ci.registration_id, ci.checked_in_at, ci.method " +
            "FROM check_ins ci " +
            "INNER JOIN registrations r ON ci.registration_id = r.id " +
            "WHERE r.event_id = ?";
    private static final String SQL_CONFIRMED_COUNT =
            "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'CONFIRMED'";

    @Override
    public CheckIn checkIn(int registrationId, String method) throws SQLException {
        // Prevent duplicate check-ins at the DB layer
        if (isCheckedIn(registrationId)) {
            throw new SQLException("Registration " + registrationId + " is already checked in.");
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, registrationId);
            ps.setString(2, method != null ? method : "MANUAL");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    CheckIn ci = new CheckIn();
                    ci.setId(keys.getInt(1));
                    ci.setRegistrationId(registrationId);
                    ci.setCheckedInAt(new Timestamp(System.currentTimeMillis()));
                    ci.setMethod(method != null ? method : "MANUAL");
                    return ci;
                }
            }
        }
        throw new SQLException("Check-in insert failed — no generated key returned.");
    }

    @Override
    public boolean isCheckedIn(int registrationId) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS)) {
            ps.setInt(1, registrationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public List<CheckIn> getCheckInsByEvent(int eventId) throws SQLException {
        List<CheckIn> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EVENT)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CheckIn ci = new CheckIn();
                    ci.setId(rs.getInt("id"));
                    ci.setRegistrationId(rs.getInt("registration_id"));
                    ci.setCheckedInAt(rs.getTimestamp("checked_in_at"));
                    ci.setMethod(rs.getString("method"));
                    list.add(ci);
                }
            }
        }
        return list;
    }

    @Override
    public int getConfirmedRegistrationCount(int eventId) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CONFIRMED_COUNT)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
