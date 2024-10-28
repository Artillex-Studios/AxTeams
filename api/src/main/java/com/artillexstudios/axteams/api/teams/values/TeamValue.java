package com.artillexstudios.axteams.api.teams.values;

import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axteams.api.AxTeamsAPI;
import com.artillexstudios.axteams.api.teams.Team;
import org.jooq.CreateTableElementListStep;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.UpdateSetMoreStep;
import org.jooq.UpdateSetStep;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.ArrayList;
import java.util.List;

public abstract class TeamValue<Y, T extends Identifiable<Y>> {
    public static final Field<Integer> ID = DSL.field("id", int.class);
    public static final Field<Integer> TEAM_ID = DSL.field("team_id", int.class);
    private final Table<Record> table;
    private final String identifier;

    public TeamValue(String identifier) {
        this.identifier = identifier;
        this.table = DSL.table("axteams_" + this.identifier);
    }

    public final String id() {
        return this.identifier;
    }

    public final Table<Record> table() {
        return this.table;
    }


    public final List<T> parse(Result<Record> result) {
        ArrayList<T> records = new ArrayList<>(result.size());
        if (result.isEmpty()) {
            return records;
        }

        for (Record record : result) {
            try {
                T parsed = this.parse(record);
                if (parsed == null) {
                    continue;
                }

                records.add(parsed);
            } catch (IllegalArgumentException | NullPointerException exception) {
                LogUtils.error("An unexpected error occurred while loading {} from record {}!", this.identifier, record, exception);
            }
        }

        return records;
    }

    public final Query create() {
        CreateTableElementListStep step = AxTeamsAPI.instance()
                .context()
                .createTableIfNotExists(this.table)
                .column(ID, SQLDataType.INTEGER.identity(true))
                .column(TEAM_ID, SQLDataType.INTEGER);

        for (Pair<Field<?>, DataType<?>> field : this.fields()) {
            step = step.column(field.first(), field.second());
        }

        step = step.primaryKey(ID);

        return step;
    }

    public final List<Query> save(Team team, List<T> values) {
        ArrayList<Query> queries = new ArrayList<>(values.size());

        for (T value : values) {
            if (value.id() == Identifiable.DEFAULT) {
                InsertSetMoreStep<Record> step = AxTeamsAPI.instance()
                        .context()
                        .insertInto(this.table)
                        .set(TEAM_ID, team.id().id());

                for (Pair<Field<?>, ?> field : this.transform(team, value)) {
                    step = step.set((Field) field.first(), field.second());
                }

                Record1<Integer> record = step.returningResult(ID)
                        .fetchOne();

                if (record == null) {
                    LogUtils.error("Failed to insert new value, due to record being null!");
                    continue;
                }

                Integer id = record.get(ID);
                if (id == null) {
                    LogUtils.error("Failed to insert new value, due to id being null!");
                    continue;
                }

                value.id(id);
            } else if (value.id() == Identifiable.DELETED) {
                queries.add(AxTeamsAPI.instance()
                        .context()
                        .deleteFrom(this.table)
                        .where(ID.eq(value.id()))
                );
            } else {
                UpdateSetStep<Record> step = AxTeamsAPI.instance()
                        .context()
                        .update(this.table);

                for (Pair<Field<?>, ?> field : this.transform(team, value)) {
                    step = step.set((Field) field.first(), field.second());
                }

                queries.add(((UpdateSetMoreStep<Record>) step).where(ID.eq(value.id())));
            }
        }

        return queries;
    }

    public abstract List<Pair<Field<?>, DataType<?>>> fields();

    public abstract List<Pair<Field<?>, ?>> transform(Team team, T value);

    public abstract T parse(Record record);

    public boolean single() {
        return false;
    }

    public T defaultValue() {
        return null;
    }
}
