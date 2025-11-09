package ru.mochkaev.cli;

import ru.mochkaev.domain.*;
import ru.mochkaev.service.AuditService;
import ru.mochkaev.service.AuthService;
import ru.mochkaev.service.CatalogService;

import java.util.Scanner;
import java.util.*;

public class ConsoleApp {
    private final AuthService auth;
    private final CatalogService catalog;
    private final AuditService audit;
    private final Scanner sc = new Scanner(System.in);

    public ConsoleApp(AuthService auth, CatalogService catalog, AuditService audit) {
        this.auth = auth;
        this.catalog = catalog;
        this.audit = audit;
    }

    public void run() {
        Menu.showWelcome();
        while (true) {
            if (auth.current().isEmpty()) {
                if (!screenGuest()) break;
            } else {
                User u = auth.current().get();
                if (!screenUser(u)) break;
            }
        }
    }

    private boolean screenGuest() {
        System.out.println();
        System.out.println("[Неавторизован] 1) Войти  0) Выход");
        System.out.print("> ");
        String cmd = sc.nextLine().trim();
        switch (cmd) {
            case "1": doLogin(); return true;
            case "0": return false;
            default: System.out.println("Неизвестная команда."); return true;
        }
    }

    private boolean screenUser(User u) {
        System.out.println();
        System.out.println("[Пользователь: " + u.username + " | роль: " + u.role + "]");
        System.out.println("1) Найти товары");
        System.out.println("2) Показать товар по ID");
        System.out.println("3) Показать метрики");
        System.out.println("4) Показать последние N событий аудита");
        System.out.println("5) Выйти из аккаунта");
        if (u.role == Role.ADMIN) {
            System.out.println("6) Добавить товар");
            System.out.println("7) Изменить товар");
            System.out.println("8) Удалить товар");
        }
        System.out.println("0) Завершить программу");
        System.out.print("> ");
        String cmd = sc.nextLine().trim();

        switch (cmd) {
            case "1": doSearch(); return true;
            case "2": doShowById(); return true;
            case "3": doMetrics(); return true;
            case "4": doAudit(); return true;
            case "5": auth.logout(u.username); System.out.println("Вы вышли."); return true;
            case "6": ifAdmin(() -> doCreate(u.username)); return true;
            case "7": ifAdmin(() -> doUpdate(u.username)); return true;
            case "8": ifAdmin(() -> doDelete(u.username)); return true;
            case "0": return false;
            default: System.out.println("Неизвестная команда."); return true;
        }
    }

    private void ifAdmin(Runnable r) {
        if (auth.current().isPresent() && auth.current().get().role == Role.ADMIN) r.run();
        else System.out.println("Недостаточно прав (нужна роль ADMIN).");
    }

    private void doLogin() {
        System.out.print("Username: ");
        String u = sc.nextLine().trim();
        System.out.print("Password: ");
        String p = sc.nextLine();
        try {
            auth.login(u, p);
            System.out.println("Вход выполнен.");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void doCreate(String actor) {
        Product p = new Product();
        p.title = Input.readNonEmpty(sc, "Title");
        p.brand = Input.readNonEmpty(sc, "Brand");
        p.category = Input.readEnum(sc, "Category [PHONES/TV/LAPTOPS/ACCESSORIES/UNKNOWN]", Category.class, Category.UNKNOWN);
        p.priceCents = Input.readInt(sc, "Price (копейки, напр. 699000)", 0, Integer.MAX_VALUE, 0);
        p.stock = Input.readInt(sc, "Stock", 0, Integer.MAX_VALUE, 0);
        p.attributes = Input.readAttributes(sc, "Attributes (k=v;... , пусто чтобы пропустить)");

        Product saved = catalog.create(p, actor);
        System.out.println("Создан товар: #" + saved.id + " " + saved.title);
    }

    private void doUpdate(String actor) {
        long id = Input.readLong(sc, "ID товара", 1, Long.MAX_VALUE, -1);
        Optional<Product> op = catalog.get(id);
        if (op.isEmpty()) { System.out.println("Товар не найден."); return; }
        Product patch = new Product();
        System.out.println("Оставьте поле пустым, чтобы не менять значение.");
        patch.title = Input.readOptional(sc, "Title").orElse(null);
        patch.brand = Input.readOptional(sc, "Brand").orElse(null);
        patch.category = Input.readEnumOptional(sc, "Category [PHONES/TV/LAPTOPS/ACCESSORIES/UNKNOWN]", Category.class).orElse(null);
        Integer price = Input.readIntOptional(sc, "Price (копейки)").orElse(null);
        if (price != null) patch.priceCents = price;
        Integer stock = Input.readIntOptional(sc, "Stock").orElse(null);
        if (stock != null) patch.stock = stock;
        Map<String,String> attrs = Input.readAttributes(sc, "Attributes (k=v;... , пусто — не менять / '-' — очистить)");
        if (attrs != null) patch.attributes = attrs.isEmpty() ? new HashMap<>() : attrs;

        Product saved = catalog.update(id, patch, actor);
        System.out.println("Обновлено: #" + saved.id);
    }

    private void doDelete(String actor) {
        long id = Input.readLong(sc, "ID товара", 1, Long.MAX_VALUE, -1);
        if (!Input.readYesNo(sc, "Подтвердите удаление #" + id)) {
            System.out.println("Отменено.");
            return;
        }
        boolean ok = catalog.delete(id, actor);
        System.out.println(ok ? "Удалено." : "Не найдено.");
    }

    private void doShowById() {
        long id = Input.readLong(sc, "ID товара", 1, Long.MAX_VALUE, -1);
        Optional<Product> op = catalog.get(id);
        if (op.isEmpty()) { System.out.println("Товар не найден."); return; }
        Printers.printProduct(op.get());
    }

    private void doSearch() {
        SearchQuery q = new SearchQuery();
        q.text = Input.readOptional(sc, "Text contains").orElse(null);
        q.brand = Input.readOptional(sc, "Brand").orElse(null);
        q.category = Input.readEnumOptional(sc, "Category [PHONES/TV/LAPTOPS/ACCESSORIES/UNKNOWN]", Category.class).orElse(null);
        q.minPriceCents = Input.readIntOptional(sc, "Min price (копейки)").orElse(null);
        q.maxPriceCents = Input.readIntOptional(sc, "Max price (копейки)").orElse(null);
        q.inStockOnly = Input.readYesNoOptional(sc, "Only in stock? (y/n)").orElse(null);
        q.attrEquals = Input.readAttributes(sc, "Attr equals (k=v;...)");

        long t0 = System.nanoTime();
        List<Product> res = catalog.search(q);
        long micros = (System.nanoTime() - t0) / 1000;
        System.out.println("Результатов: " + res.size() + " (" + micros + " µs)");
        if (res.isEmpty()) return;
        int page = 1, pageSize = 20;
        for (int i = 0; i < res.size(); i++) {
            if ((i % pageSize) == 0) {
                System.out.println("-- page " + page++ + " --");
            }
            Printers.printProductLine(res.get(i));
        }
    }

    private void doMetrics() {
        CatalogService.Metrics m = catalog.metrics();
        System.out.println("Products: " + m.productCount());
        System.out.println("CRUD ops: " + m.crudOps());
        System.out.println("Cache: hits=" + m.cacheHits() + ", misses=" + m.cacheMisses());
        System.out.println("Search: count=" + m.searchCount()
                + ", last=" + m.lastSearchMicros() + " µs"
                + ", avg=" + String.format("%.1f", m.avgSearchMicros()) + " µs");
    }

    private void doAudit() {
        int n = Input.readInt(sc, "Сколько последних событий вывести?", 1, 1000, 20);
        audit.recent(n).forEach(Printers::printAudit);
    }
}
