package com.eventra.service.interfaces;

import com.eventra.exception.AuthenticationException;
import com.eventra.exception.DuplicateRegistrationException;
import com.eventra.model.User;

import java.util.List;

/**
 * Service contract for user authentication, registration, and profile management.
 */
public interface IUserService {

    /**
     * Authenticates a user by email and password.
     *
     * @param email user email
     * @param password raw plain-text password
     * @return authenticated User object
     * @throws AuthenticationException if email is not found or password does not match
     */
    User login(String email, String password) throws AuthenticationException;

    /**
     * Registers a new user account with selected interests.
     *
     * @param user User object containing name, email, password (hashed or plain), and role
     * @param interestIds list of interest IDs selected by the user
     * @return newly persisted User object with generated ID
     * @throws DuplicateRegistrationException if the email is already in use
     */
    User register(User user, List<Integer> interestIds) throws DuplicateRegistrationException;

    /**
     * Retrieves a user's full profile by ID.
     *
     * @param userId User ID
     * @return User object or null if not found
     */
    User getUserProfile(int userId);

    /**
     * Updates an existing user's profile and interest mappings.
     *
     * @param user updated User object
     * @param interestIds updated interest IDs
     * @return true if updated successfully, false otherwise
     */
    boolean updateUserProfile(User user, List<Integer> interestIds);
}
