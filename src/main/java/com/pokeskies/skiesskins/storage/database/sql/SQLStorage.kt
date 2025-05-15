package com.pokeskies.skiesskins.storage.database.sql

import com.google.gson.reflect.TypeToken
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.SkiesSkinsConfig
import com.pokeskies.skiesskins.data.UserData
import com.pokeskies.skiesskins.data.UserSkinData
import com.pokeskies.skiesskins.data.shop.UserShopData
import com.pokeskies.skiesskins.storage.IStorage
import com.pokeskies.skiesskins.storage.StorageType
import com.pokeskies.skiesskins.storage.database.sql.providers.MySQLProvider
import com.pokeskies.skiesskins.storage.database.sql.providers.SQLiteProvider
import java.lang.reflect.Type
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture

class SQLStorage(private val config: SkiesSkinsConfig.Storage) : IStorage {
    private val connectionProvider: ConnectionProvider = when (config.type) {
        StorageType.MYSQL -> MySQLProvider(config)
        StorageType.SQLITE -> SQLiteProvider(config)
        else -> throw IllegalStateException("Invalid storage type!")
    }
    private val inventoryType: Type = object : TypeToken<List<UserSkinData>>() {}.type
    private val shopDataType: Type = object : TypeToken<HashMap<String, UserShopData>>() {}.type

    init {
        connectionProvider.init()
    }

    override fun getUser(uuid: UUID): UserData {
        val userData = UserData(uuid)
        try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                val result = statement.executeQuery(String.format("SELECT * FROM ${config.tablePrefix}userdata WHERE uuid='%s'", uuid.toString()))
                if (result != null && result.next()) {
                    userData.inventory = SkiesSkins.INSTANCE.gson.fromJson(result.getString("inventory"), inventoryType)
                    userData.shopData = SkiesSkins.INSTANCE.gson.fromJson(result.getString("shopData"), shopDataType)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return userData
    }

    override fun saveUser(uuid: UUID, userData: UserData): Boolean {
        return try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                statement.execute(String.format("REPLACE INTO ${config.tablePrefix}userdata (uuid, `inventory`, `shopData`) VALUES ('%s', '%s', '%s')",
                    uuid.toString(),
                    SkiesSkins.INSTANCE.gson.toJsonTree(userData.inventory).asJsonObject,
                    SkiesSkins.INSTANCE.gson.toJsonTree(userData.shopData).asJsonObject
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUserAsync(uuid: UUID): CompletableFuture<UserData?> {
        return CompletableFuture.supplyAsync({
            try {
                val result = getUser(uuid)
                result
            } catch (e: Exception) {
                UserData(uuid)  // Return default data rather than throwing
            }
        }, SkiesSkins.INSTANCE.asyncExecutor)
    }

    override fun saveUserAsync(uuid: UUID, userData: UserData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUser(uuid, userData)
        }, SkiesSkins.INSTANCE.asyncExecutor)
    }

    override fun close() {
        connectionProvider.shutdown()
    }
}
