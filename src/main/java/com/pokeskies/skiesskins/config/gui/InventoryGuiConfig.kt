package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.items.GenericItem
import com.pokeskies.skiesskins.gui.GenericClickType
import com.pokeskies.skiesskins.gui.InventoryType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

/*
 * Config options for the skin inventory GUI.
 */
class InventoryGuiConfig(
    val title: String = "Skin Inventory",
    val type: InventoryType = InventoryType.GENERIC_9x6,
    @SerializedName("skin_options")
    val skinOptions: SkinSlotOptions = SkinSlotOptions(),
    val buttons: Buttons = Buttons(),
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
        val applyClickType: List<GenericClickType> = listOf(GenericClickType.LEFT_CLICK),
        @SerializedName("scrap_click")
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val scrapClickType: List<GenericClickType> = listOf(GenericClickType.RIGHT_CLICK),
        @SerializedName("tokenize_click")
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val tokenizeClickType: List<GenericClickType> = listOf()
    ) {
        override fun toString(): String {
            return "SkinSlotOptions(slots=$slots, name='$name', lore=$lore, " +
                    "applyClickType=$applyClickType, scrapClickType=$scrapClickType, " +
                    "tokenizeClickType=$tokenizeClickType)"
        }
    }

    /*
     * Button options for the inventory GUI.
     */
    class Buttons(
        @SerializedName("next_page")
        val nextPage: GenericItem = GenericItem(),
        @SerializedName("previous_page")
        val previousPage: GenericItem = GenericItem(),
        @SerializedName("remove_skin")
        val removeSkin: GenericItem = GenericItem(),
    ) {
        override fun toString(): String {
            return "Buttons(nextPage=$nextPage, previousPage=$previousPage, removeSkin=$removeSkin)"
        }
    }

    override fun toString(): String {
        return "InventoryConfig(title='$title', type=$type, skinOptions=$skinOptions, buttons=$buttons, items=$items)"
    }
}
