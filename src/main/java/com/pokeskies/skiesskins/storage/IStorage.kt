package com.pokeskies.skiesskins.storage

import com.pokeskies.skiesskins.config.SkiesSkinsConfig
import com.pokeskies.skiesskins.data.UserData
import com.pokeskies.skiesskins.storage.database.MongoStorage
import com.pokeskies.skiesskins.storage.file.FileStorage
import java.util.*

interface IStorage {
    companion object {
        fun load(config: SkiesSkinsConfig.Storage): IStorage {
            return when (config.type) {
                StorageType.JSON -> FileStorage()
                StorageType.MONGO -> MongoStorage(config)
            }
        }
    }

    fun getUser(uuid: UUID): UserData?

    fun saveUser(uuid: UUID, userData: UserData): Boolean

    fun close() {}
}
