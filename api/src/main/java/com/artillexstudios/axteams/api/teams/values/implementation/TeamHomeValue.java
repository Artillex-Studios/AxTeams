package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.TeamValueOld;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

public final class TeamHomeValue implements TeamValueOld<Location, IdentifiableLocation> {
    private static final Logger logger = LoggerFactory.getLogger(TeamHomeValue.class);
    private final Table<Record> HOMES_TABLE = DSL.table("axteams_homes");
    private final Field<String> WORLD = DSL.field("world", String.class);
    private final Field<Integer> LOCATION_X = DSL.field("location_x", int.class);
    private final Field<Integer> LOCATION_Y = DSL.field("location_y", int.class);
    private final Field<Integer> LOCATION_Z = DSL.field("location_z", int.class);
    private final Field<Double> LOCATION_YAW = DSL.field("location_yaw", double.class);
    private final Field<Double> LOCATION_PITCH = DSL.field("location_pitch", double.class);

    @Override
    public boolean single() {
        return true;
    }

    @Override
    public Table<Record> table() {
        return this.HOMES_TABLE;
    }

    @Override
    public String id() {
        return "home";
    }

    @Override
    public Query create() {
        return AxTeamsAPI.instance()
                .context()
                .createTableIfNotExists(this.HOMES_TABLE)
                .column(TeamValueOld.ID, SQLDataType.INTEGER.identity(true))
                .column(TeamValueOld.TEAM_ID, SQLDataType.INTEGER)
                .column(this.WORLD, SQLDataType.VARCHAR)
                .column(this.LOCATION_X, SQLDataType.INTEGER)
                .column(this.LOCATION_Y, SQLDataType.INTEGER)
                .column(this.LOCATION_Z, SQLDataType.INTEGER)
                .column(this.LOCATION_YAW, SQLDataType.FLOAT)
                .column(this.LOCATION_PITCH, SQLDataType.FLOAT)
                .primaryKey(TeamValueOld.ID);
    }

    @Override
    public List<IdentifiableLocation> parse(Result<Record> result) {
        ArrayList<IdentifiableLocation> locations = new ArrayList<>(result.size());
        if (result.isEmpty()) {
            return locations;
        }

        for (Record record : result) {
            try {
                locations.add(new IdentifiableLocation(record.get(TeamValueOld.ID), new Location(Bukkit.getWorld(record.get(this.WORLD)), record.get(this.LOCATION_X), record.get(this.LOCATION_Y), record.get(this.LOCATION_Z), ((Number) record.get(this.LOCATION_YAW)).floatValue(), ((Number) record.get(this.LOCATION_PITCH)).floatValue())));
            } catch (IllegalArgumentException | NullPointerException exception) {
                logger.error("An unexpected error occurred while loading warp from record {}!", record, exception);
            }
        }

        return locations;
    }

    @Override
    public List<Query> save(Team team, List<IdentifiableLocation> values) {
        ArrayList<Query> queries = new ArrayList<>(values.size());

        for (IdentifiableLocation value : values) {
            if (value.id() == Identifiable.DEFAULT) {
                Record1<Integer> record = AxTeamsAPI.instance()
                        .context()
                        .insertInto(this.HOMES_TABLE)
                        .set(TeamValueOld.TEAM_ID, team.id().id())
                        .set(this.WORLD, value.get().getWorld().getName())
                        .set(this.LOCATION_X, value.get().getBlockX())
                        .set(this.LOCATION_Y, value.get().getBlockY())
                        .set(this.LOCATION_Z, value.get().getBlockZ())
                        .set(this.LOCATION_YAW, ((Number) value.get().getYaw()).doubleValue())
                        .set(this.LOCATION_PITCH, ((Number) value.get().getPitch()).doubleValue())
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
                        .deleteFrom(this.HOMES_TABLE)
                        .where(TeamValueOld.ID.eq(value.id()))
                        .limit(1)
                );
            } else {
                queries.add(AxTeamsAPI.instance()
                        .context()
                        .update(this.HOMES_TABLE)
                        .set(this.WORLD, value.get().getWorld().getName())
                        .set(this.LOCATION_X, value.get().getBlockX())
                        .set(this.LOCATION_Y, value.get().getBlockY())
                        .set(this.LOCATION_Z, value.get().getBlockZ())
                        .set(this.LOCATION_YAW, ((Number) value.get().getYaw()).doubleValue())
                        .set(this.LOCATION_PITCH, ((Number) value.get().getPitch()).doubleValue())
                        .where(ID.eq(value.id()))
                        .limit(1)
                );
            }
        }

        return queries;
    }
}
