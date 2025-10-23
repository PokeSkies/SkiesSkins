package com.pokeskies.skiesskins.gui

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.PokemonItem
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.utils.IRefreshableGui
import com.pokeskies.skiesskins.utils.Utils
import com.pokeskies.skiesskins.utils.clear
import com.pokeskies.skiesskins.utils.setSlots
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.component.ItemLore

class InventoryGui(
    player: ServerPlayer
) : IRefreshableGui(
    ConfigManager.INVENTORY_GUI.type.type, player, false
) {
    private var page = 0
    private var maxPages = 1

    init {
        SkiesSkins.INSTANCE.inventoryInstances[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        try {
            val user = SkiesSkinsAPI.getUserData(player)
            val slots = ConfigManager.INVENTORY_GUI.skinOptions.slots
            maxPages = (user.inventory.size / (slots.size + 1)) + 1

            clear()

            for ((_, item) in ConfigManager.INVENTORY_GUI.items) {
                setSlots(item.slots, GuiElementBuilder.from(item.createItemStack(player))
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

            var index = 0
            for (skinData in user.inventory.subList(slots.size * page, user.inventory.size)) {
                if (index < slots.size) {
                    val slot = slots[index++]
                    val skin: SkinConfig? = ConfigManager.SKINS[skinData.id]
                    if (skin == null) {
                        setSlot(slot, Utils.getErrorButton("<red>Error while fetching Skin! Missing Entry?"))
                        continue
                    }

                    val species = PokemonSpecies.getByIdentifier(skin.species)
                    if (species == null) {
                        setSlot(slot, Utils.getErrorButton("<red>Error while fetching Skin! Invalid Species?"))
                        continue
                    }

                    val pokemon = species.create()
                    for (aspect in skin.aspects.apply) {
                        PokemonProperties.parse(aspect).apply(pokemon)
                    }

                    setSlot(slot, GuiElementBuilder.from(
                        PokemonItem.from(pokemon, 1).also { stack ->
                            stack.applyComponents(DataComponentPatch.builder()
                                .set(DataComponents.ITEM_NAME, Component.empty().setStyle(Style.EMPTY.withItalic(false))
                                    .append(skin.parse(ConfigManager.INVENTORY_GUI.skinOptions.name, player)))
                                .set(DataComponents.LORE, ItemLore(skin.parse(ConfigManager.INVENTORY_GUI.skinOptions.lore, player)))
                                .build())
                        })
                        .setCallback { type ->
                            val user = SkiesSkinsAPI.getUserData(player)
                            if (user.inventory.contains(skinData)) {
                                if (ConfigManager.INVENTORY_GUI.skinOptions.tokenizeClickType.any { it.buttonClicks.contains(type) }) {
                                    if (!SkiesSkinsAPI.canTokenize(skin)) {
                                        player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                                        player.sendMessage(Utils.deserializeText("<red>This skin cannot be tokenized!"))
                                        return@setCallback
                                    }

                                    if (!user.inventory.remove(skinData) || !SkiesSkinsAPI.saveUserData(player, user)) {
                                        player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                                        player.sendMessage(Component.literal("There was an error while tokenizing this skin!")
                                            .withStyle { it.withColor(ChatFormatting.RED) })
                                        close()
                                        return@setCallback
                                    }

                                    val token = SkiesSkinsAPI.tokenizeSkin(skin) ?: run {
                                        player.sendMessage(
                                            Component.literal("Failed to create a token for skin ")
                                                .append(Utils.deserializeText(skin.name))
                                                .append(Component.literal(". Please contact an admin!").withStyle { it.withColor(ChatFormatting.RED) })
                                        )
                                        return@setCallback
                                    }

                                    player.inventory.placeItemBackInInventory(token.copyWithCount(1))
                                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.15F, 1.0F)
                                    player.sendMessage(Component.literal("Successfully tokenized the skin ")
                                        .append(Utils.deserializeText(skin.name))
                                        .append(Component.literal("!"))
                                        .withStyle { it.withColor(ChatFormatting.GREEN) }
                                    )
                                    refresh()
                                } else if (ConfigManager.INVENTORY_GUI.skinOptions.scrapClickType.any { it.buttonClicks.contains(type) }) {
                                    if (skin.scrapping != null && !skin.infinite) {
                                        ScrapConfirmGui(player, skinData, skin, this).open()
                                    } else {
                                        player.sendSystemMessage(Utils.deserializeText("<red>This skin cannot be scrapped!"))
                                    }
                                } else if (ConfigManager.INVENTORY_GUI.skinOptions.applyClickType.any { it.buttonClicks.contains(type) }) {
                                    ApplyGui(player, skinData, skin).open()
                                }
                            } else {
                                refresh()
                            }
                        }
                        .build())
                }
            }

            // BUTTONS
            setSlots(ConfigManager.INVENTORY_GUI.buttons.previousPage.slots, GuiElementBuilder
                .from(ConfigManager.INVENTORY_GUI.buttons.previousPage.createItemStack(player))
                .setCallback { type ->
                    if (page > 0) {
                        page--
                        refresh()
                    }
                }
            )
            setSlots(ConfigManager.INVENTORY_GUI.buttons.nextPage.slots, GuiElementBuilder
                .from(ConfigManager.INVENTORY_GUI.buttons.nextPage.createItemStack(player))
                .setCallback { type ->
                    if (maxPages > page + 1) {
                        page++
                        refresh()
                    }
                }
            )
            setSlots(ConfigManager.INVENTORY_GUI.buttons.removeSkin.slots, GuiElementBuilder
                .from(ConfigManager.INVENTORY_GUI.buttons.removeSkin.createItemStack(player))
                .setCallback { type ->
                    RemoverGui(player, this).open()
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClose() {
        SkiesSkins.INSTANCE.inventoryInstances.remove(player.uuid, this)
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, ConfigManager.INVENTORY_GUI.title))
    }
}
