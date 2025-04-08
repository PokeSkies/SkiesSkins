package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.config.gui.items.GenericItem
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

/*
 * Config options for the GUI that appears when attempting to scrap a skin.
 */
class ScrapConfirmGuiConfig(
    val title: String = "Confirm Scrapping",
    val size: Int = 3,
    val skin: SkinSlotOptions = SkinSlotOptions(),
    val confirm: GenericItem = GenericItem(),
    val cancel: GenericItem = GenericItem(),
    val items: Map<String, GenericItem> = emptyMap()
) {
    /*
     * Options for the skin information button.
     */
    class SkinSlotOptions(
        val item: Item = Items.BARRIER,
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

    override fun toString(): String {
        return "ScrapConfirmConfig(title='$title', size=$size, skin=$skin, confirm=$confirm, cancel=$cancel, items=$items)"
    }
}
