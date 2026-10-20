package com.eventra.dao.impl;

import com.eventra.dao.AbstractDAO;
import com.eventra.dao.interfaces.IEventDAO;
import com.eventra.model.Event;
import com.eventra.model.EventStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of IEventDAO.
 * All SQL lives here. Supports transactional seat updates via shared Connection.
 */
public class EventDAOImpl extends AbstractDAO implements IEventDAO {

    private static final String COLS =
            "id, title, description, category, venue, start_time, end_time, " +
            "capacity, seats_left, status, organizer_id";

    private static final String SQL_FIND_ALL =
            "SELECT " + COLS + " FROM events ORDER BY start_time ASC";
    private static final String SQL_FIND_BY_ID =
            "SELECT " + COLS + " FROM events WHERE id = ?";
    private static final String SQL_FIND_BY_ID_FOR_UPDATE =
            "SELECT " + COLS + " FROM events WHERE id = ? FOR UPDATE";
    private static final String SQL_FIND_BY_CATEGORY =
            "SELECT " + COLS + " FROM events WHERE category = ? ORDER BY start_time ASC";
    private static final String SQL_FIND_BY_STATUS =
            "SELECT " + COLS + " FROM events WHERE status = ? ORDER BY start_time ASC";
    private static final String SQL_INSERT =
            "INSERT INTO events (title, description, category, venue, start_time, end_time, " +
            "capacity, seats_left, status, organizer_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE events SET title=?, description=?, category=?, venue=?, start_time=?, " +
            "end_time=?, capacity=?, seats_left=?, status=?, organizer_id=? WHERE id=?";
    private static final String SQL_DELETE =
            "DELETE FROM events WHERE id = ?";
    private static final String SQL_UPDATE_SEATS_STATUS =
            "UPDATE events SET seats_left = ?, status = ? WHERE id = ?";

    @Override
    public List<Event> findAll() throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        }
        return events;
    }

    @Override
    public Event findById(int id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public Event findByIdForUpdate(int id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID_FOR_UPDATE)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public List<Event> findByCategory(String category) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CATEGORY)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) events.add(mapRow(rs));
            }
        }
        return events;
    }

    @Override
    public List<Event> findByStatus(EventStatus status) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_STATUS)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) events.add(mapRow(rs));
            }
        }
        return events;
    }

    @Override
    public boolean save(Event event) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setEventParams(ps, event);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) event.setId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Event event) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            setEventParams(ps, event);
            ps.setInt(11, event.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateSeatsAndStatus(int eventId, int seatsLeft, EventStatus status, Connection conn)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_SEATS_STATUS)) {
            ps.setInt(1, seatsLeft);
            ps.setString(2, status.name());
            ps.setInt(3, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Binds all Event fields (except id) to a PreparedStatement for INSERT (10 params).
     * For UPDATE, the caller must additionally bind id as param 11.
     */
    private void setEventParams(PreparedStatement ps, Event event) throws SQLException {
        ps.setString(1, event.getTitle());
        ps.setString(2, event.getDescription());
        ps.setString(3, event.getCategory());
        ps.setString(4, event.getVenue());
        ps.setTimestamp(5, event.getStartTime());
        ps.setTimestamp(6, event.getEndTime());
        ps.setInt(7, event.getCapacity());
        ps.setInt(8, event.getSeatsLeft());
        ps.setString(9, event.getStatus().name());
        if (event.getOrganizerId() > 0) {
            ps.setInt(10, event.getOrganizerId());
        } else {
            ps.setNull(10, Types.INTEGER);
        }
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setCategory(rs.getString("category"));
        e.setVenue(rs.getString("venue"));
        e.setStartTime(rs.getTimestamp("start_time"));
        e.setEndTime(rs.getTimestamp("end_time"));
        e.setCapacity(rs.getInt("capacity"));
        e.setSeatsLeft(rs.getInt("seats_left"));
        e.setStatus(EventStatus.valueOf(rs.getString("status")));
        e.setOrganizerId(rs.getInt("organizer_id"));
        return e;
    }
}
