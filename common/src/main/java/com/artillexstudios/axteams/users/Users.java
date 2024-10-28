package com.artillexstudios.axteams.users;

import com.artillexstudios.axteams.api.LoadContext;
import com.artillexstudios.axteams.api.users.User;
import com.artillexstudios.axteams.database.DataHandler;
import com.artillexstudios.axteams.exception.UserAlreadyLoadedException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public final class Users {
    private static final ConcurrentHashMap<UUID, User> loadedUsers = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<User> unsaved = new ConcurrentLinkedQueue<>();
    private static final Cache<UUID, User> tempUsers = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    public static User getUserIfLoadedImmediately(UUID uuid) {
        User user = loadedUsers.get(uuid);
        if (user != null) {
            return user;
        }

        user = tempUsers.getIfPresent(uuid);
        if (user != null) {
            return user;
        }

        Optional<User> unsavedUser = unsaved.stream().filter(u -> u.player().getUniqueId().equals(uuid)).findAny();

        if (unsavedUser.isPresent()) {
            user = unsavedUser.get();
            tempUsers.put(uuid, user);
        }

        return user;
    }

    public static CompletableFuture<User> loadUser(UUID uuid) throws UserAlreadyLoadedException {
        if (loadedUsers.containsKey(uuid)) {
            throw new UserAlreadyLoadedException();
        }

        User user = tempUsers.getIfPresent(uuid);
        if (user != null) {
            tempUsers.invalidate(uuid);
            loadedUsers.put(uuid, user);
            return CompletableFuture.completedFuture(user);
        }

        return getUser(uuid, LoadContext.FULL);
    }

    public static void loadWithContext(User user, LoadContext context) {
        switch (context) {
            case FULL -> loadedUsers.put(user.player().getUniqueId(), user);
            case TEMPORARY -> tempUsers.put(user.player().getUniqueId(), user);
        }
    }

    public static CompletableFuture<User> getUser(UUID uuid, LoadContext loadContext) {
        User user = getUserIfLoadedImmediately(uuid);
        if (user != null) {
            return CompletableFuture.completedFuture(user);
        }

        return DataHandler.loadUser(uuid, loadContext).toCompletableFuture();
    }

    public static User disconnect(UUID uuid) {
        User user = loadedUsers.remove(uuid);

        if (user != null) {
            tempUsers.put(uuid, user);
        }

        return user;
    }

    public static void markUnsaved(User user) {
        if (unsaved.contains(user)) {
            return;
        }

        unsaved.add(user);
    }

    public static ObjectArrayList<User> unsaved() {
        ObjectArrayList<User> users = new ObjectArrayList<>(unsaved);
        unsaved.clear();
        return users;
    }
}
