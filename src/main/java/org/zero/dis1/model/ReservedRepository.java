package org.zero.dis1.model;

import org.zero.dis1.entity.Reserved;

import java.util.function.Consumer;

public interface ReservedRepository {
    void save(Reserved reserved, Consumer<Boolean> consumer);
}
