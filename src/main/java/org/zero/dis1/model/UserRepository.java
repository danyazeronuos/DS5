package org.zero.dis1.model;

import org.zero.dis1.entity.User;

import java.util.function.Consumer;

public interface UserRepository {
    void findUserById(Integer id, Consumer<User> consumer);
    void updateUser(User user, Consumer<Boolean> runnable);
    void startTransaction(Runnable runnable);
    void rollback();
    void commit();
}
