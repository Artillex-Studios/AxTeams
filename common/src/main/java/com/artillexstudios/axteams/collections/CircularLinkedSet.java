package com.artillexstudios.axteams.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

public final class CircularLinkedSet<E> implements Queue<E> {
    private final int maxSize;
    private final Set<E> asSet;
    private final LinkedList<E> list;
    private int size = 0;

    public CircularLinkedSet(int size) {
        this.maxSize = size;
        this.asSet = new HashSet<>(this.size);
        this.list = new LinkedList<>();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return this.asSet.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return this.asSet.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return this.asSet.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return this.asSet.toArray(a);
    }

    @Override
    public boolean add(E e) {
        if (this.size + 1 > this.maxSize) {
            E element = this.list.pollFirst();
            if (!this.asSet.remove(element)) {
                throw new NoSuchElementException();
            }
        } else {
            this.size++;
        }

        if (!this.asSet.add(e)) {
            this.list.remove(e);
        }
        this.list.add(e);
        return true;
    }

    @Override
    public boolean offer(E e) {
        return this.add(e);
    }

    @Override
    public E remove() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        }

        E last = this.list.getFirst();
        this.remove(last);
        return last;
    }

    @Override
    public E poll() {
        E last = this.list.getFirst();
        this.remove(last);
        return last;
    }

    @Override
    public E element() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        }

        return this.list.getFirst();
    }

    @Override
    public E peek() {
        return this.list.getFirst();
    }

    public E last() {
        return this.list.getLast();
    }

    public E removeLast() {
        E last = this.list.getLast();
        this.remove(last);
        return last;
    }

    public E get(int i) {
        return this.list.get(i);
    }

    public E remove(int i) {
        E element = this.list.remove(i);
        this.asSet.remove(element);
        this.size--;
        return element;
    }

    @Override
    public boolean remove(Object o) {
        boolean success = this.asSet.remove(o);
        this.list.remove(o);
        if (success) {
            this.size--;
        }

        return success;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.asSet.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        for (E e : c) {
            this.add(e);
        }

        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            this.remove(o);
        }

        return true;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        for (Object e : this.asSet.toArray(new Object[0])) {
            if (c.contains(e)) {
                continue;
            }

            this.remove(e);
        }
        return true;
    }

    @Override
    public void clear() {
        this.size = 0;
        this.asSet.clear();
        this.list.clear();
    }

    @Override
    public String toString() {
        return "CircularLinkedSet{" +
                "maxSize=" + maxSize +
                ", size=" + size +
                ", asSet=" + asSet +
                ", list=" + list +
                '}';
    }
}
