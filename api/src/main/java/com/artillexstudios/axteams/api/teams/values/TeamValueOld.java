package com.artillexstudios.axteams.api.teams.values;

import com.artillexstudios.axteams.api.teams.Team;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.List;

public interface TeamValueOld<Y, T extends Identifiable<Y>> {
    Field<Integer> ID = DSL.field("id", int.class);
    Field<Integer> TEAM_ID = DSL.field("team_id", int.class);

    default boolean single() {
        return false;
    }

    default T defaultValue() {
        return null;
    }

    Table<Record> table();

    String id();

    Query create();

    List<T> parse(Result<Record> result);

    List<Query> save(Team team, List<T> values);
}
