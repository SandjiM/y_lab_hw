package ru.mochkaev.repository;

import ru.mochkaev.domain.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Collection<User> findAll();
    void replaceAll(Collection<User> users);
}
