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
import ru.mochkaev.storage.AuditFileStorage;
import ru.mochkaev.storage.ProductsFileStorage;
import ru.mochkaev.storage.UsersFileStorage;
import ru.mochkaev.util.Hasher;

import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Product Catalog Service ===");
        System.out.println("(Java Core + Collections, console MVP)");

        Path dataDir = Path.of("data");
        Path productsPath = dataDir.resolve("products.tsv");
        Path usersPath = dataDir.resolve("users.tsv");
        Path auditPath = dataDir.resolve("audit.log");

        ProductsFileStorage prodFs = new ProductsFileStorage(productsPath);
        UsersFileStorage usersFs = new UsersFileStorage(usersPath);
        AuditFileStorage auditFs = new AuditFileStorage(auditPath);

        InMemoryProductRepository prodRepo = new InMemoryProductRepository();
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        InMemoryAuditRepository auditRepo = new InMemoryAuditRepository();

        // Load users
        List<User> users = usersFs.load();
        if (users.isEmpty()) {
            User admin = new User();
            admin.username = "admin";
            admin.passwordHash = Hasher.sha256("admin");
            admin.role = Role.ADMIN;

            User viewer = new User();
            viewer.username = "viewer";
            viewer.passwordHash = Hasher.sha256("viewer");
            viewer.role = Role.VIEWER;

            userRepo.add(admin);
            userRepo.add(viewer);
            usersFs.saveAll(userRepo.findAll());
            System.out.println("INFO: users.tsv создан с пользователями admin/admin и viewer/viewer");
        } else {
            userRepo.replaceAll(users);
        }

        prodFs.load().forEach(prodRepo::save);

        AuditService audit = new AuditServiceImpl(auditRepo, auditFs);
        CatalogService catalog = new CatalogServiceImpl(prodRepo, audit, prodFs::saveAll);
        AuthService auth = new AuthServiceImpl(userRepo, audit);

        new ConsoleApp(auth, catalog, audit).run();
        System.out.println("До встречи!");
    }
}