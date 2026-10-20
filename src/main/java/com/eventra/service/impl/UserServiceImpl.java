package com.eventra.service.impl;

import com.eventra.dao.impl.InterestDAOImpl;
import com.eventra.dao.impl.UserDAOImpl;
import com.eventra.dao.interfaces.IInterestDAO;
import com.eventra.dao.interfaces.IUserDAO;
import com.eventra.exception.AuthenticationException;
import com.eventra.exception.DuplicateRegistrationException;
import com.eventra.model.User;
import com.eventra.service.interfaces.IUserService;
import com.eventra.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic implementation for user authentication and account management.
 * Calls DAO layer only — never writes SQL directly.
 */
public class UserServiceImpl implements IUserService {

    private final IUserDAO userDAO;
    private final IInterestDAO interestDAO;

    /** Default constructor — uses concrete DAO implementations. */
    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
        this.interestDAO = new InterestDAOImpl();
    }

    /** Injection constructor for unit testing with mocks. */
    public UserServiceImpl(IUserDAO userDAO, IInterestDAO interestDAO) {
        this.userDAO = userDAO;
        this.interestDAO = interestDAO;
    }

    @Override
    public User login(String email, String password) throws AuthenticationException {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Email and password are required.");
        }
        try {
            User user = userDAO.findByEmail(email.trim().toLowerCase());
            if (user == null) {
                throw new AuthenticationException("No account found with that email address.");
            }
            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                throw new AuthenticationException("Incorrect password. Please try again.");
            }
            return user;
        } catch (SQLException e) {
            System.err.println("[UserService.login] DB error: " + e.getMessage());
            throw new AuthenticationException("A database error occurred. Please try again later.");
        }
    }

    @Override
    public User register(User user, List<Integer> interestIds) throws DuplicateRegistrationException {
        if (user == null || user.getEmail() == null || user.getPasswordHash() == null) {
            throw new IllegalArgumentException("User object with email and password is required.");
        }
        try {
            String normalizedEmail = user.getEmail().trim().toLowerCase();
            if (userDAO.existsByEmail(normalizedEmail)) {
                throw new DuplicateRegistrationException(
                        "An account with email '" + normalizedEmail + "' already exists.");
            }
            user.setEmail(normalizedEmail);
            // Hash the plain password before saving
            user.setPasswordHash(PasswordUtil.hashPassword(user.getPasswordHash()));

            boolean saved = userDAO.save(user);
            if (!saved) {
                throw new RuntimeException("User could not be persisted — save returned false.");
            }
            // Save interests if provided
            if (interestIds != null && !interestIds.isEmpty()) {
                interestDAO.saveUserInterests(user.getId(), interestIds);
            }
            return user;
        } catch (DuplicateRegistrationException e) {
            throw e;
        } catch (SQLException e) {
            System.err.println("[UserService.register] DB error: " + e.getMessage());
            throw new RuntimeException("Registration failed due to a database error.", e);
        }
    }

    @Override
    public User getUserProfile(int userId) {
        try {
            return userDAO.findById(userId);
        } catch (SQLException e) {
            System.err.println("[UserService.getUserProfile] DB error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean updateUserProfile(User user, List<Integer> interestIds) {
        try {
            boolean updated = userDAO.update(user);
            if (updated && interestIds != null) {
                interestDAO.saveUserInterests(user.getId(), interestIds);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("[UserService.updateUserProfile] DB error: " + e.getMessage());
            return false;
        }
    }
}
