package org.zero.dis1.model;

import java.sql.Connection;
import java.util.Optional;

public interface Database {
    void newDatabaseConnection(String databaseName, String user, String password);
    Optional<?> getConnectionByName(String databaseName);
}
