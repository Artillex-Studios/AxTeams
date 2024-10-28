package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.TeamValueOld;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableBoolean;
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

public final class TeamPvPValue implements TeamValueOld<Boolean, IdentifiableBoolean> {
    private static final Logger logger = LoggerFactory.getLogger(TeamPvPValue.class);
    private final Field<Boolean> PVP_ENABLED = DSL.field("enabled", Boolean.class);
    private final Table<Record> PVP_TABLE = DSL.table("axteams_pvp");

    @Override
    public boolean single() {
        return true;
    }

    @Override
    public Table<Record> table() {
        return this.PVP_TABLE;
    }

    @Override
    public IdentifiableBoolean defaultValue() {
        return new IdentifiableBoolean(false);
    }

    @Override
    public String id() {
        return "pvp";
    }

    @Override
    public Query create() {
        return AxTeamsAPI.instance()
                .context()
                .createTableIfNotExists(this.PVP_TABLE)
                .column(TeamValueOld.ID, SQLDataType.INTEGER.identity(true))
                .column(TeamValueOld.TEAM_ID, SQLDataType.INTEGER)
                .column(this.PVP_ENABLED, SQLDataType.BOOLEAN)
                .primaryKey(TeamValueOld.ID);
    }

    @Override
    public List<IdentifiableBoolean> parse(Result<Record> result) {
        ArrayList<IdentifiableBoolean> booleans = new ArrayList<>(1);
        if (result.isEmpty()) {
            return booleans;
        }

        for (Record record : result) {
            try {
                booleans.add(new IdentifiableBoolean(record.get(TeamValueOld.ID), record.get(this.PVP_ENABLED)));
            } catch (IllegalArgumentException exception) {
                logger.error("An unexpected error occurred while loading team name from record {}!", record, exception);
            }
        }

        return booleans;
    }

    @Override
    public List<Query> save(Team team, List<IdentifiableBoolean> values) {
        ArrayList<Query> queries = new ArrayList<>(values.size());

        for (IdentifiableBoolean value : values) {
            if (value.id() == Identifiable.DEFAULT) {
                Record1<Integer> record = AxTeamsAPI.instance()
                        .context()
                        .insertInto(this.PVP_TABLE)
                        .set(TeamValueOld.TEAM_ID, team.id().id())
                        .set(this.PVP_ENABLED, value.get())
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
                        .deleteFrom(this.PVP_TABLE)
                        .where(TeamValueOld.ID.eq(value.id()))
                        .limit(1)
                );
            } else {
                queries.add(AxTeamsAPI.instance()
                        .context()
                        .update(this.PVP_TABLE)
                        .set(this.PVP_ENABLED, value.get())
                        .where(TeamValueOld.ID.eq(value.id()))
                        .limit(1)
                );
            }
        }

        return queries;
    }
}
