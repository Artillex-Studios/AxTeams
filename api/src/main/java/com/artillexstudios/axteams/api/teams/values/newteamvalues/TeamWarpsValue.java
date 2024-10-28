package com.artillexstudios.axteams.api.teams.values.newteamvalues;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.TeamValueOld;
import com.artillexstudios.axteams.api.teams.values.identifiables.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.List;

public final class TeamWarpsValue extends TeamValue<Warp, Warp> {
    private final Field<String> name = DSL.field("name", String.class);
    private final Field<String> world = DSL.field("world", String.class);
    private final Field<Integer> locationX = DSL.field("location_x", int.class);
    private final Field<Integer> locationY = DSL.field("location_y", int.class);
    private final Field<Integer> locationZ = DSL.field("location_z", int.class);
    private final Field<Double> locationYaw = DSL.field("location_yaw", double.class);
    private final Field<Double> locationPitch = DSL.field("location_pitch", double.class);
    private final Field<String> password = DSL.field("password", String.class);

    public TeamWarpsValue() {
        super("warps");
    }

    @Override
    public List<Pair<Field<?>, DataType<?>>> fields() {
        return List.of(Pair.of(this.name, SQLDataType.VARCHAR),
                Pair.of(this.world, SQLDataType.VARCHAR),
                Pair.of(this.locationX, SQLDataType.INTEGER),
                Pair.of(this.locationY, SQLDataType.INTEGER),
                Pair.of(this.locationZ, SQLDataType.INTEGER),
                Pair.of(this.locationYaw, SQLDataType.FLOAT),
                Pair.of(this.locationPitch, SQLDataType.FLOAT),
                Pair.of(this.password, SQLDataType.VARCHAR)
        );
    }

    @Override
    public List<Pair<Field<?>, ?>> transform(Team team, Warp value) {
        return List.of(Pair.of(this.name, value.name()),
                Pair.of(this.world, value.location().getWorld().getName()),
                Pair.of(this.locationX, value.location().getBlockX()),
                Pair.of(this.locationY, value.location().getBlockY()),
                Pair.of(this.locationZ, value.location().getBlockZ()),
                Pair.of(this.locationYaw, ((Number) value.location().getYaw()).doubleValue()),
                Pair.of(this.locationPitch, ((Number) value.location().getPitch()).doubleValue()),
                Pair.of(this.password, value.password())
        );
    }

    @Override
    public Warp parse(Record record) {
        return new Warp(record.get(TeamValueOld.ID), record.get(this.name), new Location(Bukkit.getWorld(record.get(this.world)), record.get(this.locationX), record.get(this.locationY), record.get(this.locationZ), record.get(this.locationYaw).floatValue(), record.get(this.locationPitch).floatValue()), record.get(this.password));
    }
}
