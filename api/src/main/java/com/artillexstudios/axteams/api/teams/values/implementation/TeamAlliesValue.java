package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableInteger;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.List;

public final class TeamAlliesValue extends TeamValue<Integer, IdentifiableInteger> {
    private final Field<Integer> otherId = DSL.field("other_id", int.class);

    public TeamAlliesValue() {
        super("allies");
    }

    @Override
    public List<Pair<Field<?>, DataType<?>>> fields() {
        return List.of(Pair.of(this.otherId, SQLDataType.INTEGER));
    }

    @Override
    public List<Pair<Field<?>, ?>> transform(Team team, IdentifiableInteger value) {
        return List.of(Pair.of(this.otherId, value.get()));
    }

    @Override
    public IdentifiableInteger parse(Record record) {
        int id = record.get(this.otherId);
        if (AxTeamsAPI.instance().team(new TeamID(id)).join() == null) {
            return null;
        }

        return new IdentifiableInteger(record.get(TeamValue.ID), record.get(this.otherId));
    }
}
