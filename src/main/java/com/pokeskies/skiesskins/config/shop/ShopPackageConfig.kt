package com.pokeskies.skiesskins.config.shop

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.ShopGuiItem
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

class ShopPackageConfig(
    val name: String,
    val gui: PackageGUIOptions,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val skins: List<String>,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val cost: List<ShopCostConfig> = emptyList(),
    val limit: Int = 0,
) {
    class PackageGUIOptions(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val slots: List<Int>,
        val available: ShopGuiItem,
        @SerializedName("max_uses")
        val maxUses: ShopGuiItem,
    ) {
        override fun toString(): String {
            return "RandomGUIOptions(slots=$slots, available=$available, maxUses=$maxUses)"
        }
    }

    override fun toString(): String {
        return "ShopPackageConfig(name='$name', gui=$gui, skins=$skins, cost=$cost, limit=$limit)"
    }
}
