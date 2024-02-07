package com.pokeskies.skiesskins.config.shop

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.item.PokemonItem
import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class ShopGuiItem(
    val item: Item = Items.BARRIER,
    val amount: Int = 1,
    val name: String? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val lore: List<String> = emptyList(),
    val nbt: CompoundTag? = null
) {
    companion object {
        val amountRegex = "%skin_cost_amount:([0-9]+)%".toRegex()
        val currencyRegex = "%skin_cost_currency:([0-9]+)%".toRegex()
    }

    fun createItemStack(player: ServerPlayer, shopConfig: ShopConfig, skin: SkinOptions.RandomSet.RandomSkin, skinConfig: SkinConfig): ItemStack {
        val pokemon = PokemonSpecies.getByIdentifier(skinConfig.species)?.create()
        if (pokemon != null) {
            for (aspect in skinConfig.aspects.apply) {
                PokemonProperties.parse(aspect).apply(pokemon)
            }
        }
        val stack = if (item is PokemonItem && pokemon != null) PokemonItem.from(pokemon, amount) else ItemStack(item, amount)

        if (name != null) {
            stack.setHoverName(Component.empty().setStyle(Style.EMPTY.withItalic(false))
                .append(parseString(name, player, shopConfig, skin, skinConfig)))
        }

        val tag = stack.orCreateTag
        if (lore.isNotEmpty()) {
            val parsedLore = parseStringList(lore, player, shopConfig, skin, skinConfig)
            val display = tag.getCompound(ItemStack.TAG_DISPLAY)
            val loreList = ListTag()
            for (line in parsedLore) {
                loreList.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.empty().setStyle(Style.EMPTY.withItalic(false))
                        .append(line)
                )))
            }
            display.put(ItemStack.TAG_LORE, loreList)
            tag.put(ItemStack.TAG_DISPLAY, display)
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

            nbtCopy.allKeys.forEach { key ->
                nbtCopy[key]?.let { tag.put(key, it) }
            }
        }

        stack.tag = tag

        return stack
    }

    override fun toString(): String {
        return "ShopGuiItem(item=$item, amount=$amount, name=$name, lore=$lore, nbt=$nbt)"
    }

    private fun parseStringSimple(
        string: String,
        player: ServerPlayer,
        shopConfig: ShopConfig,
        skin: SkinOptions.RandomSet.RandomSkin,
        skinConfig: SkinConfig
    ): String {
        var parsed = string.replace("%skin_name%", skinConfig.name)
            .replace("%skin_species%", PokemonSpecies.getByIdentifier(skinConfig.species)?.name ?: "Invalid Species")
            .replace("%skin_cost%", skin.cost.joinToString(" ") { "${it.amount} ${it.getCurrencyFormatted(shopConfig, (it.amount > 1))}" })

        amountRegex.findAll(parsed).toList().forEach { matchResult ->
            val idx = matchResult.groupValues[1].toInt()
            parsed = parsed.replace(
                "%skin_cost_amount:$idx%",
                if (skin.cost.size > idx)
                    skin.cost[idx].amount.toString()
                else ""
            )
        }

        currencyRegex.findAll(parsed).toList().forEach { matchResult ->
            val idx = matchResult.groupValues[1].toInt()
            parsed = parsed.replace(
                "%skin_cost_currency:$idx%",
                if (skin.cost.size > idx)
                    skin.cost[idx].let { it.getCurrencyFormatted(shopConfig, (it.amount > 1)) }
                else ""
            )
        }

        return parsed
    }

    private fun parseString(
        string: String,
        player: ServerPlayer,
        shopConfig: ShopConfig,
        skin: SkinOptions.RandomSet.RandomSkin,
        skinConfig: SkinConfig
    ): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, parseStringSimple(string, player, shopConfig, skin, skinConfig)))
    }

    private fun parseStringList(
        list: List<String>,
        player: ServerPlayer,
        shopConfig: ShopConfig,
        skin: SkinOptions.RandomSet.RandomSkin,
        skinConfig: SkinConfig
    ): List<Component> {
        val newList: MutableList<Component> = mutableListOf()
        for (line in list) {
            val initialParsed = Utils.parsePlaceholders(
                player,
                parseStringSimple(line, player, shopConfig, skin, skinConfig)
            )
            if (initialParsed.contains("%skin_description%", true)) {
                for (dLine in skinConfig.description) {
                    newList.add(Utils.deserializeText(initialParsed.replace("%skin_description%", dLine)))
                }
            } else {
                newList.add(Utils.deserializeText(initialParsed))
            }
        }
        return newList
    }

}