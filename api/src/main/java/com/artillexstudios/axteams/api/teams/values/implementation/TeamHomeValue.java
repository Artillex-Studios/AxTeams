package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.identifiables.IdentifiableLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.List;

public final class TeamHomeValue extends TeamValue<Location, IdentifiableLocation> {
    private final Field<String> world = DSL.field("world", String.class);
    private final Field<Integer> locationX = DSL.field("location_x", int.class);
    private final Field<Integer> locationY = DSL.field("location_y", int.class);
    private final Field<Integer> locationZ = DSL.field("location_z", int.class);
    private final Field<Double> locationYaw = DSL.field("location_yaw", double.class);
    private final Field<Double> locationPitch = DSL.field("location_pitch", double.class);

    public TeamHomeValue() {
        super("homes");
    }

    @Override
    public boolean single() {
        return true;
    }

    @Override
    public List<Pair<Field<?>, DataType<?>>> fields() {
        return List.of(Pair.of(this.world, SQLDataType.VARCHAR),
                Pair.of(this.locationX, SQLDataType.INTEGER),
                Pair.of(this.locationY, SQLDataType.INTEGER),
                Pair.of(this.locationZ, SQLDataType.INTEGER),
                Pair.of(this.locationYaw, SQLDataType.FLOAT),
                Pair.of(this.locationPitch, SQLDataType.FLOAT)
        );
    }

    @Override
    public List<Pair<Field<?>, ?>> transform(Team team, IdentifiableLocation value) {
        return List.of(Pair.of(this.world, value.get().getWorld().getName()),
                Pair.of(this.locationX, value.get().getBlockX()),
                Pair.of(this.locationY, value.get().getBlockY()),
                Pair.of(this.locationZ, value.get().getBlockZ()),
                Pair.of(this.locationYaw, ((Number) value.get().getYaw()).doubleValue()),
                Pair.of(this.locationPitch, ((Number) value.get().getPitch()).doubleValue())
        );
    }

    @Override
    public IdentifiableLocation parse(Record record) {
        return new IdentifiableLocation(record.get(TeamValue.ID), new Location(Bukkit.getWorld(record.get(this.world)), record.get(this.locationX), record.get(this.locationY), record.get(this.locationZ), record.get(this.locationYaw).floatValue(), record.get(this.locationPitch).floatValue()));
    }
}
