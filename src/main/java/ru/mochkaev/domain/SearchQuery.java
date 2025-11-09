package ru.mochkaev.domain;

import java.util.Map;
import java.util.Objects;

public class SearchQuery {
    public String text;
    public String brand;
    public Category category;
    public Integer minPriceCents;
    public Integer maxPriceCents;
    public Boolean inStockOnly;
    public Map<String, String> attrEquals;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchQuery)) return false;
        SearchQuery q = (SearchQuery) o;
        return Objects.equals(text, q.text) &&
                Objects.equals(brand, q.brand) &&
                category == q.category &&
                Objects.equals(minPriceCents, q.minPriceCents) &&
                Objects.equals(maxPriceCents, q.maxPriceCents) &&
                Objects.equals(inStockOnly, q.inStockOnly) &&
                Objects.equals(attrEquals, q.attrEquals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, brand, category, minPriceCents, maxPriceCents, inStockOnly, attrEquals);
    }
}
