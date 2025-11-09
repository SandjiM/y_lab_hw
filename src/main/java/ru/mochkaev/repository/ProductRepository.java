package ru.mochkaev.repository;

import ru.mochkaev.domain.Product;

import java.util.Collection;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product p);
    Optional<Product> findById(long id);
    boolean deleteById(long id);
    Collection<Product> findAll();
}
