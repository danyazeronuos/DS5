package org.zero.dis1.repository;

import io.vertx.sqlclient.Tuple;
import org.zero.dis1.entity.User;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.model.UserRepository;
import org.zero.dis1.utils.VertxDatabase;

import java.util.function.Consumer;

public class VertxUserRepository implements UserRepository {
    private final VertxDatabase db = VertxDatabase.getInstance();

    public VertxUserRepository() {
        db.newDatabaseConnection(
                DatabaseEnum.USER_DATABASE.get(),
                "postgres",
                "111"
        );
    }

    public void rollback() {
        var usersConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (usersConnection.isEmpty()) {
            System.out.println("Users connection not found");
            return;
        }

        var request = "rollback transaction;";

        usersConnection.get().query(request).execute(ar -> {
            if (ar.succeeded()) {
                System.out.println("Rollback success");
            }
        });
    }

    @Override
    public void commit() {
        var usersConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (usersConnection.isEmpty()) {
            System.out.println("Users connection not found");
            return;
        }

        var request = "commit transaction;";

        usersConnection.get().query(request).execute(ar -> {
            if (ar.succeeded()) {
                System.out.println("Users connection committed");
            }
        });
    }

    @Override
    public void findUserById(Integer id, Consumer<User> consumer) {
        var usersConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (usersConnection.isEmpty()) {
            System.out.println("Users connection not found");
            return;
        }

        var request = "select * from users where id = $1";

        usersConnection.get().preparedQuery(request)
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

    @Override
    public void updateUser(User user, Consumer<Boolean> consumer) {
        var usersConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (usersConnection.isEmpty()) {
            System.out.println("Users connection not found");
            return;
        }

        var request = "update users set username = $3, balance = $2 where id = $1";

        usersConnection.get().preparedQuery(request)
                .execute(Tuple.of(user.getId(), user.getBalance(), user.getUsername()), ar -> {
                    if (ar.succeeded()) {
                        consumer.accept(true);
                    } {
                        consumer.accept(false);
                    }
                });
    }

    @Override
    public void startTransaction(Runnable runnable) {
        var usersConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (usersConnection.isEmpty()) {
            System.out.println("Users connection not found");
            return;
        }

        usersConnection.get().query("start transaction").execute(ar -> {
            if (ar.succeeded()) {
                runnable.run();
            }
        });
    }
}
