package ru.mochkaev.cli;

import ru.mochkaev.domain.AuditEvent;
import ru.mochkaev.domain.Product;

import java.util.Map;

public final class Printers {
    private Printers() {}

    public static void printProduct(Product p) {
        System.out.println("#" + p.id + " " + safe(p.title));
        System.out.println("  brand: " + safe(p.brand));
        System.out.println("  category: " + p.category);
        System.out.println("  price: " + p.priceCents);
        System.out.println("  stock: " + p.stock);
        System.out.println("  updatedAt: " + p.updatedAt);
        if (p.attributes != null && !p.attributes.isEmpty()) {
            System.out.println("  attributes:");
            for (Map.Entry<String,String> e : p.attributes.entrySet()) {
                System.out.println("    - " + e.getKey() + "=" + e.getValue());
            }
        }
    }

    public static void printProductLine(Product p) {
        System.out.println("#" + p.id + " | " + safe(p.title) + " | " + safe(p.brand)
                + " | " + p.category + " | " + p.priceCents + " | stock=" + p.stock);
    }

    public static void printAudit(AuditEvent e) {
        System.out.println(e.ts + " " + e.username + " " + e.action + " " + (e.details == null ? "" : e.details));
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
