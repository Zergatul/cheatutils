package com.zergatul.cheatutils.collections;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ImmutableList<E> implements Iterable<E> {

    private final Object[] array;

    public ImmutableList() {
        array = new Object[0];
    }

    public ImmutableList(Collection<E> collection) {
        this(collection.toArray());
    }

    public static <E> ImmutableList<E> empty() {
        return new ImmutableList<>();
    }

    @SafeVarargs
    public static <E> ImmutableList<E> from(E... items) {
        return new ImmutableList<>(items);
    }

    private ImmutableList(Object[] array) {
        this.array = array;
    }

    public ImmutableList<E> add(E element) {
        int size = array.length;
        Object[] newArray = new Object[size + 1];
        System.arraycopy(array, 0, newArray, 0, size);
        newArray[size] = element;
        return new ImmutableList<>(newArray);
    }

    public boolean contains(E item) {
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], item)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Predicate<E> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test((E) array[i])) {
                return true;
            }
        }
        return false;
    }

    public ImmutableList<E> distinct() {
        if (array.length <= 1) {
            return this;
        }

        Set<E> set = new HashSet<>(array.length);
        for (E element : this) {
            set.add(element);
        }

        if (set.size() == array.length) {
            return this;
        } else {
            return new ImmutableList<>(set);
        }
    }

    public <K> ImmutableList<E> distinctBy(Function<E, K> selector) {
        if (isEmpty()) {
            return this;
        }

        Set<K> seen = new HashSet<>(array.length);
        List<E> result = new ArrayList<>(array.length);
        for (E element : this) {
            if (seen.add(selector.apply(element))) {
                result.add(element);
            }
        }

        return new ImmutableList<>(result);
    }

    public boolean intersects(ImmutableList<E> other) {
        for (int i1 = 0; i1 < this.array.length; i1++) {
            for (int i2 = 0; i2 < other.array.length; i2++) {
                if (Objects.equals(this.array[i1], other.array[i2])) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ImmutableList<?> other) {
            int size = this.size();
            if (size != other.size()) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                if (!Objects.equals(this.array[i], other.array[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) array[index];
    }

    public boolean isEmpty() {
        return array.length == 0;
    }

    @SuppressWarnings("unchecked")
    public int indexOf(Predicate<E> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test((E) array[i])) {
                return i;
            }
        }
        return -1;
    }

    public ImmutableList<E> remove(E element) {
        int size = array.length;
        int i = 0;
        found: {
            if (element == null) {
                for (; i < size; i++) {
                    if (array[i] == null) {
                        break found;
                    }
                }
            } else {
                for (; i < size; i++) {
                    if (element.equals(array[i])) {
                        break found;
                    }
                }
            }
            return this;
        }

        Object[] newArray = new Object[size - 1];
        if (i > 0) {
            System.arraycopy(array, 0, newArray, 0, i);
        }
        if (i < size - 1) {
            System.arraycopy(array, i + 1, newArray, i, size - i - 1);
        }
        return new ImmutableList<>(newArray);
    }

    @SuppressWarnings("unchecked")
    public ImmutableList<E> removeAll(Predicate<E> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test((E) array[i])) {
                List<E> list = new ArrayList<>();
                for (int j = 0; j < i; j++) {
                    list.add((E) array[j]);
                }
                for (int j = i + 1; j < array.length; j++) {
                    if (!predicate.test((E) array[j])) {
                        list.add((E) array[j]);
                    }
                }
                return new ImmutableList<>(list);
            }
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    public ImmutableList<E> replaceFirst(Predicate<E> predicate, UnaryOperator<E> map) {
        List<E> list = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            if (predicate.test((E) array[i])) {
                list.add(map.apply((E) array[i]));
                for (int j = i + 1; j < array.length; j++) {
                    list.add((E) array[j]);
                }
                return new ImmutableList<>(list);
            } else {
                list.add((E) array[i]);
            }
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    public ImmutableList<E> replaceAll(UnaryOperator<E> map) {
        for (int i = 0; i < array.length; i++) {
            E oldElement = (E) array[i];
            E newElement = map.apply(oldElement);
            if (oldElement != newElement) {
                Object[] newArray = new Object[array.length];
                System.arraycopy(array, 0, newArray, 0, i);
                newArray[i] = newElement;
                for (int j = i + 1; j < array.length; j++) {
                    newArray[j] = map.apply((E) array[j]);
                }
                return new ImmutableList<>(newArray);
            }
        }

        return this;
    }

    public ImmutableList<E> set(int index, E element) {
        int size = array.length;
        Object[] newArray = new Object[size];
        System.arraycopy(array, 0, newArray, 0, size);
        newArray[index] = element;
        return new ImmutableList<>(newArray);
    }

    public ImmutableList<E> swap(int index1, int index2) {
        int size = array.length;
        Object[] newArray = new Object[size];
        System.arraycopy(array, 0, newArray, 0, size);
        newArray[index1] = array[index2];
        newArray[index2] = array[index1];
        return new ImmutableList<>(newArray);
    }

    public int size() {
        return array.length;
    }

    public ImmutableList<E> sorted(Comparator<E> comparator) {
        if (array.length <= 1) {
            return this;
        }

        List<E> list = new ArrayList<>(array.length);
        for (E e : this) {
            list.add(e);
        }
        list.sort(comparator);

        return new ImmutableList<>(list);
    }

    @SuppressWarnings("unchecked")
    public Stream<E> stream() {
        return Arrays.stream(array).map(e -> (E) e);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < array.length;
            }

            @Override
            @SuppressWarnings("unchecked")
            public E next() {
                return (E) array[index++];
            }
        };
    }
}