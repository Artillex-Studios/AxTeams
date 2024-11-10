package com.artillexstudios.axteams.utils;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class IdentifiableSupplier<E> implements Supplier<E> {
    private final Object identity;

    public IdentifiableSupplier(Object identity) {
        this.identity = identity;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof IdentifiableSupplier<?> that)) {
            return false;
        }

        return Objects.equals(this.identity, that.identity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.identity);
    }

    @Override
    public String toString() {
        return "IdentifiableSupplier{" +
                "identity=" + identity +
                '}';
    }
}
