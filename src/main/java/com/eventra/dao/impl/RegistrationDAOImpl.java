package com.eventra.dao.impl;

import com.eventra.dao.AbstractDAO;
import com.eventra.dao.interfaces.IRegistrationDAO;
import com.eventra.exception.DuplicateRegistrationException;
import com.eventra.exception.EventFullException;
import com.eventra.model.Event;
import com.eventra.model.EventStatus;
import com.eventra.model.Registration;
import com.eventra.model.RegistrationStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JDBC implementation of IRegistrationDAO.
 * register() and cancel() both run in explicit JDBC transactions with
 * conn.setAutoCommit(false) / commit() / rollback().
 */
public class RegistrationDAOImpl extends AbstractDAO implements IRegistrationDAO {

    private static final String SQL_EXISTS =
            "SELECT COUNT(*) FROM registrations WHERE user_id = ? AND event_id = ? " +
            "AND status != 'CANCELLED'";
    private static final String SQL_MAX_SEAT =
            "SELECT COALESCE(MAX(seat_number), 0) FROM registrations WHERE event_id = ? " +
            "AND status != 'CANCELLED'";
    private static final String SQL_INSERT_REG =
            "INSERT INTO registrations (user_id, event_id, booking_ref, seat_number, status) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_FIND_BY_ID =
            "SELECT r.id, r.user_id, r.event_id, r.booking_ref, r.seat_number, r.status, " +
            "r.registered_at, e.title, e.category, e.venue, e.start_time, e.end_time, " +
            "u.name, u.email " +
            "FROM registrations r " +
            "LEFT JOIN events e ON r.event_id = e.id " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "WHERE r.id = ?";
    private static final String SQL_FIND_BY_USER =
            "SELECT r.id, r.user_id, r.event_id, r.booking_ref, r.seat_number, r.status, " +
            "r.registered_at, e.title, e.category, e.venue, e.start_time, e.end_time, " +
            "u.name, u.email " +
            "FROM registrations r " +
            "LEFT JOIN events e ON r.event_id = e.id " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "WHERE r.user_id = ? ORDER BY e.start_time ASC";
    private static final String SQL_FIND_BY_EVENT =
            "SELECT r.id, r.user_id, r.event_id, r.booking_ref, r.seat_number, r.status, " +
            "r.registered_at, e.title, e.category, e.venue, e.start_time, e.end_time, " +
            "u.name, u.email " +
            "FROM registrations r " +
            "LEFT JOIN events e ON r.event_id = e.id " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "WHERE r.event_id = ? ORDER BY r.registered_at ASC";
    private static final String SQL_FIND_EVENT_IDS_BY_USER =
            "SELECT event_id FROM registrations WHERE user_id = ? AND status != 'CANCELLED'";
    private static final String SQL_FIND_FIRST_WAITLIST =
            "SELECT id, user_id, event_id, booking_ref, seat_number, status, registered_at " +
            "FROM registrations WHERE event_id = ? AND status = 'WAITLIST' " +
            "ORDER BY registered_at ASC LIMIT 1";
    private static final String SQL_UPDATE_STATUS =
            "UPDATE registrations SET status = ? WHERE id = ?";
    private static final String SQL_FIND_REG_BY_ID_PLAIN =
            "SELECT id, user_id, event_id, booking_ref, seat_number, status, registered_at " +
            "FROM registrations WHERE id = ?";
    private static final String SQL_COUNT_WAITLIST =
            "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'WAITLIST'";

    // ── register() ───────────────────────────────────────────────────────────

    @Override
    public Registration register(int userId, int eventId)
            throws EventFullException, DuplicateRegistrationException, SQLException {

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Duplicate check
                if (existsByUserAndEvent(userId, eventId, conn)) {
                    throw new DuplicateRegistrationException(
                            "User " + userId + " is already registered for event " + eventId);
                }

                // 2. Lock event row and read current capacity
                Event event = findEventForUpdate(eventId, conn);
                if (event == null) {
                    throw new SQLException("Event not found: " + eventId);
                }

                // Check if event is no longer active
                if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
                    throw new EventFullException(eventId, "Event is " + event.getStatus().name().toLowerCase() + " and not accepting registrations.");
                }

                // 3. Determine status
                RegistrationStatus regStatus;
                int seatNumber;
                if (event.getSeatsLeft() > 0) {
                    regStatus = RegistrationStatus.CONFIRMED;
                    seatNumber = getNextSeatNumber(eventId, conn);
                } else {
                    // Check if waitlist is at max capacity (max 20% of event capacity, min 5)
                    int waitlistCount = getWaitlistCount(eventId, conn);
                    int maxWaitlist = Math.max(5, (int) Math.ceil(event.getCapacity() * 0.20));
                    if (waitlistCount >= maxWaitlist) {
                        throw new EventFullException(eventId, "Event " + eventId + " has reached maximum capacity and the waitlist is full.");
                    }
                    // Waitlisted — seat number 0 means no physical seat yet
                    regStatus = RegistrationStatus.WAITLIST;
                    seatNumber = 0;
                }

                // 4. Insert registration
                String bookingRef = "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Registration reg = insertRegistration(userId, eventId, bookingRef, seatNumber, regStatus, conn);

                // 5. Decrement seats and update event status (only for confirmed)
                if (regStatus == RegistrationStatus.CONFIRMED) {
                    int newSeatsLeft = event.getSeatsLeft() - 1;
                    EventStatus newStatus = computeEventStatus(newSeatsLeft, event.getCapacity());
                    updateEventSeats(eventId, newSeatsLeft, newStatus, conn);
                }

                conn.commit();
                return reg;

            } catch (EventFullException | DuplicateRegistrationException e) {
                conn.rollback();
                throw e;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ── cancel() ─────────────────────────────────────────────────────────────

    @Override
    public boolean cancel(int registrationId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Load the registration
                Registration reg = findByIdPlain(registrationId, conn);
                if (reg == null || reg.getStatus() == RegistrationStatus.CANCELLED) {
                    conn.rollback();
                    return false;
                }

                // 2. Mark as CANCELLED
                updateStatus(registrationId, RegistrationStatus.CANCELLED, conn);

                // 3. If it was a CONFIRMED seat, give it back and promote waitlist
                if (reg.getStatus() == RegistrationStatus.CONFIRMED) {
                    Event event = findEventForUpdate(reg.getEventId(), conn);
                    if (event != null) {
                        int newSeatsLeft = event.getSeatsLeft() + 1;

                        // 4. Promote first waitlisted attendee
                        Registration waitlisted = findFirstWaitlistByEventId(reg.getEventId(), conn);
                        if (waitlisted != null) {
                            int promotedSeat = getNextSeatNumber(reg.getEventId(), conn);
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "UPDATE registrations SET status = 'CONFIRMED', seat_number = ? WHERE id = ?")) {
                                ps.setInt(1, promotedSeat);
                                ps.setInt(2, waitlisted.getId());
                                ps.executeUpdate();
                            }
                            // Seat was immediately re-allocated, so net change = 0
                            newSeatsLeft = event.getSeatsLeft(); // no change to seats_left
                        }

                        EventStatus newStatus = computeEventStatus(newSeatsLeft, event.getCapacity());
                        updateEventSeats(reg.getEventId(), newSeatsLeft, newStatus, conn);
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

    // ── Query methods ─────────────────────────────────────────────────────────

    @Override
    public Registration findById(int id) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowJoined(rs);
            }
        }
        return null;
    }

    @Override
    public List<Registration> findByUserId(int userId) throws SQLException {
        List<Registration> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowJoined(rs));
            }
        }
        return list;
    }

    @Override
    public List<Registration> findByEventId(int eventId) throws SQLException {
        List<Registration> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EVENT)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowJoined(rs));
            }
        }
        return list;
    }

    @Override
    public boolean existsByUserAndEvent(int userId, int eventId) throws SQLException {
        try (Connection conn = getConnection()) {
            return existsByUserAndEvent(userId, eventId, conn);
        }
    }

    @Override
    public List<Integer> findRegisteredEventIdsByUserId(int userId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_EVENT_IDS_BY_USER)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    @Override
    public Registration findFirstWaitlistByEventId(int eventId, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_FIRST_WAITLIST)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowPlain(rs);
            }
        }
        return null;
    }

    @Override
    public boolean updateStatus(int registrationId, RegistrationStatus status, Connection conn)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {
            ps.setString(1, status.name());
            ps.setInt(2, registrationId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean existsByUserAndEvent(int userId, int eventId, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_EXISTS)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Event findEventForUpdate(int eventId, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, capacity, seats_left, status FROM events WHERE id = ? FOR UPDATE")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Event e = new Event();
                    e.setId(rs.getInt("id"));
                    e.setCapacity(rs.getInt("capacity"));
                    e.setSeatsLeft(rs.getInt("seats_left"));
                    e.setStatus(EventStatus.valueOf(rs.getString("status")));
                    return e;
                }
            }
        }
        return null;
    }

    private int getNextSeatNumber(int eventId, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_MAX_SEAT)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) + 1 : 1;
            }
        }
    }

    private Registration insertRegistration(int userId, int eventId, String bookingRef,
                                            int seatNumber, RegistrationStatus status,
                                            Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_REG, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ps.setString(3, bookingRef);
            ps.setInt(4, seatNumber);
            ps.setString(5, status.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Registration reg = new Registration();
                    reg.setId(keys.getInt(1));
                    reg.setUserId(userId);
                    reg.setEventId(eventId);
                    reg.setBookingRef(bookingRef);
                    reg.setSeatNumber(seatNumber);
                    reg.setStatus(status);
                    reg.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
                    return reg;
                }
            }
        }
        throw new SQLException("Registration insert failed — no generated key returned");
    }

    private void updateEventSeats(int eventId, int seatsLeft, EventStatus status, Connection conn)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE events SET seats_left = ?, status = ? WHERE id = ?")) {
            ps.setInt(1, seatsLeft);
            ps.setString(2, status.name());
            ps.setInt(3, eventId);
            ps.executeUpdate();
        }
    }

    private EventStatus computeEventStatus(int seatsLeft, int capacity) {
        if (seatsLeft <= 0) return EventStatus.FULL;
        double remaining = (double) seatsLeft / capacity;
        if (remaining <= 0.10) return EventStatus.ALMOST_FULL;
        return EventStatus.UPCOMING;
    }

    private Registration findByIdPlain(int id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_REG_BY_ID_PLAIN)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowPlain(rs);
            }
        }
        return null;
    }

    private Registration mapRowJoined(ResultSet rs) throws SQLException {
        Registration r = mapRowPlain(rs);
        r.setEventTitle(rs.getString("title"));
        r.setEventCategory(rs.getString("category"));
        r.setEventVenue(rs.getString("venue"));
        r.setEventStartTime(rs.getTimestamp("start_time"));
        r.setEventEndTime(rs.getTimestamp("end_time"));
        r.setAttendeeName(rs.getString("name"));
        r.setAttendeeEmail(rs.getString("email"));
        return r;
    }

    private Registration mapRowPlain(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setEventId(rs.getInt("event_id"));
        r.setBookingRef(rs.getString("booking_ref"));
        r.setSeatNumber(rs.getInt("seat_number"));
        r.setStatus(RegistrationStatus.valueOf(rs.getString("status")));
        r.setRegisteredAt(rs.getTimestamp("registered_at"));
        return r;
    }

    private int getWaitlistCount(int eventId, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT_WAITLIST)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
