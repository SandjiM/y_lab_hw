package ru.mochkaev.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public final class Input {
    private Input() {}

    public static String readNonEmpty(Scanner sc, String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Поле не может быть пустым.");
        }
    }

    public static Optional<String> readOptional(Scanner sc, String label) {
        System.out.print(label + ": ");
        String s = sc.nextLine().trim();
        return s.isEmpty() ? Optional.empty() : Optional.of(s);
    }

    public static int readInt(Scanner sc, String label, int min, int max, int def) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return def;
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Введите число в диапазоне [" + min + ".." + max + "].");
            }
        }
    }

    public static Optional<Integer> readIntOptional(Scanner sc, String label) {
        System.out.print(label + ": ");
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return Optional.empty();
        try { return Optional.of(Integer.parseInt(s)); }
        catch (NumberFormatException e) { System.out.println("Неверное число, пропускаю."); return Optional.empty(); }
    }

    public static long readLong(Scanner sc, String label, long min, long max, long def) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return def;
            try {
                long v = Long.parseLong(s);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число в диапазоне [" + min + ".." + max + "].");
            }
        }
    }

    public static <E extends Enum<E>> E readEnum(Scanner sc, String label, Class<E> enumCls, E def) {
        while (true) {
            System.out.print(label + ": ");
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return def;
            try {
                return Enum.valueOf(enumCls, s.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Недопустимое значение.");
            }
        }
    }

    public static <E extends Enum<E>> Optional<E> readEnumOptional(Scanner sc, String label, Class<E> enumCls) {
        System.out.print(label + ": ");
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Enum.valueOf(enumCls, s.toUpperCase()));
        } catch (IllegalArgumentException e) {
            System.out.println("Недопустимое значение, пропускаю.");
            return Optional.empty();
        }
    }

    public static Optional<Boolean> readYesNoOptional(Scanner sc, String label) {
        System.out.print(label + " ");
        String s = sc.nextLine().trim().toLowerCase();
        if (s.isEmpty()) return Optional.empty();
        if (s.startsWith("y") || s.equals("да")) return Optional.of(true);
        if (s.startsWith("n") || s.equals("нет")) return Optional.of(false);
        System.out.println("Ожидалось y/n, пропускаю.");
        return Optional.empty();
    }

    // "k=v;size=M;color=red"  -> Map
    // пустая строка -> null (не менять); "-" -> пустая Map (очистить)
    public static Map<String,String> readAttributes(Scanner sc, String label) {
        System.out.print(label + ": ");
        String line = sc.nextLine().trim();
        if (line.isEmpty()) return null;
        if (line.equals("-")) return new HashMap<>();
        Map<String,String> map = new HashMap<>();
        String[] parts = line.split(";");
        for (String p : parts) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }
}
