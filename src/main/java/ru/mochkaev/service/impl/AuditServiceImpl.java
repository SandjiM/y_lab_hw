package ru.mochkaev.service.impl;

import ru.mochkaev.domain.AuditEvent;
import ru.mochkaev.repository.AuditRepository;
import ru.mochkaev.service.AuditService;

import java.util.List;
import java.util.Objects;

public class AuditServiceImpl implements AuditService {
    private final AuditRepository repo;

    public AuditServiceImpl(AuditRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    @Override
    public void log(String username, String action, String details) {
        AuditEvent e = new AuditEvent();
        e.ts = System.currentTimeMillis();
        e.username = username;
        e.action = action;
        e.details = details;
        repo.append(e);
    }

    @Override
    public List<AuditEvent> recent(int limit) {
        return repo.recent(limit);
    }
}
