package com.eventra.dao.impl;

import com.eventra.dao.AbstractDAO;
import com.eventra.dao.interfaces.IInterestDAO;
import com.eventra.model.Interest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of IInterestDAO.
 * Manages interest master records and user/event junction mappings.
 */
public class InterestDAOImpl extends AbstractDAO implements IInterestDAO {

    private static final String SQL_FIND_ALL =
            "SELECT id, label FROM interests ORDER BY label ASC";
    private static final String SQL_FIND_BY_ID =
            "SELECT id, label FROM interests WHERE id = ?";
    private static final String SQL_FIND_BY_USER =
            "SELECT i.id, i.label FROM interests i " +
            "INNER JOIN user_interests ui ON i.id = ui.interest_id " +
            "WHERE ui.user_id = ?";
    private static final String SQL_FIND_BY_EVENT =
            "SELECT i.id, i.label FROM interests i " +
            "INNER JOIN event_interests ei ON i.id = ei.interest_id " +
            "WHERE ei.event_id = ?";
    private static final String SQL_DELETE_USER_INTERESTS =
            "DELETE FROM user_interests WHERE user_id = ?";
    private static final String SQL_INSERT_USER_INTEREST =
            "INSERT INTO user_interests (user_id, interest_id) VALUES (?, ?)";
    private static final String SQL_DELETE_EVENT_INTERESTS =
            "DELETE FROM event_interests WHERE event_id = ?";
    private static final String SQL_INSERT_EVENT_INTEREST =
            "INSERT INTO event_interests (event_id, interest_id) VALUES (?, ?)";

    @Override
    public List<Interest> findAll() throws SQLException {
        List<Interest> interests = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                interests.add(mapRow(rs));
            }
        }
        return interests;
    }

    @Override
    public Interest findById(int id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Interest> findByUserId(int userId) throws SQLException {
        List<Interest> interests = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    interests.add(mapRow(rs));
                }
            }
        }
        return interests;
    }

    @Override
    public List<Interest> findByEventId(int eventId) throws SQLException {
        List<Interest> interests = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EVENT)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    interests.add(mapRow(rs));
                }
            }
        }
        return interests;
    }

    @Override
    public boolean saveUserInterests(int userId, List<Integer> interestIds) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Replace all existing user interests
                try (PreparedStatement del = conn.prepareStatement(SQL_DELETE_USER_INTERESTS)) {
                    del.setInt(1, userId);
                    del.executeUpdate();
                }
                if (interestIds != null && !interestIds.isEmpty()) {
                    try (PreparedStatement ins = conn.prepareStatement(SQL_INSERT_USER_INTEREST)) {
                        for (int interestId : interestIds) {
                            ins.setInt(1, userId);
                            ins.setInt(2, interestId);
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean saveEventInterests(int eventId, List<Integer> interestIds) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(SQL_DELETE_EVENT_INTERESTS)) {
                    del.setInt(1, eventId);
                    del.executeUpdate();
                }
                if (interestIds != null && !interestIds.isEmpty()) {
                    try (PreparedStatement ins = conn.prepareStatement(SQL_INSERT_EVENT_INTEREST)) {
                        for (int interestId : interestIds) {
                            ins.setInt(1, eventId);
                            ins.setInt(2, interestId);
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Interest mapRow(ResultSet rs) throws SQLException {
        Interest interest = new Interest();
        interest.setId(rs.getInt("id"));
        interest.setLabel(rs.getString("label"));
        return interest;
    }
}
