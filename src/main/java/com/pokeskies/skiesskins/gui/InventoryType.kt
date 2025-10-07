package com.pokeskies.skiesskins.gui

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.world.inventory.MenuType

enum class InventoryType(val type: MenuType<*>) {
    GENERIC_9x1(MenuType.GENERIC_9x1),
    GENERIC_9x2(MenuType.GENERIC_9x2),
    GENERIC_9x3(MenuType.GENERIC_9x3),
    GENERIC_9x4(MenuType.GENERIC_9x4),
    GENERIC_9x5(MenuType.GENERIC_9x5),
    GENERIC_9x6(MenuType.GENERIC_9x6),
    GENERIC_3x3(MenuType.GENERIC_3x3),
    CRAFTER_3x3(MenuType.CRAFTER_3x3),
    ANVIL(MenuType.ANVIL),
    BEACON(MenuType.BEACON),
    BLAST_FURNACE(MenuType.BLAST_FURNACE),
    BREWING_STAND(MenuType.BREWING_STAND),
    CRAFTING(MenuType.CRAFTING),
    ENCHANTMENT(MenuType.ENCHANTMENT),
    FURNACE(MenuType.FURNACE),
    GRINDSTONE(MenuType.GRINDSTONE),
    HOPPER(MenuType.HOPPER),
    LECTERN(MenuType.LECTERN),
    LOOM(MenuType.LOOM),
    MERCHANT(MenuType.MERCHANT),
    SHULKER_BOX(MenuType.SHULKER_BOX),
    SMITHING(MenuType.SMITHING),
    SMOKER(MenuType.SMOKER),
    CARTOGRAPHY_TABLE(MenuType.CARTOGRAPHY_TABLE),
    STONECUTTER(MenuType.STONECUTTER);

    class Adapter : TypeAdapter<InventoryType>() {
        override fun write(out: JsonWriter, value: InventoryType?) {
            out.value(value?.name)
        }

        override fun read(reader: JsonReader): InventoryType {
            val value = reader.nextString()
            return InventoryType.entries.find { it.name.equals(value, true) }
                ?: GENERIC_9x6
        }
    }
}
