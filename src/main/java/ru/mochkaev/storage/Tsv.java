package ru.mochkaev.storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class Tsv {
    private Tsv() {}

    public static List<String> readAllLines(Path path) throws IOException {
        if (!Files.exists(path)) return Collections.emptyList();
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    public static void writeAtomically(Path path, List<String> lines) throws IOException {
        Files.createDirectories(path.getParent());
        Path tmp = Paths.get(path.toString() + ".tmp");
        Files.write(tmp, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static void appendLine(Path path, String line) throws IOException {
        Files.createDirectories(path.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(line);
            w.newLine();
        }
    }

    public static String escapeTsv(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static String unescapeTsv(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                switch (c) {
                    case 't': sb.append('\t'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append(c); break;
                }
                esc = false;
            } else {
                if (c == '\\') esc = true; else sb.append(c);
            }
        }
        if (esc) sb.append('\\');
        return sb.toString();
    }

    public static String attrsToString(Map<String,String> attrs) {
        if (attrs == null || attrs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String,String> e : attrs.entrySet()) {
            if (!first) sb.append(';');
            first = false;
            sb.append(escapeKv(e.getKey())).append('=').append(escapeKv(e.getValue()));
        }
        return sb.toString();
    }

    public static Map<String,String> attrsFromString(String s) {
        if (s == null || s.isEmpty()) return new HashMap<>();
        Map<String,String> map = new HashMap<>();
        StringBuilder key = new StringBuilder();
        StringBuilder val = new StringBuilder();
        StringBuilder cur = key;
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                cur.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '=' && cur == key) {
                cur = val;
            } else if (c == ';') {
                map.put(key.toString(), val.toString());
                key.setLength(0);
                val.setLength(0);
                cur = key;
            } else {
                cur.append(c);
            }
        }
        if (key.length() > 0 || val.length() > 0) {
            map.put(key.toString(), val.toString());
        }
        return map;
    }

    private static String escapeKv(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("=", "\\=").replace(";", "\\;");
    }

    public static List<String> splitTsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (esc) {
                sb.append('\\').append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '\t') {
                cols.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        cols.add(sb.toString());
        return cols;
    }
}
