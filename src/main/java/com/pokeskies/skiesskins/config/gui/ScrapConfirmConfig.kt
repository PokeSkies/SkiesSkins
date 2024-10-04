package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

class ScrapConfirmConfig(
    val title: String = "Confirm Scrapping",
    val size: Int = 3,
    val skin: SkinSlotOptions = SkinSlotOptions(),
    val confirm: GenericGuiItem = GenericGuiItem(),
    val cancel: GenericGuiItem = GenericGuiItem(),
    val items: Map<String, GenericGuiItem> = emptyMap()
) {
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