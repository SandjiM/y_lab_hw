package ru.mochkaev.repository;

import ru.mochkaev.domain.AuditEvent;

import java.util.List;

public interface AuditRepository {
    void append(AuditEvent e);
    List<AuditEvent> recent(int limit);
}
