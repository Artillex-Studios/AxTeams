package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.TeamValueOld;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class TeamDisplayNameValue implements TeamValueOld<Component, IdentifiableComponent> {
    private static final Logger logger = LoggerFactory.getLogger(TeamDisplayNameValue.class);
    private final Table<Record> DISPLAY_TABLE = DSL.table("axteams_display_name");
    private final Field<String> DISPLAY_NAME = DSL.field("display_name", String.class);

    @Override
    public boolean single() {
        return true;
    }

    @Override
    public Table<Record> table() {
        return this.DISPLAY_TABLE;
    }

    @Override
    public String id() {
        return "display_name";
    }

    @Override
    public Query create() {
        return AxTeamsAPI.instance()
                .context()
                .createTableIfNotExists(this.DISPLAY_TABLE)
                .column(TeamValueOld.ID, SQLDataType.INTEGER.identity(true))
                .column(TeamValueOld.TEAM_ID, SQLDataType.INTEGER)
                .column(this.DISPLAY_NAME, SQLDataType.VARCHAR)
                .primaryKey(TeamValueOld.ID);
    }

    @Override
    public List<IdentifiableComponent> parse(Result<Record> result) {
        ArrayList<IdentifiableComponent> components = new ArrayList<>(1);
        if (result.isEmpty()) {
            return components;
        }

        for (Record record : result) {
            try {
                components.add(new IdentifiableComponent(record.get(TeamValueOld.ID), StringUtils.format(record.get(this.DISPLAY_NAME))));
            } catch (IllegalArgumentException exception) {
                logger.error("An unexpected error occurred while loading team name from record {}!", record, exception);
            }
        }

        return components;
    }

    @Override
    public List<Query> save(Team team, List<IdentifiableComponent> values) {
        ArrayList<Query> queries = new ArrayList<>(values.size());

        for (IdentifiableComponent value : values) {
            if (value.id() == Identifiable.DEFAULT) {
                Record1<Integer> record = AxTeamsAPI.instance()
                        .context()
                        .insertInto(this.DISPLAY_TABLE)
                        .set(TeamValueOld.TEAM_ID, team.id().id())
                        .set(this.DISPLAY_NAME, MiniMessage.miniMessage().serialize(value.get()))
                        .returningResult(TeamValueOld.ID)
                        .fetchOne();

                if (record == null) {
                    logger.error("Failed to insert new value, due to record being null!");
                    continue;
                }

                Integer id = record.get(TeamValueOld.ID);
                if (id == null) {
                    logger.error("Failed to insert new value, due to id being null!");
                    continue;
                }

                value.id(id);
            } else if (value.id() == Identifiable.DELETED) {
                queries.add(AxTeamsAPI.instance()
                        .context()
                        .deleteFrom(this.DISPLAY_TABLE)
                        .where(TeamValueOld.ID.eq(value.id()))
                        .limit(1)
                );
            } else {
                queries.add(AxTeamsAPI.instance()
                        .context()
                        .update(this.DISPLAY_TABLE)
                        .set(this.DISPLAY_NAME, MiniMessage.miniMessage().serialize(value.get()))
                        .where(TeamValueOld.ID.eq(value.id()))
                        .limit(1)
                );
            }
        }

        return queries;
    }
}
