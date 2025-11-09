package ru.mochkaev.service;

import ru.mochkaev.domain.User;

import java.util.Optional;

public interface AuthService {
    User login(String username, String password);
    void logout(String username);
    Optional<User> current();
}
