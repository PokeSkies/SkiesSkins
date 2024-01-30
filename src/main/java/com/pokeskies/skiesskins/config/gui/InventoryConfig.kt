package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.SerializedName

class InventoryConfig(
    val title: String = "Skin Inventory",
    val size: Int = 6,
    @SerializedName("skin_options")
    val skinOptions: SkinSlotOptions = SkinSlotOptions(),
    @SerializedName("next_page")
    val nextPage: GuiItem = GuiItem(),
    @SerializedName("previous_page")
    val previousPage: GuiItem = GuiItem(),
    val items: Map<String, GuiItem> = emptyMap()
) {
    class SkinSlotOptions(
        val slots: List<Int> = emptyList(),
        val lore: List<String> = emptyList()
    ) {
        override fun toString(): String {
            return "InventorySkin(slots=$slots, lore=$lore)"
        }
    }

    override fun toString(): String {
        return "InventoryConfig(title='$title', size=$size, skinOptions=$skinOptions, nextPage=$nextPage, previousPage=$previousPage, items=$items)"
    }
}