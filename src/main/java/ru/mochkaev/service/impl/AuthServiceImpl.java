package ru.mochkaev.service.impl;

import ru.mochkaev.cli.AuthContext;
import ru.mochkaev.domain.User;
import ru.mochkaev.repository.UserRepository;
import ru.mochkaev.service.AuditService;
import ru.mochkaev.service.AuthService;
import ru.mochkaev.util.Hasher;

import java.util.Objects;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final AuditService audit;

    public AuthServiceImpl(UserRepository users, AuditService audit) {
        this.users = Objects.requireNonNull(users);
        this.audit = Objects.requireNonNull(audit);
    }

    @Override
    public User login(String username, String password) {
        Optional<User> ou = users.findByUsername(username);
        if (ou.isEmpty()) throw new IllegalArgumentException("Пользователь не найден");
        User u = ou.get();
        if (!Objects.equals(u.passwordHash, Hasher.sha256(password))) {
            throw new IllegalArgumentException("Неверный пароль");
        }
        AuthContext.set(u);
        audit.log(u.username, "LOGIN", "ok");
        return u;
    }

    @Override
    public void logout(String username) {
        AuthContext.clear();
        audit.log(username, "LOGOUT", "ok");
    }

    @Override
    public Optional<User> current() {
        return AuthContext.current();
    }
}

