package com.eventra.dao.interfaces;

import com.eventra.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object contract for User persistence operations.
 */
public interface IUserDAO {

    /**
     * Finds a User by unique email address.
     *
     * @param email User's email
     * @return User POJO if found, null otherwise
     * @throws SQLException on database error
     */
    User findByEmail(String email) throws SQLException;

    /**
     * Finds a User by primary key ID.
     *
     * @param id User ID
     * @return User POJO if found, null otherwise
     * @throws SQLException on database error
     */
    User findById(int id) throws SQLException;

    /**
     * Persists a new User in the database.
     *
     * @param user User POJO to insert
     * @return true if successfully inserted, false otherwise
     * @throws SQLException on database error
     */
    boolean save(User user) throws SQLException;

    /**
     * Updates an existing User's profile details.
     *
     * @param user User POJO to update
     * @return true if successfully updated, false otherwise
     * @throws SQLException on database error
     */
    boolean update(User user) throws SQLException;

    /**
     * Retrieves all registered users in the system.
     *
     * @return List of all User objects
     * @throws SQLException on database error
     */
    List<User> findAll() throws SQLException;

    /**
     * Checks if an account already exists with the given email.
     *
     * @param email Email to check
     * @return true if email is already registered, false otherwise
     * @throws SQLException on database error
     */
    boolean existsByEmail(String email) throws SQLException;
}
