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
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
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
            for (skin in user.inventory.subList(slots.size * page, user.inventory.size)) {
                if (index < slots.size) {
                    val slot = slots[index++]
                    val skinEntry: SkinConfig? = ConfigManager.SKINS[skin.id]
                    if (skinEntry == null) {
                        setSlot(slot, Utils.getErrorButton("<red>Error while fetching Skin! Missing Entry?"))
                        continue
                    }

                    val species = PokemonSpecies.getByIdentifier(skinEntry.species)
                    if (species == null) {
                        setSlot(slot, Utils.getErrorButton("<red>Error while fetching Skin! Invalid Species?"))
                        continue
                    }

                    val pokemon = species.create()
                    for (aspect in skinEntry.aspects.apply) {
                        PokemonProperties.parse(aspect).apply(pokemon)
                    }

                    setSlot(slot, GuiElementBuilder.from(
                        PokemonItem.from(pokemon, 1).also { stack ->
                            stack.applyComponents(DataComponentPatch.builder()
                                .set(DataComponents.ITEM_NAME, Component.empty().setStyle(Style.EMPTY.withItalic(false))
                                    .append(skinEntry.parse(ConfigManager.INVENTORY_GUI.skinOptions.name, player)))
                                .set(DataComponents.LORE, ItemLore(skinEntry.parse(ConfigManager.INVENTORY_GUI.skinOptions.lore, player)))
                                .build())
                        })
                        .setCallback { type ->
                            val user = SkiesSkinsAPI.getUserData(player)
                            if (user.inventory.contains(skin)) {
                                // Scrapping
                                if (ConfigManager.INVENTORY_GUI.skinOptions.scrapClickType.any { it.buttonClicks.contains(type) }) {
                                    ScrapConfirmGui(player, skin, skinEntry, this).open()
                                } else if (ConfigManager.INVENTORY_GUI.skinOptions.applyClickType.any { it.buttonClicks.contains(type) }) {
                                    ApplyGui(player, skin, skinEntry).open()
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
