package com.pokeskies.skiesskins.config.gui

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.PokemonItem
import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.config.shop.ShopCost
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore

class ShopGuiItem(
    val item: Item = Items.BARRIER,
    val amount: Int = 1,
    val name: String? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val lore: List<String> = emptyList(),
    val nbt: CompoundTag? = null
) {
    companion object {
        val amountRegex = "%cost_amount:([0-9]+)%".toRegex()
        val currencyRegex = "%cost_currency:([0-9]+)%".toRegex()
    }

    fun createItemStack(
        player: ServerPlayer,
        shopConfig: ShopConfig,
        cost: List<ShopCost>,
        limit: Int,
        purchases: Int,
        skinConfig: SkinConfig?,
        resetTime: Long?
    ): ItemStack {
        var stack = ItemStack(item, amount)

        // If the item type is a PokemonItem and the Pokemon is valid, set it to the Pokemon Model item
        if (item is PokemonItem && skinConfig != null) {
            val pokemon = PokemonSpecies.getByIdentifier(skinConfig.species)?.create()
            if (pokemon != null) {
                for (aspect in skinConfig.aspects.apply) {
                    PokemonProperties.parse(aspect).apply(pokemon)
                }
                stack = PokemonItem.from(pokemon, amount)
            }
        }

        if (nbt != null) {
            // Parses the nbt and attempts to replace any placeholders
            val nbtCopy = nbt.copy()
            for (key in nbt.allKeys) {
                val element = nbt.get(key)
                if (element != null) {
                    if (element is StringTag) {
                        nbtCopy.putString(key, Utils.parsePlaceholders(player, element.asString))
                    } else if (element is ListTag) {
                        val parsed = ListTag()
                        for (entry in element) {
                            if (entry is StringTag) {
                                parsed.add(StringTag.valueOf(Utils.parsePlaceholders(player, entry.asString)))
                            } else {
                                parsed.add(entry)
                            }
                        }
                        nbtCopy.put(key, parsed)
                    }
                }
            }

            DataComponentPatch.CODEC.decode(SkiesSkins.INSTANCE.nbtOpts, nbtCopy).result().ifPresent { result ->
                stack.applyComponents(result.first)
            }
        }

        val dataComponents = DataComponentPatch.builder()

        if (name != null) {
            dataComponents.set(
                DataComponents.ITEM_NAME, Component.empty().setStyle(Style.EMPTY.withItalic(false))
                .append(parseString(name, player, shopConfig, cost, limit, purchases, skinConfig, resetTime)))
        }

        if (lore.isNotEmpty()) {
            val parsedLore = parseStringList(lore, player, shopConfig, cost, limit, purchases, skinConfig, resetTime)
            dataComponents.set(DataComponents.LORE, ItemLore(parsedLore.stream().map {
                Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(it) as Component
            }.toList()))
        }

        stack.applyComponents(dataComponents.build())

        return stack
    }

    override fun toString(): String {
        return "ShopGuiItem(item=$item, amount=$amount, name=$name, lore=$lore, nbt=$nbt)"
    }

    private fun parseStringSimple(
        string: String,
        player: ServerPlayer,
        shopConfig: ShopConfig,
        cost: List<ShopCost>,
        limit: Int,
        purchases: Int,
        skinConfig: SkinConfig?,
        resetTime: Long?
    ): String {
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

        return parsed
    }

    private fun parseString(
        string: String,
        player: ServerPlayer,
        shopConfig: ShopConfig,
        cost: List<ShopCost>,
        limit: Int,
        purchases: Int,
        skinConfig: SkinConfig?,
        resetTime: Long?
    ): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, parseStringSimple(
            string, player, shopConfig, cost, limit, purchases, skinConfig, resetTime
        )))
    }

    private fun parseStringList(
        list: List<String>,
        player: ServerPlayer,
        shopConfig: ShopConfig,
        cost: List<ShopCost>,
        limit: Int,
        purchases: Int,
        skinConfig: SkinConfig?,
        resetTime: Long?
    ): List<Component> {
        val newList: MutableList<Component> = mutableListOf()
        for (line in list) {
            val initialParsed = Utils.parsePlaceholders(
                player,
                parseStringSimple(line, player, shopConfig, cost, limit, purchases, skinConfig, resetTime)
            )
            if (skinConfig != null && initialParsed.contains("%description%", true)) {
                for (dLine in skinConfig.description) {
                    newList.add(Utils.deserializeText(initialParsed.replace("%description%", dLine)))
                }
            } else {
                newList.add(Utils.deserializeText(initialParsed))
            }
        }
        return newList
    }

}
