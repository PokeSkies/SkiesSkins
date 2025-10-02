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
import com.pokeskies.skiesskins.utils.RefreshableGUI
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.component.ItemLore

class InventoryGui(
    val player: ServerPlayer
) : RefreshableGUI() {
    private val template: ChestTemplate = ChestTemplate.Builder(ConfigManager.INVENTORY_GUI.size)
        .build()

    private var page = 0
    private var maxPages = 1

    init {
        this.subscribe(this, Runnable { refresh() })
        SkiesSkins.INSTANCE.inventoryControllers[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        try {
            val user = SkiesSkinsAPI.getUserData(player)
            val slots = ConfigManager.INVENTORY_GUI.skinOptions.slots
            maxPages = (user.inventory.size / (slots.size + 1)) + 1

            this.template.clear()

            for ((id, item) in ConfigManager.INVENTORY_GUI.items) {
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

            var index = 0
            for (skin in user.inventory.subList(slots.size * page, user.inventory.size)) {
                if (index < slots.size) {
                    val slot = slots[index++]
                    val skinEntry: SkinConfig? = ConfigManager.SKINS[skin.id]
                    if (skinEntry == null) {
                        this.template.set(slot, Utils.getErrorButton("<red>Error while fetching Skin! Missing Entry?"))
                        continue
                    }

                    val species = PokemonSpecies.getByIdentifier(skinEntry.species)
                    if (species == null) {
                        this.template.set(slot, Utils.getErrorButton("<red>Error while fetching Skin! Invalid Species?"))
                        continue
                    }

                    val pokemon = species.create()
                    for (aspect in skinEntry.aspects.apply) {
                        PokemonProperties.parse(aspect).apply(pokemon)
                    }

                    this.template.set(slot, GooeyButton.builder()
                        .display(PokemonItem.from(pokemon, 1).also { stack ->
                            stack.applyComponents(DataComponentPatch.builder()
                                .set(DataComponents.ITEM_NAME, Component.empty().setStyle(Style.EMPTY.withItalic(false))
                                    .append(skinEntry.parse(ConfigManager.INVENTORY_GUI.skinOptions.name, player)))
                                .set(DataComponents.LORE, ItemLore(skinEntry.parse(ConfigManager.INVENTORY_GUI.skinOptions.lore, player)))
                                .build())
                        })
                        .onClick { ctx ->
                            val user = SkiesSkinsAPI.getUserData(player)
                            if (user.inventory.contains(skin)) {
                                // Scrapping
                                if (ConfigManager.INVENTORY_GUI.skinOptions.scrapClickType.any { it.buttonClicks.contains(ctx.clickType) }) {
                                    if (skinEntry.scrapping != null) {
                                        UIManager.openUIForcefully(player, ScrapConfirmGui(player, skin, skinEntry))
                                    } else {
                                        player.sendSystemMessage(Utils.deserializeText("<red>This skin cannot be scrapped!"))
                                    }
                                } else if (ConfigManager.INVENTORY_GUI.skinOptions.applyClickType.any { it.buttonClicks.contains(ctx.clickType) }) {
                                    UIManager.openUIForcefully(player, ApplyGui(player, skin, skinEntry))
                                }
                            } else {
                                refresh()
                            }
                        }
                        .build())
                }
            }

            // NEXT AND PREVIOUS PAGE
            for (slot in ConfigManager.INVENTORY_GUI.previousPage.slots) {
                this.template.set(slot, GooeyButton.builder()
                    .display(ConfigManager.INVENTORY_GUI.previousPage.createItemStack(player))
                    .onClick { ctx ->
                        if (page > 0) {
                            page--
                            refresh()
                        }
                    }
                    .build()
                )
            }
            for (slot in ConfigManager.INVENTORY_GUI.nextPage.slots) {
                this.template.set(slot, GooeyButton.builder()
                    .display(ConfigManager.INVENTORY_GUI.nextPage.createItemStack(player))
                    .onClick { ctx ->
                        if (maxPages > page + 1) {
                            page++
                            refresh()
                        }
                    }
                    .build()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClose(action: PageAction) {
        SkiesSkins.INSTANCE.inventoryControllers.remove(player.uuid, this)
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, ConfigManager.INVENTORY_GUI.title))
    }
}
