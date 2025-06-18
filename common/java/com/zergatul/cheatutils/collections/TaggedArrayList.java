package com.zergatul.cheatutils.collections;

import java.util.ArrayList;
import java.util.Collection;

public class TaggedArrayList<E, T> extends ArrayList<E> {

    private final T tag;

    public TaggedArrayList(Collection<E> collection, T tag) {
        super(collection);
        this.tag = tag;
    }

    public T getTag() {
        return tag;
    }
}