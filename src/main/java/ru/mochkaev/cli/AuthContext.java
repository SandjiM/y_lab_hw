package ru.mochkaev.cli;

import ru.mochkaev.domain.User;

import java.util.Optional;

public final class AuthContext {
    private static volatile User current;
    private AuthContext() {}

    public static Optional<User> current() {
        return Optional.ofNullable(current);
    }
    public static void set(User u) { current = u; }
    public static void clear() { current = null; }
}
