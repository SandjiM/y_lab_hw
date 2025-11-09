package ru.mochkaev.repository.impl;

import ru.mochkaev.domain.AuditEvent;
import ru.mochkaev.repository.AuditRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class InMemoryAuditRepository implements AuditRepository {
    private final Deque<AuditEvent> ring = new ArrayDeque<>();
    private final int max = 500;

    @Override
    public synchronized void append(AuditEvent e) {
        ring.addLast(e);
        if (ring.size() > max) ring.removeFirst();
    }

    @Override
    public synchronized List<AuditEvent> recent(int limit) {
        List<AuditEvent> res = new ArrayList<>();
        int n = Math.min(limit, ring.size());
        Object[] arr = ring.toArray();
        for (int i = ring.size() - n; i < ring.size(); i++) {
            res.add((AuditEvent) arr[i]);
        }
        return res;
    }
}
