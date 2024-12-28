package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableInteger;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.List;

public final class TeamAllyLimitValue extends TeamValue<Integer, IdentifiableInteger> {
    private final Field<Integer> teamAllyLimit = DSL.field("team_ally_limit", Integer.class);

    public TeamAllyLimitValue() {
        super("team_ally_limit");
    }

    @Override
    public boolean single() {
        return true;
    }

    @Override
    public List<Pair<Field<?>, DataType<?>>> fields() {
        return List.of(Pair.of(this.teamAllyLimit, SQLDataType.INTEGER));
    }

    @Override
    public List<Pair<Field<?>, ?>> transform(Team team, IdentifiableInteger value) {
        return List.of(Pair.of(this.teamAllyLimit, value.get()));
    }

    @Override
    public IdentifiableInteger parse(Record record) {
        return new IdentifiableInteger(record.get(TeamValue.ID), record.get(this.teamAllyLimit));
    }
}
