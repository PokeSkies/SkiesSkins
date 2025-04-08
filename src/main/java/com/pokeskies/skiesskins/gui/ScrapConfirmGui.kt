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
import com.pokeskies.skiesskins.data.UserSkinData
import com.pokeskies.skiesskins.utils.RefreshableGUI
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.component.ItemLore

class ScrapConfirmGui(
    private val player: ServerPlayer,
    val skinData: UserSkinData,
    val skinConfig: SkinConfig
) : RefreshableGUI() {
    private val guiConfig = ConfigManager.SCRAP_CONFIRM_GUI
    private val template: ChestTemplate = ChestTemplate.Builder(guiConfig.size)
        .build()

    private var shouldClose = false

    init {
        this.subscribe(this, Runnable { refresh() })
        SkiesSkins.INSTANCE.inventoryControllers[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        try {
            val user = SkiesSkinsAPI.getUserData(player)

            this.template.clear()

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

            val species = PokemonSpecies.getByIdentifier(skinConfig.species)
            if (species == null) {
                val button = Utils.getErrorButton("<red>Error while fetching Skin! Invalid Species?")
                guiConfig.skin.slots.forEach { slot ->
                    this.template.set(slot, button)
                }
            } else {
                val pokemon = species.create()
                for (aspect in skinConfig.aspects.apply) {
                    PokemonProperties.parse(aspect).apply(pokemon)
                }

                val button = GooeyButton.builder()
                    .display(PokemonItem.from(pokemon, 1))
                    .with(DataComponents.ITEM_NAME, skinConfig.parse(guiConfig.skin.name, player))
                    .with(DataComponents.LORE, ItemLore(skinConfig.parse(guiConfig.skin.lore, player)))
                    .build()

                guiConfig.skin.slots.forEach { slot ->
                    this.template.set(slot, button)
                }
            }

            val confirmButton = GooeyButton.builder()
                .display(guiConfig.confirm.createItemStack(player))
                .onClick { ctx ->
                    if (user.inventory.remove(skinData) && SkiesSkinsAPI.saveUserData(player, user)) {
                        if (skinConfig.scrapping != null && skinConfig.scrapping.value.isNotEmpty()) {
                            skinConfig.scrapping.value.all { entry -> !entry.deposit(player) }
                        }
                        player.sendSystemMessage(Component.literal("Successfully scrapped skin!")
                            .withStyle { it.withColor(ChatFormatting.GREEN) })
                        player.playNotifySound(SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.MASTER, 0.5f, 1.0f)
                    } else {
                        player.sendSystemMessage(Component.literal("Something went wrong... Scrapping cancelled!")
                            .withStyle { it.withColor(ChatFormatting.RED) })
                        player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.MASTER, 0.5f, 0.5f)
                    }
                    shouldClose = true
                    UIManager.closeUI(player)
                }

            guiConfig.confirm.name?.let {
                confirmButton.with(DataComponents.ITEM_NAME, skinConfig.parse(it, player))
            }
            if (guiConfig.confirm.lore.isNotEmpty()) {
                confirmButton.with(DataComponents.LORE, ItemLore(skinConfig.parse(guiConfig.confirm.lore, player)))
            }
            guiConfig.confirm.slots.forEach { slot ->
                this.template.set(slot, confirmButton.build())
            }

            val cancelButton = GooeyButton.builder()
                .display(guiConfig.cancel.createItemStack(player))
                .onClick { ctx ->
                    UIManager.openUIForcefully(player, InventoryGui(player))
                }
            guiConfig.cancel.name?.let {
                cancelButton.with(DataComponents.ITEM_NAME, skinConfig.parse(it, player))
            }
            if (guiConfig.cancel.lore.isNotEmpty()) {
                cancelButton.with(DataComponents.LORE, ItemLore(skinConfig.parse(guiConfig.cancel.lore, player)))
            }
            guiConfig.cancel.slots.forEach { slot ->
                this.template.set(slot, cancelButton.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClose(action: PageAction) {
        SkiesSkins.INSTANCE.inventoryControllers.remove(player.uuid, this)
        if (!shouldClose) {
            SkiesSkinsAPI.openSkinInventory(player)
        }
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, guiConfig.title))
    }
}
