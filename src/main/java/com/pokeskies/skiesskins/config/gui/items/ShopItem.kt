package com.pokeskies.skiesskins.config.gui.items

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.shop.ShopEntry
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

class ShopItem(
    val item: String = "minecraft:barrier",
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
        entry: ShopEntry
    ): ItemStack {
        val parsedItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(item))
        var stack = ItemStack(parsedItem, amount)

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
                        for (e in element) {
                            if (e is StringTag) {
                                parsed.add(StringTag.valueOf(Utils.parsePlaceholders(player, e.asString)))
                            } else {
                                parsed.add(e)
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
                .append(Utils.deserializeText(entry.parse(name, player))))
        }

        if (lore.isNotEmpty()) {
            val parsedLore = entry.parse(lore, player)
            dataComponents.set(DataComponents.LORE, ItemLore(parsedLore.stream().map {
                Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(Utils.deserializeText(it)) as Component
            }.toList()))
        }

        stack.applyComponents(dataComponents.build())

        return entry.modifyStack(stack, player)
    }

    override fun toString(): String {
        return "ShopGuiItem(item=$item, amount=$amount, name=$name, lore=$lore, nbt=$nbt)"
    }
}
