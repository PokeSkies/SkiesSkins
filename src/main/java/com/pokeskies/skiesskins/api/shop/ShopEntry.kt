package com.pokeskies.skiesskins.api.shop

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/*
 * This class is the interface for a given Shop entry. This allows us to parse placeholders based on context of the given entry
 */
interface ShopEntry {
    fun parse(entry: String, player: ServerPlayer): String

    fun parse(entry: List<String>, player: ServerPlayer): List<String>

    fun modifyStack(stack: ItemStack, player: ServerPlayer): ItemStack {
        return stack
    }
}
