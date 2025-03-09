package org.zero.dis1.repository;

import lombok.SneakyThrows;
import org.zero.dis1.entity.User;
import org.zero.dis1.mapper.TripMapper;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.model.UserRepository;
import org.zero.dis1.utils.JdbcDatabase;

import java.sql.SQLException;
import java.util.function.Consumer;

public class JdbcUserRepository implements UserRepository {
    private final JdbcDatabase db = JdbcDatabase.getInstance();

    public JdbcUserRepository() {
        db.newDatabaseConnection(
                DatabaseEnum.USERS_DATABASE.get(),
                "postgres",
                "111"
        );
    }

    @Override
    @SneakyThrows
    public void findUserById(Integer id, Consumer<User> consumer) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        var request = "select * from users where id = ?";

        try (var statement = tripConnection.get().prepareStatement(request)) {
            statement.setInt(1, id);

            var response = statement.executeQuery();
            if (response.next()) {
                var user = User.builder()
                        .id(response.getInt("id"))
                        .username(response.getString("username"))
                        .balance(response.getDouble("balance"))
                        .build();

                consumer.accept(user);
            } else {
                consumer.accept(null);
            }
        }

    }

    @Override
    public void updateUser(User user, Consumer<Boolean> consumer) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        var request = "update users set username = ?, balance = ? where id = ?";

        try (var statement = tripConnection.get().prepareStatement(request)) {
            statement.setInt(3, user.getId());
            statement.setDouble(2, user.getBalance());
            statement.setString(1, user.getUsername());
            statement.executeUpdate();
            consumer.accept(true);
        } catch (SQLException e) {
            consumer.accept(false);
        }

    }

    @Override
    public void startTransaction(Runnable runnable) {

    }

    @Override
    @SneakyThrows
    public void rollback() {
        var usersConnection = db.getConnectionByName(DatabaseEnum.USER_DATABASE.get());

        if (usersConnection.isEmpty()) {
            System.out.println("Users connection is empty");
            return;
        }

        usersConnection.get().rollback();
    }

    @Override
    @SneakyThrows
    public void commit() {
        var usersConnection = db.getConnectionByName(DatabaseEnum.USER_DATABASE.get());

        if (usersConnection.isEmpty()) {
            System.out.println("Users connection is empty");
            return;
        }

        usersConnection.get().commit();
    }
}
