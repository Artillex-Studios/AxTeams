package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.TeamValueOld;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableInteger;
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

public final class TeamWarpLimitValue implements TeamValueOld<Integer, IdentifiableInteger> {
    private static final Logger logger = LoggerFactory.getLogger(TeamWarpLimitValue.class);
    private final Field<Integer> WARP_LIMIT = DSL.field("warp_limit", Integer.class);
    private final Table<Record> WARP_LIMIT_TABLE = DSL.table("axteams_warp_limit");

    @Override
    public Table<Record> table() {
        return this.WARP_LIMIT_TABLE;
    }

    @Override
    public String id() {
        return "warp_limit";
    }

    @Override
    public Query create() {
        return AxTeamsAPI.instance()
                .context()
                .createTableIfNotExists(this.WARP_LIMIT_TABLE)
                .column(TeamValueOld.ID, SQLDataType.INTEGER.identity(true))
                .column(TeamValueOld.TEAM_ID, SQLDataType.INTEGER)
                .column(this.WARP_LIMIT, SQLDataType.INTEGER)
                .primaryKey(TeamValueOld.ID);
    }

    @Override
    public List<IdentifiableInteger> parse(Result<Record> result) {
        ArrayList<IdentifiableInteger> integers = new ArrayList<>(1);
        if (result.isEmpty()) {
            return integers;
        }

        for (Record record : result) {
            try {
                integers.add(new IdentifiableInteger(record.get(TeamValueOld.ID), record.get(this.WARP_LIMIT)));
            } catch (IllegalArgumentException exception) {
                logger.error("An unexpected error occurred while loading team name from record {}!", record, exception);
            }
        }

        return integers;
    }

    @Override
    public List<Query> save(Team team, List<IdentifiableInteger> values) {
        ArrayList<Query> queries = new ArrayList<>(values.size());

        for (IdentifiableInteger value : values) {
            if (value.id() == Identifiable.DEFAULT) {
                Record1<Integer> record = AxTeamsAPI.instance()
                        .context()
                        .insertInto(this.WARP_LIMIT_TABLE)
                        .set(TeamValueOld.TEAM_ID, team.id().id())
                        .set(this.WARP_LIMIT, value.get())
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
                        .deleteFrom(this.WARP_LIMIT_TABLE)
                        .where(TeamValueOld.ID.eq(value.id()))
                        .limit(1)
                );
            } else {
                queries.add(AxTeamsAPI.instance()
                        .context()
                        .update(this.WARP_LIMIT_TABLE)
                        .set(this.WARP_LIMIT, value.get())
                        .where(TeamValueOld.ID.eq(value.id()))
                        .limit(1)
                );
            }
        }

        return queries;
    }
}
