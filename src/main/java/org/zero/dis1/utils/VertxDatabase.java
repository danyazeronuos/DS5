package org.zero.dis1.utils;

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.zero.dis1.model.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VertxDatabase implements Database {
    private final Map<String, Pool> connections = new HashMap<>();
    private static final VertxDatabase INSTANCE = new VertxDatabase();

    public VertxDatabase() {}

    public static VertxDatabase getInstance() {
        return INSTANCE;
    }

    @Override
    public void newDatabaseConnection(String databaseName, String user, String password) {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost("localhost")
                .setPort(5432)
                .setDatabase(databaseName)
                .setUser(user)
                .setPassword(password);

        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        Pool client = Pool.pool(connectOptions, poolOptions);

        connections.put(databaseName, client);
    }

    public Optional<Pool> getConnectionByName(String databaseName) {
        return Optional.ofNullable(connections.get(databaseName));
    }

}
