package org.zero.dis1.model;

public enum DatabaseEnum {
    USER_DATABASE("users_database"),
    TRIP_DATABASE("trip_database"),
    USERS_DATABASE("users_database"),
    USERAME("postgres"),
    PASSWORD("1111");

    private final String value;

    public String get() {
        return value;
    }

    DatabaseEnum(String value) {
        this.value = value;
    }
}
