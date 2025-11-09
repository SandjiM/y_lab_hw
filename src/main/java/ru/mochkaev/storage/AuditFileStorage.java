package ru.mochkaev.storage;

import ru.mochkaev.domain.AuditEvent;

import java.io.IOException;
import java.nio.file.Path;

public class AuditFileStorage {
    private final Path path;

    public AuditFileStorage(Path path) {
        this.path = path;
    }

    public void append(AuditEvent e) {
        String line = e.ts + "\t" + safe(e.username) + "\t" + safe(e.action) + "\t" + Tsv.escapeTsv(safe(e.details));
        try {
            Tsv.appendLine(path, line);
        } catch (IOException ex) {
            System.err.println("ERROR: cannot append audit.log: " + ex.getMessage());
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
