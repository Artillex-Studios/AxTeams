package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.TeamValueOld;
import com.artillexstudios.axteams.api.teams.values.identifiables.Warp;
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

public final class TeamWarpsValue implements TeamValueOld<Warp, Warp> {
    private static final Logger logger = LoggerFactory.getLogger(TeamWarpsValue.class);
    private final Table<Record> WARPS_TABLE = DSL.table("axteams_warps");
    private final Field<String> NAME = DSL.field("name", String.class);
    private final Field<String> WORLD = DSL.field("world", String.class);
    private final Field<Integer> LOCATION_X = DSL.field("location_x", int.class);
    private final Field<Integer> LOCATION_Y = DSL.field("location_y", int.class);
    private final Field<Integer> LOCATION_Z = DSL.field("location_z", int.class);
    private final Field<Double> LOCATION_YAW = DSL.field("location_yaw", double.class);
    private final Field<Double> LOCATION_PITCH = DSL.field("location_pitch", double.class);
    private final Field<String> PASSWORD = DSL.field("password", String.class);

    @Override
    public Table<Record> table() {
        return this.WARPS_TABLE;
    }

    @Override
    public String id() {
        return "warps";
    }

    @Override
    public Query create() {
        return AxTeamsAPI.instance()
                .context()
                .createTableIfNotExists(this.WARPS_TABLE)
                .column(TeamValueOld.ID, SQLDataType.INTEGER.identity(true))
                .column(TeamValueOld.TEAM_ID, SQLDataType.INTEGER)
                .column(this.NAME, SQLDataType.VARCHAR)
                .column(this.WORLD, SQLDataType.VARCHAR)
                .column(this.LOCATION_X, SQLDataType.INTEGER)
                .column(this.LOCATION_Y, SQLDataType.INTEGER)
                .column(this.LOCATION_Z, SQLDataType.INTEGER)
                .column(this.LOCATION_YAW, SQLDataType.FLOAT)
                .column(this.LOCATION_PITCH, SQLDataType.FLOAT)
                .column(this.PASSWORD, SQLDataType.VARCHAR)
                .primaryKey(TeamValueOld.ID);
    }

    @Override
    public List<Warp> parse(Result<Record> result) {
        ArrayList<Warp> warps = new ArrayList<>(result.size());
        if (result.isEmpty()) {
            return warps;
        }

        for (Record record : result) {
            try {
                warps.add(new Warp(record.get(TeamValueOld.ID), record.get(this.NAME), new Location(Bukkit.getWorld(record.get(this.WORLD)), record.get(this.LOCATION_X), record.get(this.LOCATION_Y), record.get(this.LOCATION_Z), ((Number) record.get(this.LOCATION_YAW)).floatValue(), ((Number) record.get(this.LOCATION_PITCH)).floatValue()), record.get(this.PASSWORD)));
            } catch (IllegalArgumentException | NullPointerException exception) {
                logger.error("An unexpected error occurred while loading warp from record {}!", record, exception);
            }
        }

        return warps;
    }

    @Override
    public List<Query> save(Team team, List<Warp> values) {
        ArrayList<Query> queries = new ArrayList<>(values.size());

        for (Warp value : values) {
            if (value.id() == Identifiable.DEFAULT) {
                Record1<Integer> record = AxTeamsAPI.instance()
                        .context()
                        .insertInto(this.WARPS_TABLE)
                        .set(TeamValueOld.TEAM_ID, team.id().id())
                        .set(this.NAME, value.name())
                        .set(this.WORLD, value.location().getWorld().getName())
                        .set(this.LOCATION_X, value.location().getBlockX())
                        .set(this.LOCATION_Y, value.location().getBlockY())
                        .set(this.LOCATION_Z, value.location().getBlockZ())
                        .set(this.LOCATION_YAW, ((Number) value.location().getYaw()).doubleValue())
                        .set(this.LOCATION_PITCH, ((Number) value.location().getPitch()).doubleValue())
                        .set(this.PASSWORD, value.password())
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
                        .deleteFrom(this.WARPS_TABLE)
                        .where(TeamValueOld.ID.eq(value.id()))
                        .limit(1)
                );
            } else {
                queries.add(AxTeamsAPI.instance()
                        .context()
                        .update(this.WARPS_TABLE)
                        .set(this.NAME, value.name())
                        .set(this.WORLD, value.location().getWorld().getName())
                        .set(this.LOCATION_X, value.location().getBlockX())
                        .set(this.LOCATION_Y, value.location().getBlockY())
                        .set(this.LOCATION_Z, value.location().getBlockZ())
                        .set(this.LOCATION_YAW, ((Number) value.location().getYaw()).doubleValue())
                        .set(this.LOCATION_PITCH, ((Number) value.location().getPitch()).doubleValue())
                        .set(this.PASSWORD, value.password())
                        .where(TeamValueOld.ID.eq(value.id()))
                        .limit(1)
                );
            }
        }

        return queries;
    }
}
