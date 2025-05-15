package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.actions.ClickType
import com.pokeskies.skiesskins.config.gui.items.GenericItem
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

/*
 * Config options for the skin inventory GUI.
 */
class InventoryGuiConfig(
    val title: String = "Skin Inventory",
    val size: Int = 6,
    @SerializedName("skin_options")
    val skinOptions: SkinSlotOptions = SkinSlotOptions(),
    @SerializedName("next_page")
    val nextPage: GenericItem = GenericItem(),
    @SerializedName("previous_page")
    val previousPage: GenericItem = GenericItem(),
    val items: Map<String, GenericItem> = emptyMap()
) {
    /*
     * Config options for the skin slots in the inventory GUI.
     */
    class SkinSlotOptions(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val slots: List<Int> = emptyList(),
        val name: String = "",
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val lore: List<String> = emptyList(),
        @SerializedName("apply_click")
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val applyClickType: List<ClickType> = listOf(ClickType.ANY_CLICK),
        @SerializedName("scrap_click")
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val scrapClickType: List<ClickType> = listOf(ClickType.MIDDLE_CLICK)
    ) {
        override fun toString(): String {
            return "SkinSlotOptions(slots=$slots, name='$name', lore=$lore, " +
                    "applyClickType=$applyClickType, scrapClickType=$scrapClickType)"
        }
    }

    override fun toString(): String {
        return "InventoryConfig(title='$title', size=$size, skinOptions=$skinOptions, nextPage=$nextPage, previousPage=$previousPage, items=$items)"
    }
}
