package com.pokeskies.skiesskins.config.shop.entries

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.items.ShopItem
import com.pokeskies.skiesskins.config.shop.ShopCostConfig
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

/*
 * Config settings for a set of RANDOM skin entries in a Shop.
 * @param gui defines the GUI settings the skins listed will make use of.
 * @param skins is a map of IDs to their respective skin settings.
 */
class RandomEntryConfig(
    val gui: RandomGUIOptions,
    val skins: Map<String, RandomSkin>,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    @SerializedName("reset_times")
    val resetTimes: List<String>,
) {
    /*
     * GUI options for all the skins in this RANDOM shop entry.
     */
    class RandomGUIOptions(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val slots: List<Int>,
        val available: ShopItem,
        @SerializedName("max_uses")
        val maxUses: ShopItem,
    ) {
        override fun toString(): String {
            return "RandomGUIOptions(slots=$slots, available=$available, maxUses=$maxUses)"
        }
    }

    /*
     * Config options for a single RANDOM Skin that is defined in a Shop Entry.
     */
    class RandomSkin(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val cost: List<ShopCostConfig> = emptyList(),
        val weight: Int = 1,
        val limit: Int = 0,
    ) {
        override fun toString(): String {
            return "RandomSkin(cost=$cost, weight=$weight, limit=$limit)"
        }
    }

    override fun toString(): String {
        return "RandomSet(gui=$gui, skins=$skins, resetTimes=$resetTimes)"
    }
}
