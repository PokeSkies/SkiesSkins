package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.actions.Action
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

class   GenericGuiItem(
    val item: Item = Items.BARRIER,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val slots: List<Int> = emptyList(),
    val amount: Int = 1,
    val name: String? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val lore: List<String> = emptyList(),
    val nbt: CompoundTag? = null,
    @SerializedName("click_actions")
    val clickActions: Map<String, Action> = emptyMap(),
) {
    fun createItemStack(player: ServerPlayer): ItemStack {
        val stack = ItemStack(item, amount)

        if (name != null) {
            stack.setHoverName(Component.empty().setStyle(Style.EMPTY.withItalic(false))
                .append(Utils.deserializeText(Utils.parsePlaceholders(player, name))))
        }

        val tag = stack.orCreateTag
        if (lore.isNotEmpty()) {
            val display = tag.getCompound(ItemStack.TAG_DISPLAY)
            val loreList = ListTag()
            for (line in lore) {
                loreList.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.empty().setStyle(Style.EMPTY.withItalic(false))
                        .append(Utils.deserializeText((Utils.parsePlaceholders(player, line))))
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
        return "GuiItem(item=$item, slots=$slots, amount=$amount, name=$name, lore=$lore, nbt=$nbt, click_actions=$clickActions)"
    }
}