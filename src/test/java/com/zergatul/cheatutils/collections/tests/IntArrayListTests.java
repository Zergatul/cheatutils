package com.zergatul.cheatutils.collections.tests;

import com.zergatul.cheatutils.collections.IntArrayList;

import java.util.function.Supplier;

public class IntArrayListTests {

    public static void main(String[] args) {
        test("RemoveTest1", () -> {
            IntArrayList list = new IntArrayList(1);
            list.addRange(new int[] { 1, 2, 3, 4, 5 });
            list.remove(0);
            return list;
        }, new int[] { 1, 2, 3, 4, 5 });

        test("RemoveTest2", () -> {
            IntArrayList list = new IntArrayList(1);
            list.addRange(new int[] { 1, 2, 3, 4, 5 });
            list.remove(1);
            return list;
        }, new int[] { 2, 3, 4, 5 });

        test("RemoveTest3", () -> {
            IntArrayList list = new IntArrayList(1);
            list.addRange(new int[] { 1, 2, 3, 4, 5 });
            list.remove(3);
            return list;
        }, new int[] { 1, 2, 4, 5 });

        test("RemoveTest4", () -> {
            IntArrayList list = new IntArrayList(1);
            list.addRange(new int[] { 1, 2, 3, 4, 5 });
            list.remove(5);
            return list;
        }, new int[] { 1, 2, 3, 4 });

        test("RemoveTest5", () -> {
            IntArrayList list = new IntArrayList(1);
            list.addRange(new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 });
            list.remove(1);
            return list;
        }, new int[0]);
    }

    private static void test(String name, Supplier<IntArrayList> supplier, int[] expected) {
        System.out.printf("%s: %s%n", name, equals(supplier.get(), expected) ? "OK" : "Fail");
    }

    private static boolean equals(IntArrayList list, int[] expected) {
        int[] elements = list.getElements();
        int size = list.size();
        if (size != expected.length) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (elements[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }
}