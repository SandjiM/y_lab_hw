package ru.mochkaev.service;

import ru.mochkaev.domain.AuditEvent;

import java.util.List;

public interface AuditService {
    void log(String username, String action, String details);
    List<AuditEvent> recent(int limit);
}
