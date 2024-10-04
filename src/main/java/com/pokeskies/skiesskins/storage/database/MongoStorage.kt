package com.pokeskies.skiesskins.storage.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.connection.ClusterSettings
import com.pokeskies.skiesskins.config.MainConfig
import com.pokeskies.skiesskins.data.UserData
import com.pokeskies.skiesskins.storage.IStorage
import com.pokeskies.skiesskins.utils.Utils
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.io.IOException
import java.util.*

class MongoStorage(config: MainConfig.Storage) : IStorage {
    private var mongoClient: MongoClient? = null
    private var mongoDatabase: MongoDatabase? = null
    private var userdataCollection: MongoCollection<UserData>? = null

    init {
        try {
            val credential = MongoCredential.createCredential(
                config.username,
                config.database,
                config.password.toCharArray()
            )
            var settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)

            settings = if (config.urlOverride.isNotEmpty()) {
                settings.applyConnectionString(ConnectionString(config.urlOverride))
            } else {
                settings
                    .credential(credential)
                    .applyToClusterSettings { builder: ClusterSettings.Builder ->
                        builder.hosts(listOf(ServerAddress(config.host, config.port)))
                    }
            }

            this.mongoClient = MongoClients.create(settings.build())

            val codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(UUIDCodec()),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
            )

            this.mongoDatabase = mongoClient!!.getDatabase(config.database)
                .withCodecRegistry(codecRegistry)
            this.userdataCollection = this.mongoDatabase!!.getCollection("userdata", UserData::class.java)
        } catch (e: Exception) {
            throw IOException("Error while attempting to setup Mongo Database: $e")
        }

    }

    override fun getUser(uuid: UUID): UserData? {
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to fetch data from the Mongo database!")
            return UserData()
        }
        return userdataCollection?.find(Filters.eq("uuid", uuid))?.first()
    }

    override fun saveUser(uuid: UUID, userData: UserData): Boolean {
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to save data to the Mongo database!")
            return false
        }
        val query = Filters.eq("uuid", uuid)
        val result = this.userdataCollection?.replaceOne(query, userData, ReplaceOptions().upsert(true))

        return result?.wasAcknowledged() ?: false
    }

    override fun close() {
        mongoClient?.close()
    }
}