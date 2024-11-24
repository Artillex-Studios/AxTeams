package com.artillexstudios.axteams.api.teams.values;

public interface Identifiable<T> {

    T get();

    void set(T value);

    State state();

    void state(State state);

    int id();

    void id(int id);
}
