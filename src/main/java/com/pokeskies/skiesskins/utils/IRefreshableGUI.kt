package com.pokeskies.skiesskins.utils

import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType

abstract class IRefreshableGui(
    type: MenuType<*>,
    player: ServerPlayer,
    manipulatePlayerSlots: Boolean
): SimpleGui(type, player, manipulatePlayerSlots) {
    abstract fun refresh()
}