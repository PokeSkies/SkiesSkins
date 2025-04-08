package com.pokeskies.skiesskins.api.shop

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.PokemonItem
import com.cobblemon.mod.common.item.components.PokemonItemComponent
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.config.gui.items.ShopItem.Companion.amountRegex
import com.pokeskies.skiesskins.config.gui.items.ShopItem.Companion.currencyRegex
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.config.shop.ShopCostConfig
import com.pokeskies.skiesskins.config.shop.entries.RandomEntryConfig
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class RandomEntry(
    val shopId: String,
    val shopConfig: ShopConfig,
    val randomConfig: RandomEntryConfig,
    val skinId: String,
    val skinConfig: SkinConfig,
    val cost: List<ShopCostConfig>,
    val limit: Int,
    val purchases: Int,
    val resetTime: Long?
) : ShopEntry {
    override fun parse(entry: String, player: ServerPlayer): String {
        var parsed = entry.replace("%cost%", cost.joinToString(" ") { "${it.amount} ${it.getCurrencyFormatted(shopConfig, (it.amount > 1))}" })
            .replace("%limit%", if (limit > 0) limit.toString() else "∞")
            .replace("%purchases%", purchases.toString())
            .replace("%name%", skinConfig.name)

        if (skinConfig != null) {
            parsed = parsed.replace("%species%", PokemonSpecies.getByIdentifier(skinConfig.species)?.name ?: "Invalid Species")
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

        return parsed
    }

    override fun parse(entry: List<String>, player: ServerPlayer): List<String> {
        val newList: MutableList<String> = mutableListOf()
        for (line in entry) {
            val initialParsed = Utils.parsePlaceholders(player, parse(line, player))
            if (initialParsed.contains("%description%", true)) {
                for (dLine in skinConfig.description) {
                    newList.add(initialParsed.replace("%description%", dLine))
                }
            } else {
                newList.add(initialParsed)
            }
        }
        return newList
    }

    override fun modifyStack(stack: ItemStack, player: ServerPlayer): ItemStack {
        // If the item type is a PokemonItem and the Pokemon is valid, set it to the Pokemon Model item
        if (stack.item is PokemonItem) {
            val pokemon = PokemonSpecies.getByIdentifier(skinConfig.species)?.create()
            if (pokemon != null) {
                stack.set(CobblemonItemComponents.POKEMON_ITEM, PokemonItemComponent(skinConfig.species, skinConfig.aspects.apply.toSet(), null))
            }
        }

        return stack
    }
}
