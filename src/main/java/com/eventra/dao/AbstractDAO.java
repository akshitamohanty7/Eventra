package com.eventra.dao;

import com.eventra.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Base abstract class for all Data Access Objects in Eventra.
 * Demonstrates OOP abstraction and centralizes database connection retrieval.
 */
public abstract class AbstractDAO {

    /**
     * Provides an active database Connection from the DBConnection singleton.
     *
     * @return active java.sql.Connection
     * @throws SQLException if connection retrieval fails
     */
    protected Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }
}
