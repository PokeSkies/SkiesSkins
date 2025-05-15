package com.pokeskies.skiesskins.config

import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.gui.*
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.utils.Utils
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors

object ConfigManager {
    private var assetPackage = "assets/${SkiesSkins.MOD_ID}"

    lateinit var CONFIG: SkiesSkinsConfig

    // GUIs!
    lateinit var INVENTORY_GUI: InventoryGuiConfig
    lateinit var APPLY_GUI: ApplyGuiConfig
    lateinit var REMOVER_GUI: RemoverGuiConfig
    lateinit var SCRAP_CONFIRM_GUI: ScrapConfirmGuiConfig
    lateinit var PURCHASE_CONFIRM_GUI: PurchaseConfirmGuiConfig

    var SKINS: MutableMap<String, SkinConfig> = mutableMapOf()
    var SHOPS: MutableMap<String, ShopConfig> = mutableMapOf()

    fun load() {
        copyDefaults()

        CONFIG = loadFile("config.json", SkiesSkinsConfig())

        INVENTORY_GUI = loadFile("guis/inventory.json", InventoryGuiConfig())
        APPLY_GUI = loadFile("guis/apply.json", ApplyGuiConfig())
        REMOVER_GUI = loadFile("guis/remover.json", RemoverGuiConfig())
        SCRAP_CONFIRM_GUI = loadFile("guis/scrap_confirm.json", ScrapConfirmGuiConfig())
        PURCHASE_CONFIRM_GUI = loadFile("guis/purchase_confirm.json", PurchaseConfirmGuiConfig())

        loadSkins()
        loadShops()
    }

    private fun copyDefaults() {
        val classLoader = SkiesSkins::class.java.classLoader

        SkiesSkins.INSTANCE.configDir.mkdirs()

        attemptDefaultFileCopy(classLoader, "config.json")

        attemptDefaultFileCopy(classLoader, "guis/inventory.json")
        attemptDefaultFileCopy(classLoader, "guis/apply.json")
        attemptDefaultFileCopy(classLoader, "guis/remover.json")
        attemptDefaultFileCopy(classLoader, "guis/scrap_confirm.json")
        attemptDefaultFileCopy(classLoader, "guis/purchase_confirm.json")

        attemptDefaultDirectoryCopy(classLoader, "skins")
        attemptDefaultDirectoryCopy(classLoader, "shops")
    }

    private fun attemptDefaultFileCopy(classLoader: ClassLoader, fileName: String) {
        val file = SkiesSkins.INSTANCE.configDir.resolve(fileName)
        if (!file.exists()) {
            file.mkdirs()
            try {
                val stream = classLoader.getResourceAsStream("${assetPackage}/$fileName")
                    ?: throw NullPointerException("File not found $fileName")

                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default file '$fileName': $e")
            }
        }
    }

    private fun attemptDefaultDirectoryCopy(classLoader: ClassLoader, directoryName: String) {
        val directory = SkiesSkins.INSTANCE.configDir.resolve(directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
            try {
                val sourceUrl = classLoader.getResource("${assetPackage}/$directoryName")
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

        val dir = SkiesSkins.INSTANCE.configDir.resolve("skins")
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

        val dir = SkiesSkins.INSTANCE.configDir.resolve("shops")
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

    fun <T : Any> loadFile(filename: String, default: T, path: String = "", create: Boolean = false): T {
        var dir = SkiesSkins.INSTANCE.configDir
        if (path.isNotEmpty()) {
            dir = dir.resolve(path)
        }
        val file = File(dir, filename)
        var value: T = default
        try {
            Files.createDirectories(SkiesSkins.INSTANCE.configDir.toPath())
            if (file.exists()) {
                FileReader(file).use { reader ->
                    val jsonReader = JsonReader(reader)
                    value = SkiesSkins.INSTANCE.gsonPretty.fromJson(jsonReader, default::class.java)
                }
            } else if (create) {
                Files.createFile(file.toPath())
                FileWriter(file).use { fileWriter ->
                    fileWriter.write(SkiesSkins.INSTANCE.gsonPretty.toJson(default))
                    fileWriter.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }

    fun <T> saveFile(filename: String, `object`: T): Boolean {
        val dir = SkiesSkins.INSTANCE.configDir
        val file = File(dir, filename)
        try {
            FileWriter(file).use { fileWriter ->
                fileWriter.write(SkiesSkins.INSTANCE.gsonPretty.toJson(`object`))
                fileWriter.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }
}
