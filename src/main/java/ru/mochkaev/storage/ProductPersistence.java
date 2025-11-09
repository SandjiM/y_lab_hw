package ru.mochkaev.storage;

import ru.mochkaev.domain.Product;

import java.util.Collection;

public interface ProductPersistence {
    void saveAll(Collection<Product> products);
}
