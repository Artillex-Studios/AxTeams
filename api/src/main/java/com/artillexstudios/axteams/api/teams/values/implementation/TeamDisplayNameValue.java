package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.List;

public final class TeamDisplayNameValue extends TeamValue<Component, IdentifiableComponent> {
    private final Field<String> displayName = DSL.field("display_name", String.class);

    public TeamDisplayNameValue() {
        super("display_name");
    }

    @Override
    public boolean single() {
        return true;
    }

    @Override
    public List<Pair<Field<?>, DataType<?>>> fields() {
        return List.of(Pair.of(this.displayName, SQLDataType.VARCHAR));
    }

    @Override
    public List<Pair<Field<?>, ?>> transform(Team team, IdentifiableComponent value) {
        return List.of(Pair.of(this.displayName, MiniMessage.miniMessage().serialize(value.get())));
    }

    @Override
    public IdentifiableComponent parse(Record record) {
        return new IdentifiableComponent(record.get(TeamValue.ID), StringUtils.format(record.get(this.displayName)));
    }
}
