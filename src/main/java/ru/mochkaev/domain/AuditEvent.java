package ru.mochkaev.domain;

public class AuditEvent {
    public long ts;
    public String username;
    public String action;
    public String details;
}
