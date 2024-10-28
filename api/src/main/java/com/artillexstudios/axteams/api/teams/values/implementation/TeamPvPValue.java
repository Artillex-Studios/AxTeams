package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableBoolean;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.List;

public final class TeamPvPValue extends TeamValue<Boolean, IdentifiableBoolean> {
    private final Field<Boolean> enabled = DSL.field("enabled", Boolean.class);

    public TeamPvPValue() {
        super("pvp");
    }

    @Override
    public boolean single() {
        return true;
    }

    @Override
    public List<Pair<Field<?>, DataType<?>>> fields() {
        return List.of(Pair.of(this.enabled, SQLDataType.BOOLEAN));
    }

    @Override
    public List<Pair<Field<?>, ?>> transform(Team team, IdentifiableBoolean value) {
        return List.of(Pair.of(this.enabled, value.get()));
    }

    @Override
    public IdentifiableBoolean parse(Record record) {
        return new IdentifiableBoolean(record.get(TeamValue.ID), record.get(this.enabled));
    }
}
