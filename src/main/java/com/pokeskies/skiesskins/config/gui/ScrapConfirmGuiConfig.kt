package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.config.gui.items.GenericItem
import com.pokeskies.skiesskins.gui.InventoryType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

/*
 * Config options for the GUI that appears when attempting to scrap a skin.
 */
class ScrapConfirmGuiConfig(
    val title: String = "Confirm Scrapping",
    val type: InventoryType = InventoryType.GENERIC_9x3,
    val skin: SkinSlotOptions = SkinSlotOptions(),
    val buttons: Buttons = Buttons(),
    val items: Map<String, GenericItem> = emptyMap()
) {
    /*
     * Options for the skin information button.
     */
    class SkinSlotOptions(
        val item: String = "minecraft:barrier",
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val slots: List<Int> = emptyList(),
        val name: String = "",
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val lore: List<String> = emptyList()
    ) {
        override fun toString(): String {
            return "SkinSlotOptions(item=$item, slots=$slots, name='$name', lore=$lore)"
        }
    }

    class Buttons(
        val confirm: GenericItem = GenericItem(),
        val cancel: GenericItem = GenericItem(),
    ) {
        override fun toString(): String {
            return "Buttons(confirm=$confirm, cancel=$cancel)"
        }
    }

    override fun toString(): String {
        return "ScrapConfirmConfig(title='$title', type=$type, skin=$skin, buttons=$buttons, items=$items)"
    }
}
