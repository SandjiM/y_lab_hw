package ru.mochkaev.domain;

import java.util.Map;
import java.util.Objects;

public class Product {
    public long id;
    public String title;
    public String brand;
    public Category category;
    public int priceCents;
    public int stock;
    public Map<String, String> attributes;
    public long updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product p = (Product) o;
        return id == p.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
