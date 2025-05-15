package com.pokeskies.skiesskins.storage.database.sql.providers

import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.SkiesSkinsConfig
import com.zaxxer.hikari.HikariConfig
import java.io.File

class H2Provider(config: SkiesSkinsConfig.Storage) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:h2:%s;AUTO_SERVER=TRUE",
        File(SkiesSkins.INSTANCE.configDir, "storage.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.h2.Driver"
    override fun getDriverName(): String = "h2"
    override fun configure(config: HikariConfig) {}
}
