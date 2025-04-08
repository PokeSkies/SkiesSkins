package com.pokeskies.skiesskins.config.shop.entries

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.items.ShopItem
import com.pokeskies.skiesskins.config.shop.ShopCostConfig
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

/*
 * Config settings for a set of STATIC skin entries in a Shop.
 * @param gui defines the GUI settings the skins listed will make use of.
 * @param skins is a map of skin names to their respective skin settings.
 */
class StaticEntryConfig(
    val gui: StaticGUIOptions,
    val skins: Map<String, StaticSkin>
) {
    /*
     * GUI options for all the skins in this STATIC shop entry.
     */
    class StaticGUIOptions(
        val available: ShopItem,
        @SerializedName("max_uses")
        val maxUses: ShopItem,
    ) {
        override fun toString(): String {
            return "StaticGUIOptions(available=$available, maxUses=$maxUses)"
        }
    }

    /*
     * Config options for a single STATIC Skin that is defined in a Shop Entry.
     */
    class StaticSkin(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val slots: List<Int>,
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val cost: List<ShopCostConfig> = emptyList(),
        val limit: Int = 0,
    ) {
        override fun toString(): String {
            return "StaticSkin(slots=$slots, cost=$cost, limit=$limit)"
        }
    }

    override fun toString(): String {
        return "StaticSet(gui=$gui, skins=$skins)"
    }
}
