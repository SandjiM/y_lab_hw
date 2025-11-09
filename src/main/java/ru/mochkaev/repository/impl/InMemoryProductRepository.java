package ru.mochkaev.repository.impl;

import ru.mochkaev.domain.Category;
import ru.mochkaev.domain.Product;
import ru.mochkaev.repository.ProductRepository;
import ru.mochkaev.util.IdGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryProductRepository implements ProductRepository {
    private final Map<Long, Product> byId = new HashMap<>();
    private final Map<String, Set<Long>> byBrand = new HashMap<>();
    private final Map<Category, Set<Long>> byCategory = new HashMap<>();
    private final NavigableMap<Integer, Set<Long>> byPrice = new TreeMap<>();
    private final Set<Long> inStock = new HashSet<>();

    private static String norm(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    public synchronized Product save(Product p) {
        Objects.requireNonNull(p, "product");
        boolean isCreate = p.id == 0 || !byId.containsKey(p.id);

        Product toSave;
        if (isCreate) {
            toSave = cloneProduct(p);
            toSave.id = (p.id == 0 ? IdGenerator.nextId() : p.id);
            toSave.updatedAt = System.currentTimeMillis();
            byId.put(toSave.id, toSave);
            addToIndexes(toSave);
        } else {
            Product old = byId.get(p.id);
            removeFromIndexes(old);

            toSave = cloneProduct(p);
            toSave.updatedAt = System.currentTimeMillis();
            byId.put(toSave.id, toSave);

            addToIndexes(toSave);
        }
        return cloneProduct(toSave);
    }

    @Override
    public synchronized Optional<Product> findById(long id) {
        Product p = byId.get(id);
        return Optional.ofNullable(p == null ? null : cloneProduct(p));
    }

    @Override
    public synchronized boolean deleteById(long id) {
        Product removed = byId.remove(id);
        if (removed == null) return false;
        removeFromIndexes(removed);
        return true;
    }

    @Override
    public synchronized Collection<Product> findAll() {
        return byId.values().stream()
                .map(InMemoryProductRepository::cloneProduct)
                .collect(Collectors.toList());
    }


    private void addToIndexes(Product p) {
        String brand = norm(p.brand);
        byBrand.computeIfAbsent(brand, k -> new HashSet<>()).add(p.id);

        Category cat = p.category == null ? Category.UNKNOWN : p.category;
        byCategory.computeIfAbsent(cat, k -> new HashSet<>()).add(p.id);

        byPrice.computeIfAbsent(p.priceCents, k -> new HashSet<>()).add(p.id);

        if (p.stock > 0) inStock.add(p.id); else inStock.remove(p.id);
    }

    private void removeFromIndexes(Product p) {
        String brand = norm(p.brand);
        Set<Long> s1 = byBrand.get(brand);
        if (s1 != null) { s1.remove(p.id); if (s1.isEmpty()) byBrand.remove(brand); }

        Category cat = p.category == null ? Category.UNKNOWN : p.category;
        Set<Long> s2 = byCategory.get(cat);
        if (s2 != null) { s2.remove(p.id); if (s2.isEmpty()) byCategory.remove(cat); }

        Set<Long> s3 = byPrice.get(p.priceCents);
        if (s3 != null) { s3.remove(p.id); if (s3.isEmpty()) byPrice.remove(p.priceCents); }

        inStock.remove(p.id);
    }

    private static Product cloneProduct(Product p) {
        Product c = new Product();
        c.id = p.id;
        c.title = p.title;
        c.brand = p.brand;
        c.category = p.category;
        c.priceCents = p.priceCents;
        c.stock = p.stock;
        c.updatedAt = p.updatedAt;
        c.attributes = (p.attributes == null) ? null : new HashMap<>(p.attributes);
        return c;
    }


    public synchronized int size() { return byId.size(); }
    public synchronized Set<Long> idsByBrand(String brand) {
        return Optional.ofNullable(byBrand.get(norm(brand))).map(HashSet::new).orElseGet(HashSet::new);
    }
    public synchronized Set<Long> idsByCategory(Category c) {
        return Optional.ofNullable(byCategory.get(c)).map(HashSet::new).orElseGet(HashSet::new);
    }
    public synchronized Set<Long> idsByPrice(int priceCents) {
        return Optional.ofNullable(byPrice.get(priceCents)).map(HashSet::new).orElseGet(HashSet::new);
    }
    public synchronized boolean isInStock(long id) { return inStock.contains(id); }
}
