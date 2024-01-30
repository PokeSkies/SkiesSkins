package com.pokeskies.skiesskins.gui

import ca.landonjw.gooeylibs2.api.data.UpdateEmitter
import ca.landonjw.gooeylibs2.api.page.Page
import ca.landonjw.gooeylibs2.api.page.PageAction
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class ShopGui(
    private val player: ServerPlayer
) : UpdateEmitter<Page?>(), Page {
    private val template: ChestTemplate = ChestTemplate.Builder(6)
        .build()

    init {
        refresh()
    }

    fun refresh() {
//        val user = SkiesSkins.INSTANCE.storage.getUser(player.uuid)
//
//        var slots = SkiesSkins.INSTANCE.configManager.config.gui.shop.skins.slots
//        var slotIndex = 0
//
//        for (skinId in user.shopData.skins) {
//            if (slotIndex >= slots.size)
//                break
//
//            val skin: SkinConfig? = ConfigManager.SKINS[skinId]
//            if (skin != null) {
//                val species = PokemonSpecies.getByIdentifier(skin.pokemon.species)
//                if (species == null) {
//                    template.set(
//                        slots[slotIndex++],
//                        Utils.getErrorButton("<red>Error while fetching Skin! Invalid Species?")
//                    )
//                    continue
//                }
//
//                template.set(
//                    slots[slotIndex++],
//                    GooeyButton.builder()
//                        .display(PokemonItem.from(species, skin.pokemon.aspects.apply.toSet(), 1))
//                        .title(Utils.deserializeText(skin.name))
//                        .lore(
//                            Text::class.java,
//                            SkiesSkins.INSTANCE.configManager.config.gui.inventory.skins.lore.map {
//                                Utils.deserializeText(it)
//                            }
//                        )
//                        .build()
//                )
//            }
//        }
//
//        // NAVIGATION
//        for (slot in SkiesSkins.INSTANCE.configManager.config.gui.inventory.navigation.inventory.slots) {
//            this.template.set(slot, GooeyButton.builder()
//                .display(Utils.processItemStack(SkiesSkins.INSTANCE.configManager.config.gui.inventory.navigation.inventory.item))
//                .onClick { ctx -> UIManager.openUIForcefully(player, InventoryGui(player).getPage()) }
//                .build()
//            )
//        }
//        for (slot in SkiesSkins.INSTANCE.configManager.config.gui.inventory.navigation.shop.slots) {
//            this.template.set(slot, GooeyButton.builder()
//                .display(Utils.processItemStack(SkiesSkins.INSTANCE.configManager.config.gui.inventory.navigation.shop.item))
//                .build()
//            )
//        }
//        for (slot in SkiesSkins.INSTANCE.configManager.config.gui.inventory.navigation.collections.slots) {
//            this.template.set(slot, GooeyButton.builder()
//                .display(Utils.processItemStack(SkiesSkins.INSTANCE.configManager.config.gui.inventory.navigation.collections.item))
//                .build()
//            )
//        }
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText("")
    }

    override fun onClose(action: PageAction) {

    }
}