package com.pokeskies.skiesskins.gui

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.PokemonItem
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.data.UserSkinData
import com.pokeskies.skiesskins.utils.IRefreshableGui
import com.pokeskies.skiesskins.utils.Utils
import com.pokeskies.skiesskins.utils.clear
import com.pokeskies.skiesskins.utils.setSlots
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

class ScrapConfirmGui(
    player: ServerPlayer,
    val skinData: UserSkinData,
    val skinConfig: SkinConfig,
    private val returnGUI: IRefreshableGui? = null
) : IRefreshableGui(
    ConfigManager.SCRAP_CONFIRM_GUI.type.type, player, false
) {
    private val guiConfig = ConfigManager.SCRAP_CONFIRM_GUI

    private var shouldClose = false

    init {
        SkiesSkins.INSTANCE.inventoryInstances[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        try {
            val user = SkiesSkinsAPI.getUserData(player)

            clear()

            for ((_, item) in guiConfig.items) {
                setSlots(item.slots, GuiElementBuilder
                    .from(item.createItemStack(player))
                    .setCallback { type ->
                        for (actionEntry in item.clickActions) {
                            val action = actionEntry.value
                            if (action.matchesClick(type)) {
                                action.executeAction(player, this)
                            }
                        }
                    }
                )
            }

            val species = PokemonSpecies.getByIdentifier(skinConfig.species)
            if (species == null) {
                setSlots(guiConfig.skin.slots, Utils.getErrorButton("<red>Error while fetching Skin! Invalid Species?"))
            } else {
                val pokemon = species.create()
                for (aspect in skinConfig.aspects.apply) {
                    PokemonProperties.parse(aspect).apply(pokemon)
                }

                setSlots(guiConfig.skin.slots, GuiElementBuilder
                    .from(PokemonItem.from(pokemon, 1))
                    .setName(skinConfig.parse(guiConfig.skin.name, player))
                    .setLore(skinConfig.parse(guiConfig.skin.lore, player))
                )
            }

            guiConfig.buttons.confirm.let { confirm ->
                val confirmButton = GuiElementBuilder
                    .from(confirm.createItemStack(player))
                    .setCallback { type ->
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
                        close()
                    }

                confirm.name?.let {
                    confirmButton.setName(skinConfig.parse(it, player))
                }
                if (confirm.lore.isNotEmpty()) {
                    confirmButton.setLore(skinConfig.parse(confirm.lore, player))
                }
                setSlots(confirm.slots, confirmButton)
            }

            guiConfig.buttons.cancel.let { cancel ->
                val cancelButton = GuiElementBuilder
                    .from(cancel.createItemStack(player))
                    .setCallback { type ->
                        InventoryGui(player).open()
                    }
                cancel.name?.let {
                    cancelButton.setName(skinConfig.parse(it, player))
                }
                if (cancel.lore.isNotEmpty()) {
                    cancelButton.setLore(skinConfig.parse(cancel.lore, player))
                }
                setSlots(cancel.slots, cancelButton)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClose() {
        SkiesSkins.INSTANCE.inventoryInstances.remove(player.uuid, this)
        if (!shouldClose) {
            returnGUI?.open()
        }
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, guiConfig.title))
    }
}
