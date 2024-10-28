package com.artillexstudios.axteams.database;

import com.artillexstudios.axapi.utils.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axteams.config.Config;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class TeamSaver {
    private final ScheduledExecutorService service;
    private ScheduledFuture<?> future;

    public TeamSaver() {
        this.service = new ExceptionReportingScheduledThreadPool(1, runnable -> new Thread(runnable, "AxTeams-Autosave-Thread"));
    }

    public void start() {
        if (this.future != null) {
            LogUtils.error("Future was not cancelled, but it's loading!");
            return;
        }

        this.future = this.service.scheduleAtFixedRate(() -> {
            DataHandler.saveTeams().thenAccept(pair -> {
                if (Config.DEBUG) {
                    LogUtils.debug("Saved {} teams in {} ms!", pair.firstLong(), pair.secondLong() / 1_000_000L);
                }
            }).exceptionallyAsync(throwable -> {
                LogUtils.error("An unexpected error occurred while saving teams!", throwable);
                return null;
            });

            DataHandler.saveUsers().thenAccept(pair -> {
                if (Config.DEBUG) {
                    LogUtils.debug("Saved {} users in {} ms!", pair.firstLong(), pair.secondLong() / 1_000_000L);
                }
            }).exceptionallyAsync(throwable -> {
                LogUtils.error("An unexpected error occurred while saving teams!", throwable);
                return null;
            });
        }, 0, Config.AUTOSAVE_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        if (this.future != null && !this.future.isCancelled()) {
            this.future.cancel(false);
            this.future = null;

            DataHandler.saveTeams().toCompletableFuture().thenAccept(pair -> {
                if (Config.DEBUG) {
                    LogUtils.debug("Saved {} teams in {} ms!", pair.firstLong(), pair.secondLong() / 1_000_000L);
                }
            }).join();

            DataHandler.saveUsers().toCompletableFuture().thenAccept(pair -> {
                if (Config.DEBUG) {
                    LogUtils.debug("Saved {} users in {} ms!", pair.firstLong(), pair.secondLong() / 1_000_000L);
                }
            }).join();
        }
    }
}
