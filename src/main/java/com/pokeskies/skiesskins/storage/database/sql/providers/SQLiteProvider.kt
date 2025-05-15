package com.pokeskies.skiesskins.storage.database.sql.providers

import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.SkiesSkinsConfig
import com.zaxxer.hikari.HikariConfig
import java.io.File

class SQLiteProvider(config: SkiesSkinsConfig.Storage) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:sqlite:%s",
        File(SkiesSkins.INSTANCE.configDir, "storage.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.sqlite.JDBC"
    override fun getDriverName(): String = "sqlite"
    override fun configure(config: HikariConfig) {}
}
