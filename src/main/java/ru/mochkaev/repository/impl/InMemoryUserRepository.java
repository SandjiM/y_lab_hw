package ru.mochkaev.repository.impl;

import ru.mochkaev.domain.Role;
import ru.mochkaev.domain.User;
import ru.mochkaev.repository.UserRepository;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> byUsername = new HashMap<>();

    public InMemoryUserRepository() {
        // Пустой репозиторий по умолчанию; заполним позже из файла или теста
    }

    @Override
    public synchronized Optional<User> findByUsername(String username) {
        User u = byUsername.get(username);
        if (u == null) return Optional.empty();
        User c = new User();
        c.username = u.username;
        c.passwordHash = u.passwordHash;
        c.role = u.role == null ? Role.VIEWER : u.role;
        return Optional.of(c);
    }

    @Override
    public synchronized Collection<User> findAll() {
        List<User> list = new ArrayList<>();
        for (User u : byUsername.values()) {
            User c = new User();
            c.username = u.username;
            c.passwordHash = u.passwordHash;
            c.role = u.role;
            list.add(c);
        }
        return list;
    }

    @Override
    public synchronized void replaceAll(Collection<User> users) {
        byUsername.clear();
        for (User u : users) {
            User c = new User();
            c.username = u.username;
            c.passwordHash = u.passwordHash;
            c.role = (u.role == null ? Role.VIEWER : u.role);
            byUsername.put(c.username, c);
        }
    }

    public synchronized void put(User u) {
        replaceAll(Collections.singletonList(u));
    }

    public synchronized void add(User u) {
        User c = new User();
        c.username = u.username;
        c.passwordHash = u.passwordHash;
        c.role = (u.role == null ? Role.VIEWER : u.role);
        byUsername.put(c.username, c);
    }
}
