package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableBigDecimal;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.math.BigDecimal;
import java.util.List;

public final class TeamBankValue extends TeamValue<BigDecimal, IdentifiableBigDecimal> {
    private final Field<BigDecimal> amount = DSL.field("amount", BigDecimal.class);

    public TeamBankValue() {
        super("bank");
    }

    @Override
    public boolean single() {
        return true;
    }

    @Override
    public List<Pair<Field<?>, DataType<?>>> fields() {
        return List.of(Pair.of(this.amount, SQLDataType.DECIMAL));
    }

    @Override
    public List<Pair<Field<?>, ?>> transform(Team team, IdentifiableBigDecimal value) {
        return List.of(Pair.of(this.amount, value.get()));
    }

    @Override
    public IdentifiableBigDecimal parse(Record record) {
        return new IdentifiableBigDecimal(record.get(TeamValue.ID), record.get(this.amount));
    }
}
