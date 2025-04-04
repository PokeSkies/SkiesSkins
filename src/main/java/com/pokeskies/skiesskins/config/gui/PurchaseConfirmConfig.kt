package com.pokeskies.skiesskins.config.gui

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.config.gui.ShopGuiItem.Companion.amountRegex
import com.pokeskies.skiesskins.config.gui.ShopGuiItem.Companion.currencyRegex
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.config.shop.ShopCostConfig
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.Utils
import com.pokeskies.skiesskins.utils.Utils.deserializeText
import com.pokeskies.skiesskins.utils.Utils.parsePlaceholders
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
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
        companion object {
            fun parseString(
                string: String,
                player: ServerPlayer,
                shopConfig: ShopConfig,
                cost: List<ShopCostConfig>,
                limit: Int,
                purchases: Int,
                skinConfig: SkinConfig?,
                resetTime: Long?
            ): Component {
                var parsed = string.replace("%cost%", cost.joinToString(" ") { "${it.amount} ${it.getCurrencyFormatted(shopConfig, (it.amount > 1))}" })
                    .replace("%limit%", if (limit > 0) limit.toString() else "∞")
                    .replace("%purchases%", purchases.toString())

                if (skinConfig != null) {
                    parsed = parsed.replace("%name%", skinConfig.name)
                        .replace("%species%", PokemonSpecies.getByIdentifier(skinConfig.species)?.name ?: "Invalid Species")
                }

                if (resetTime != null) {
                    parsed = parsed.replace("%reset_time%", Utils.getFormattedTime((resetTime - System.currentTimeMillis()) / 1000))
                }

                amountRegex.findAll(parsed).toList().forEach { matchResult ->
                    val idx = matchResult.groupValues[1].toInt()
                    parsed = parsed.replace(
                        "%cost_amount:$idx%",
                        if (cost.size > idx)
                            cost[idx].amount.toString()
                        else ""
                    )
                }

                currencyRegex.findAll(parsed).toList().forEach { matchResult ->
                    val idx = matchResult.groupValues[1].toInt()
                    parsed = parsed.replace(
                        "%cost_currency:$idx%",
                        if (cost.size > idx)
                            cost[idx].let { it.getCurrencyFormatted(shopConfig, (it.amount > 1)) }
                        else ""
                    )
                }

                return deserializeText(parsePlaceholders(player, parsed))
            }

            fun parseStringList(
                list: List<String>,
                player: ServerPlayer,
                shopConfig: ShopConfig,
                cost: List<ShopCostConfig>,
                limit: Int,
                purchases: Int,
                skinConfig: SkinConfig?,
                resetTime: Long?
            ): List<Component> {
                val newList: MutableList<Component> = mutableListOf()
                for (line in list) {
                    var parsed = line.replace(
                        "%cost%",
                        cost.joinToString(" ") {
                            "${it.amount} ${
                                it.getCurrencyFormatted(
                                    shopConfig,
                                    (it.amount > 1)
                                )
                            }"
                        })
                        .replace("%limit%", if (limit > 0) limit.toString() else "∞")
                        .replace("%purchases%", purchases.toString())

                    if (skinConfig != null) {
                        parsed = parsed.replace("%name%", skinConfig.name)
                            .replace(
                                "%species%",
                                PokemonSpecies.getByIdentifier(skinConfig.species)?.name ?: "Invalid Species"
                            )
                    }

                    if (resetTime != null) {
                        parsed = parsed.replace(
                            "%reset_time%",
                            Utils.getFormattedTime((resetTime - System.currentTimeMillis()) / 1000)
                        )
                    }

                    amountRegex.findAll(parsed).toList().forEach { matchResult ->
                        val idx = matchResult.groupValues[1].toInt()
                        parsed = parsed.replace(
                            "%cost_amount:$idx%",
                            if (cost.size > idx)
                                cost[idx].amount.toString()
                            else ""
                        )
                    }

                    currencyRegex.findAll(parsed).toList().forEach { matchResult ->
                        val idx = matchResult.groupValues[1].toInt()
                        parsed = parsed.replace(
                            "%cost_currency:$idx%",
                            if (cost.size > idx)
                                cost[idx].let { it.getCurrencyFormatted(shopConfig, (it.amount > 1)) }
                            else ""
                        )
                    }

                    if (parsed.contains("%description%", true)) {
                        if (skinConfig != null) {
                            for (dLine in skinConfig.description) {
                                newList.add(deserializeText(parsed.replace("%description%", dLine)))
                            }
                        }
                    } else {
                        newList.add(deserializeText(parsed))
                    }
                }
                return newList
            }
        }

        override fun toString(): String {
            return "SkinSlotOptions(item=$item, slots=$slots, name='$name', lore=$lore)"
        }
    }

    override fun toString(): String {
        return "PurchaseConfirmConfig(title='$title', size=$size, purchase=$purchase, " +
                "confirm=$confirm, cancel=$cancel, items=$items)"
    }

}
