package ru.mochkaev.cli;

import ru.mochkaev.domain.Role;
import ru.mochkaev.domain.User;
import ru.mochkaev.repository.impl.InMemoryAuditRepository;
import ru.mochkaev.repository.impl.InMemoryProductRepository;
import ru.mochkaev.repository.impl.InMemoryUserRepository;
import ru.mochkaev.service.AuditService;
import ru.mochkaev.service.AuthService;
import ru.mochkaev.service.CatalogService;
import ru.mochkaev.service.impl.AuditServiceImpl;
import ru.mochkaev.service.impl.AuthServiceImpl;
import ru.mochkaev.service.impl.CatalogServiceImpl;
import ru.mochkaev.util.Hasher;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Product Catalog Service ===");
        System.out.println("(Java Core + Collections, console MVP)");

        // Repositories
        InMemoryProductRepository prodRepo = new InMemoryProductRepository();
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        InMemoryAuditRepository auditRepo = new InMemoryAuditRepository();

        // Seed users: admin/admin, viewer/viewer
        User admin = new User();
        admin.username = "admin";
        admin.passwordHash = Hasher.sha256("admin");
        admin.role = Role.ADMIN;
        userRepo.add(admin);

        User viewer = new User();
        viewer.username = "viewer";
        viewer.passwordHash = Hasher.sha256("viewer");
        viewer.role = Role.VIEWER;
        userRepo.add(viewer);

        // Services
        AuditService audit = new AuditServiceImpl(auditRepo);
        AuthService auth = new AuthServiceImpl(userRepo, audit);
        CatalogService catalog = new CatalogServiceImpl(prodRepo, audit);

        // CLI app
        new ConsoleApp(auth, catalog, audit).run();

    }
}