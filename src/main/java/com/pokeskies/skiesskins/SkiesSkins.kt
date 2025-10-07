package com.pokeskies.skiesskins

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pokeskies.skiesskins.commands.AliasCommands
import com.pokeskies.skiesskins.commands.BaseCommand
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.gui.actions.Action
import com.pokeskies.skiesskins.config.gui.actions.ActionType
import com.pokeskies.skiesskins.economy.EconomyManager
import com.pokeskies.skiesskins.gui.GenericClickType
import com.pokeskies.skiesskins.gui.InventoryType
import com.pokeskies.skiesskins.placeholders.PlaceholderManager
import com.pokeskies.skiesskins.storage.IStorage
import com.pokeskies.skiesskins.storage.StorageType
import com.pokeskies.skiesskins.utils.IRefreshableGui
import com.pokeskies.skiesskins.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SkiesSkins : ModInitializer {
    companion object {
        lateinit var INSTANCE: SkiesSkins

        var MOD_ID = "skiesskins"
        var MOD_NAME = "SkiesSkins"

        val LOGGER = LogManager.getLogger(MOD_ID)

        val asyncScope = CoroutineScope(Dispatchers.IO)
    }

    lateinit var configDir: File
    var storage: IStorage? = null

    var adventure: FabricServerAudiences? = null
    lateinit var server: MinecraftServer
    lateinit var nbtOpts: RegistryOps<Tag>

    lateinit var economyManager: EconomyManager
    lateinit var placeholderManager: PlaceholderManager
    lateinit var shopManager: ShopManager

    val asyncExecutor: ExecutorService = Executors.newFixedThreadPool(8, ThreadFactoryBuilder()
        .setNameFormat("SkiesSkins-Async-%d")
        .setDaemon(true)
        .build())

    var inventoryInstances: MutableMap<UUID, IRefreshableGui> = mutableMapOf()

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeAdapter(Action::class.java, ActionType.Adapter())
        .registerTypeAdapter(GenericClickType::class.java, GenericClickType.Adapter())
        .registerTypeAdapter(StorageType::class.java, StorageType.Adapter())
        .registerTypeAdapter(InventoryType::class.java, InventoryType.Adapter())
        .registerTypeAdapter(ResourceLocation::class.java, Utils.ResourceLocationSerializer())
        .registerTypeHierarchyAdapter(Item::class.java, Utils.RegistrySerializer(BuiltInRegistries.ITEM))
        .registerTypeHierarchyAdapter(SoundEvent::class.java, Utils.RegistrySerializer(BuiltInRegistries.SOUND_EVENT))
        .registerTypeHierarchyAdapter(CompoundTag::class.java, Utils.CodecSerializer(CompoundTag.CODEC))
        .create()

    var gsonPretty: Gson = gson.newBuilder().setPrettyPrinting().create()

    override fun onInitialize() {
        INSTANCE = this

        this.configDir = File(FabricLoader.getInstance().configDirectory, "skiesskins")
        ConfigManager.load()
        this.storage = IStorage.load(ConfigManager.CONFIG.storage)

        this.economyManager = EconomyManager()

        this.shopManager = ShopManager()

        ServerLifecycleEvents.SERVER_STARTING.register(ServerStarting { server ->
            this.adventure = FabricServerAudiences.of(
                server
            )
            this.server = server
            this.nbtOpts = server.registryAccess().createSerializationContext(NbtOps.INSTANCE)
            this.placeholderManager = PlaceholderManager()
        })
        ServerLifecycleEvents.SERVER_STOPPED.register(ServerStopped { _ ->
            this.adventure = null
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(
                dispatcher
            )
            AliasCommands().register(
                dispatcher
            )
        }
    }

    fun reload() {
        ConfigManager.load()
        this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        this.shopManager.reload(ConfigManager.CONFIG.ticksPerUpdate)
        this.placeholderManager = PlaceholderManager()
        this.economyManager = EconomyManager()

        // Reset all players with active GUIs
        this.inventoryInstances.forEach { (uuid, gui) ->
            this.server.playerList?.getPlayer(uuid)?.let { player ->
                gui.close()
            }
        }
        this.inventoryInstances.clear()
    }
}
