package com.artillexstudios.axteams.api.teams.values.implementation;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Permission;
import com.artillexstudios.axteams.api.teams.Permissions;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class TeamGroupsValue extends TeamValue<Group, Group> {
    private final Pattern delimiter = Pattern.compile("\\|");
    private final Field<Integer> priority = DSL.field("priority", int.class);
    private final Field<String> name = DSL.field("name", String.class);
    private final Field<String> displayName = DSL.field("displayname", String.class);
    private final Field<String> permissions = DSL.field("permissions", String.class);

    public TeamGroupsValue() {
        super("groups");
    }

    public String serialize(List<Permission> permissions) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < permissions.size(); i++) {
            builder.append(permissions.get(i).permission());
            if (i != permissions.size() - 1) {
                builder.append('|');
            }
        }
        return builder.toString();
    }

    public List<Permission> deserialize(String serialized) {
        List<Permission> permissions = new ArrayList<>();
        if (serialized.isBlank()) {
            return permissions;
        }

        for (String string : this.delimiter.split(serialized)) {
            Permission permission = Permissions.get(string);
            if (permission == null) {
                LogUtils.warn("Tried to load unknown permission: {}!", string);
                continue;
            }

            permissions.add(permission);
        }

        return permissions;
    }

    @Override
    public List<Pair<Field<?>, DataType<?>>> fields() {
        return List.of(Pair.of(this.priority, SQLDataType.INTEGER),
                Pair.of(this.name, SQLDataType.VARCHAR),
                Pair.of(this.displayName, SQLDataType.VARCHAR),
                Pair.of(this.permissions, SQLDataType.VARCHAR)
        );
    }

    @Override
    public List<Pair<Field<?>, ?>> transform(Team team, Group value) {
        return List.of(Pair.of(this.priority, value.priority()),
                Pair.of(this.name, value.name()),
                Pair.of(this.displayName, MiniMessage.miniMessage().serialize(value.displayName())),
                Pair.of(this.permissions, this.serialize(value.permissions()))
        );
    }

    @Override
    public Group parse(Record record) {
        return new Group(record.get(TeamValue.ID), record.get(this.priority), record.get(this.name), StringUtils.format(record.get(this.displayName)), this.deserialize(record.get(this.permissions)));
    }
}
