package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

class PurchaseConfirmConfig(
    val title: String = "Confirm Purchase",
    val size: Int = 3,
    val purchase: PurchaseSlot = PurchaseSlot(),
    val confirm: GenericGuiItem = GenericGuiItem(),
    val cancel: GenericGuiItem = GenericGuiItem(),
    val items: Map<String, GenericGuiItem> = emptyMap()
) {
    class PurchaseSlot(
        @SerializedName("random")
        val randomSlot: SlotOption = SlotOption(),
        @SerializedName("static")
        val staticSlot: SlotOption = SlotOption(),
        @SerializedName("package")
        val packageSlot: SlotOption = SlotOption(),
    ) {
        override fun toString(): String {
            return "PurchaseSlot(randomSlot=$randomSlot, staticSlot=$staticSlot, packageSlot=$packageSlot)"
        }
    }

    class SlotOption(
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
        return "PurchaseConfirmConfig(title='$title', size=$size, purchase=$purchase, " +
                "confirm=$confirm, cancel=$cancel, items=$items)"
    }

}