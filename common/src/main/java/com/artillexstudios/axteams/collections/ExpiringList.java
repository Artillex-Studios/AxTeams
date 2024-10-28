package com.artillexstudios.axteams.collections;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public final class ExpiringList<T> {
    private final Object2LongArrayMap<T> cooldowns = new Object2LongArrayMap<>();
    private final long duration;
    private long lastCheck = 0L;

    public ExpiringList(long duration) {
        this.duration = duration;
    }

    public void add(T key) {
        this.doHouseKeeping();
        this.cooldowns.put(key, System.currentTimeMillis() + this.duration);
    }

    public boolean contains(T key) {
        this.doHouseKeeping();
        return this.cooldowns.containsKey(key);
    }

    public void clear() {
        this.cooldowns.clear();
    }

    public void remove(T key) {
        this.doHouseKeeping();
        this.cooldowns.removeLong(key);
    }

    private void doHouseKeeping() {
        if (System.currentTimeMillis() - this.lastCheck >= this.duration) {
            ObjectIterator<Object2LongMap.Entry<T>> iterator = this.cooldowns.object2LongEntrySet().iterator();
            long time = this.lastCheck = System.currentTimeMillis();

            while (iterator.hasNext()) {
                Object2LongMap.Entry<T> entry = iterator.next();
                if (time >= entry.getLongValue()) {
                    iterator.remove();
                }
            }

        }
    }
}
