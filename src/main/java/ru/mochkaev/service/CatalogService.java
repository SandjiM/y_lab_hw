package ru.mochkaev.service;

import ru.mochkaev.domain.Product;
import ru.mochkaev.domain.SearchQuery;

import java.util.List;
import java.util.Optional;

public interface CatalogService {
    Product create(Product p, String actor);
    Product update(long id, Product patch, String actor);
    boolean delete(long id, String actor);
    List<Product> search(SearchQuery q);
    Optional<Product> get(long id);

    interface Metrics {
        long productCount();
        long crudOps();
        long cacheHits();
        long cacheMisses();

        long searchCount();
        long lastSearchMicros();
        double avgSearchMicros();
    }
    Metrics metrics();
}
