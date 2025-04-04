package com.pokeskies.skiesskins.gui

import ca.landonjw.gooeylibs2.api.UIManager
import ca.landonjw.gooeylibs2.api.button.GooeyButton
import ca.landonjw.gooeylibs2.api.page.PageAction
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.PokemonItem
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.config.gui.PurchaseConfirmConfig
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.config.shop.ShopPackageConfig
import com.pokeskies.skiesskins.config.shop.ShopRandomSetConfig
import com.pokeskies.skiesskins.data.UserSkinData
import com.pokeskies.skiesskins.data.shop.PackageShopData
import com.pokeskies.skiesskins.data.shop.RandomShopData
import com.pokeskies.skiesskins.data.shop.UserShopData
import com.pokeskies.skiesskins.utils.RefreshableGUI
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

class PurchasePackageGui(
    private val player: ServerPlayer,
    val shopId: String,
    val shopConfig: ShopConfig,
    val userShopData: UserShopData,
    val packageId: String,
    val shopPackage: ShopPackageConfig,
    val packageData: PackageShopData
) : RefreshableGUI() {
    private val guiConfig = ConfigManager.PURCHASE_CONFIRM_GUI
    private val template: ChestTemplate = ChestTemplate.Builder(guiConfig.size)
        .build()

    init {
        this.subscribe(this, Runnable { refresh() })
        SkiesSkins.INSTANCE.inventoryControllers[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        try {
            val userData = SkiesSkinsAPI.getUserData(player)

            this.template.clear()

            // Load background items
            for ((id, item) in guiConfig.items) {
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

            // Create the center info item
            val button = GooeyButton.builder()
                .display(ItemStack(guiConfig.purchase.packageSlot.item))
                .with(DataComponents.ITEM_NAME, PurchaseConfirmConfig.SlotOption.parseString(guiConfig.purchase.randomSlot.name, player, shopConfig, shopPackage.cost, shopPackage.limit, packageData.purchases, null, null))
                .with(DataComponents.LORE, ItemLore(PurchaseConfirmConfig.SlotOption.parseStringList(guiConfig.purchase.randomSlot.lore, player, shopConfig, shopPackage.cost, shopPackage.limit, packageData.purchases, null, null)))
                .build()

            guiConfig.purchase.randomSlot.slots.forEach { slot ->
                this.template.set(slot, button)
            }

            // Confirm button
            guiConfig.confirm.let { item ->
                val confirmButton = GooeyButton.builder()
                    .display(item.createItemStack(player))
                    .onClick { ctx ->
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
                    }

                item.name?.let {
                    confirmButton.with(DataComponents.ITEM_NAME, Utils.parsePackageString(it, player, shopPackage))
                }
                if (item.lore.isNotEmpty()) {
                    confirmButton.with(DataComponents.LORE, ItemLore(Utils.parsePackageStringList(guiConfig.confirm.lore, player, shopPackage)))
                }
                item.slots.forEach { slot ->
                    this.template.set(slot, confirmButton.build())
                }
            }

            // Cancel button
            guiConfig.cancel.let { item ->
                val cancelButton = GooeyButton.builder()
                    .display(item.createItemStack(player))
                    .onClick { ctx ->
                        UIManager.openUIForcefully(player, ShopGui(player, shopId, shopConfig))
                    }

                item.name?.let {
                    cancelButton.with(DataComponents.ITEM_NAME, Utils.parsePackageString(it, player, shopPackage))
                }
                if (item.lore.isNotEmpty()) {
                    cancelButton.with(DataComponents.LORE, ItemLore(Utils.parsePackageStringList(guiConfig.cancel.lore, player, shopPackage)))
                }
                item.slots.forEach { slot ->
                    this.template.set(slot, cancelButton.build())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClose(action: PageAction) {
        SkiesSkins.INSTANCE.inventoryControllers.remove(player.uuid, this)
        UIManager.openUIForcefully(player, ShopGui(player, shopId, shopConfig))
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, guiConfig.title))
    }
}
