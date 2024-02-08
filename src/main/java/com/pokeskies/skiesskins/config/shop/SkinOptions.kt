package com.pokeskies.skiesskins.config.shop

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import net.minecraft.world.item.Item

class SkinOptions(
    val random: Map<String, RandomSet>,
    val static: Map<String, StaticSet>,
) {

    class RandomSet(
        val gui: RandomGUIOptions,
        val skins: Map<String, RandomSkin>,
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        @SerializedName("reset_times")
        val resetTimes: List<String>,
    ) {
        class RandomGUIOptions(
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

        class RandomSkin(
            @JsonAdapter(FlexibleListAdaptorFactory::class)
            val cost: List<ShopCost> = emptyList(),
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

    class StaticSet {

    }
}