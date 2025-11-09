package ru.mochkaev.storage;

import ru.mochkaev.domain.Role;
import ru.mochkaev.domain.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UsersFileStorage {
    private final Path path;

    public UsersFileStorage(Path path) {
        this.path = path;
    }

    public List<User> load() {
        try {
            List<String> lines = Tsv.readAllLines(path);
            if (lines.isEmpty()) return new ArrayList<>();
            List<User> res = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String ln = lines.get(i);
                if (ln.trim().isEmpty()) continue;
                List<String> c = Tsv.splitTsvLine(ln);
                User u = new User();
                u.username = Tsv.unescapeTsv(c.get(0));
                u.passwordHash = c.get(1);
                u.role = Role.valueOf(c.get(2));
                res.add(u);
            }
            return res;
        } catch (IOException e) {
            System.err.println("WARN: cannot load users.tsv: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveAll(Collection<User> users) {
        List<String> lines = new ArrayList<>();
        lines.add("username\tpasswordHash\trole");
        for (User u : users) {
            lines.add(String.join("\t",
                    Tsv.escapeTsv(u.username),
                    u.passwordHash == null ? "" : u.passwordHash,
                    (u.role == null ? Role.VIEWER : u.role).name()
            ));
        }
        try {
            Tsv.writeAtomically(path, lines);
        } catch (IOException e) {
            System.err.println("ERROR: cannot write users.tsv: " + e.getMessage());
        }
    }

}
