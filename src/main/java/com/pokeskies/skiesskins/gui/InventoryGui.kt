package com.pokeskies.skiesskins.gui

import ca.landonjw.gooeylibs2.api.UIManager
import ca.landonjw.gooeylibs2.api.button.GooeyButton
import ca.landonjw.gooeylibs2.api.data.UpdateEmitter
import ca.landonjw.gooeylibs2.api.page.Page
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.PokemonItem
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class InventoryGui(
    val player: ServerPlayer
) : UpdateEmitter<Page>(), Page {
    private val template: ChestTemplate = ChestTemplate.Builder(ConfigManager.INVENTORY_GUI.size)
        .build()

    private var page = 0
    private var maxPages = 1

    init {
        refresh()
    }

    fun refresh() {
        val user = SkiesSkinsAPI.getUserData(player)
        val slots = ConfigManager.INVENTORY_GUI.skinOptions.slots
        maxPages = (user.inventory.size / (slots.size + 1)) + 1

        this.template.clear()

        var index = 0
        for (skin in user.inventory.subList(slots.size * page, user.inventory.size)) {
            if (index < slots.size) {
                val slot = slots[index++]
                val skinEntry: SkinConfig? = ConfigManager.SKINS[skin.id]
                if (skinEntry == null) {
                    this.template.set(slot, Utils.getErrorButton("<red>Error while fetching Skin! Missing Entry?"))
                } else {
                    val species = PokemonSpecies.getByIdentifier(skinEntry.species)
                    if (species == null) {
                        this.template.set(slot, Utils.getErrorButton("<red>Error while fetching Skin! Invalid Species?"))
                        continue
                    }

                    this.template.set(slot, GooeyButton.builder()
                        .display(PokemonItem.from(species, skinEntry.aspects.apply.toSet(), 1))
                        .title(Utils.deserializeText(skinEntry.name))
                        .lore(
                            Component::class.java,
                            ConfigManager.INVENTORY_GUI.skinOptions.lore.map {
                                Utils.deserializeText(it)
                            }
                        )
                        .onClick { ctx -> UIManager.openUIForcefully(player, ApplyGui(player, skin, skinEntry)) }
                        .build())
                }
            }
        }

        // NEXT AND PREVIOUS PAGE
        for (slot in ConfigManager.INVENTORY_GUI.previousPage.slots) {
            this.template.set(slot, GooeyButton.builder()
                .display(ConfigManager.INVENTORY_GUI.previousPage.createItemStack())
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
                .display(ConfigManager.INVENTORY_GUI.nextPage.createItemStack())
                .onClick { ctx ->
                    if (maxPages > page + 1) {
                        page++
                        refresh()
                    }
                }
                .build()
            )
        }

        for ((id, item) in ConfigManager.INVENTORY_GUI.items) {
            val button = GooeyButton.builder()
                .display(item.createItemStack())
                .build();
            for (slot in item.slots) {
                this.template.set(slot, button)
            }
        }
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(ConfigManager.INVENTORY_GUI.title)
    }
}