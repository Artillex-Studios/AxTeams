package com.artillexstudios.axteams.database;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axteams.api.LoadContext;
import com.artillexstudios.axteams.api.teams.Group;
import com.artillexstudios.axteams.api.teams.Team;
import com.artillexstudios.axteams.api.teams.TeamID;
import com.artillexstudios.axteams.api.teams.values.Identifiable;
import com.artillexstudios.axteams.api.teams.values.TeamValue;
import com.artillexstudios.axteams.api.teams.values.TeamValues;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.config.Groups;
import com.artillexstudios.axteams.teams.Teams;
import com.artillexstudios.axteams.users.Users;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DataHandler {
    private static final Table<Record> USERS = DSL.table("axteams_users");
    private static final Table<Record> TEAMS = DSL.table("axteams_teams");
    private static final Field<Integer> ID = DSL.field("id", int.class);
    private static final Field<UUID> USER_UUID = DSL.field("uuid", UUID.class);
    private static final Field<String> USERNAME = DSL.field("username", String.class);
    private static final Field<String> TEAM_NAME = DSL.field("team_name", String.class);
    private static final Field<Integer> TEAM_GROUP = DSL.field("team_group", int.class);
    private static final Field<String> TEXTURES = DSL.field("textures", String.class);
    private static final Field<Integer> TEAM_ID = DSL.field("team_id", int.class);
    private static final Field<Integer> TEAM_LEADER = DSL.field("team_leader", int.class);
    private static final Field<Long> LAST_SEEN = DSL.field("last_seen", long.class);

    public static CompletionStage<Void> setup() {
        ArrayList<CompletableFuture<Integer>> futures = new ArrayList<>();

        CompletionStage<Integer> teams = DatabaseConnector.getInstance().context().createTableIfNotExists(TEAMS)
                .column(ID, SQLDataType.INTEGER.identity(true))
                .column(TEAM_NAME, SQLDataType.VARCHAR)
                .column(TEAM_LEADER, SQLDataType.INTEGER)
                .primaryKey(ID)
                .executeAsync(AsyncUtils.executor())
                .exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while running teams table creation query!", throwable);
                    return 0;
                });

        futures.add(teams.toCompletableFuture());
        CompletionStage<Integer> users = DatabaseConnector.getInstance().context().createTableIfNotExists(USERS)
                .column(ID, SQLDataType.INTEGER.identity(true))
                .column(USER_UUID, SQLDataType.UUID)
                .column(USERNAME, SQLDataType.VARCHAR(16))
                .column(TEXTURES, SQLDataType.VARCHAR)
                .column(LAST_SEEN, SQLDataType.BIGINT)
                .column(TEAM_GROUP, SQLDataType.INTEGER)
                .column(TEAM_ID, SQLDataType.INTEGER)
                .primaryKey(ID)
                .executeAsync(AsyncUtils.executor())
                .exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while running user table creation query!", throwable);
                    return 0;
                });

        futures.add(users.toCompletableFuture());

        if (Config.DATABASE_TYPE == DatabaseType.SQLITE) {
            CompletableFuture<Integer> pragma = new CompletableFuture<>();
            AsyncUtils.executor().submit(() -> {
                DatabaseConnector.getInstance().context().fetch("PRAGMA journal_mode=WAL;");
                DatabaseConnector.getInstance().context().execute("PRAGMA synchronous = off;");
                DatabaseConnector.getInstance().context().execute("PRAGMA page_size = 32768;");
                DatabaseConnector.getInstance().context().fetch("PRAGMA mmap_size = 30000000000;");
                pragma.complete(1);
            });
            futures.add(pragma);
        }

        CompletableFuture<Integer> teamNames = CompletableFuture.supplyAsync(() -> {
            Result<Record2<String, Integer>> result = DatabaseConnector.getInstance().context()
                    .select(TEAM_NAME, ID)
                    .from(TEAMS)
                    .fetch();

            Map<String, TeamID> ids = new HashMap<>();
            for (Record2<String, Integer> res : result) {
                ids.put(res.get(TEAM_NAME), new TeamID(res.get(ID)));
            }
            Teams.loadNames(ids);
            return 0;
        }, AsyncUtils.executor());

        futures.add(teamNames.toCompletableFuture());
        CompletableFuture<Integer> values = new CompletableFuture<>();
        Scheduler.get().runLater(() -> {
            List<CompletableFuture<Integer>> teamValues = new ArrayList<>();
            for (TeamValue<?, ?> value : TeamValues.values()) {
                teamValues.add(value.create().executeAsync(AsyncUtils.executor()).toCompletableFuture().exceptionallyAsync(throwable -> {
                    LogUtils.error("An unexpected error occurred while creating teamvalue {} query!", value.id(), throwable);
                    return 0;
                }));
            }

            CompletableFuture.allOf(teamValues.toArray(new CompletableFuture[0])).thenRun(() -> values.complete(1));
        }, 60);

        futures.add(values);
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static void loadUser(int userId, LoadContext context, Consumer<User> userConsumer) {
        if (Config.DEBUG) {
            LogUtils.debug("Loading user with userId: {}", userId);
        }

        Result<Record> select = DatabaseConnector.getInstance().context()
                .select()
                .from(USERS)
                .where(ID.eq(userId))
                .limit(1)
                .fetch();

        if (select.isEmpty()) {
            userConsumer.accept(null);
            return;
        }

        Record record = select.get(0);
        loadUser(record.get(USER_UUID), record, context, userConsumer);
    }

    private static void loadUser(UUID uuid, Record record, LoadContext context, Consumer<User> userConsumer) {
        if (Config.DEBUG) {
            LogUtils.debug("User data select record for record {} with context: {}", record, context);
        }

        User loadedUser = Users.getUserIfLoadedImmediately(uuid);
        if (loadedUser != null) {
            if (Config.DEBUG) {
                LogUtils.debug("Loaded user is not null: {}", loadedUser);
            }
            userConsumer.accept(loadedUser);
            return;
        }

        if (record != null) {
            if (Config.DEBUG) {
                LogUtils.debug("Record is not null!");
            }

            Integer teamID = record.get(TEAM_ID);
            if (teamID != null) {
                Team loaded = Teams.getTeamIfLoadedImmediately(new TeamID(teamID));
                if (loaded != null) {
                    for (User member : loaded.members(true)) {
                        if (member.player().getUniqueId().equals(uuid)) {
                            // TODO: Figure out what causes this
                            LogUtils.warn("Got user from loaded team! This is a fail-safe, something went wrong and caused this!");
                            Users.loadWithContext(member, context);
                            Teams.loadWithContext(loaded, context);
                            userConsumer.accept(member);
                            return;
                        }
                    }
                }
            }

            Scheduler.get().run(() -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer.isOnline()) {
                    Pair<String, String> textures = NMSHandlers.getNmsHandler().textures(offlinePlayer.getPlayer());

                    if (teamID == null) {
                        if (Config.DEBUG) {
                            LogUtils.debug("Null team id; online!");
                        }
                        userConsumer.accept(new com.artillexstudios.axteams.users.User(record.get(ID), offlinePlayer, null, textures == null ? "" : textures.first(), null, System.currentTimeMillis()));
                    } else {
                        if (Config.DEBUG) {
                            LogUtils.debug("Not null team ({}) id; online!", teamID);
                        }
                        User user = new com.artillexstudios.axteams.users.User(record.get(ID), offlinePlayer, null, textures == null ? "" : textures.first(), Group.ofId(record.get(TEAM_GROUP)), System.currentTimeMillis());
                        Users.loadWithContext(user, context);
                        if (context == LoadContext.FULL) {
                            if (Config.DEBUG) {
                                LogUtils.debug("Full team load");
                            }

                            Teams.loadTeam(new TeamID(teamID)).thenAcceptAsync(team -> {
                                if (Config.DEBUG) {
                                    LogUtils.debug("Loaded team {}", team == null ? "null" : team.name());
                                }

                                userConsumer.accept(user);
                            }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
                                LogUtils.error("An unexpected error occurred while loading team {}!", teamID, throwable);
                                return null;
                            }, AsyncUtils.executor());
                        } else {
                            Teams.getTeam(new TeamID(teamID), LoadContext.TEMPORARY).thenAcceptAsync(team -> {
                                if (Config.DEBUG) {
                                    LogUtils.debug("Temp loaded team {}", team.name());
                                }

                                userConsumer.accept(user);
                            }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
                                LogUtils.error("An unexpected error occurred while loading team {}!", teamID, throwable);
                                return null;
                            }, AsyncUtils.executor());
                        }
                    }
                } else {
                    if (teamID == null) {
                        userConsumer.accept(new com.artillexstudios.axteams.users.User(record.get(ID), offlinePlayer, null, record.get(TEXTURES), null, record.get(LAST_SEEN)));
                    } else {
                        User user = new com.artillexstudios.axteams.users.User(record.get(ID), offlinePlayer, null, record.get(TEXTURES), Group.ofId(record.get(TEAM_GROUP)), record.get(LAST_SEEN));
                        Users.loadWithContext(user, context);
                        if (context == LoadContext.FULL) {
                            Teams.loadTeam(new TeamID(teamID)).thenAccept(team -> {
                                userConsumer.accept(user);
                            });
                        } else {
                            Teams.getTeam(new TeamID(teamID), LoadContext.TEMPORARY).thenAcceptAsync(team -> {
                                userConsumer.accept(user);
                            }, AsyncUtils.executor());
                        }
                    }
                }
            });

            return;
        }

        if (Config.DEBUG) {
            LogUtils.debug("Creating new user!", uuid);
        }

        CompletableFuture<Triple<OfflinePlayer, Boolean, String>> playerFuture = CompletableFuture.supplyAsync(() -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (Config.DEBUG) {
                LogUtils.debug("Is player online? {}", player.isOnline());
            }

            Pair<String, String> textures = NMSHandlers.getNmsHandler().textures(player.getPlayer());
            return Triple.of(player, player.isOnline(), player.isOnline() ? textures == null ? "" : textures.first() : "");
        }, command -> Scheduler.get().run(command)).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while saving users!", throwable);
            return null;
        }, AsyncUtils.executor());

        playerFuture.thenAcceptAsync(offlinePlayer -> {
            Record1<Integer> insert = DatabaseConnector.getInstance().context()
                    .insertInto(USERS)
                    .set(USER_UUID, uuid)
                    .set(USERNAME, offlinePlayer.getLeft().getName())
                    .set(TEXTURES, offlinePlayer.getRight())
                    .set(LAST_SEEN, offlinePlayer.getMiddle() ? System.currentTimeMillis() : 0)
                    .set(TEAM_GROUP, 0)
                    .set(TEAM_ID, 0)
                    .returningResult(ID)
                    .fetchOne();

            User user = new com.artillexstudios.axteams.users.User(insert.get(ID), offlinePlayer.getLeft(), null, offlinePlayer.getRight(), null, offlinePlayer.getMiddle() ? System.currentTimeMillis() : 0);
            Users.loadWithContext(user, context);
            userConsumer.accept(user);
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while loading user {}!", uuid, throwable);
            return null;
        }, AsyncUtils.executor());
    }

    public static CompletionStage<User> loadUser(UUID uuid, LoadContext context) {
        if (Config.DEBUG) {
            LogUtils.debug("Loading user with uuid: {}", uuid);
        }

        CompletableFuture<User> userFuture = new CompletableFuture<>();
        AsyncUtils.executor().submit(() -> {
            Result<Record> select = DatabaseConnector.getInstance().context()
                    .select()
                    .from(USERS)
                    .where(USER_UUID.eq(uuid))
                    .limit(1)
                    .fetch();

            if (select.isEmpty()) {
                loadUser(uuid, null, context, userFuture::complete);
            } else {
                loadUser(uuid, select.get(0), context, userFuture::complete);
            }
        });
        return userFuture;
    }

    public static CompletableFuture<com.artillexstudios.axteams.teams.Team> createTeam(User user, String name) {
        return CompletableFuture.supplyAsync(() -> {
            Record1<Integer> insert = DatabaseConnector.getInstance().context()
                    .insertInto(TEAMS)
                    .set(TEAM_NAME, name)
                    .set(TEAM_LEADER, user.id())
                    .returningResult(ID)
                    .fetchOne();

            if (insert == null) {
                return null;
            }

            TeamID teamID = new TeamID(insert.get(ID));
            DatabaseConnector.getInstance().context()
                    .update(USERS)
                    .set(TEAM_ID, teamID.id())
                    .where(ID.eq(user.id()))
                    .execute();

            com.artillexstudios.axteams.teams.Team team = new com.artillexstudios.axteams.teams.Team(teamID, user, name);
            for (Supplier<Group> groupSupplier : Groups.DEFAULT_GROUPS) {
                Group group = groupSupplier.get();
                if (Config.DEBUG) {
                    LogUtils.debug("Adding default group {}!", group.name());
                }
                team.add(TeamValues.GROUPS, group);
            }

            Group ownerGroup = team.values(TeamValues.GROUPS).stream().filter(group -> group.priority() == Group.OWNER_PRIORITY).findFirst().orElse(null);
            if (ownerGroup == null) {
                LogUtils.error("No owner group was set up! Could not create team!");
                return null;
            }

            user.group(ownerGroup);
            user.team(team);
            return team;
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while creating team {}!", name, throwable);
            return null;
        }, AsyncUtils.executor());
    }


    public static CompletionStage<Team> loadTeam(TeamID teamID, LoadContext context) {
        if (Config.DEBUG) {
            LogUtils.debug("Loading team! Thread: {}", Thread.currentThread().getName());
        }
        CompletableFuture<Team> teamFuture = new CompletableFuture<>();
        AsyncUtils.submit(() -> {
            Result<Record> select = DatabaseConnector.getInstance().context()
                    .select()
                    .from(TEAMS)
                    .where(ID.eq(teamID.id()))
                    .limit(1)
                    .fetch();

            if (select.isEmpty()) {
                teamFuture.complete(null);
                return;
            }

            Record record = select.get(0);
            String name = record.get(TEAM_NAME);
            loadUser(record.get(TEAM_LEADER), context, user -> {
                if (Config.DEBUG) {
                    LogUtils.debug("Loaded leader: {}", user);
                }
                com.artillexstudios.axteams.teams.Team team = new com.artillexstudios.axteams.teams.Team(teamID, user, name);
                user.team(team);

                for (TeamValue<?, ?> value : TeamValues.values()) {
                    Result<Record> valueSelect = DatabaseConnector.getInstance().context()
                            .select()
                            .from(value.table())
                            .where(TEAM_ID.eq(teamID.id()))
                            .fetch();

                    if (Config.DEBUG) {
                        LogUtils.debug("Select for value: {}: {}", value.id(), valueSelect);
                    }
                    team.loadAll(value, (List<Identifiable<?>>) value.parse(valueSelect));
                }

                Result<Record> users = DatabaseConnector.getInstance().context()
                        .select()
                        .from(USERS)
                        .where(TEAM_ID.eq(teamID.id()))
                        .fetch();

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (Record rec : users) {
                    UUID uuid = rec.get(USER_UUID);
                    if (uuid.equals(user.player().getUniqueId())) {
                        continue;
                    }

                    futures.add(loadUser(uuid, context).thenAccept(team::add).toCompletableFuture());
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                    Teams.loadWithContext(team, context);
                    teamFuture.complete(team);
                });
            });
        });

        return teamFuture;
    }

    public static CompletableFuture<LongLongPair> saveTeams() {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();
            long saved = 0;

            ObjectArrayList<Team> teams = Teams.unsaved();
            ObjectArrayList<Query> queries = new ObjectArrayList<>(teams.size() * TeamValues.keys().size());
            for (Team team : teams) {
                queries.add(DatabaseConnector.getInstance().context()
                        .update(TEAMS)
                        .set(TEAM_LEADER, team.leader().id())
                        .set(TEAM_NAME, team.name())
                        .where(ID.eq(team.id().id()))
                );

                for (TeamValue<?, ?> value : TeamValues.values()) {
                    queries.addAll(value.save(team, (List) team.rawValues(value)));
                }

                team.clearDeleted();
                saved++;
            }

            DatabaseConnector.getInstance().context()
                    .batch(queries)
                    .execute();

            long took = System.nanoTime() - start;
            return LongLongPair.of(saved, took);
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while saving teams!", throwable);
            return null;
        }, AsyncUtils.executor());
    }

    public static CompletableFuture<LongLongPair> saveUsers() {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();

            ObjectArrayList<User> users = Users.unsaved();
            ObjectArrayList<Query> queries = new ObjectArrayList<>(users.size());
            for (User user : users) {
                OfflinePlayer player = user.player();
                Team team = user.team();
                Group group = user.group();
                queries.add(DatabaseConnector.getInstance().context()
                        .update(USERS)
                        .set(USERNAME, player.getName())
                        .set(TEXTURES, user.textures())
                        .set(LAST_SEEN, player.isOnline() ? System.currentTimeMillis() : user.lastOnline())
                        .set(TEAM_GROUP, group != null ? group.id() : 0)
                        .set(TEAM_ID, team != null ? team.id().id() : 0)
                        .where(ID.eq(user.id()))
                );
            }

            DatabaseConnector.getInstance().context()
                    .batch(queries)
                    .execute();

            long took = System.nanoTime() - start;
            return LongLongPair.of(queries.size(), took);
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while saving users!", throwable);
            return null;
        }, AsyncUtils.executor());
    }

    public static CompletableFuture<Void> disband(Team team) {
        return CompletableFuture.runAsync(() -> {
            DatabaseConnector.getInstance().context()
                    .deleteFrom(TEAMS)
                    .where(ID.eq(team.id().id()))
                    .execute();
        }, AsyncUtils.executor()).exceptionallyAsync(throwable -> {
            LogUtils.error("An unexpected error occurred while disbanding team!", throwable);
            return null;
        }, AsyncUtils.executor());
    }
}
