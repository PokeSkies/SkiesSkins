package com.pokeskies.skiesskins.config.shop.entries

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.items.ShopItem
import com.pokeskies.skiesskins.config.shop.ShopCostConfig
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

/*
 * Config settings for a single PACKAGE entry in a Shop.
 * @param gui defines the GUI settings the skins listed will make use of.
 * @param skins is a map of IDs to their respective skin settings.
 */
class PackageEntryConfig(
    val name: String,
    val gui: PackageGUIOptions,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val skins: List<String>,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val cost: List<ShopCostConfig> = emptyList(),
    val limit: Int = 0,
) {
    /*
     * GUI options for this PACKAGE shop entry.
     */
    class PackageGUIOptions(
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

    override fun toString(): String {
        return "ShopPackageConfig(name='$name', gui=$gui, skins=$skins, cost=$cost, limit=$limit)"
    }
}
