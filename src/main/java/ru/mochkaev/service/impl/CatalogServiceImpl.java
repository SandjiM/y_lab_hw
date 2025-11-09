package ru.mochkaev.service.impl;

import ru.mochkaev.domain.Product;
import ru.mochkaev.domain.SearchQuery;
import ru.mochkaev.repository.ProductRepository;
import ru.mochkaev.service.AuditService;
import ru.mochkaev.service.CatalogService;
import ru.mochkaev.util.IdGenerator;
import ru.mochkaev.util.LruCache;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class CatalogServiceImpl implements CatalogService {
    private final ProductRepository repo;
    private final AuditService audit;

    private final AtomicLong dataVersion = new AtomicLong(1);
    private final LruCache<SearchQuery, CacheEntry> cache = new LruCache<>(200);

    private final MetricsImpl metrics = new MetricsImpl();

    public CatalogServiceImpl(ProductRepository repo, AuditService audit) {
        this.repo = Objects.requireNonNull(repo);
        this.audit = Objects.requireNonNull(audit);
    }

    @Override
    public synchronized Product create(Product p, String actor) {
        Product toSave = cloneForSave(p);
        toSave.id = (p.id == 0 ? IdGenerator.nextId() : p.id);
        toSave.updatedAt = System.currentTimeMillis();

        Product saved = repo.save(toSave);
        metrics.crudOps++;
        bumpVersion();
        audit.log(actor, "CREATE_PRODUCT", "id=" + saved.id + ", title=" + safe(saved.title));
        return saved;
    }

    @Override
    public synchronized Product update(long id, Product patch, String actor) {
        Product existing = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Товар не найден: " + id));
        // применим патч: только ненулевые поля
        Product merged = new Product();
        merged.id = id;
        merged.title = notNull(patch.title, existing.title);
        merged.brand = notNull(patch.brand, existing.brand);
        merged.category = (patch.category != null ? patch.category : existing.category);
        merged.priceCents = (patch.priceCents != 0 ? patch.priceCents : existing.priceCents);
        merged.stock = (patch.stock != 0 ? patch.stock : existing.stock);
        merged.attributes = (patch.attributes != null ? new HashMap<>(patch.attributes)
                : (existing.attributes != null ? new HashMap<>(existing.attributes) : null));
        merged.updatedAt = System.currentTimeMillis();

        Product saved = repo.save(merged);
        metrics.crudOps++;
        bumpVersion();
        audit.log(actor, "UPDATE_PRODUCT", "id=" + id);
        return saved;
    }

    @Override
    public synchronized boolean delete(long id, String actor) {
        boolean ok = repo.deleteById(id);
        if (ok) {
            metrics.crudOps++;
            bumpVersion();
            audit.log(actor, "DELETE_PRODUCT", "id=" + id);
        }
        return ok;
    }

    @Override
    public synchronized List<Product> search(SearchQuery q) {
        long start = System.nanoTime();
        CacheEntry ce = cache.get(q);
        long currentVersion = dataVersion.get();
        if (ce != null && ce.version == currentVersion) {
            metrics.cacheHits++;
            return idsToProducts(ce.ids);
        }
        metrics.cacheMisses++;

        List<Product> all = new ArrayList<>(repo.findAll());

        List<Product> filtered = all.stream()
                .filter(p -> q.brand == null || equalsIgnoreCase(p.brand, q.brand))
                .filter(p -> q.category == null || p.category == q.category)
                .filter(p -> q.minPriceCents == null || p.priceCents >= q.minPriceCents)
                .filter(p -> q.maxPriceCents == null || p.priceCents <= q.maxPriceCents)
                .filter(p -> q.inStockOnly == null || !q.inStockOnly || p.stock > 0)
                .filter(p -> q.text == null || containsIgnoreCase(p.title, q.text))
                .filter(p -> attrsMatch(p, q))
                .sorted(Comparator.comparingLong(p -> p.id))
                .collect(Collectors.toList());

        List<Long> ids = filtered.stream().map(p -> p.id).collect(Collectors.toList());
        cache.put(q, new CacheEntry(currentVersion, ids));

        long tookMicros = (System.nanoTime() - start) / 1000;
        return filtered;
    }

    @Override
    public synchronized Optional<Product> get(long id) {
        return repo.findById(id);
    }

    @Override
    public synchronized Metrics metrics() {
        return metrics.snapshot(repo.findAll().size());
    }

    private static Product cloneForSave(Product p) {
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

    private void bumpVersion() {
        cache.clear();
        dataVersion.incrementAndGet();
    }

    private static String notNull(String v, String fallback) {
        return (v != null ? v : fallback);
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private static boolean containsIgnoreCase(String text, String needle) {
        if (text == null || needle == null) return false;
        return text.toLowerCase().contains(needle.toLowerCase());
    }

    private static boolean attrsMatch(Product p, SearchQuery q) {
        if (q.attrEquals == null || q.attrEquals.isEmpty()) return true;
        if (p.attributes == null) return false;
        for (Map.Entry<String, String> e : q.attrEquals.entrySet()) {
            String val = p.attributes.get(e.getKey());
            if (!Objects.equals(val, e.getValue())) return false;
        }
        return true;
    }

    private List<Product> idsToProducts(List<Long> ids) {
        List<Product> res = new ArrayList<>(ids.size());
        for (Long id : ids) {
            repo.findById(id).ifPresent(res::add);
        }
        return res;
    }

    private static final class CacheEntry {
        final long version;
        final List<Long> ids;
        CacheEntry(long version, List<Long> ids) {
            this.version = version;
            this.ids = ids;
        }
    }

    private static final class MetricsImpl implements Metrics {
        private long crudOps;
        private long cacheHits;
        private long cacheMisses;

        @Override public long productCount() { return lastProductCount; }
        @Override public long crudOps() { return crudOps; }
        @Override public long cacheHits() { return cacheHits; }
        @Override public long cacheMisses() { return cacheMisses; }

        private long lastProductCount;
        Metrics snapshot(long productCountNow) {
            MetricsImpl m = new MetricsImpl();
            m.crudOps = this.crudOps;
            m.cacheHits = this.cacheHits;
            m.cacheMisses = this.cacheMisses;
            m.lastProductCount = productCountNow;
            return m;
        }
    }
}
