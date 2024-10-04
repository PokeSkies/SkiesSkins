package com.pokeskies.skiesskins.config

import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.gui.*
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.utils.Utils
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.lang.NullPointerException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors

class ConfigManager(val configDir: File) {
    companion object {
        lateinit var CONFIG: MainConfig
        lateinit var INVENTORY_GUI: InventoryConfig
        lateinit var APPLY_GUI: ApplyConfig
        lateinit var REMOVER_GUI: RemoverConfig
        lateinit var SCRAP_CONFIRM_GUI: ScrapConfirmConfig
        lateinit var PURCHASE_CONFIRM_GUI: PurchaseConfirmConfig
        var SKINS: MutableMap<String, SkinConfig> = mutableMapOf()
        var SHOPS: MutableMap<String, ShopConfig> = mutableMapOf()
    }

    init {
        reload()
    }

    fun reload() {
        copyDefaults()

        CONFIG = SkiesSkins.INSTANCE.loadFile("config.json", MainConfig())
        INVENTORY_GUI = SkiesSkins.INSTANCE.loadFile("guis/inventory.json", InventoryConfig())
        APPLY_GUI = SkiesSkins.INSTANCE.loadFile("guis/apply.json", ApplyConfig())
        REMOVER_GUI = SkiesSkins.INSTANCE.loadFile("guis/remover.json", RemoverConfig())
        SCRAP_CONFIRM_GUI = SkiesSkins.INSTANCE.loadFile("guis/scrap_confirm.json", ScrapConfirmConfig())
        PURCHASE_CONFIRM_GUI = SkiesSkins.INSTANCE.loadFile("guis/purchase_confirm.json", PurchaseConfirmConfig())

        loadSkins()
        loadShops()
    }

    private fun copyDefaults() {
        val classLoader = SkiesSkins::class.java.classLoader
        configDir.mkdirs()

        copyDefaultFile(classLoader, "config.json")
        copyDefaultFile(classLoader, "guis/inventory.json")
        copyDefaultFile(classLoader, "guis/apply.json")
        copyDefaultFile(classLoader, "guis/remover.json")
        copyDefaultFile(classLoader, "guis/scrap_confirm.json")
        copyDefaultFile(classLoader, "guis/purchase_confirm.json")

        copyDefaultDirectory(classLoader, "skins")
        copyDefaultDirectory(classLoader, "shops")
    }

    private fun copyDefaultFile(classLoader: ClassLoader, fileName: String) {
        val file = configDir.resolve(fileName)
        if (!file.exists()) {
            try {
                val stream = classLoader.getResourceAsStream("assets/skiesskins/$fileName")
                    ?: throw NullPointerException("File not found $fileName")

                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                SkiesSkins.LOGGER.error("Failed to copy the default file '$fileName': $e")
            }
        }
    }

    private fun copyDefaultDirectory(classLoader: ClassLoader, directoryName: String) {
        val directory = configDir.resolve(directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
            try {
                val sourceUrl = classLoader.getResource("assets/skiesskins/$directoryName")
                    ?: throw NullPointerException("Directory not found $directoryName")
                val sourcePath = Paths.get(sourceUrl.toURI())

                Files.walk(sourcePath).use { stream ->
                    stream.filter { Files.isRegularFile(it) }
                        .forEach { sourceFile ->
                            val destinationFile = directory.resolve(sourcePath.relativize(sourceFile).toString())
                            Files.copy(sourceFile, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                }
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default directory '$directoryName': " + e.message)
            }
        }
    }

    private fun loadSkins() {
        SKINS = mutableMapOf()

        val dir = configDir.resolve("skins")
        if (dir.exists() && dir.isDirectory) {
            val files = Files.walk(dir.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())
            if (files != null) {
                SkiesSkins.LOGGER.info("Found ${files.size} Skin files: ${files.map { it.name }}")
                val enabledFiles = mutableListOf<String>()
                for (file in files) {
                    val fileName = file.name
                    if (file.isFile && fileName.contains(".json")) {
                        val id = fileName.substring(0, fileName.lastIndexOf(".json"))
                        val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        try {
                            val config = SkiesSkins.INSTANCE.gsonPretty.fromJson(JsonParser.parseReader(jsonReader), SkinConfig::class.java)
                            if (config.enabled) {
                                SKINS[id] = config
                                enabledFiles.add(fileName)
                            }
                        } catch (ex: Exception) {
                            Utils.printError("Error while trying to parse the skin $fileName!")
                            ex.printStackTrace()
                        }
                    } else {
                        Utils.printError("File $fileName is either not a file or is not a .json file!")
                    }
                }
                Utils.printInfo("Successfully read and loaded the following enabled skin files: $enabledFiles")
            }
        } else {
            Utils.printError("The 'skins' directory either does not exist or is not a directory!")
        }
    }

    private fun loadShops() {
        SHOPS = mutableMapOf()

        val dir = configDir.resolve("shops")
        if (dir.exists() && dir.isDirectory) {
            val files = Files.walk(dir.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())
            if (files != null) {
                SkiesSkins.LOGGER.info("Found ${files.size} Shop files: ${files.map { it.name }}")
                val enabledFiles = mutableListOf<String>()
                for (file in files) {
                    val fileName = file.name
                    if (file.isFile && fileName.contains(".json")) {
                        val id = fileName.substring(0, fileName.lastIndexOf(".json"))
                        val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        try {
                            val config = SkiesSkins.INSTANCE.gsonPretty.fromJson(JsonParser.parseReader(jsonReader), ShopConfig::class.java)
                            if (config.options.enabled) {
                                SHOPS[id] = config
                                enabledFiles.add(fileName)
                            }
                        } catch (ex: Exception) {
                            Utils.printError("Error while trying to parse the shop $fileName!")
                            ex.printStackTrace()
                        }
                    } else {
                        Utils.printError("File $fileName is either not a file or is not a .json file!")
                    }
                }
                Utils.printInfo("Successfully read and loaded the following enabled shop files: $enabledFiles")
            }
        } else {
            Utils.printError("The 'skins' directory either does not exist or is not a directory!")
        }
    }
}