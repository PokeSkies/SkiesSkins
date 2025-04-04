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
import com.pokeskies.skiesskins.config.shop.ShopRandomSetConfig
import com.pokeskies.skiesskins.data.UserSkinData
import com.pokeskies.skiesskins.data.shop.RandomShopData
import com.pokeskies.skiesskins.utils.RefreshableGUI
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.component.ItemLore

class PurchaseRandomSkinGui(
    private val player: ServerPlayer,
    val shopId: String,
    val setId: String,
    val setConfig: ShopRandomSetConfig,
    val randomShopData: RandomShopData,
    val shopConfig: ShopConfig,
    val skinConfig: SkinConfig,
    val randomSkinConfig: ShopRandomSetConfig.RandomSkin,
    val skinData: RandomShopData.SkinData,
    val resetTime: Long?
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
            val species = PokemonSpecies.getByIdentifier(skinConfig.species)
            if (species == null) {
                // Invalid Species Error
                val button = Utils.getErrorButton("<red>Error while fetching Skin! Invalid Species?")
                guiConfig.purchase.randomSlot.slots.forEach { slot ->
                    this.template.set(slot, button)
                }
            } else {
                val pokemon = species.create()
                for (aspect in skinConfig.aspects.apply) {
                    PokemonProperties.parse(aspect).apply(pokemon)
                }

                val button = GooeyButton.builder()
                    .display(PokemonItem.from(pokemon, 1))
                    .with(DataComponents.ITEM_NAME, PurchaseConfirmConfig.SlotOption.parseString(guiConfig.purchase.randomSlot.name, player, shopConfig, randomSkinConfig.cost, randomSkinConfig.limit, skinData.purchases, skinConfig, resetTime))
                    .with(DataComponents.LORE, ItemLore(PurchaseConfirmConfig.SlotOption.parseStringList(guiConfig.purchase.randomSlot.lore, player, shopConfig, randomSkinConfig.cost, randomSkinConfig.limit, skinData.purchases, skinConfig, resetTime)))
                    .build()

                guiConfig.purchase.randomSlot.slots.forEach { slot ->
                    this.template.set(slot, button)
                }
            }

            // Confirm button
            guiConfig.confirm.let { item ->
                val confirmButton = GooeyButton.builder()
                    .display(item.createItemStack(player))
                    .onClick { ctx ->
                        if (SkiesSkins.INSTANCE.shopManager.userNeedsReset(shopId, setId, randomShopData.resetTime)) {
                            UIManager.closeUI(player)
                            return@onClick
                        }
                        if (randomSkinConfig.cost.all { it.withdraw(player, shopConfig) }) {
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
                            UIManager.closeUI(player)
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
                    }

                item.name?.let {
                    confirmButton.with(DataComponents.ITEM_NAME, Utils.parseSkinString(it, player, skinConfig))
                }
                if (item.lore.isNotEmpty()) {
                    confirmButton.with(DataComponents.LORE, ItemLore(Utils.parseSkinStringList(guiConfig.confirm.lore, player, skinConfig)))
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
                    cancelButton.with(DataComponents.ITEM_NAME, Utils.parseSkinString(it, player, skinConfig))
                }
                if (item.lore.isNotEmpty()) {
                    cancelButton.with(DataComponents.LORE, ItemLore(Utils.parseSkinStringList(guiConfig.cancel.lore, player, skinConfig)))
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
