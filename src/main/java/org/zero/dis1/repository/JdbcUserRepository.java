package org.zero.dis1.repository;

import lombok.SneakyThrows;
import org.zero.dis1.entity.Trip;
import org.zero.dis1.entity.User;
import org.zero.dis1.mapper.TripMapper;
import org.zero.dis1.model.DatabaseEnum;
import org.zero.dis1.utils.JdbcDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class JdbcUserRepository {
    private final JdbcDatabase db = JdbcDatabase.getInstance();

    public JdbcUserRepository() {
        db.newDatabaseConnection(
                DatabaseEnum.USERS_DATABASE.get(),
                "postgres",
                "111"
        );
    }

    @SneakyThrows
    public Optional<User> findUserById(Integer id) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return Optional.empty();
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

                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public void updateUser(User user) throws SQLException {
        var tripConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return;
        }

        var request = "update users set username = ?, balance = ? where id = ?";

        var statement = tripConnection.get().prepareStatement(request);
        statement.setInt(3, user.getId());
        statement.setDouble(2, user.getBalance());
        statement.setString(1, user.getUsername());
        statement.executeUpdate();


    }

    @SneakyThrows
    public List<User> getUsers(String request) {
        var tripConnection = db.getConnectionByName(DatabaseEnum.USERS_DATABASE.get());

        if (tripConnection.isEmpty()) {
            return List.of();
        }

        var statement = tripConnection.get().createStatement();
        var response = statement.executeQuery(request);

        List<User> users = new ArrayList<>();
        while (response.next()) {
            var user = User.builder()
                    .id(response.getInt("id"))
                    .username(response.getString("username"))
                    .balance(response.getDouble("balance"))
                    .build();

            users.add(user);
        }

        return users;
    }


    @SneakyThrows
    public void rollback() {
        var usersConnection = db.getConnectionByName(DatabaseEnum.USER_DATABASE.get());

        if (usersConnection.isEmpty()) {
            System.out.println("Users connection is empty");
            return;
        }

        usersConnection.get().rollback();
    }

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
