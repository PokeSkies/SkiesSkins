package com.pokeskies.skiesskins.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.storage.StorageType

class MainConfig(
    var debug: Boolean = false,
    val timezone: String = "",
    val storage: Storage = Storage(),
    @SerializedName("find_equivalent")
    val findEquivalent: Boolean = false,
    val untradable: Boolean = true,
    @SerializedName("ticks_per_update")
    val ticksPerUpdate: Int = 20,
) {
    class Storage(
        val type: StorageType = StorageType.JSON,
        val host: String = "",
        val port: Int = 3306,
        val database: String = "skiesskins",
        val username: String = "root",
        val password: String = "",
        val properties: Map<String, String> = mapOf("useUnicode" to "true", "characterEncoding" to "utf8"),
        @SerializedName("pool_settings")
        val poolSettings: PoolSettings = PoolSettings(),
        @SerializedName("url_override")
        val urlOverride: String = ""
    ) {
        class PoolSettings(
            @SerializedName("maximum_pool_size")
            val maximumPoolSize: Int = 10,
            @SerializedName("minimum_idle")
            val minimumIdle: Int = 10,
            @SerializedName("keepalive_time")
            val keepaliveTime: Long = 0,
            @SerializedName("connection_timeout")
            val connectionTimeout: Long = 30000,
            @SerializedName("idle_timeout")
            val idleTimeout: Long = 600000,
            @SerializedName("max_lifetime")
            val maxLifetime: Long = 1800000
        ) {
            override fun toString(): String {
                return "PoolSettings(maximumPoolSize=$maximumPoolSize, minimumIdle=$minimumIdle, " +
                        "keepaliveTime=$keepaliveTime, connectionTimeout=$connectionTimeout, " +
                        "idleTimeout=$idleTimeout, maxLifetime=$maxLifetime)"
            }
        }

        override fun toString(): String {
            return "Storage(type=$type, host='$host', port=$port, database='$database', " +
                    "username='$username', password='$password', properties=$properties, " +
                    "poolSettings=$poolSettings, urlOverride='$urlOverride')"
        }

    }

    class ShopSettings(
        val currency: CurrencySettings = CurrencySettings(),
        val skins: SkinSettings = SkinSettings(),
        val packageSettings: PackageSettings = PackageSettings()
    ) {
        class CurrencySettings(
            val id: String = ""
        ) {
            override fun toString(): String {
                return "CurrencySettings(id='$id')"
            }
        }

        class SkinSettings(
            val amount: Int = 0,
            val random: Map<String, RandomOptions> = emptyMap(),
            val resetTimes: List<String> = emptyList()
        ) {
            class RandomOptions(
                val price: Int = 0,
                val weight: Int = 0
            ) {
                override fun toString(): String {
                    return "RandomOptions(price=$price, weight=$weight)"
                }
            }

            override fun toString(): String {
                return "SkinSettings(amount=$amount, random=$random, resetTimes=$resetTimes)"
            }
        }

        class PackageSettings(
            val id: String = "",
            val endTime: String = ""
        ) {
            override fun toString(): String {
                return "PackageSettings(id='$id', endTime='$endTime')"
            }
        }
    }

    override fun toString(): String {
        return "MainConfig(debug=$debug, timezone='$timezone', storage=$storage, findEquivalent=$findEquivalent, untradable=$untradable, ticksPerUpdate=$ticksPerUpdate)"
    }
}