package com.eventra.dao.interfaces;

import com.eventra.model.Interest;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object contract for Interest domain and relationship mappings.
 */
public interface IInterestDAO {

    /**
     * Retrieves all available interest tags.
     *
     * @return List of all Interest records
     * @throws SQLException on database error
     */
    List<Interest> findAll() throws SQLException;

    /**
     * Finds an interest by primary key ID.
     *
     * @param id Interest ID
     * @return Interest object if found, null otherwise
     * @throws SQLException on database error
     */
    Interest findById(int id) throws SQLException;

    /**
     * Retrieves all interests associated with a specific user.
     *
     * @param userId User ID
     * @return List of matching Interest records
     * @throws SQLException on database error
     */
    List<Interest> findByUserId(int userId) throws SQLException;

    /**
     * Retrieves all interests associated with a specific event.
     *
     * @param eventId Event ID
     * @return List of matching Interest records
     * @throws SQLException on database error
     */
    List<Interest> findByEventId(int eventId) throws SQLException;

    /**
     * Associates a user with a list of interest IDs, replacing existing associations.
     *
     * @param userId User ID
     * @param interestIds List of interest IDs
     * @return true if successfully saved, false otherwise
     * @throws SQLException on database error
     */
    boolean saveUserInterests(int userId, List<Integer> interestIds) throws SQLException;

    /**
     * Associates an event with a list of interest IDs, replacing existing associations.
     *
     * @param eventId Event ID
     * @param interestIds List of interest IDs
     * @return true if successfully saved, false otherwise
     * @throws SQLException on database error
     */
    boolean saveEventInterests(int eventId, List<Integer> interestIds) throws SQLException;
}
