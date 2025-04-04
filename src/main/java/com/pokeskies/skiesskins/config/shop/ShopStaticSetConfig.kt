package com.pokeskies.skiesskins.config.shop

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.ShopGuiItem
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

class ShopStaticSetConfig(
    val gui: StaticGUIOptions,
    val skins: Map<String, StaticSkin>
) {
    class StaticGUIOptions(
        val available: ShopGuiItem,
        @SerializedName("max_uses")
        val maxUses: ShopGuiItem,
    ) {
        override fun toString(): String {
            return "StaticGUIOptions(available=$available, maxUses=$maxUses)"
        }
    }

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
