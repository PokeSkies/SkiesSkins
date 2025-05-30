package com.pokeskies.skiesskins.gui

import ca.landonjw.gooeylibs2.api.UIManager
import ca.landonjw.gooeylibs2.api.button.GooeyButton
import ca.landonjw.gooeylibs2.api.page.PageAction
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.api.shop.PackageEntry
import com.pokeskies.skiesskins.api.shop.RandomEntry
import com.pokeskies.skiesskins.api.shop.StaticEntry
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.config.shop.entries.RandomEntryConfig
import com.pokeskies.skiesskins.data.UserSkinData
import com.pokeskies.skiesskins.data.shop.PackageEntryShopData
import com.pokeskies.skiesskins.data.shop.RandomEntryShopData
import com.pokeskies.skiesskins.data.shop.RandomEntryShopData.SkinData
import com.pokeskies.skiesskins.data.shop.StaticEntryShopData
import com.pokeskies.skiesskins.data.shop.UserShopData
import com.pokeskies.skiesskins.utils.RandomCollection
import com.pokeskies.skiesskins.utils.RefreshableGUI
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

class ShopGui(
    private val player: ServerPlayer,
    private val shopId: String,
    private val shopConfig: ShopConfig,
) : RefreshableGUI() {
    private val template: ChestTemplate = ChestTemplate.Builder(shopConfig.options.size)
        .build()

    init {
        this.subscribe(this, Runnable { refresh() })
        SkiesSkins.INSTANCE.inventoryControllers[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        if (SkiesSkins.INSTANCE.storage == null) {
            Utils.printDebug("The storage system is not available at the moment. Please try again later.")
            return
        }

        val userData = SkiesSkinsAPI.getUserData(player)

        if (!userData.shopData.containsKey(shopId)) {
            userData.shopData[shopId] = generateShopData()
            SkiesSkinsAPI.saveUserData(player, userData)
        }

        val userShopData = userData.shopData[shopId]
        if (userShopData == null) {
            Utils.printDebug("There was an error getting your data from the storage system. Please try again later.")
            return
        }

        // Default items
        for ((id, item) in shopConfig.items) {
            val button = GooeyButton.builder()
                .display(item.createItemStack(player))
                .onClick { ctx ->
                    for (actionEntry in item.clickActions) {
                        val action = actionEntry.value
                        if (action.matchesClick(ctx.clickType)) {
                            action.executeAction(player)
                        }
                    }
                }
                .build();
            for (slot in item.slots) {
                this.template.set(slot, button)
            }
        }

        // RANDOM SKINS
        // Iterate through every set of Random skins in this shop
        for ((setId, set) in shopConfig.skins.random) {
            var randomShopData = userShopData.randomData[setId]
            // If the random data does not contain the set, then we have an error
            if (randomShopData == null ||
                SkiesSkins.INSTANCE.shopManager.userNeedsReset(shopId, setId, randomShopData.resetTime)) {
                randomShopData = generateRandomShopData(set)
                userShopData.randomData[setId] = randomShopData
                SkiesSkinsAPI.saveUserData(player, userData)
            }

            val resetTime = SkiesSkins.INSTANCE.shopManager.getShopSetResetTime(shopId, setId)

            val slots = set.gui.slots.toMutableList()
            // Iterate through every skin in this set and attempt to add it to the GUI
            for (skinData in randomShopData.skins) {
                val slot = slots.removeFirstOrNull() ?: break
                if (skinData == null) continue
                // Check to ensure that this skin even exists in this shop
                val shopSkinConfig = set.skins[skinData.id]
                if (shopSkinConfig == null) {
                    this.template.set(
                        slot,
                        Utils.getErrorButton("<red>Error while fetching shop skin data. It's missing?")
                    )
                } else {
                    // Check to ensure that this skin even exists in the configs
                    val skinConfig = ConfigManager.SKINS[skinData.id]
                    if (skinConfig == null) {
                        this.template.set(
                            slot,
                            Utils.getErrorButton("<red>Error while fetching skin. It's missing?")
                        )
                    } else {
                        val packageEntry = RandomEntry(shopId, shopConfig, set, skinData.id, skinConfig, shopSkinConfig.cost, shopSkinConfig.limit, skinData.purchases, resetTime)
                        if (shopSkinConfig.limit <= 0 || skinData.purchases < shopSkinConfig.limit) {
                            template.set(slot, GooeyButton.builder()
                                .display(
                                    set.gui.available.createItemStack(player, packageEntry)
                                )
                                .onClick { ctx ->
                                    if (SkiesSkins.INSTANCE.shopManager.userNeedsReset(shopId, setId, randomShopData.resetTime)) {
                                        refresh()
                                        return@onClick
                                    }
                                    UIManager.openUIForcefully(player, PurchaseConfirmGui(player, shopId, shopConfig, ConfigManager.PURCHASE_CONFIRM_GUI.buttons.info.randomInfo,
                                        {
                                            ConfigManager.PURCHASE_CONFIRM_GUI.buttons.info.randomInfo.createItemStack(player, packageEntry)
                                        }
                                    ) { gui ->
                                        if (SkiesSkins.INSTANCE.shopManager.userNeedsReset(shopId, setId, randomShopData.resetTime)) {
                                            gui.forceReturn()
                                            return@PurchaseConfirmGui
                                        }
                                        if (shopSkinConfig.cost.all { it.withdraw(player, shopConfig) }) {
                                            skinData.purchases += 1
                                            userData.inventory.add(UserSkinData(skinData.id))
                                            SkiesSkinsAPI.saveUserData(player, userData)
                                            refresh()
                                            player.sendMessage(
                                                Component.literal("You purchased a skin!")
                                                    .withStyle { it.withColor(ChatFormatting.GREEN) }
                                            )
                                            player.playNotifySound(
                                                SoundEvents.PLAYER_LEVELUP,
                                                SoundSource.MASTER,
                                                0.5f,
                                                0.5f
                                            )
                                            gui.forceReturn()
                                        } else {
                                            player.sendMessage(
                                                Component.literal("You do not have enough to purchase this skin!")
                                                    .withStyle { it.withColor(ChatFormatting.RED) }
                                            )
                                            player.playNotifySound(
                                                SoundEvents.LAVA_EXTINGUISH,
                                                SoundSource.MASTER,
                                                0.5f,
                                                0.5f
                                            )
                                        }
                                    })
                                }
                                .build()
                            )
                        } else {
                            template.set(slot, GooeyButton.builder()
                                .display(
                                    set.gui.maxUses.createItemStack(player, packageEntry)
                                )
                                .build()
                            )
                        }
                    }
                }
            }
        }

        // STATIC SKINS
        // Iterate through every set of Static skins in this shop
        for ((setId, set) in shopConfig.skins.static) {
            for ((skinId, skin) in set.skins) {
                var button: GooeyButton
                // Check to ensure that this skin even exists in the configs
                val skinConfig = ConfigManager.SKINS[skinId]
                if (skinConfig == null) {
                    button = Utils.getErrorButton("<red>Error while fetching skin. It's missing?")
                } else {
                    val staticSetData = userShopData.staticData[setId] ?: mutableMapOf()
                    val staticSkinData = staticSetData[skinId] ?: StaticEntryShopData()

                    val packageEntry = StaticEntry(shopId, shopConfig, set, skinId, skinConfig, skin.cost, skin.limit, staticSkinData.purchases)
                    if (skin.limit <= 0 || staticSkinData.purchases < skin.limit) {
                        button = GooeyButton.builder()
                            .display(set.gui.available.createItemStack(player, packageEntry))
                            .onClick { ctx ->
                                UIManager.openUIForcefully(player, PurchaseConfirmGui(player, shopId, shopConfig, ConfigManager.PURCHASE_CONFIRM_GUI.buttons.info.staticInfo,
                                    {
                                        ConfigManager.PURCHASE_CONFIRM_GUI.buttons.info.staticInfo.createItemStack(player, packageEntry)
                                    }
                                ) { gui ->
                                    if (skin.cost.all { it.withdraw(player, shopConfig) }) {
                                        staticSkinData.purchases += 1
                                        staticSetData[skinId] = staticSkinData
                                        userShopData.staticData[setId] = HashMap(staticSetData)
                                        userData.inventory.add(UserSkinData(skinId))
                                        SkiesSkinsAPI.saveUserData(player, userData)
                                        refresh()
                                        player.sendMessage(
                                            Component.literal("You purchased a skin!")
                                                .withStyle { it.withColor(ChatFormatting.GREEN) }
                                        )
                                        player.playNotifySound(
                                            SoundEvents.PLAYER_LEVELUP,
                                            SoundSource.MASTER,
                                            0.5f,
                                            0.5f
                                        )
                                        gui.forceReturn()
                                    } else {
                                        player.sendMessage(
                                            Component.literal("You do not have enough to purchase this skin!")
                                                .withStyle { it.withColor(ChatFormatting.RED) }
                                        )
                                        player.playNotifySound(
                                            SoundEvents.LAVA_EXTINGUISH,
                                            SoundSource.MASTER,
                                            0.5f,
                                            0.5f
                                        )
                                    }
                                })
                            }
                            .build()
                    } else {
                        button = GooeyButton.builder()
                            .display(set.gui.maxUses.createItemStack(player, packageEntry))
                            .build()
                    }
                }

                for (slot in skin.slots) {
                    template.set(slot, button)
                }
            }
        }

        // PACKAGES
        for ((packageId, shopPackage) in shopConfig.packages) {
            var button: GooeyButton

            val packageData = userShopData.packagesData[packageId] ?: PackageEntryShopData()
            val packageEntry = PackageEntry(shopId, shopConfig, shopPackage, shopPackage.cost, shopPackage.limit, packageData.purchases)
            if (shopPackage.limit <= 0 || packageData.purchases < shopPackage.limit) {
                button = GooeyButton.builder()
                    .display(shopPackage.gui.available.createItemStack(player, packageEntry))
                    .onClick { ctx ->
                        UIManager.openUIForcefully(player, PurchaseConfirmGui(player, shopId, shopConfig, ConfigManager.PURCHASE_CONFIRM_GUI.buttons.info.packageInfo,
                            {
                                ConfigManager.PURCHASE_CONFIRM_GUI.buttons.info.packageInfo.createItemStack(player, packageEntry)
                            }
                        ) { gui ->
                            if (shopPackage.cost.all { it.withdraw(player, shopConfig) }) {
                                packageData.purchases += 1
                                userShopData.packagesData[packageId] = packageData
                                for (skin in shopPackage.skins) {
                                    if (!ConfigManager.SKINS.containsKey(skin)) {
                                        Utils.printError("Skin $skin from the Shop Package $packageId does not exist and was not able to be given to ${player.name.string}!")
                                        continue
                                    }
                                    userData.inventory.add(UserSkinData(skin))
                                }
                                SkiesSkinsAPI.saveUserData(player, userData)
                                refresh()
                                player.sendMessage(
                                    Component.literal("You purchased a package!")
                                        .withStyle { it.withColor(ChatFormatting.GREEN) }
                                )
                                player.playNotifySound(
                                    SoundEvents.PLAYER_LEVELUP,
                                    SoundSource.MASTER,
                                    0.5f,
                                    0.5f
                                )
                                gui.forceReturn()
                            } else {
                                player.sendMessage(
                                    Component.literal("You do not have enough to purchase this package!")
                                        .withStyle { it.withColor(ChatFormatting.RED) }
                                )
                                player.playNotifySound(
                                    SoundEvents.LAVA_EXTINGUISH,
                                    SoundSource.MASTER,
                                    0.5f,
                                    0.5f
                                )
                            }
                        })
                    }
                    .build()
            } else {
                button = GooeyButton.builder()
                    .display(
                        shopPackage.gui.maxUses.createItemStack(
                            player, packageEntry
                        )
                    )
                    .build()
            }

            for (slot in shopPackage.gui.slots) {
                template.set(slot, button)
            }
        }
    }

    fun generateShopData(): UserShopData {
        return UserShopData(generateRandomData(), HashMap(), HashMap())
    }

    fun generateRandomData(): HashMap<String, RandomEntryShopData> {
        val data = HashMap<String, RandomEntryShopData>()

        for ((setId, set) in shopConfig.skins.random) {
            data[setId] = generateRandomShopData(set)
        }

        return data
    }

    fun generateRandomShopData(set: RandomEntryConfig): RandomEntryShopData {
        val rc: RandomCollection<Pair<String, RandomEntryConfig.RandomSkin>> = RandomCollection()
        for ((skinId, skin) in set.skins) {
            rc.add(skin.weight.toDouble(), Pair(skinId, skin))
        }

        val skins: MutableList<SkinData?> = mutableListOf()

        for (slot in set.gui.slots) {
            if (rc.size() > 0) {
                val skinPair = rc.next(true)
                skins.add(
                    if (ConfigManager.SKINS.containsKey(skinPair.first))
                        SkinData(skinPair.first, 0)
                    else
                        null
                )
            }
        }

        return RandomEntryShopData(
            System.currentTimeMillis(),
            skins
        )
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(shopConfig.options.title)
    }

    override fun onClose(action: PageAction) {
        SkiesSkins.INSTANCE.inventoryControllers.remove(player.uuid, this)
    }
}
