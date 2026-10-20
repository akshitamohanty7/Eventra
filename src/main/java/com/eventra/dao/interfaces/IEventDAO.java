package com.eventra.dao.interfaces;

import com.eventra.model.Event;
import com.eventra.model.EventStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object contract for Event persistence operations.
 */
public interface IEventDAO {

    /**
     * Retrieves all events in the system.
     *
     * @return List of all events
     * @throws SQLException on database error
     */
    List<Event> findAll() throws SQLException;

    /**
     * Finds an event by primary key ID.
     *
     * @param id Event ID
     * @return Event POJO if found, null otherwise
     * @throws SQLException on database error
     */
    Event findById(int id) throws SQLException;

    /**
     * Finds an event by ID using an existing transaction connection with row-level lock (FOR UPDATE).
     *
     * @param id Event ID
     * @param conn active transactional Connection
     * @return Event POJO if found, null otherwise
     * @throws SQLException on database error
     */
    Event findByIdForUpdate(int id, Connection conn) throws SQLException;

    /**
     * Filters events by category.
     *
     * @param category category name (e.g., Workshop, Hackathon)
     * @return List of matching events
     * @throws SQLException on database error
     */
    List<Event> findByCategory(String category) throws SQLException;

    /**
     * Filters events by status.
     *
     * @param status EventStatus enum
     * @return List of matching events
     * @throws SQLException on database error
     */
    List<Event> findByStatus(EventStatus status) throws SQLException;

    /**
     * Inserts a new event.
     *
     * @param event Event to create
     * @return true if created successfully, false otherwise
     * @throws SQLException on database error
     */
    boolean save(Event event) throws SQLException;

    /**
     * Updates an existing event.
     *
     * @param event Event to update
     * @return true if updated successfully, false otherwise
     * @throws SQLException on database error
     */
    boolean update(Event event) throws SQLException;

    /**
     * Deletes an event by ID.
     *
     * @param id Event ID
     * @return true if deleted, false otherwise
     * @throws SQLException on database error
     */
    boolean delete(int id) throws SQLException;

    /**
     * Updates seat count and status atomically inside a transaction connection.
     *
     * @param eventId Event ID
     * @param seatsLeft updated seats left
     * @param status updated status
     * @param conn active transaction connection
     * @return true if updated, false otherwise
     * @throws SQLException on database error
     */
    boolean updateSeatsAndStatus(int eventId, int seatsLeft, EventStatus status, Connection conn) throws SQLException;
}
