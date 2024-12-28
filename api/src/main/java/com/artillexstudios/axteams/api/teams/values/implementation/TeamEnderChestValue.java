package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableByteArray;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableInteger;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.List;

public class TeamEnderChestValue extends TeamValue<byte[], IdentifiableByteArray> {
    private final Field<byte[]> storage = DSL.field("storage", byte[].class);

    public TeamEnderChestValue() {
        super("ender_chest");
    }

    @Override
    public boolean single() {
        return true;
    }

    @Override
    public List<Pair<Field<?>, DataType<?>>> fields() {
        return List.of(Pair.of(this.storage, SQLDataType.BLOB));
    }

    @Override
    public List<Pair<Field<?>, ?>> transform(Team team, IdentifiableByteArray value) {
        return List.of(Pair.of(this.storage, value.get()));
    }

    @Override
    public IdentifiableByteArray parse(Record record) {
        return new IdentifiableByteArray(record.get(TeamValue.ID), record.get(this.storage));
    }
}
