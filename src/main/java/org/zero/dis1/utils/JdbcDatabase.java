package org.zero.dis1.utils;

import lombok.Getter;
import org.zero.dis1.model.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class JdbcDatabase implements Database {
    private final Map<String, Connection> connections = new HashMap<>();
    private static final JdbcDatabase INSTANCE = new JdbcDatabase();

    private JdbcDatabase() {}

    public static JdbcDatabase getInstance() {
        return INSTANCE;
    }

    public void newDatabaseConnection(
            String databaseName,
            String user,
            String password
    ) {
        try {
            String databaseUrl = "jdbc:postgresql://localhost:5432/" + databaseName;
            var connection = DriverManager.getConnection(
                    databaseUrl,
                    user,
                    password
            );
            System.out.println(connection == null);

            connection.setAutoCommit(false);
            connections.put(databaseName, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating connection with database");
        }
    }

    public Optional<Connection> getConnectionByName(String databaseName) {
        return Optional.ofNullable(connections.get(databaseName));
    }
}
