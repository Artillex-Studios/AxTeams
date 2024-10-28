package com.artillexstudios.axteams.api.teams.values;

public interface Identifiable<T> {
    int DELETED = -100;
    int DEFAULT = 0;

    T get();

    void set(T value);

    int id();

    void id(int id);
}
