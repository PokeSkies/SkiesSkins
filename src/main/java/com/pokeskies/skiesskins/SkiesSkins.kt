package com.pokeskies.skiesskins

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.pokeskies.skiesskins.commands.BaseCommand
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.gui.actions.Action
import com.pokeskies.skiesskins.config.gui.actions.ActionType
import com.pokeskies.skiesskins.config.gui.actions.ClickType
import com.pokeskies.skiesskins.placeholders.PlaceholderManager
import com.pokeskies.skiesskins.storage.IStorage
import com.pokeskies.skiesskins.storage.StorageType
import com.pokeskies.skiesskins.utils.ImpactorService
import com.pokeskies.skiesskins.utils.Utils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files

class SkiesSkins : ModInitializer {
    companion object {
        lateinit var INSTANCE: SkiesSkins
        val LOGGER = LogManager.getLogger("skiesskins")
    }

    lateinit var configDir: File
    lateinit var configManager: ConfigManager
    var storage: IStorage? = null

    var adventure: FabricServerAudiences? = null
    var server: MinecraftServer? = null

    var economyService: ImpactorService? = null
    lateinit var placeholderManager: PlaceholderManager
    lateinit var shopManager: ShopManager

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeAdapter(Action::class.java, ActionType.ActionTypeAdaptor())
        .registerTypeAdapter(ClickType::class.java, ClickType.ClickTypeAdaptor())
        .registerTypeAdapter(StorageType::class.java, StorageType.StorageTypeAdaptor())
        .registerTypeAdapter(ResourceLocation::class.java, Utils.ResourceLocationSerializer())
        .registerTypeHierarchyAdapter(Item::class.java, Utils.RegistrySerializer(BuiltInRegistries.ITEM))
        .registerTypeHierarchyAdapter(SoundEvent::class.java, Utils.RegistrySerializer(BuiltInRegistries.SOUND_EVENT))
        .registerTypeHierarchyAdapter(CompoundTag::class.java, Utils.CodecSerializer(CompoundTag.CODEC))
        .create()

    var gsonPretty: Gson = gson.newBuilder().setPrettyPrinting().create()

    override fun onInitialize() {
        INSTANCE = this

        this.configDir = File(FabricLoader.getInstance().configDirectory, "skiesskins")
        this.configManager = ConfigManager(configDir)
        this.storage = IStorage.load(ConfigManager.CONFIG.storage)

        this.placeholderManager = PlaceholderManager()

        try {
            this.economyService = ImpactorService()
        } catch (ex: IllegalArgumentException) {
            LOGGER.error("The provided currency '${null}' was not a valid Impactor Currency!}")
        } catch (ex: Exception) {
            LOGGER.error("There was an exception while initializing the Impactor Economy Service. Is it loaded?")
        }

        this.shopManager = ShopManager()

        ServerLifecycleEvents.SERVER_STARTING.register(ServerStarting { server ->
            this.adventure = FabricServerAudiences.of(
                server
            )
            this.server = server
        })
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerStopped { _ ->
            this.adventure = null
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(
                dispatcher
            )
        }
    }

    fun reload() {
        this.configManager.reload()
        this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        this.shopManager.reload()
        this.placeholderManager = PlaceholderManager()
    }

    fun <T : Any> loadFile(filename: String, default: T, create: Boolean = false): T {
        val file = File(configDir, filename)
        var value: T = default
        try {
            Files.createDirectories(configDir.toPath())
            if (file.exists()) {
                FileReader(file).use { reader ->
                    val jsonReader = JsonReader(reader)
                    value = gsonPretty.fromJson(jsonReader, default::class.java)
                }
            } else if (create) {
                Files.createFile(file.toPath())
                FileWriter(file).use { fileWriter ->
                    fileWriter.write(gsonPretty.toJson(default))
                    fileWriter.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }

    fun <T> saveFile(filename: String, `object`: T): Boolean {
        val file = File(configDir, filename)
        try {
            FileWriter(file).use { fileWriter ->
                fileWriter.write(gsonPretty.toJson(`object`))
                fileWriter.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }
}
