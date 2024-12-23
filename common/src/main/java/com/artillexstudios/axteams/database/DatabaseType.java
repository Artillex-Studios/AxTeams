package com.artillexstudios.axteams.database;

import com.artillexstudios.axteams.config.Config;
import com.artillexstudios.axteams.utils.FileUtils;
import com.zaxxer.hikari.HikariConfig;
import org.jooq.SQLDialect;

public enum DatabaseType {
    H2(SQLDialect.H2) {
        @Override
        public HikariConfig getConfig() {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
            hikariConfig.setPoolName("axteams-database-pool");
            hikariConfig.setMaximumPoolSize(Config.Database.Pool.maximumPoolSize);
            hikariConfig.addDataSourceProperty("url", "jdbc:h2:./" + FileUtils.PLUGIN_DIRECTORY.toFile() + "/data;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;IGNORECASE=TRUE");
            return hikariConfig;
        }
    },
    SQLITE(SQLDialect.SQLITE) {
        @Override
        public HikariConfig getConfig() {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setPoolName("axteams-database-pool");
            hikariConfig.setMaximumPoolSize(Config.Database.Pool.maximumPoolSize);
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + FileUtils.PLUGIN_DIRECTORY.toFile() + "/data");
            return hikariConfig;
        }
    },
    MySQL(SQLDialect.MYSQL) {
        @Override
        public HikariConfig getConfig() {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setPoolName("axteams-database-pool");
            hikariConfig.setMaximumPoolSize(Config.Database.Pool.maximumPoolSize);
            hikariConfig.setMinimumIdle(Config.Database.Pool.minimumIdle);
            hikariConfig.setMaxLifetime(Config.Database.Pool.maximumLifetime);
            hikariConfig.setKeepaliveTime(Config.Database.Pool.keepaliveTime);
            hikariConfig.setConnectionTimeout(Config.Database.Pool.connectionTimeout);

            hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
            hikariConfig.setJdbcUrl("jdbc:mysql://" + Config.Database.address + ":" + Config.Database.port + "/" + Config.Database.database);
            hikariConfig.addDataSourceProperty("user", Config.Database.username);
            hikariConfig.addDataSourceProperty("password", Config.Database.password);
            return hikariConfig;
        }
    };

    public static final DatabaseType[] entries = DatabaseType.values();
    private final SQLDialect dialect;

    DatabaseType(SQLDialect dialect) {
        this.dialect = dialect;
    }

    public static DatabaseType parse(String name) {
        for (DatabaseType entry : entries) {
            if (entry.name().equalsIgnoreCase(name)) {
                return entry;
            }
        }

        return DatabaseType.H2;
    }

    public SQLDialect getType() {
        return this.dialect;
    }

    public abstract HikariConfig getConfig();
}
