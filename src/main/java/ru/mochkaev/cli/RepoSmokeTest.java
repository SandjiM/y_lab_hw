package ru.mochkaev.cli;

import ru.mochkaev.domain.Category;
import ru.mochkaev.domain.Product;
import ru.mochkaev.repository.impl.InMemoryProductRepository;

public class RepoSmokeTest {
    public static void main(String[] args) {
        System.out.println("=== InMemoryProductRepository smoke test ===");
        InMemoryProductRepository repo = new InMemoryProductRepository();

        Product p1 = new Product();
        p1.title = "iPhone 13";
        p1.brand = "Apple";
        p1.category = Category.PHONES;
        p1.priceCents = 699_000;
        p1.stock = 12;

        Product p2 = new Product();
        p2.title = "Galaxy S23";
        p2.brand = "Samsung";
        p2.category = Category.PHONES;
        p2.priceCents = 649_000;
        p2.stock = 5;

        Product p3 = new Product();
        p3.title = "MacBook Air";
        p3.brand = "Apple";
        p3.category = Category.LAPTOPS;
        p3.priceCents = 1_199_000;
        p3.stock = 3;

        p1 = repo.save(p1);
        p2 = repo.save(p2);
        p3 = repo.save(p3);

        System.out.println("Created ids: " + p1.id + ", " + p2.id + ", " + p3.id);
        System.out.println("Repo size: " + repo.size());

        // update p2 price & stock
        p2.priceCents = 599_000;
        p2.stock = 0;
        repo.save(p2);

        // check indexes
        System.out.println("Apple ids: " + repo.idsByBrand("Apple"));
        System.out.println("PHONES ids: " + repo.idsByCategory(Category.PHONES));
        System.out.println("Price 599000 ids: " + repo.idsByPrice(599_000));
        System.out.println("p2 inStock? " + repo.isInStock(p2.id));

        // delete p1
        boolean del = repo.deleteById(p1.id);
        System.out.println("Delete p1: " + del + ", size now: " + repo.size());

        // list all
        System.out.println("All products:");
        for (Product p : repo.findAll()) {
            System.out.println("#" + p.id + " " + p.title + " | " + p.brand + " | " + p.category + " | " + p.priceCents + " | stock=" + p.stock);
        }

        System.out.println("OK");
    }
}
