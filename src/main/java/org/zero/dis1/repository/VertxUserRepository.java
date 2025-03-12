package org.zero.dis1.repository;

import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;
import org.zero.dis1.entity.User;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.utils.VertxDatabase;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VertxUserRepository {
    private final VertxDatabase db = VertxDatabase.getInstance();

    private SqlClient getClient(SqlConnection connection) {
        if (connection == null) {
            var foundedConnection = db.getConnectionByName(DatabaseEnum.TRIP_DATABASE.get());

            return foundedConnection.orElse(null);
        }

        return connection;
    }

    public VertxUserRepository() {
        db.newDatabaseConnection(
                DatabaseEnum.USER_DATABASE.get(),
                "postgres",
                "111"
        );
    }

    public void findUserById(Integer id, Consumer<User> consumer, SqlConnection connection) {
        var request = "select * from users where id = $1";

        getClient(connection).preparedQuery(request)
                .execute(Tuple.of(id), ar -> {
                    if (ar.succeeded() && ar.result().rowCount() > 0) {

                        var row = ar.result().iterator().next();
                        User trip = User.builder()
                                .id(row.getInteger("id"))
                                .username(row.getString("username"))
                                .balance(row.getDouble("balance"))
                                .build();

                        consumer.accept(trip);
                    } else {
                        consumer.accept(null);
                    }
                });
    }

    public void updateUser(User user, Consumer<Boolean> consumer, SqlConnection connection) {
        var request = "update users set username = $1, balance = $2 where id = $3";

        getClient(connection).preparedQuery(request)
                .execute(Tuple.of(user.getUsername(), user.getBalance(), user.getId()))
                .onSuccess(h -> consumer.accept(true))
                .onFailure(err -> {
                    consumer.accept(false);
                });
    }

    public void startTransaction(BiConsumer<SqlConnection, Transaction> consumer) {
        var usersConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (usersConnection.isEmpty()) {
            System.out.println("Users connection not found");
            return;
        }

        usersConnection.get().getConnection().onSuccess(conn -> {
                conn.begin().onSuccess(tx -> consumer.accept(conn, tx));
        });
    }
}
