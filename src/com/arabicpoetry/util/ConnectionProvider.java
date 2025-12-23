package com.arabicpoetry.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple abstraction to supply JDBC connections so tests can inject alternates.
 */
@FunctionalInterface
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
