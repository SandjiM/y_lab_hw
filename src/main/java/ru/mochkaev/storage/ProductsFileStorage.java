package ru.mochkaev.storage;

import ru.mochkaev.domain.Category;
import ru.mochkaev.domain.Product;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductsFileStorage {
    private final Path path;

    public ProductsFileStorage(Path path) {
        this.path = path;
    }

    public List<Product> load() {
        try {
            List<String> lines = Tsv.readAllLines(path);
            if (lines.isEmpty()) return new ArrayList<>();
            List<Product> res = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String ln = lines.get(i);
                if (ln.trim().isEmpty()) continue;
                List<String> c = Tsv.splitTsvLine(ln);
                Product p = new Product();
                p.id = Long.parseLong(c.get(0));
                p.title = Tsv.unescapeTsv(c.get(1));
                p.brand = Tsv.unescapeTsv(c.get(2));
                p.category = Category.valueOf(c.get(3));
                p.priceCents = Integer.parseInt(c.get(4));
                p.stock = Integer.parseInt(c.get(5));
                p.attributes = Tsv.attrsFromString(Tsv.unescapeTsv(c.get(6)));
                p.updatedAt = Long.parseLong(c.get(7));
                res.add(p);
            }
            return res;
        } catch (IOException e) {
            System.err.println("WARN: cannot load products.tsv: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveAll(Collection<Product> products) {
        List<String> lines = new ArrayList<>();
        lines.add("id\ttitle\tbrand\tcategory\tpriceCents\tstock\tattributes\tupdatedAt");
        for (Product p : products) {
            lines.add(String.join("\t",
                    String.valueOf(p.id),
                    Tsv.escapeTsv(p.title),
                    Tsv.escapeTsv(p.brand),
                    p.category == null ? "UNKNOWN" : p.category.name(),
                    String.valueOf(p.priceCents),
                    String.valueOf(p.stock),
                    Tsv.escapeTsv(Tsv.attrsToString(p.attributes)),
                    String.valueOf(p.updatedAt)
            ));
        }
        try {
            Tsv.writeAtomically(path, lines);
        } catch (IOException e) {
            System.err.println("ERROR: cannot write products.tsv: " + e.getMessage());
        }
    }
}
