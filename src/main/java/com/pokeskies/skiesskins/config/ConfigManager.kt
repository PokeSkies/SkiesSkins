package com.pokeskies.skiesskins.config

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.gui.ApplyConfig
import com.pokeskies.skiesskins.config.gui.InventoryConfig
import com.pokeskies.skiesskins.utils.Utils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors




class ConfigManager(val configDir: File) {
    companion object {
        lateinit var CONFIG: MainConfig
        lateinit var INVENTORY_GUI: InventoryConfig
        lateinit var APPLY_GUI: ApplyConfig
        var SKINS: BiMap<String, SkinConfig> = HashBiMap.create()
        var PACKAGES: BiMap<String, String> = HashBiMap.create()
    }

    init {
        reload()
    }

    fun reload() {
        copyDefaults()
        CONFIG = SkiesSkins.INSTANCE.loadFile("config.json", MainConfig())
        INVENTORY_GUI = SkiesSkins.INSTANCE.loadFile("guis/inventory.json", InventoryConfig())
        APPLY_GUI = SkiesSkins.INSTANCE.loadFile("guis/apply.json", ApplyConfig())
        loadSkins()
    }

    fun copyDefaults() {
        val classLoader = SkiesSkins::class.java.classLoader

        configDir.mkdirs()

        // Main Config
        val configFile = configDir.resolve("config.json")
        if (!configFile.exists()) {
            try {
                val inputStream: InputStream = classLoader.getResourceAsStream("assets/skiesskins/config.json")
                Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                SkiesSkins.LOGGER.error("Failed to copy the default config file: $e")
            }
        }

        // Inventory GUI Config
        val inventoryFile = configDir.resolve("guis/inventory.json")
        if (!inventoryFile.exists()) {
            inventoryFile.mkdirs()
            try {
                val inputStream: InputStream = classLoader.getResourceAsStream("assets/skiesskins/guis/inventory.json")
                Files.copy(inputStream, inventoryFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                SkiesSkins.LOGGER.error("Failed to copy the default inventory GUI file: $e")
            }
        }

        // Apply GUI Config
        val applyFile = configDir.resolve("guis/apply.json")
        if (!applyFile.exists()) {
            applyFile.mkdirs()
            try {
                val inputStream: InputStream = classLoader.getResourceAsStream("assets/skiesskins/guis/apply.json")
                Files.copy(inputStream, applyFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                SkiesSkins.LOGGER.error("Failed to copy the default apply GUI file: $e")
            }
        }

        // If the 'skins' directory does not exist, create it and copy the default example skin
        val skinsDirectory = configDir.resolve("skins")
        if (!skinsDirectory.exists()) {
            skinsDirectory.mkdirs()
            val file = skinsDirectory.resolve("pikachu_example.json")
            try {
                val resourceFile: Path =
                    Path.of(classLoader.getResource("assets/skiesskins/skins/pikachu_example.json").toURI())
                Files.copy(resourceFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default skins file: " + e.message)
            }
        }

        // If the 'packages' directory does not exist, create it and copy the default example package
        val packagesDirectory = configDir.resolve("packages")
        if (!packagesDirectory.exists()) {
            packagesDirectory.mkdirs()
            val file = packagesDirectory.resolve("example_package.json")
            try {
                val resourceFile: Path =
                    Path.of(classLoader.getResource("assets/skiesskins/packages/example_package.json").toURI())
                Files.copy(resourceFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default packages file: " + e.message)
            }
        }

        // If the 'shops' directory does not exist, create it and copy the default example shop
        val shopsDirectory = configDir.resolve("shops")
        if (!shopsDirectory.exists()) {
            shopsDirectory.mkdirs()
            val file = shopsDirectory.resolve("example_shop.json")
            try {
                val resourceFile: Path =
                    Path.of(classLoader.getResource("assets/skiesskins/shops/example_shop.json").toURI())
                Files.copy(resourceFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default shops file: " + e.message)
            }
        }
    }

    private fun loadSkins() {
        SKINS.clear()

        val dir = configDir.resolve("skins")
        if (dir.exists() && dir.isDirectory) {
            val files = Files.walk(dir.toPath())
                .filter { path: Path -> Files.isRegularFile(path) }
                .map { it.toFile() }
                .collect(Collectors.toList())
            if (files != null) {
                SkiesSkins.LOGGER.info("Found ${files.size} Skin files: ${files.map { it.name }}")
                for (file in files) {
                    val fileName = file.name
                    if (file.isFile && fileName.contains(".json")) {
                        val id = fileName.substring(0, fileName.lastIndexOf(".json"))
                        val jsonReader = JsonReader(InputStreamReader(FileInputStream(file), Charsets.UTF_8))
                        try {
                            SKINS[id] = SkiesSkins.INSTANCE.gsonPretty.fromJson(JsonParser.parseReader(jsonReader), SkinConfig::class.java)
                            Utils.printInfo("Successfully read and loaded the skin $fileName!")
                        } catch (ex: Exception) {
                            Utils.printError("Error while trying to parse the skin $fileName!")
                            ex.printStackTrace()
                        }
                    } else {
                        Utils.printError("File $fileName is either not a file or is not a .json file!")
                    }
                }
            }
        } else {
            Utils.printError("The 'skins' directory either does not exist or is not a directory!")
        }
    }
}